package blog.code.codeblog.command.feed;

import blog.code.codeblog.model.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetFeedCommand {
    private final User user;
    private final int page;
    private final int size;
}
