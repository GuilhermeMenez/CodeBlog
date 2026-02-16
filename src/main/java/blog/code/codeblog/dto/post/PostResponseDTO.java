package blog.code.codeblog.dto.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseDTO {
    private UUID postId;
    private String title;
    private String content;
    private PostAuthorDTO author;
    private LocalDate createdAt;
    private Map<String, String> images;
}