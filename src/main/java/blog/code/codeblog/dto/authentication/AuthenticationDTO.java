package blog.code.codeblog.dto.authentication;

import blog.code.codeblog.enums.AuthFlow;
import jakarta.validation.constraints.NotNull;

public record AuthenticationDTO(
        @NotNull String login,
        @NotNull String credential,
        @NotNull AuthFlow flow
) {}