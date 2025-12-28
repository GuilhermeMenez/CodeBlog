package blog.code.codeblog.dto.user;


import blog.code.codeblog.enums.UserRoles;
import jakarta.validation.constraints.NotNull;

public record CreateUserDTO(@NotNull String name, @NotNull String email, @NotNull String password, UserRoles user) { }

