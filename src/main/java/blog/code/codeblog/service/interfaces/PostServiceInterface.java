package blog.code.codeblog.service.interfaces;

import blog.code.codeblog.command.post.*;
import blog.code.codeblog.dto.PageResponseDTO;
import blog.code.codeblog.dto.comment.CommentResponseDTO;
import blog.code.codeblog.dto.post.PostResponseDTO;

import java.util.List;
import java.util.UUID;

public interface PostServiceInterface {
    List<PostResponseDTO> findAll();
    PostResponseDTO findById(UUID id);
     String createPost(CreatePostCommand postCommand);
    void deletePost(DeletePostCommand deletePostCommand);
    PageResponseDTO<PostResponseDTO> getAllUserPosts(GetAllUserPostsCommand command);
    PostResponseDTO updatePost(PutPostCommand putPostCommand);
    PageResponseDTO<CommentResponseDTO> getPostComments(GetPostCommentsCommand command);
}
