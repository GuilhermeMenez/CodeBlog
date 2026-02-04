package blog.code.codeblog.dto.post;

import lombok.Builder;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Builder
public record PostResponseDTO(
        UUID postId,
        String title,
        String content,
        PostAuthorDTO author,
        LocalDate createdAt,
        Map<String, String> images ) {
}