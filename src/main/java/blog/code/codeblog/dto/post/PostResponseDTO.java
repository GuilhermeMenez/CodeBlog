package blog.code.codeblog.dto.post;

import blog.code.codeblog.dto.comment.CommentResponseDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PostResponseDTO(UUID postId, String title, String content, PostAuthorDTO author, LocalDate createdAt, List<CommentResponseDTO> comments) {
}