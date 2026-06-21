package blog.code.codeblog.service;

import blog.code.codeblog.dto.cloudinary.ImageUploadResponseDTO;
import blog.code.codeblog.enums.FlowImageFlag;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
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


    private CloudinaryService cloudinaryService;

    @BeforeEach
    void setUp() {
        cloudinaryService = new CloudinaryService(cloudinary);
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

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResponse);

        ImageUploadResponseDTO result = cloudinaryService.uploadFile(file, FlowImageFlag.PROFILE, userId.toString(), null);

        assertNotNull(result);
        assertEquals("Upload successful", result.message());
        assertEquals(imageUrl, result.imageUrl());
        assertEquals(publicId, result.publicId());
        verify(cloudinary.uploader()).upload(any(byte[].class), anyMap());
    }

    @Test
    @DisplayName("Should upload post image successfully")
    void uploadPostImageSuccess() throws IOException {
        UUID postId = UUID.randomUUID();
        String imageUrl = "https://cloudinary.com/post_pics/test-image.jpg";
        String publicId = "post_pics/test-image";
        MultipartFile file = createMockFile();
        Map<String, Object> uploadResponse = createUploadResponse(imageUrl, publicId);

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResponse);

        ImageUploadResponseDTO result = cloudinaryService.uploadFile(file, FlowImageFlag.POST, null, postId.toString());

        assertNotNull(result);
        assertEquals("Upload successful", result.message());
        assertEquals(imageUrl, result.imageUrl());
        assertEquals(publicId, result.publicId());
        verify(cloudinary.uploader()).upload(any(byte[].class), anyMap());
    }

    @Test
    @DisplayName("Should throw IOException when Cloudinary upload fails")
    void uploadFileShouldThrowWhenCloudinaryFails() throws IOException {
        MultipartFile file = createMockFile();

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenThrow(new IOException("Cloudinary upload failed"));


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
    @DisplayName("Should delete image successfully")
    void deleteFileSuccess() throws IOException {
        String publicId = "post_pics/test-image";
        Map<String, Object> deleteResponse = new HashMap<>();
        deleteResponse.put("result", "ok");

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.destroy(eq(publicId), anyMap())).thenReturn(deleteResponse);

        Map<String, Object> result = cloudinaryService.deleteFile(publicId);

        assertNotNull(result);
        assertEquals("ok", result.get("result"));
        verify(cloudinary.uploader()).destroy(eq(publicId), anyMap());
    }

    @Test
    @DisplayName("Should delete file by publicId")
    void deleteFileSuccessfully() throws Exception {
        String publicId = "post_pics/test-image";
        Map<String, Object> expectedResponse = Map.of("result", "ok");

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.destroy(eq(publicId), anyMap())).thenReturn(expectedResponse);

        Map<String, Object> result = cloudinaryService.deleteFile(publicId);

        assertNotNull(result);
        assertEquals("ok", result.get("result"));
        verify(uploader).destroy(eq(publicId), anyMap());
    }


}
