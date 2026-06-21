package blog.code.codeblog.controller;

import blog.code.codeblog.dto.post.CommentDTO;
import blog.code.codeblog.dto.comment.CommentResponseDTO;
import blog.code.codeblog.service.interfaces.CommentServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    CommentServiceInterface commentServiceInterface;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponseDTO createComment(@RequestBody CommentDTO comment){
    log.info("Create comment request received: {}", comment);
        return commentServiceInterface.saveComment(comment);
    }
    @PutMapping("update/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CommentResponseDTO updateComment(@PathVariable UUID id, @RequestBody CommentDTO comment) {
        log.info("Update comment request received: {}", comment);
        return commentServiceInterface.updateComment(comment, id);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable UUID  id) {
        log.info("Delete comment request received: {}", id);
        commentServiceInterface.deleteComment(id);
    }
}
