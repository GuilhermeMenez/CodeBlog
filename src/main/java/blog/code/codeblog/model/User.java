package blog.code.codeblog.model;

import blog.code.codeblog.enums.UserRoles;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.UUID;


import java.util.*;
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
    public String getUsername() {
        return this.login;
    }

    @OneToMany(mappedBy = "followed", fetch = FetchType.LAZY)
    private Set<UserFollow> followers = new HashSet<>();

    @OneToMany(mappedBy = "follower", fetch = FetchType.LAZY)
    private Set<UserFollow> following = new HashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Post> posts = new ArrayList<>();

    // TODO: Replace raw favorite post UUIDs with a proper JPA mapping (e.g., @ManyToMany to Post)
    private List<UUID> favoritePosts = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == UserRoles.ADMIN) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_USER"));
        }else
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }





}