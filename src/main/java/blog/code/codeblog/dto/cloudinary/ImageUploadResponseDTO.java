package blog.code.codeblog.dto.cloudinary;

import lombok.Builder;

@Builder
public record ImageUploadResponseDTO(String message, String imageUrl, String publicId){
}
