package blog.code.codeblog.command.post;

import blog.code.codeblog.model.User;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Builder
@Getter
public class SavePostImageCommand {
    private final UUID postId;
    private final MultipartFile file;
    private final User author;
}

