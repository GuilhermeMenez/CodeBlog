package blog.code.codeblog.service;

import blog.code.codeblog.dto.post.CommentDTO;
import blog.code.codeblog.dto.comment.CommentResponseDTO;
import blog.code.codeblog.model.Comment;
import blog.code.codeblog.model.Post;
import blog.code.codeblog.model.User;
import blog.code.codeblog.repository.CommentRepository;
import blog.code.codeblog.service.interfaces.CommentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    private final UserService userService;

    private final PostServiceImpl postServiceImpl;

    @Override
    public CommentResponseDTO saveComment(@NotNull CommentDTO comment) {
        log.info("[saveComment] Attempting to save comment for postId: {} by authorId: {}", comment.postId(), comment.authorId());

        if (comment.content() == null) {
            throw new IllegalArgumentException("Comment content cannot be null");
        }

        User commentAuthor = userService.getReference(comment.authorId());
        Post post = postServiceImpl.getReference(comment.postId());

        Comment newComment = new Comment(comment.content(), commentAuthor, post, commentAuthor.getName());

        post.getComments().add(newComment);
        commentRepository.save(newComment);

        log.info("[saveComment] Comment saved successfully. commentId: {}", newComment.getId());

        return new CommentResponseDTO(newComment.getId(), newComment.getContent(), commentAuthor.getName(), newComment.getCreatedAt());
    }

    @Override
    public void deleteComment(UUID id) {
        log.info("[deleteComment] Attempting to delete comment with id: {}", id);

        if (!commentRepository.existsById(id)) {
            log.warn("[deleteComment] Comment not found for deletion. id: {}", id);
            throw new EntityNotFoundException("Comment not found with id: " + id);
        }

        commentRepository.deleteById(id);
        log.info("[deleteComment] Comment deleted successfully. id: {}", id);
    }

    @Override
    public CommentResponseDTO updateComment(CommentDTO dto, UUID commentId) {
        log.info("[updateComment] Attempting to update comment. commentId: {}", commentId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.warn("[updateComment] Comment not found for update. id: {}", commentId);
                    return new EntityNotFoundException("Comment not found with id " + commentId);
                });

        comment.setContent(dto.content());
        comment.setCreatedAt(LocalDateTime.now());

        log.info("[updateComment] Comment updated successfully. commentId: {}", commentId);

        return new CommentResponseDTO(
                comment.getId(),
                comment.getContent(),
                comment.getAutor(),
                comment.getCreatedAt()
        );
    }




}
