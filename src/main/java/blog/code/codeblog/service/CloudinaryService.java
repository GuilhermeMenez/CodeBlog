package blog.code.codeblog.service;

import blog.code.codeblog.dto.cloudinary.ImageUploadResponseDTO;
import blog.code.codeblog.enums.FlowImageFlag;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private static final String URL_KEY = "url";
    private static final String PUBLIC_ID_KEY = "public_id";
    private static final String POST_FOLDER = "post_pics";
    private static final String PROFILE_FOLDER = "profile_pics";

    private final Cloudinary cloudinary;

    @Lazy
    private final UserService userService;

    @Lazy
    private final PostServiceImpl postServiceImpl;



    public ImageUploadResponseDTO uploadFile(MultipartFile file, FlowImageFlag flag, String userId, String postId) throws IOException {
        logUploadStart(flag, userId, postId);

        Map<String, Object> uploadResult = uploadToCloudinary(file, flag);
        log.info("Upload completed. PublicId: {}", uploadResult.get(PUBLIC_ID_KEY));

        return processUploadResult(uploadResult, flag, userId, postId);
    }

    private void logUploadStart(FlowImageFlag flag, String userId, String postId) {
        switch (flag) {
            case POST -> log.info("Starting upload for post: {}", postId);
            case PROFILE -> log.info("Starting upload for user: {}", userId);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> uploadToCloudinary(MultipartFile file, FlowImageFlag flag) throws IOException {
        String folder = flag == FlowImageFlag.POST ? POST_FOLDER : PROFILE_FOLDER;
        try {
            return cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("folder", folder));
        } catch (IOException e) {
            log.error("Upload to Cloudinary failed: {}", e.getMessage());
            throw e;
        }
    }

    private ImageUploadResponseDTO processUploadResult(Map<String, Object> uploadResult, FlowImageFlag flag, String userId, String postId) {
        String imageUrl = extractString(uploadResult, URL_KEY);
        String publicId = extractString(uploadResult, PUBLIC_ID_KEY);

        return switch (flag) {
            case POST -> {
                log.info("Processing as POST_IMAGE for post: {}", postId);
                yield postServiceImpl.saveuploadedImage(UUID.fromString(postId), imageUrl, publicId);
            }
            case PROFILE -> {
                log.info("Processing as USER_AVATAR for user: {}", userId);
                yield userService.saveUploadProfilePic(UUID.fromString(userId), imageUrl, publicId);
            }
        };
    }

    private String extractString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            throw new IllegalStateException("Missing required field in Cloudinary response: " + key);
        }
        return value.toString();
    }


    @SuppressWarnings("unchecked")
    public Map<String, Object> deleteFile(String publicId) throws IOException {
        log.info("Deleting file with publicId: {}", publicId);

        if (postServiceImpl.deleteImage(publicId)) {
            return cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        }

        if (userService.deleteProfilePic(publicId)) {
            return cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        }

        log.warn("PublicId not found in any post or user: {}", publicId);
        throw new EntityNotFoundException("Image with publicId " + publicId + " not found");
    }


}