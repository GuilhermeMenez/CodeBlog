package blog.code.codeblog.dto.user;

import blog.code.codeblog.enums.AuthFlow;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record CreateUserDTO(
        @NotNull String name,
        @NotNull String email,
        @NotNull String credential,
        @NotNull AuthFlow flow,
        @Nullable MultipartFile profileImage
) {}