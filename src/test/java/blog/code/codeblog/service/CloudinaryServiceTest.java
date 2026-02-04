package blog.code.codeblog.service;

import blog.code.codeblog.dto.cloudinary.ImageUploadResponseDTO;
import blog.code.codeblog.enums.FlowImageFlag;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @Mock
    private UserService userService;

    @Mock
    private PostServiceImpl postServiceImpl;

    private CloudinaryService cloudinaryService;

    @BeforeEach
    void setUp() {
        cloudinaryService = new CloudinaryService(cloudinary, userService, postServiceImpl);
    }

    private MultipartFile createMockFile() {
        return new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
    }

    private Map<String, Object> createUploadResponse(String url, String publicId) {
        Map<String, Object> response = new HashMap<>();
        response.put("url", url);
        response.put("public_id", publicId);
        return response;
    }

    @Test
    @DisplayName("Should upload profile image successfully")
    void uploadProfileImageSuccess() throws IOException {
        UUID userId = UUID.randomUUID();
        String imageUrl = "https://cloudinary.com/profile_pics/test-image.jpg";
        String publicId = "profile_pics/test-image";
        MultipartFile file = createMockFile();
        Map<String, Object> uploadResponse = createUploadResponse(imageUrl, publicId);
        ImageUploadResponseDTO expectedResponse = new ImageUploadResponseDTO("Profile pic updated successfully", imageUrl, publicId);

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResponse);
        when(userService.saveUploadProfilePic(eq(userId), eq(imageUrl), eq(publicId)))
                .thenReturn(expectedResponse);

        ImageUploadResponseDTO result = cloudinaryService.uploadFile(file, FlowImageFlag.PROFILE, userId.toString(), null);

        assertNotNull(result);
        assertEquals(expectedResponse.message(), result.message());
        assertEquals(expectedResponse.imageUrl(), result.imageUrl());
        assertEquals(expectedResponse.publicId(), result.publicId());
        verify(cloudinary.uploader()).upload(any(byte[].class), anyMap());
        verify(userService).saveUploadProfilePic(eq(userId), eq(imageUrl), eq(publicId));
    }

    @Test
    @DisplayName("Should upload post image successfully")
    void uploadPostImageSuccess() throws IOException {
        UUID postId = UUID.randomUUID();
        String imageUrl = "https://cloudinary.com/post_pics/test-image.jpg";
        String publicId = "post_pics/test-image";
        MultipartFile file = createMockFile();
        Map<String, Object> uploadResponse = createUploadResponse(imageUrl, publicId);
        ImageUploadResponseDTO expectedResponse = new ImageUploadResponseDTO("Image uploaded", imageUrl, publicId);

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResponse);
        when(postServiceImpl.saveUploadedImage(eq(postId), eq(imageUrl), eq(publicId)))
                .thenReturn(expectedResponse);

        ImageUploadResponseDTO result = cloudinaryService.uploadFile(file, FlowImageFlag.POST, null, postId.toString());

        assertNotNull(result);
        assertEquals(expectedResponse.message(), result.message());
        assertEquals(expectedResponse.imageUrl(), result.imageUrl());
        assertEquals(expectedResponse.publicId(), result.publicId());
        verify(cloudinary.uploader()).upload(any(byte[].class), anyMap());
        verify(postServiceImpl).saveUploadedImage(eq(postId), eq(imageUrl), eq(publicId));
    }

    @Test
    @DisplayName("Should throw IOException when Cloudinary upload fails")
    void uploadFileShouldThrowWhenCloudinaryFails() throws IOException {
        MultipartFile file = createMockFile();

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenThrow(new IOException("Cloudinary upload failed"));

        // Act & Assert
        assertThrows(IOException.class, () ->
            cloudinaryService.uploadFile(file, FlowImageFlag.PROFILE, UUID.randomUUID().toString(), null)
        );
    }

    @Test
    @DisplayName("Should throw IllegalStateException when URL is missing from response")
    void uploadFileShouldThrowWhenUrlMissing() throws IOException {

        MultipartFile file = createMockFile();
        Map<String, Object> incompleteResponse = new HashMap<>();
        incompleteResponse.put("public_id", "some-id");

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(incompleteResponse);

        assertThrows(IllegalStateException.class, () ->
            cloudinaryService.uploadFile(file, FlowImageFlag.PROFILE, UUID.randomUUID().toString(), null)
        );
    }

    @Test
    @DisplayName("Should throw IllegalStateException when public_id is missing from response")
    void uploadFileShouldThrowWhenPublicIdMissing() throws IOException {
        MultipartFile file = createMockFile();
        Map<String, Object> incompleteResponse = new HashMap<>();
        incompleteResponse.put("url", "https://cloudinary.com/test.jpg");

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(incompleteResponse);

        assertThrows(IllegalStateException.class, () ->
            cloudinaryService.uploadFile(file, FlowImageFlag.PROFILE, UUID.randomUUID().toString(), null)
        );
    }

    @Test
    @DisplayName("Should delete post image successfully")
    void deletePostImageSuccess() throws IOException {
        String publicId = "post_pics/test-image";
        Map<String, Object> deleteResponse = new HashMap<>();
        deleteResponse.put("result", "ok");

        when(postServiceImpl.deleteImage(publicId)).thenReturn(true);
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.destroy(eq(publicId), anyMap())).thenReturn(deleteResponse);

        Map<String, Object> result = cloudinaryService.deleteFile(publicId);

        assertNotNull(result);
        assertEquals("ok", result.get("result"));
        verify(postServiceImpl).deleteImage(publicId);
        verify(cloudinary.uploader()).destroy(eq(publicId), anyMap());
        verify(userService, never()).deleteProfilePic(anyString());
    }

    @Test
    @DisplayName("Should delete profile image successfully")
    void deleteProfileImageSuccess() throws IOException {
        String publicId = "profile_pics/test-image";
        Map<String, Object> deleteResponse = new HashMap<>();
        deleteResponse.put("result", "ok");

        when(postServiceImpl.deleteImage(publicId)).thenReturn(false);
        when(userService.deleteProfilePic(publicId)).thenReturn(true);
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.destroy(eq(publicId), anyMap())).thenReturn(deleteResponse);

        Map<String, Object> result = cloudinaryService.deleteFile(publicId);

        assertNotNull(result);
        assertEquals("ok", result.get("result"));
        verify(postServiceImpl).deleteImage(publicId);
        verify(userService).deleteProfilePic(publicId);
        verify(cloudinary.uploader()).destroy(eq(publicId), anyMap());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when image not found in any entity")
    void deleteFileShouldThrowWhenImageNotFound() {
        String publicId = "unknown/test-image";

        when(postServiceImpl.deleteImage(publicId)).thenReturn(false);
        when(userService.deleteProfilePic(publicId)).thenReturn(false);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
            cloudinaryService.deleteFile(publicId)
        );
        assertEquals("Image with publicId " + publicId + " not found", exception.getMessage());
        verify(postServiceImpl).deleteImage(publicId);
        verify(userService).deleteProfilePic(publicId);
    }
}
