package blog.code.codeblog.controller;

import blog.code.codeblog.dto.post.CommentDTO;
import blog.code.codeblog.dto.comment.CommentResponseDTO;
import blog.code.codeblog.service.interfaces.CommentService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController commentController;

    private CommentDTO commentDTO;
    private CommentResponseDTO commentResponseDTO;

    @BeforeEach
    void setUp() {
        UUID postId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();

        commentDTO = new CommentDTO("Comment content", postId, authorId);
        commentResponseDTO = new CommentResponseDTO(commentId, "Comment content", "Test Author", LocalDateTime.now());
    }

    @Test
    @DisplayName("Should create a comment successfully")
    void createCommentShouldReturnCreatedComment() {
        when(commentService.saveComment(any(CommentDTO.class))).thenReturn(commentResponseDTO);

        CommentResponseDTO response = commentController.createComment(commentDTO);

        assertNotNull(response);
        assertEquals(commentResponseDTO.id(), response.id());
        assertEquals("Comment content", response.content());
        assertEquals("Test Author", response.author());
        assertNotNull(response.createdAt());

        verify(commentService, times(1)).saveComment(any(CommentDTO.class));
    }

    @Test
    @DisplayName("Should update an existing comment")
    void updateCommentShouldReturnUpdatedComment() {
        UUID commentId = UUID.randomUUID();
        when(commentService.updateComment(any(CommentDTO.class), eq(commentId))).thenReturn(commentResponseDTO);

        CommentResponseDTO response = commentController.updateComment(commentId, commentDTO);

        assertNotNull(response);
        assertEquals(commentResponseDTO.id(), response.id());
        assertEquals("Comment content", response.content());
        assertEquals("Test Author", response.author());
        assertNotNull(response.createdAt());

        verify(commentService, times(1)).updateComment(any(CommentDTO.class), eq(commentId));
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when updating a non-existent comment")
    void updateCommentWhenNotFoundShouldThrowException() {
        UUID commentId = UUID.randomUUID();
        when(commentService.updateComment(any(CommentDTO.class), eq(commentId)))
                .thenThrow(new EntityNotFoundException("Comment not found with id " + commentId));

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> commentController.updateComment(commentId, commentDTO));
        assertEquals("Comment not found with id " + commentId, thrown.getMessage());
        verify(commentService, times(1)).updateComment(any(CommentDTO.class), eq(commentId));
    }

    @Test
    @DisplayName("Should delete an existing comment")
    void deleteCommentShouldReturnNoContent() {
        UUID commentId = UUID.randomUUID();
        doNothing().when(commentService).deleteComment(commentId);

        // Should not throw exception
        assertDoesNotThrow(() -> commentController.deleteComment(commentId));
        verify(commentService, times(1)).deleteComment(commentId);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when deleting a non-existent comment")
    void deleteCommentWhenNotFoundShouldThrowException() {
        UUID commentId = UUID.randomUUID();
        doThrow(new EntityNotFoundException("Comment not found with id " + commentId))
                .when(commentService).deleteComment(commentId);

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> commentController.deleteComment(commentId));
        assertEquals("Comment not found with id " + commentId, thrown.getMessage());
        verify(commentService, times(1)).deleteComment(commentId);
    }
}
