package blog.code.codeblog.controller;
import blog.code.codeblog.model.Post;
import blog.code.codeblog.service.interfaces.PostService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
public class PostController {

    @Autowired
    PostService postService;

    @GetMapping(value = "{id}/posts")
    public List<Post> getAllUserPosts(@PathVariable("id") String userid) {
        return postService.getAllUserPosts(userid);
    }

    @GetMapping(value = "/posts/{id}")
    public Post getPostsbyId(@PathVariable("id") String id){
        return postService.findById(id).orElse(null);
    }

    @PostMapping(value = "/newpost")
    public ResponseEntity<Post> createPost(@RequestBody @Valid Post post) {
        Post savedPost = postService.save(post);

        return ResponseEntity.status(201).body(savedPost);
    }

    @PutMapping("/posts/edit/{id}")
    public ResponseEntity<?> updatePost(@PathVariable("id") String id, @RequestBody @Valid Post updatedPost) {
        return postService.findById(id)
                .map(existingPost -> {
                    existingPost.setTitulo(updatedPost.getTitulo());
                    existingPost.setAutor(updatedPost.getAutor());
                    existingPost.setTexto(updatedPost.getTexto());
                    existingPost.setData(updatedPost.getData());
                    postService.save(existingPost);
                    return ResponseEntity.ok().body(existingPost);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<?> deletePost(@PathVariable("id") String id) {
        return postService.findById(id)
                .map(existingPost -> {
                    postService.delete(id);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }


    @GetMapping("/{userId}/feed")
    public ResponseEntity<List<Post>> getBalancedFeed(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.getBalancedFeed(userId, page, size));
    }


}