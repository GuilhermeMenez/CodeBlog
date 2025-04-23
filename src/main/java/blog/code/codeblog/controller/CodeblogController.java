package blog.code.codeblog.controller;
import blog.code.codeblog.model.Post;
import blog.code.codeblog.service.CodeBlogService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
public class CodeblogController {

    @Autowired
    CodeBlogService codeBlogService;

    @GetMapping(value = "/posts")
    public List<Post> getPosts() {
        return codeBlogService.findAll();
    }


    @GetMapping(value = "/posts/{id}")
    public Post getPostsbyId(@PathVariable("id") long id){
        return codeBlogService.findById(id);
    }


    @PostMapping(value="/newpost")
    public Post savePost(@RequestBody Post post){
        return codeBlogService.save(post);
    }

    @PutMapping("/posts/{id}")
    public ResponseEntity<?> updatePost(@PathVariable("id") long id, @RequestBody @Valid Post updatedPost) {
        Post existingPost = codeBlogService.findById(id);
        if (existingPost == null) {
            return ResponseEntity.notFound().build();
        }

        existingPost.setTitulo(updatedPost.getTitulo());
        existingPost.setAutor(updatedPost.getAutor());
        existingPost.setTexto(updatedPost.getTexto());
        existingPost.setData(updatedPost.getData());
        codeBlogService.save(existingPost);

        return ResponseEntity.ok(existingPost);
    }





}
