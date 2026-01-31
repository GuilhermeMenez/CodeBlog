package blog.code.codeblog.service.interfaces;

import blog.code.codeblog.dto.post.CreatePostRequestDTO;
import blog.code.codeblog.dto.post.PostResponseDTO;
import blog.code.codeblog.dto.post.PutPostDTO;
import blog.code.codeblog.model.Post;


import java.util.List;
import java.util.UUID;

public interface PostService {
    List<PostResponseDTO> findAll();
    PostResponseDTO findById(UUID id);
    String save(CreatePostRequestDTO post);
    void deletePost(UUID postId, String token);
    List<Post> getBalancedFeed(UUID userId, int page, int size);
    List<Post>getAllUserPosts(UUID userId);
    PostResponseDTO  updatePost(UUID postId, PutPostDTO updatedPost);

}
