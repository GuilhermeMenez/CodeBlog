package blog.code.codeblog.service.interfaces;

import blog.code.codeblog.dto.PageResponseDTO;
import blog.code.codeblog.dto.comment.CommentResponseDTO;
import blog.code.codeblog.dto.post.CreatePostRequestDTO;
import blog.code.codeblog.dto.post.PostResponseDTO;
import blog.code.codeblog.dto.post.PutPostDTO;

import java.util.List;
import java.util.UUID;

public interface PostService {
    List<PostResponseDTO> findAll();
    PostResponseDTO findById(UUID id);
    String save(CreatePostRequestDTO post);
    void deletePost(UUID postId, String token);
    PageResponseDTO<PostResponseDTO> getBalancedFeed(UUID userId, int page, int size);
    PageResponseDTO<PostResponseDTO> getAllUserPosts(UUID userId, int page, int size);
    PostResponseDTO updatePost(UUID postId, PutPostDTO updatedPost);
    PageResponseDTO<CommentResponseDTO> getPostComments(UUID postId, int page, int size);
}
