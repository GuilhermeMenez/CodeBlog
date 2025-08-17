package blog.code.codeblog.service;

import blog.code.codeblog.dto.CommentDTO;
import blog.code.codeblog.dto.CommentResponseDTO;
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
    @DisplayName("Deve salvar comentário com sucesso e retornar DTO de resposta")
    void saveCommentShouldReturnCommentResponseDto() {

        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        CommentDTO commentDTO = new CommentDTO("Conteúdo do comentário", userId, postId);
        User mockUser = new User();
        mockUser.setName("Autor Teste");

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
        assertEquals("Conteúdo do comentário", result.content());
        assertEquals("Autor Teste", result.author() );
        assertNotNull(result.createdAt());

        verify(commentRepository, times(1)).save(any(Comment.class));
        verify(postServiceImpl, times(1)).getReference(any(UUID.class));
        verify(userService, times(1)).getReference(any(UUID.class));
    }

    @Test
    @DisplayName("Deve deletar comentário ao receber ID válido")
    void deleteCommentShouldCallRepositoryDelete() {

        UUID commentId = UUID.randomUUID();
        doNothing().when(commentRepository).deleteById(commentId);

        // Act
        commentService.deleteComment(commentId);

        // Assert
        verify(commentRepository, times(1)).deleteById(commentId);
    }

    @Test
    @DisplayName("Deve atualizar comentário existente e retornar DTO atualizado")
    void updateCommentWhenExistsShouldUpdateAndReturnDto() {

        UUID commentId = UUID.randomUUID();
        CommentDTO updateDTO = new CommentDTO("Novo conteúdo", null, null);

        Comment existingComment = new Comment();
        existingComment.setId(commentId);
        existingComment.setContent("Conteúdo antigo");
        existingComment.setAutor("Autor");

        LocalDateTime originalCreatedAt = LocalDateTime.now().minusDays(1);
        existingComment.setCreatedAt(originalCreatedAt);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(existingComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(existingComment);

        CommentResponseDTO result = commentService.updateComment(updateDTO, commentId);

        assertNotNull(result);
        assertEquals(commentId, result.id());
        assertEquals("Novo conteúdo", result.content());
        assertEquals("Autor", result.author());
        assertTrue(
                result.createdAt().isAfter(originalCreatedAt)

        );

        verify(commentRepository, times(1)).findById(commentId);
        verify(commentRepository, times(1)).save(existingComment);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar comentário inexistente")
    void updateCommentWhenNotFoundShouldThrowException() {

        UUID commentId = UUID.randomUUID();
        CommentDTO updateDTO = new CommentDTO("Conteúdo", null, null);

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> commentService.updateComment(updateDTO, commentId)
        );

        assertEquals("Comment not found with id " + commentId, exception.getMessage());

        verify(commentRepository, times(1)).findById(commentId);
        verify(commentRepository, never()).save(any());
    }
}