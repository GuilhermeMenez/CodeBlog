package blog.code.codeblog.service;

import blog.code.codeblog.dto.CommentDTO;
import blog.code.codeblog.dto.CommentResponseDTO;
import blog.code.codeblog.model.Comment;
import blog.code.codeblog.model.Post;
import blog.code.codeblog.model.User;
import blog.code.codeblog.repository.CommentRepository;
import blog.code.codeblog.service.interfaces.CommentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    private final UserService userService;

    private final PostServiceImpl postServiceImpl;

    @Override
    public CommentResponseDTO saveComment(@NotNull CommentDTO comment) {
        User commentAuthor = userService.getReference(comment.authorId());
        Post post = postServiceImpl.getReference(comment.postId());

        Comment newComment = new Comment(comment.content(), commentAuthor, post, commentAuthor.getName());

        post.getComments().add(newComment);
        commentRepository.save(newComment);

        return new CommentResponseDTO(newComment.getId(), newComment.getContent(), commentAuthor.getName(), newComment.getCreatedAt());
    }

    @Override
    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }

    @Override
    public CommentResponseDTO updateComment(CommentDTO comment, long commentId) {
        return commentRepository.findById(commentId)
                .map(existingComment -> {
                    existingComment.setContent(comment.content());
                    existingComment.setCreatedAt(LocalDateTime.now());
                    commentRepository.save(existingComment);
                    return new CommentResponseDTO(
                            existingComment.getId(),
                            existingComment.getContent(),
                            existingComment.getAutor(),
                            existingComment.getCreatedAt());
                })
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id " + commentId));
    }



}
