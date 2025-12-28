package blog.code.codeblog.service;

import blog.code.codeblog.dto.post.CommentDTO;
import blog.code.codeblog.dto.comment.CommentResponseDTO;
import blog.code.codeblog.model.Comment;
import blog.code.codeblog.model.Post;
import blog.code.codeblog.model.User;
import blog.code.codeblog.repository.CommentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserService userService;

    @Mock
    private PostServiceImpl postServiceImpl;

    @InjectMocks
    private CommentServiceImpl commentService;

    @Test
    @DisplayName("Should save comment successfully and return response DTO")
    void saveCommentShouldReturnCommentResponseDto() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        CommentDTO commentDTO = new CommentDTO("Comment content", userId, postId);
        User mockUser = new User();
        mockUser.setName("Test Author");
        Post mockPost = new Post();
        mockPost.setId(postId);
        when(userService.getReference(any(UUID.class))).thenReturn(mockUser);
        when(postServiceImpl.getReference(any(UUID.class))).thenReturn(mockPost);
        UUID generatedId = UUID.randomUUID();
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId(generatedId);
            comment.setCreatedAt(LocalDateTime.now());
            return comment;
        });
        CommentResponseDTO result = commentService.saveComment(commentDTO);
        assertNotNull(result);
        assertEquals(generatedId, result.id());
        assertEquals("Comment content", result.content());
        assertEquals("Test Author", result.author());
        assertNotNull(result.createdAt());
        verify(commentRepository, times(1)).save(any(Comment.class));
        verify(postServiceImpl, times(1)).getReference(any(UUID.class));
        verify(userService, times(1)).getReference(any(UUID.class));
    }

    @Test
    @DisplayName("Should delete comment when given valid ID")
    void deleteCommentShouldCallRepositoryDelete() {
        UUID commentId = UUID.randomUUID();
        when(commentRepository.existsById(commentId)).thenReturn(true);
        doNothing().when(commentRepository).deleteById(commentId);
        assertDoesNotThrow(() -> commentService.deleteComment(commentId));
        verify(commentRepository, times(1)).existsById(commentId);
        verify(commentRepository, times(1)).deleteById(commentId);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when deleting non-existent comment")
    void deleteCommentWhenNotFoundShouldThrowException() {
        UUID commentId = UUID.randomUUID();
        when(commentRepository.existsById(commentId)).thenReturn(false);
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> commentService.deleteComment(commentId)
        );
        assertEquals("Comment not found with id: " + commentId, exception.getMessage());
        verify(commentRepository, times(1)).existsById(commentId);
        verify(commentRepository, never()).deleteById(commentId);
    }

    @Test
    @DisplayName("Should update existing comment and return updated DTO")
    void updateCommentWhenExistsShouldUpdateAndReturnDto() {
        UUID commentId = UUID.randomUUID();
        CommentDTO updateDTO = new CommentDTO("New content", null, null);
        Comment existingComment = new Comment();
        existingComment.setId(commentId);
        existingComment.setContent("Old content");
        existingComment.setAutor("Author");
        LocalDateTime originalCreatedAt = LocalDateTime.now().minusDays(1);
        existingComment.setCreatedAt(originalCreatedAt);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(existingComment));
        CommentResponseDTO result = commentService.updateComment(updateDTO, commentId);
        assertNotNull(result);
        assertEquals(commentId, result.id());
        assertEquals("New content", result.content());
        assertEquals("Author", result.author());
        assertTrue(result.createdAt().isAfter(originalCreatedAt));
        verify(commentRepository, times(1)).findById(commentId);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when updating non-existent comment")
    void updateCommentWhenNotFoundShouldThrowException() {
        UUID commentId = UUID.randomUUID();
        CommentDTO updateDTO = new CommentDTO("Content", null, null);
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> commentService.updateComment(updateDTO, commentId)
        );
        assertEquals("Comment not found with id " + commentId, exception.getMessage());
        verify(commentRepository, times(1)).findById(commentId);
        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when updating with null ID")
    void updateCommentWithNullIdShouldThrowException() {
        CommentDTO updateDTO = new CommentDTO("Content", null, null);
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> commentService.updateComment(updateDTO, null)
        );
        assertEquals("Comment not found with id null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when saving comment with null content")
    void saveCommentWithNullContentShouldThrowException() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        CommentDTO commentDTO = new CommentDTO(null, userId, postId);
        assertThrows(IllegalArgumentException.class, () -> commentService.saveComment(commentDTO));
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("Should throw exception when author not found")
    void saveCommentWithAuthorNotFoundShouldThrowException() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        CommentDTO commentDTO = new CommentDTO("content", userId, postId);
        when(userService.getReference(any(UUID.class))).thenThrow(new EntityNotFoundException("User not found"));
        assertThrows(EntityNotFoundException.class, () -> commentService.saveComment(commentDTO));
        verify(userService, times(1)).getReference(any(UUID.class));
    }

    @Test
    @DisplayName("Should throw exception when post not found")
    void saveCommentWithPostNotFoundShouldThrowException() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        CommentDTO commentDTO = new CommentDTO("content", userId, postId);
        User mockUser = new User();
        mockUser.setName("Test Author");
        when(userService.getReference(any(UUID.class))).thenReturn(mockUser);
        when(postServiceImpl.getReference(any(UUID.class))).thenThrow(new EntityNotFoundException("Post not found"));
        assertThrows(EntityNotFoundException.class, () -> commentService.saveComment(commentDTO));
        verify(postServiceImpl, times(1)).getReference(any(UUID.class));
    }

    @Test
    @DisplayName("Should save and update comment with large content")
    void saveAndUpdateCommentWithLargeContent() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        String largeContent = "A".repeat(10000);
        CommentDTO commentDTO = new CommentDTO(largeContent, userId, postId);
        User mockUser = new User();
        mockUser.setName("Test Author");
        Post mockPost = new Post();
        mockPost.setId(postId);
        when(userService.getReference(any(UUID.class))).thenReturn(mockUser);
        when(postServiceImpl.getReference(any(UUID.class))).thenReturn(mockPost);
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId(UUID.randomUUID());
            comment.setCreatedAt(LocalDateTime.now());
            return comment;
        });
        CommentResponseDTO result = commentService.saveComment(commentDTO);
        assertEquals(largeContent, result.content());
        verify(commentRepository, times(1)).save(any(Comment.class));

        // Update
        UUID commentId = result.id();
        Comment existingComment = new Comment();
        existingComment.setId(commentId);
        existingComment.setContent("Old content");
        existingComment.setAutor("Author");
        existingComment.setCreatedAt(LocalDateTime.now().minusDays(1));
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(existingComment));
        CommentDTO updateDTO = new CommentDTO(largeContent, null, null);
        CommentResponseDTO updated = commentService.updateComment(updateDTO, commentId);
        assertEquals(largeContent, updated.content());
        verify(commentRepository, times(1)).findById(commentId);
    }

    @Test
    @DisplayName("Should propagate repository exception on delete")
    void deleteCommentShouldPropagateRepositoryException() {
        UUID commentId = UUID.randomUUID();
        when(commentRepository.existsById(commentId)).thenReturn(true);
        doThrow(new RuntimeException("DB error")).when(commentRepository).deleteById(commentId);
        assertThrows(RuntimeException.class, () -> commentService.deleteComment(commentId));
        verify(commentRepository, times(1)).deleteById(commentId);
    }

    @Test
    @DisplayName("Should propagate repository exception on update")
    void updateCommentShouldPropagateRepositoryException() {
        UUID commentId = UUID.randomUUID();
        CommentDTO updateDTO = new CommentDTO("content", null, null);
        when(commentRepository.findById(commentId)).thenThrow(new RuntimeException("DB error"));
        assertThrows(RuntimeException.class, () -> commentService.updateComment(updateDTO, commentId));
        verify(commentRepository, times(1)).findById(commentId);
    }



}
