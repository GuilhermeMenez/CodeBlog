package blog.code.codeblog.service;

import blog.code.codeblog.model.Post;

import java.util.List;

public interface CodeBlogService {
    List<Post> findAll();
    Post findById(Long id);
    Post save(Post post);
    void delete(Long id);
}
