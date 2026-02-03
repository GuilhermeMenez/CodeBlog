package blog.code.codeblog.dto.user;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UserFollowDTO(
        UUID id,
        String name,
        String login,
        String urlProfilePic
) {}
