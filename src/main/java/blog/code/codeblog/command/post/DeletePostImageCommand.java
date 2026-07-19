package blog.code.codeblog.command.post;

import blog.code.codeblog.model.User;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DeletePostImageCommand {
    String publicId;
    User user;
}
