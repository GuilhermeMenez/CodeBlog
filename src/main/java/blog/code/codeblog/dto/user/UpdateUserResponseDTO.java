package blog.code.codeblog.dto.user;

import lombok.Builder;

@Builder
public record UpdateUserResponseDTO(String name, String email) {
}
