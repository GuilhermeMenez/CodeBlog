package blog.code.codeblog.controller;

import blog.code.codeblog.dto.post.CreatePostRequestDTO;
import blog.code.codeblog.dto.post.PostResponseDTO;
import blog.code.codeblog.dto.post.PutPostDTO;
import blog.code.codeblog.model.Post;
import blog.code.codeblog.service.interfaces.PostService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/post")
public class PostController {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(PostController.class);
    @Autowired
    PostService postService;

    @GetMapping(value = "userPosts/{id}")
    @ResponseStatus(HttpStatus.OK)
    public List<Post> getAllUserPosts(@PathVariable("id") UUID userid) {
        log.info("Get all user posts request received for user {}", userid);
        return postService.getAllUserPosts(userid);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PostResponseDTO getPostsbyId(@PathVariable("id") UUID id) {
        log.info("Get post by id request received for post {}", id);
        return postService.findById(id);
    }

    @GetMapping("feed/{userId}")
    public List<Post> getBalancedFeed(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return postService.getBalancedFeed(userId, page, size);
    }

    @PostMapping("/newpost")
    @ResponseStatus(HttpStatus.CREATED)
    public String createPost(@RequestBody @Valid CreatePostRequestDTO post) {
        log.info("Create post request received: {}", post);
        return postService.save(post);
    }

    @PutMapping("/edit/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PostResponseDTO updatePost(@PathVariable("id") UUID postId, @RequestBody @Valid PutPostDTO updatedPost) {
        log.info("Update post request received: {}", updatedPost);
        return postService.updatePost(postId, updatedPost);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable("id") UUID postId, @RequestHeader("Authorization") String token) {
        log.info("Delete post request received: {}", postId);
        postService.deletePost(postId, token);
    }

    @GetMapping("posts")
    @ResponseStatus(HttpStatus.OK)
    public List<PostResponseDTO> getAllPosts() {
        log.info("Get all posts request received");
        return postService.findAll();
    }


}