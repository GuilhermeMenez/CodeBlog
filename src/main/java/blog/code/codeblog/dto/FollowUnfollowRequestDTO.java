package blog.code.codeblog.dto;

import jakarta.validation.Valid;

public record FollowUnfollowRequestDTO(@Valid String followerId, @Valid String followedId, boolean isFollow)
{
}
