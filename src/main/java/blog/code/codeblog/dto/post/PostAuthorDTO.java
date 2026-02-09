package blog.code.codeblog.dto.post;

import lombok.Builder;

import java.io.Serializable;
import java.util.UUID;

@Builder
public record PostAuthorDTO(UUID id, String name) implements Serializable {
}
