package blog.code.codeblog.mapper;

import blog.code.codeblog.command.feed.GetFeedCommand;
import blog.code.codeblog.command.post.*;
import blog.code.codeblog.dto.post.CreatePostRequestDTO;
import blog.code.codeblog.dto.post.PostAuthorDTO;
import blog.code.codeblog.dto.post.PostResponseDTO;
import blog.code.codeblog.dto.post.PutPostDTO;
import blog.code.codeblog.model.Post;
import blog.code.codeblog.model.User;
import lombok.experimental.UtilityClass;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@UtilityClass
public class PostMapper {
    public CreatePostCommand mapToCreatePostCommand(CreatePostRequestDTO request, User author) {
        return CreatePostCommand.builder()
                .title(request.title())
                .content(request.content())
                .authorName(author.getUsername())
                .author(author)
                .images(request.images())
                .build();
    }

    public Post toPostEntity(CreatePostCommand postCommand) {
        return Post.builder()
                .title(postCommand.getTitle())
                .content(postCommand.getContent())
                .authorName(postCommand.getAuthorName())
                .date(LocalDate.now())
                .user(postCommand.getAuthor())
                .build();

    }

    public PutPostCommand mapToPutPostCommand(PutPostDTO request, User author) {
        return PutPostCommand.builder()
                .postId(request.postId())
                .title(request.title())
                .content(request.content())
                .authorId(author.getId())
                .build();
    }

    public DeletePostCommand mapToDeletePostCommand(UUID postId, User author) {
        return DeletePostCommand.builder()
                .postId(postId)
                .authorId(author.getId())
                .build();
    }

    public PostResponseDTO toPostResponseDTO(Post post) {
        Map<String, String> imagesCopy = post.getImages() != null
                ? new HashMap<>(post.getImages())
                : null;

        PostAuthorDTO author = new PostAuthorDTO(
                post.getUser().getId(),
                post.getUser().getName()
        );

        return new PostResponseDTO(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                author,
                post.getDate(),
                imagesCopy
        );
    }

    public SavePostImageCommand toSavePostImageCommand(UUID postId, MultipartFile file, User author) {
        return SavePostImageCommand.builder()
                .postId(postId)
                .file(file)
                .author(author)
                .build();
    }

    public GetFeedCommand toGetFeedCommand(User user, int page, int size) {
        return GetFeedCommand.builder()
                .user(user)
                .page(page)
                .size(size)
                .build();
    }

    public DeletePostImageCommand toDeletePostImageCommand(UUID publicId, User author) {
        return DeletePostImageCommand.builder()
                .publicId(publicId.toString())
                .user(author)
                .build();
    }

    public GetPostCommentsCommand toGetPostCommentsCommand(UUID postId, int page, int size) {
        return GetPostCommentsCommand.builder()
                .postId(postId)
                .page(page)
                .size(size)
                .build();
    }

    public GetAllUserPostsCommand toGetAllUserPostsCommand(User user, int page, int size) {
        return GetAllUserPostsCommand.builder()
                .user(user)
                .page(page)
                .size(size)
                .build();
    }
}
