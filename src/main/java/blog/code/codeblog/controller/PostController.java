package blog.code.codeblog.controller;

import blog.code.codeblog.dto.PageResponseDTO;
import blog.code.codeblog.dto.cloudinary.ImageUploadResponseDTO;
import blog.code.codeblog.dto.comment.CommentResponseDTO;
import blog.code.codeblog.dto.post.CreatePostRequestDTO;
import blog.code.codeblog.dto.post.PostResponseDTO;
import blog.code.codeblog.dto.post.PutPostDTO;
import blog.code.codeblog.facade.PostFacade;
import blog.code.codeblog.service.provider.UserProvider;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/post")
public class PostController {

    @Autowired
    PostFacade postFacade;
    @Autowired
    UserProvider userProvider;

    @GetMapping(value = "userPosts/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PageResponseDTO<PostResponseDTO> getAllUserPosts(
            @PathVariable("id") UUID userid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Get all user posts request received for user {} (page: {}, size: {})", userid, page, size);
        return postFacade.getAllUserPosts(userProvider.getCurrentUser(), page, size);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PostResponseDTO getPostbyId(@PathVariable UUID id) {
        log.info("Get post by id request received for post {}", id);
        return postFacade.getPostById(id);
    }

    @GetMapping("/users/{userId}/feed")
    @ResponseStatus(HttpStatus.OK)
    public PageResponseDTO<PostResponseDTO> getBalancedFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Get balanced feed request received for user {} (page: {}, size: {})",userProvider.getCurrentUser().getId() , page, size);
        return postFacade.getFeed(userProvider.getCurrentUser(), page, size);
    }

    @PostMapping("/newpost")
    @ResponseStatus(HttpStatus.CREATED)
    public String createPost(@ModelAttribute @Valid CreatePostRequestDTO post)  {
        log.info("Create post request received: {}", post);
        return postFacade.createPost(post, userProvider.getCurrentUser());
    }

    @PutMapping("/edit")
    @ResponseStatus(HttpStatus.OK)
    public PostResponseDTO updatePost(@RequestBody @Valid PutPostDTO updatedPost)  {
        log.info("Update post request received: {}", updatedPost);
        return postFacade.updatePost(updatedPost, userProvider.getCurrentUser());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable("id") UUID postId)  {
        log.info("Delete post request received: {}", postId);
        postFacade.deletePost(postId, userProvider.getCurrentUser());
    }

    @GetMapping("/posts")
    @ResponseStatus(HttpStatus.OK)
    public List<PostResponseDTO> getAllPosts() {
        log.info("Get all posts request received");
        return postFacade.getAllPosts();
    }

    @GetMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.OK)
    public PageResponseDTO<CommentResponseDTO> getAllComments(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Get all comments for post request received for post {} (page: {}, size: {})", id, page, size);
        return postFacade.getPostComments(id, page, size);

    }

    @PostMapping("upload")
    public ImageUploadResponseDTO uploadImage(@RequestParam("file") MultipartFile file, @RequestParam("postId") UUID postId) throws IOException {
        log.info("Upload image request received: {}", file.getOriginalFilename());
        return postFacade.savePostImage(postId, file, userProvider.getCurrentUser());
    }
}