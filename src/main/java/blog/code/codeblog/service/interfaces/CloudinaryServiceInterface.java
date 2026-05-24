package blog.code.codeblog.service.interfaces;

import blog.code.codeblog.dto.cloudinary.ImageUploadResponseDTO;
import blog.code.codeblog.enums.FlowImageFlag;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface CloudinaryServiceInterface {
    ImageUploadResponseDTO uploadFile(MultipartFile file, FlowImageFlag flag, String userId, String postId) throws IOException;
    Map<String, Object> deleteFile(String publicId) throws IOException;
}

