package blog.code.codeblog.controller;
import blog.code.codeblog.dto.PostDTO;
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
public class PostController {

    @Autowired
    PostService postService;

    @GetMapping(value = "{id}/posts")
    public List<Post> getAllUserPosts(@PathVariable("id") UUID userid) {
        return postService.getAllUserPosts(userid);
    }

    @GetMapping(value = "/posts/{id}")
    public Post getPostsbyId(@PathVariable("id") UUID id){
        return postService.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Post não encontrado") );
    }

    @PostMapping(value = "/newpost")
    public ResponseEntity<Post> createPost(@RequestBody @Valid Post post) {
        Post savedPost = postService.save(post);
        return ResponseEntity.status(201).body(savedPost);
    }

    @PutMapping("/posts/edit/{id}")
    public ResponseEntity<Post> updatePost(@PathVariable("id") UUID id, @RequestBody @Valid PostDTO updatedPost) {
        return postService.updatePost(id, updatedPost)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<?> deletePost(@PathVariable("id") UUID postId) {
        postService.deletePost(postId);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/{userId}/feed")
    public ResponseEntity<List<Post>> getBalancedFeed(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.getBalancedFeed(userId, page, size));
    }


}