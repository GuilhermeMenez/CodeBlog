package blog.code.codeblog.command.user;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
public class GetFollowingCommand {
    UUID userId;
    int page;
    int size;
}
