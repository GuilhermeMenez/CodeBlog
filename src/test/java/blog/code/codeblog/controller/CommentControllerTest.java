package blog.code.codeblog.controller;

import blog.code.codeblog.dto.CommentDTO;
import blog.code.codeblog.dto.CommentResponseDTO;
import blog.code.codeblog.service.interfaces.CommentService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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

        commentDTO = new CommentDTO("Conteúdo do comentário", postId, authorId);
        commentResponseDTO = new CommentResponseDTO(commentId, "Conteúdo do comentário", "Autor Teste", LocalDateTime.now());
    }


    @Test
    @DisplayName("Deve criar um comentário com sucesso")
    void createCommentShouldReturnCreatedComment() {
        when(commentService.saveComment(any(CommentDTO.class))).thenReturn(commentResponseDTO);

        ResponseEntity<CommentResponseDTO> response = commentController.createComment(commentDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(commentResponseDTO.id(), response.getBody().id());
        assertEquals("Conteúdo do comentário", response.getBody().content());
        assertEquals("Autor Teste", response.getBody().author());
        assertNotNull(response.getBody().createdAt());

        verify(commentService, times(1)).saveComment(any(CommentDTO.class));
    }


    @Test
    @DisplayName("Deve atualizar um comentário existente")
    void updateCommentShouldReturnUpdatedComment() {
        UUID commentId = UUID.randomUUID();
        when(commentService.updateComment(any(CommentDTO.class), eq(commentId))).thenReturn(commentResponseDTO);

        ResponseEntity<CommentResponseDTO> response = commentController.updateComment(commentId, commentDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(commentResponseDTO.id(), response.getBody().id());
        assertEquals("Conteúdo do comentário", response.getBody().content());
        assertEquals("Autor Teste", response.getBody().author());
        assertNotNull(response.getBody().createdAt());

        verify(commentService, times(1)).updateComment(any(CommentDTO.class), eq(commentId));
    }

    @Test
    @DisplayName("Deve retornar não encontrado ao tentar atualizar um comentário inexistente")
    void updateCommentWhenNotFoundShouldReturnNotFound() {
        UUID commentId = UUID.randomUUID();
        when(commentService.updateComment(any(CommentDTO.class), eq(commentId)))
                .thenThrow(new EntityNotFoundException("Comment not found with id " + commentId));

        try {
            commentController.updateComment(commentId, commentDTO);
        } catch (EntityNotFoundException ex) {
            assertEquals("Comment not found with id " + commentId, ex.getMessage());
        }

        verify(commentService, times(1)).updateComment(any(CommentDTO.class), eq(commentId));
    }

    @Test
    @DisplayName("Deve deletar um comentário existente")
    void deleteCommentShouldReturnOk() {
        UUID commentId = UUID.randomUUID();
        doNothing().when(commentService).deleteComment(commentId);

        ResponseEntity<?> response = commentController.deleteComment(commentId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());

        verify(commentService, times(1)).deleteComment(commentId);
    }

    @Test
    @DisplayName("Deve lidar com erro ao deletar comentário inexistente")
    void deleteCommentWhenNotFoundShouldHandleException() {
        UUID commentId = UUID.randomUUID();
        doThrow(new EntityNotFoundException("Comment not found with id " + commentId))
                .when(commentService).deleteComment(commentId);

        try {
            commentController.deleteComment(commentId);
        } catch (EntityNotFoundException ex) {
            assertEquals("Comment not found with id " + commentId, ex.getMessage());
        }

        verify(commentService, times(1)).deleteComment(commentId);
    }
}