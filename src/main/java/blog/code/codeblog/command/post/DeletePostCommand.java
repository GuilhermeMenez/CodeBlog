package blog.code.codeblog.command.post;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
public class DeletePostCommand {
    private final UUID postId;
    private final UUID authorId;
}

