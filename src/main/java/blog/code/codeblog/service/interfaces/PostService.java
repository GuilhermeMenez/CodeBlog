package blog.code.codeblog.service.interfaces;

import blog.code.codeblog.model.Post;


import java.util.List;
import java.util.Optional;

public interface PostService {
    List<Post> findAll();
    Optional<Post> findById(String id);
    Post save(Post post);
    void delete(String id);
    List<Post> getBalancedFeed(String userId, int page, int size);
    List<Post>getAllUserPosts(String userId);
}
