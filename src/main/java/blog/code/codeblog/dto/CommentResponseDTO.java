package blog.code.codeblog.dto;

import java.time.LocalDateTime;

public record CommentResponseDTO(long id, String content, String author, LocalDateTime createdAt) {
}
