package blog.code.codeblog.dto.user;

import lombok.Builder;

import java.io.Serializable;
import java.util.UUID;

@Builder
public record UserResponseDTO(
        UUID id,
        String name,
        String login,
        String urlProfilePic,
        long followersCount,
        long followingCount
) implements Serializable {}
