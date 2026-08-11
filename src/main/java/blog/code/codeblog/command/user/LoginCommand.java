package blog.code.codeblog.command.user;

import blog.code.codeblog.enums.AuthFlow;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginCommand {
    private final String login;
    private final String credential;   
    private final AuthFlow flow;
}