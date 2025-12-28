package blog.code.codeblog.controller;

import blog.code.codeblog.dto.post.CommentDTO;
import blog.code.codeblog.dto.comment.CommentResponseDTO;
import blog.code.codeblog.service.interfaces.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController("/comment")
public class CommentController {

    @Autowired
    CommentService commentService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponseDTO createComment(@RequestBody CommentDTO comment){
    log.info("Create comment request received: {}", comment);
        return commentService.saveComment(comment);
    }
    @PutMapping("Update/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CommentResponseDTO updateComment(@PathVariable("id") UUID id, @RequestBody CommentDTO comment) {
        log.info("Update comment request received: {}", comment);
        return commentService.updateComment(comment, id);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable("id") UUID  id) {
        log.info("Delete comment request received: {}", id);
        commentService.deleteComment(id);
    }
}
