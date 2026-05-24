package blog.code.codeblog.controller;

import blog.code.codeblog.dto.PageResponseDTO;
import blog.code.codeblog.dto.comment.CommentResponseDTO;
import blog.code.codeblog.dto.post.CreatePostRequestDTO;
import blog.code.codeblog.dto.post.PostResponseDTO;
import blog.code.codeblog.dto.post.PutPostDTO;
import blog.code.codeblog.service.interfaces.PostServiceInterface;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/post")
public class PostController {

    @Autowired
    PostServiceInterface postServiceInterface;

    @GetMapping(value = "userPosts/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PageResponseDTO<PostResponseDTO> getAllUserPosts(
            @PathVariable("id") UUID userid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Get all user posts request received for user {} (page: {}, size: {})", userid, page, size);
        return postServiceInterface.getAllUserPosts(userid, page, size);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PostResponseDTO getPostbyId(@PathVariable UUID id) {
        log.info("Get post by id request received for post {}", id);
        return postServiceInterface.findById(id);
    }

    @GetMapping("/users/{userId}/feed")
    @ResponseStatus(HttpStatus.OK)
    public PageResponseDTO<PostResponseDTO> getBalancedFeed(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Get balanced feed request received for user {} (page: {}, size: {})", userId, page, size);
        return postServiceInterface.getBalancedFeed(userId, page, size);
    }

    @PostMapping("/newpost")
    @ResponseStatus(HttpStatus.CREATED)
    public String createPost(@ModelAttribute @Valid CreatePostRequestDTO post) {
        log.info("Create post request received: {}", post);
        return postServiceInterface.save(post);
    }

    @PutMapping("/edit/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PostResponseDTO updatePost(@PathVariable("id") UUID postId, @RequestBody @Valid PutPostDTO updatedPost) {
        log.info("Update post request received: {}", updatedPost);
        return postServiceInterface.updatePost(postId, updatedPost);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable("id") UUID postId, @RequestHeader("Authorization") String token) {
        log.info("Delete post request received: {}", postId);
        postServiceInterface.deletePost(postId, token);
    }

    @GetMapping("/posts")
    @ResponseStatus(HttpStatus.OK)
    public List<PostResponseDTO> getAllPosts() {
        log.info("Get all posts request received");
        return postServiceInterface.findAll();
    }

    @GetMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.OK)
    public PageResponseDTO<CommentResponseDTO> getAllComments(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Get all comments for post request received for post {} (page: {}, size: {})", id, page, size);
        return postServiceInterface.getPostComments(id, page, size);

    }
}