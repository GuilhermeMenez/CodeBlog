package blog.code.codeblog.dto.post;

import java.util.UUID;

public record PutPostDTO(
        UUID postId,
        String title,
        String content
) {
}
