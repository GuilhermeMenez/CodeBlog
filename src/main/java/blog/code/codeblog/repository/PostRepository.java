package blog.code.codeblog.repository;

import blog.code.codeblog.model.Post;
import blog.code.codeblog.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {
    @Query("SELECT p FROM Post p WHERE p.author IN :following ORDER BY p.date DESC")
    List<Post> findRecentPosts(@Param("following") Set<User> following, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.author IN :following ORDER BY function('RANDOM')")
    List<Post> findRandomPosts(@Param("following") Set<User> following, Pageable pageable);
}
