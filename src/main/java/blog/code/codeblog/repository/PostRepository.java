package blog.code.codeblog.repository;

import blog.code.codeblog.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    @Query("SELECT p FROM Post p WHERE p.date >= :since ORDER BY p.date DESC")
    List<Post> findAllRecentPosts(@Param("since") LocalDate since, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN p.images i WHERE KEY(i) = :publicId")
    Optional<Post> findByImagePublicId(@Param("publicId") String publicId);

    @Query("SELECT p FROM Post p WHERE p.user.id = :userId ORDER BY p.date DESC")
    Page<Post> findByAuthorId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.date >= :since")
    long countAllRecentPosts(@Param("since") LocalDate since);


    @Query("""
        SELECT p FROM Post p\s
        WHERE (
            p.user.id IN (SELECT uf.followed.id FROM UserFollow uf WHERE uf.follower.id = :userId)
            OR (p.user.id <> :userId AND p.date >= :since)
        )""")
    List<Post> findFeedPosts(
        @Param("userId") UUID userId,
        @Param("since") LocalDate since,
        Pageable pageable
    );

    @Query("""
        SELECT COUNT(p) FROM Post p
        WHERE (
            p.user.id IN (SELECT uf.followed.id FROM UserFollow uf WHERE uf.follower.id = :userId)
            OR (p.user.id <> :userId AND p.date >= :since)
        )
        """)
    long countFeedPosts(@Param("userId") UUID userId, @Param("since") LocalDate since);
}



