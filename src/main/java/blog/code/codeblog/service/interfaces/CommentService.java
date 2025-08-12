package blog.code.codeblog.service.interfaces;

import blog.code.codeblog.dto.CommentDTO;
import blog.code.codeblog.dto.CommentResponseDTO;
import blog.code.codeblog.model.Comment;

public interface CommentService {
    CommentResponseDTO saveComment(CommentDTO comment);
    void deleteComment(Long id);
    CommentResponseDTO updateComment(CommentDTO comment,long commentId);

}
