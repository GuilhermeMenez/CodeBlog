package blog.code.codeblog.command.post;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
public class PutPostCommand {
    private final UUID postId;
    private final String title;
    private final String content;
    private final UUID authorId;
}
