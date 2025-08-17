package blog.code.codeblog.service;

import blog.code.codeblog.dto.PostDTO;
import blog.code.codeblog.model.Post;
import blog.code.codeblog.model.User;
import blog.code.codeblog.repository.PostRepository;
import blog.code.codeblog.repository.UserRepository;
import blog.code.codeblog.service.interfaces.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class PostServiceImpl implements PostService {

    @Autowired
    PostRepository postRepository;

    @Autowired
    UserRepository userRepository;

    @Override
    public List<Post> findAll() {
        return postRepository.findAll();
    }

    @Override
    public Optional<Post> findById(UUID id) {
        return postRepository.findById(id);
    }

    @Override
    public Post save(Post post) {
        return postRepository.save(post);
    }

    @Override
    public void deletePost(UUID postId) {
        postRepository.deleteById(postId);
    }
    @Override
    public List<Post> getBalancedFeed(UUID userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        Set<User> following = user.getFollowing();

        int recentSize = (int) (size * 0.7);
        int randomSize = size - recentSize;

        Pageable recentPageable = PageRequest.of(page, recentSize);
        Pageable randomPageable = PageRequest.of(page, randomSize);

        List<Post> recentPosts = postRepository.findRecentPosts(following, recentPageable);
        List<Post> randomPosts = postRepository.findRandomPosts(following, randomPageable);

        List<Post> combined = new ArrayList<>();
        combined.addAll(recentPosts);
        combined.addAll(randomPosts);

        Collections.shuffle(combined);

        return combined;
    }

    @Override
    public List<Post>getAllUserPosts(UUID userId){
       return userRepository.findById(userId)
                .map(User::getPosts)
               .orElseThrow(() -> new RuntimeException("usuário não encontrado"));
    }
    @Override
    public Optional<Post> updatePost(UUID postId, PostDTO updatedPost){
        return findById(postId)
                .map(existingPost ->{
                    existingPost.setTitulo(updatedPost.title());
                    existingPost.setTexto(updatedPost.content());
                    existingPost.setData(LocalDate.now());
                    return postRepository.save(existingPost);
                });
    }

    public Post getReference(UUID  id){
        return postRepository.getReferenceById(id);
    }

}
