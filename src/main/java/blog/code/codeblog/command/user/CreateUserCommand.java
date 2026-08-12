package blog.code.codeblog.command.user;

import blog.code.codeblog.enums.AuthFlow;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
@Setter
public class CreateUserCommand {
    private final String name;
    private final String email;
    private  String credential;
    private final AuthFlow flow;
    @Nullable
    private final MultipartFile profileImage;
}