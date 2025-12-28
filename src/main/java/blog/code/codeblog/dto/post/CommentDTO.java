package blog.code.codeblog.dto.post;


import java.util.UUID;

public record CommentDTO(String content, UUID postId, UUID authorId) { }
