package blog.code.codeblog.command.post;

import blog.code.codeblog.model.User;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Builder
public class CreatePostCommand {
    private final String title;
    private final String content;
    private final String authorName;
    private final User author;
    @Nullable
    private final List<MultipartFile> images;

}
