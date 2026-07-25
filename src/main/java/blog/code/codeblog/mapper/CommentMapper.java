package blog.code.codeblog.mapper;

import blog.code.codeblog.dto.comment.CommentResponseDTO;
import blog.code.codeblog.model.Comment;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CommentMapper {

    public CommentResponseDTO toCommentResponseDTO(Comment comment) {
        if (comment == null) return null;

        return CommentResponseDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .author(comment.getAutor())
                .createdAt(comment.getCreatedAt())
                .build();
    }

}

