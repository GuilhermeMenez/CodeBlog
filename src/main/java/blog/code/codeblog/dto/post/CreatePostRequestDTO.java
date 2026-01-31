package blog.code.codeblog.dto.post;

import jakarta.annotation.Nullable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public record CreatePostRequestDTO(
        String title,
        String content,
        UUID authorId,
        @Nullable List<MultipartFile> images
) {
}
