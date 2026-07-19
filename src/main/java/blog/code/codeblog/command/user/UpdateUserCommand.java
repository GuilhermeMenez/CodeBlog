package blog.code.codeblog.command.user;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
public class UpdateUserCommand {
    private final UUID userId;
    private final String name;
    private final String email;
    private final String password;
}
