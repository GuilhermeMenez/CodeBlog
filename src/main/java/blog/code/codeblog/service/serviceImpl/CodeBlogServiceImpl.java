package blog.code.codeblog.service.serviceImpl;

import blog.code.codeblog.model.Post;
import blog.code.codeblog.repository.CodeblogRepository;
import blog.code.codeblog.service.CodeBlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CodeBlogServiceImpl implements CodeBlogService {

    @Autowired
    CodeblogRepository codeblogRepository;

    @Override
    public List<Post> findAll() {
        return codeblogRepository.findAll();
    }

    @Override
    public Post findById(Long id) {
        return codeblogRepository.findById(id).get();
    }

    @Override
    public Post save(Post post) {
        return codeblogRepository.save(post);
    }

    @Override
    public void delete(Long id) {
        codeblogRepository.deleteById(id);
    }
}
