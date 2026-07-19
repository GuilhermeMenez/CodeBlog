package blog.code.codeblog.facade;

import blog.code.codeblog.dto.PageResponseDTO;
import blog.code.codeblog.dto.cloudinary.ImageUploadResponseDTO;
import blog.code.codeblog.dto.comment.CommentResponseDTO;
import blog.code.codeblog.dto.post.CreatePostRequestDTO;
import blog.code.codeblog.dto.post.PostResponseDTO;
import blog.code.codeblog.dto.post.PutPostDTO;
import blog.code.codeblog.mapper.PostMapper;
import blog.code.codeblog.model.User;
import blog.code.codeblog.service.FeedService;
import blog.code.codeblog.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class PostFacade {
    @Autowired
    PostService postService;
    @Autowired
    FeedService feedService;


    public String createPost(CreatePostRequestDTO post, User author) {
        return postService.createPost(PostMapper.mapToCreatePostCommand(post, author));
    }

    public PostResponseDTO updatePost(PutPostDTO post, User author) {
        return postService.updatePost(PostMapper.mapToPutPostCommand(post, author));
    }

    public void deletePost(UUID postId, User author) {
        postService.deletePost(PostMapper.mapToDeletePostCommand(postId, author));
    }

    public PageResponseDTO<PostResponseDTO> getFeed(User user, int page, int size) {
        return feedService.getBalancedFeed(PostMapper.toGetFeedCommand(user, page, size));
    }

    public PostResponseDTO getPostById(UUID id) {
        return postService.findById(id);
    }

    public ImageUploadResponseDTO savePostImage(UUID postId, MultipartFile file, User author) throws IOException {
        return postService.savePostImage(PostMapper.toSavePostImageCommand(postId, file, author));

    }

    public void deletePostImage(UUID publicId, User author) throws IOException {
        postService.deleteImage(PostMapper.toDeletePostImageCommand(publicId, author));
    }

    public PageResponseDTO<PostResponseDTO> getAllUserPosts(User user, int page, int size) {
        return postService.getAllUserPosts(PostMapper.toGetAllUserPostsCommand(user, page, size));
    }

    public List<PostResponseDTO> getAllPosts() {
        return postService.findAll();
    }

    public PageResponseDTO<CommentResponseDTO> getPostComments(UUID postId, int page, int size) {
        return postService.getPostComments(PostMapper.toGetPostCommentsCommand(postId, page, size));
    }

}
