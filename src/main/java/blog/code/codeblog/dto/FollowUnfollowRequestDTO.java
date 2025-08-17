package blog.code.codeblog.dto;


import java.util.UUID;

public record FollowUnfollowRequestDTO(UUID followerId, UUID followedId, boolean isFollow)
{
}
