package blog.code.codeblog.controller;
import blog.code.codeblog.dto.PostDTO;
import blog.code.codeblog.dto.PostRequestDTO;
import blog.code.codeblog.model.Post;
import blog.code.codeblog.service.interfaces.PostService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/post")
public class PostController {

    @Autowired
    PostService postService;

    @GetMapping(value = "{id}/posts")
    public ResponseEntity<List<Post>> getAllUserPosts(@PathVariable("id") UUID userid) {
        return ResponseEntity.ok(postService.getAllUserPosts(userid));
    }

    @GetMapping(value = "/posts/{id}")
    public Post getPostsbyId(@PathVariable("id") UUID id){
        return postService.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Post não encontrado") );
    }

    @GetMapping("/{userId}/feed")
    public ResponseEntity<List<Post>> getBalancedFeed(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.getBalancedFeed(userId, page, size));
    }

    @PostMapping(value = "/newpost")
    public ResponseEntity<String> createPost(@RequestBody @Valid PostRequestDTO post) {
        String savedPostid = postService.save(post);
        return ResponseEntity.ok(savedPostid);
    }

    @PutMapping("/posts/edit/{id}")
    public ResponseEntity<Void> updatePost(@PathVariable("id") UUID id, @RequestBody @Valid PostDTO updatedPost) {
        postService.updatePost(id, updatedPost);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<?> deletePost(@PathVariable("id") UUID postId) {
        postService.deletePost(postId);
        return ResponseEntity.ok().build();
    }




}