package blog.code.codeblog.service.interfaces;

import blog.code.codeblog.dto.post.CommentDTO;
import blog.code.codeblog.dto.comment.CommentResponseDTO;

import java.util.UUID;

public interface CommentService {
    CommentResponseDTO saveComment(CommentDTO comment);
    void deleteComment(UUID id);
    CommentResponseDTO updateComment(CommentDTO comment, UUID commentId);

}
