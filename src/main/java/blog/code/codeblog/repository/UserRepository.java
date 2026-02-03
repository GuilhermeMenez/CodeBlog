package blog.code.codeblog.repository;

import blog.code.codeblog.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    User findByLogin(String email);
    Optional<User> findByProfilePicId(String profilePicId);

    @Query("SELECT f FROM User u JOIN u.followers f WHERE u.id = :userId")
    Page<User> findFollowersByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT f FROM User u JOIN u.following f WHERE u.id = :userId")
    Page<User> findFollowingByUserId(@Param("userId") UUID userId, Pageable pageable);
}
