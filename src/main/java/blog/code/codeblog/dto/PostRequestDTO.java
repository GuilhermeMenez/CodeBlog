package blog.code.codeblog.dto;

import java.util.UUID;

public record PostRequestDTO(String title, String content, UUID authorId) {
}
