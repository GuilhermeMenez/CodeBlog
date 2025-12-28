package blog.code.codeblog.dto.user;

public record UpdateUserRequestDTO(
        String name,
        String email,
        String password
) {}