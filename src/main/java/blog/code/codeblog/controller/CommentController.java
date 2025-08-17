package blog.code.codeblog.controller;

import blog.code.codeblog.dto.CommentDTO;
import blog.code.codeblog.dto.CommentResponseDTO;
import blog.code.codeblog.service.interfaces.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController("/comment")
public class CommentController {

    @Autowired
    CommentService commentService;

    @PostMapping("/create")
    public ResponseEntity<CommentResponseDTO> createComment(@RequestBody CommentDTO comment){
        return ResponseEntity.ok(commentService.saveComment(comment));

    }

    @PutMapping("UpdateComment/{id}")
    public ResponseEntity<CommentResponseDTO> updateComment(@PathVariable("id") UUID id, @RequestBody CommentDTO comment) {
        return ResponseEntity.ok(commentService.updateComment(comment, id));
    }

    @DeleteMapping("/deleteComment/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable("id") UUID  id) {
        commentService.deleteComment(id);
        return ResponseEntity.ok().build();
    }
}
