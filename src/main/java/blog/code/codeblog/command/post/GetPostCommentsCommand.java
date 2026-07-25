package blog.code.codeblog.command.post;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
public class GetPostCommentsCommand {
    private final UUID postId;
    int page;
    int size;
}
