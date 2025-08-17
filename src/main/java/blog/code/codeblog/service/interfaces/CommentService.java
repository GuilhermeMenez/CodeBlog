package blog.code.codeblog.service.interfaces;

import blog.code.codeblog.dto.CommentDTO;
import blog.code.codeblog.dto.CommentResponseDTO;

import java.util.UUID;

public interface CommentService {
    CommentResponseDTO saveComment(CommentDTO comment);
    void deleteComment(UUID id);
    CommentResponseDTO updateComment(CommentDTO comment, UUID commentId);

}
