package blog.code.codeblog.dto.post;

import lombok.Builder;

import java.util.UUID;

@Builder
public record PostAuthorDTO(UUID id, String name) {
}
