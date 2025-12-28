package blog.code.codeblog.dto.comment;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentResponseDTO(UUID id, String content, String author, LocalDateTime createdAt) {
}
