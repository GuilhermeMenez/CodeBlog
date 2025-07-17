package blog.code.codeblog.dto;

import jakarta.validation.Valid;

public record FollowRequestDTO(@Valid String followerId, @Valid String followedId)
{
}
