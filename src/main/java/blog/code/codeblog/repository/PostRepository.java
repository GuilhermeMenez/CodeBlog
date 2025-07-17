package blog.code.codeblog.repository;

import blog.code.codeblog.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PostRepository extends JpaRepository<Post, Long> {
}
