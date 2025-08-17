package blog.code.codeblog.dto;


import java.util.UUID;

public record CommentDTO(String content, UUID postId, UUID authorId) { }
