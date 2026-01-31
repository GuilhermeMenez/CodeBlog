package blog.code.codeblog.dto.user;


import blog.code.codeblog.enums.UserRoles;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record CreateUserDTO(@NotNull String name, @NotNull String email, @NotNull String password, UserRoles user,
                            @Nullable MultipartFile profileImage) { }

