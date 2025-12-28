package blog.code.codeblog.dto.post;

import java.util.UUID;

public record CreatePostRequestDTO(String title, String content, UUID authorId) {
}
