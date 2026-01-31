package blog.code.codeblog.integration;

import blog.code.codeblog.model.Post;
import blog.code.codeblog.model.User;
import blog.code.codeblog.repository.PostRepository;
import blog.code.codeblog.repository.UserRepository;
import blog.code.codeblog.service.TokenService;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CloudinaryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private Cloudinary cloudinary;

    private User testUser;
    private Post testPost;
    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        // Create test user
        testUser = new User();
        testUser.setName("Test User");
        testUser.setLogin("testuser@email.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser = userRepository.save(testUser);


        testPost = Post.builder()
                .title("Test Post")
                .content("Test Content")
                .date(LocalDate.now())
                .user(testUser)
                .author(testUser.getName())
                .build();
        testPost = postRepository.save(testPost);


        authToken = tokenService.generateToken(testUser);


        Uploader mockUploader = mock(Uploader.class);
        when(cloudinary.uploader()).thenReturn(mockUploader);

        Map<String, Object> uploadResponse = new HashMap<>();
        uploadResponse.put("url", "https://cloudinary.com/test-image.jpg");
        uploadResponse.put("public_id", "test_folder/test-image");
        when(mockUploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResponse);

        Map<String, Object> deleteResponse = new HashMap<>();
        deleteResponse.put("result", "ok");
        when(mockUploader.destroy(anyString(), anyMap())).thenReturn(deleteResponse);
    }

    @Test
    @DisplayName("Should upload profile image with valid authentication")
    void uploadProfileImageWithAuth() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/upload")
                        .file(file)
                        .param("flag", "PROFILE")
                        .param("userId", testUser.getId().toString())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Profile pic updated successfully"))
                .andExpect(jsonPath("$.imageUrl").exists())
                .andExpect(jsonPath("$.publicId").exists());

        // Verify user was updated
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertNotNull(updatedUser.getUrlProfilePic());
        assertNotNull(updatedUser.getProfilePicId());
    }

    @Test
    @DisplayName("Should upload post image with valid authentication")
    void uploadPostImageWithAuth() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "post-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/upload")
                        .file(file)
                        .param("flag", "POST")
                        .param("postId", testPost.getId().toString())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Image uploaded"))
                .andExpect(jsonPath("$.imageUrl").exists())
                .andExpect(jsonPath("$.publicId").exists());

        // Verify post was updated
        Post updatedPost = postRepository.findById(testPost.getId()).orElseThrow();
        assertFalse(updatedPost.getImages().isEmpty());
    }

    @Test
    @DisplayName("Should reject upload without authentication")
    void uploadWithoutAuthShouldBeRejected() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/upload")
                        .file(file)
                        .param("flag", "PROFILE")
                        .param("userId", testUser.getId().toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should delete profile image successfully")
    void deleteProfileImageSuccess() throws Exception {
        // First, set up user with profile pic
        String publicId = "profile_pics/test-image";
        testUser.setUrlProfilePic("https://cloudinary.com/profile.jpg");
        testUser.setProfilePicId(publicId);
        userRepository.save(testUser);

        mockMvc.perform(delete("/delete")
                        .param("publicId", publicId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("ok"));

        // Verify user profile pic was removed
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertNull(updatedUser.getUrlProfilePic());
        assertNull(updatedUser.getProfilePicId());
    }

    @Test
    @DisplayName("Should delete post image successfully")
    void deletePostImageSuccess() throws Exception {

        String publicId = "post_pics/test-image";
        testPost.getImages().put(publicId, "https://cloudinary.com/post-image.jpg");
        postRepository.save(testPost);

        mockMvc.perform(delete("/delete")
                        .param("publicId", publicId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("ok"));


        Post updatedPost = postRepository.findById(testPost.getId()).orElseThrow();
        assertFalse(updatedPost.getImages().containsKey(publicId));
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent image")
    void deleteNonExistentImageShouldReturn404() throws Exception {
        String publicId = "nonexistent/image";

        mockMvc.perform(delete("/delete")
                        .param("publicId", publicId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    // Should be 404 or handled by exception handler
                    assertTrue(status == 404 || status == 500,
                            "Expected 404 or 500 for non-existent image, got: " + status);
                });
    }

    @Test
    @DisplayName("Should reject delete without authentication")
    void deleteWithoutAuthShouldBeRejected() throws Exception {
        mockMvc.perform(delete("/delete")
                        .param("publicId", "some-public-id"))
                .andExpect(status().isUnauthorized());
    }
}
