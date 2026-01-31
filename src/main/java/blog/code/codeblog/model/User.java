package blog.code.codeblog.model;

import blog.code.codeblog.enums.UserRoles;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.UUID;


import java.util.*;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Table(name = "tb_user")
@Entity
public class User implements UserDetails {

    public User(String name, String login, String password, UserRoles role) {
        this.name = name;
        this.login = login;
        this.password = password;
        this.role = role;
    }
    public User(String name, String login, String password) {
        this.name = name;
        this.login = login;
        this.password = password;

    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String login;

    @Column(nullable = false)
    private String password;

    private String name;

    private UserRoles role;

    @Column(name = "url_profile_pic")
    private String urlProfilePic;

    @Column(name = "profile_pic_id")
    private String profilePicId;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == UserRoles.ADMIN) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_USER"));
        }else
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getUsername() {
        return this.login;
    }

    public void addFollower(User user){
        this.followers.add(user);
    }

    public void removeFollower(User user){
        this.followers.remove(user);
    }

    @ManyToMany
    @JoinTable(
            name = "tb_user_followers",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id", columnDefinition = "uuid"),
            inverseJoinColumns = @JoinColumn(name = "follower_id", referencedColumnName = "id", columnDefinition = "uuid")
    )
    private Set<User> followers = new HashSet<>();

    @ManyToMany(mappedBy = "followers")
    private Set<User> following = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private List<Post> posts;

    // TODO: Replace raw favorite post UUIDs with a proper JPA mapping (e.g., @ManyToMany to Post)
    private List<UUID> favoritePosts = new ArrayList<>();



}