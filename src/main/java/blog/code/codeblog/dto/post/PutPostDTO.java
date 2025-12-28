package blog.code.codeblog.dto.post;

import java.util.UUID;

public record PutPostDTO(String title, String content, UUID authorId, UUID userId) {
}
