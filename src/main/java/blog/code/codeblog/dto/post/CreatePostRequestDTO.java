package blog.code.codeblog.dto.post;

import jakarta.annotation.Nullable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record CreatePostRequestDTO(
        String title,
        String content,
        @Nullable List<MultipartFile> images
) {
}
