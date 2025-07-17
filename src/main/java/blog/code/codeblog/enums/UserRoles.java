package blog.code.codeblog.enums;

import lombok.Getter;

@Getter
public enum UserRoles {
    ADMIN("admin"),
    COSTUMER("user");

    private final String role;

    UserRoles(String role) {
        this.role = role;
    }

}

