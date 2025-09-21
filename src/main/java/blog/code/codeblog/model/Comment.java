package blog.code.codeblog.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "TB_COMMENT")
public class Comment  {


    public Comment(String content, User user, Post post, String autor) {
        this.content = content;
        this.user = user;
        this.post = post;
        this.autor = autor;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @NotNull
    @Lob
    private String content;

    private String autor;

    @NotNull
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;


}

