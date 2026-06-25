package blog.code.codeblog.facade;

import blog.code.codeblog.dto.PageResponseDTO;
import blog.code.codeblog.dto.cloudinary.ImageUploadResponseDTO;
import blog.code.codeblog.dto.comment.CommentResponseDTO;
import blog.code.codeblog.dto.post.CreatePostRequestDTO;
import blog.code.codeblog.dto.post.PostResponseDTO;
import blog.code.codeblog.dto.post.PutPostDTO;
import blog.code.codeblog.model.Post;
import blog.code.codeblog.service.FeedService;
import blog.code.codeblog.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.PublicKey;
import java.util.UUID;

@Slf4j
@Component
public class PostFacade {
    @Autowired
    PostService postService;
    @Autowired
    FeedService feedService;

    public String createPost(CreatePostRequestDTO post) throws IOException {
       Post createdPost = postService.save(post);
       try {
           postService.processImages(createdPost, post.images());
       }catch (IOException e){
           log.error("[createPost] Failed to process images for post: {}. Error: {}", createdPost.getId(), e.getMessage());
       }
       return createdPost.getId().toString();
    }

    public PostResponseDTO updatePost(UUID postId, PutPostDTO post) {
        return postService.updatePost(postId, post);
    }

    public void deletePost(UUID postId, String token) {
        postService.deletePost(postId, token);
    }

    public PageResponseDTO<PostResponseDTO> getFeed(UUID userId, int page, int size) {
        return feedService.getBalancedFeed(userId, page, size);
    }

    public PostResponseDTO getPostById(UUID id) {
        return postService.findById(id);
    }

    public ImageUploadResponseDTO savePostImage(UUID postId, MultipartFile file) throws IOException {
        return postService.savePostImage(postId, file);
    }

    public void deletePostImage(String publicId) throws IOException {
        postService.deleteImage(publicId);
    }

    public PageResponseDTO<PostResponseDTO> getAllUserPosts(UUID userId, int page, int size) {
        return postService.getAllUserPosts(userId, page, size);
    }

    public PageResponseDTO<CommentResponseDTO> getPostComments(UUID postId, int page, int size) {
        return postService.getPostComments(postId, page, size);
    }

}
