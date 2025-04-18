package blog.code.codeblog.controller;
import blog.code.codeblog.model.Post;
import blog.code.codeblog.service.CodeBlogService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
public class CodeblogController {

    @Autowired
    CodeBlogService codeBlogService;

    @RequestMapping(value = "/posts", method = RequestMethod.GET)
    public ModelAndView getPosts(){
        ModelAndView mv = new ModelAndView("posts");
        List<Post> posts = codeBlogService.findAll();
        return mv.addObject("posts", posts);
    }

    @RequestMapping(value = "/posts/{id}", method = RequestMethod.GET)
    public ModelAndView getPostsbyId(@PathVariable("id") long id){
        ModelAndView mv = new ModelAndView("postDetails");
        Post post = codeBlogService.findById(id);
        return mv.addObject("post", post);
    }

    @RequestMapping(value = "/newpost", method = RequestMethod.GET)
    public String getPostForm(){
        return "postForm";
    }


    @RequestMapping(value="/newpost", method=RequestMethod.POST)
    public String savePost(@Valid Post post, BindingResult result, RedirectAttributes attributes){
        if(result.hasErrors()){
            attributes.addFlashAttribute("mensagem", "Verifique se os campos obrigatórios foram preenchidos!");
            return "redirect:/newpost";
        }
        post.setData(LocalDate.now());
        codeBlogService.save(post);
        return "redirect:/posts";
    }

    @RequestMapping(value = "/editpost/{id}", method = RequestMethod.PUT)
    public String editPost(@PathVariable("id") long id, @Valid Post updatedPost,
                           BindingResult result, RedirectAttributes attributes) {
        if (result.hasErrors()) {
            attributes.addFlashAttribute("mensagem", "Verifique se os campos obrigatórios foram preenchidos!");
            return "redirect:/editpost/" + id;
        }
        Post post = codeBlogService.findById(id);
        post.setTitulo(updatedPost.getTitulo());
        post.setAutor(updatedPost.getAutor());
        post.setTexto(updatedPost.getTexto());
        codeBlogService.save(post);
        return "redirect:/posts/" + id;
    }



    

}
