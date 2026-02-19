package blog.code.codeblog.repository;

import blog.code.codeblog.model.User;
import blog.code.codeblog.model.UserFollow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, UUID> {

    @Query("SELECT COUNT(uf) FROM UserFollow uf WHERE uf.followed.id = :userId")
    long countFollowersByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(uf) FROM UserFollow uf WHERE uf.follower.id = :userId")
    long countFollowingByUserId(@Param("userId") UUID userId);

    boolean existsByFollower_IdAndFollowed_Id(UUID followerId, UUID followedId);

    int deleteByFollower_IdAndFollowed_Id(UUID followerId, UUID followedId);

    @Query("SELECT uf.follower FROM UserFollow uf WHERE uf.followed.id = :userId")
    Page<User> findFollowersByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT uf.followed FROM UserFollow uf WHERE uf.follower.id = :userId")
    Page<User> findFollowingByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT uf FROM UserFollow uf WHERE uf.follower.id = :followerId AND uf.followed.id = :followedId")
    Optional<UserFollow> findByFollowerIdAndFollowedId(@Param("followerId") UUID followerId, @Param("followedId") UUID followedId);

}

