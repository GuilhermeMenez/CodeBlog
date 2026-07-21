package blog.code.codeblog.command.user;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
public class UnfollowCommand {
    private final UUID followerId;
    private final UUID followedId;
}
