package blog.code.codeblog.service.interfaces;

import blog.code.codeblog.dto.PostDTO;
import blog.code.codeblog.model.Post;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostService {
    List<Post> findAll();
    Optional<Post> findById(UUID id);
    Post save(Post post);
    void deletePost(UUID postId);
    List<Post> getBalancedFeed(UUID userId, int page, int size);
    List<Post>getAllUserPosts(UUID userId);
    Optional<Post> updatePost(UUID id, PostDTO updatedPost);
}
