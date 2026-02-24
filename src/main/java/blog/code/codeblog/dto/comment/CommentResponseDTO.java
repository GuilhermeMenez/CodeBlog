package blog.code.codeblog.dto.comment;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record CommentResponseDTO(
        UUID id,
        String content,
        String author,
        LocalDateTime createdAt)
{ }
