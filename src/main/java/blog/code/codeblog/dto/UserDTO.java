package blog.code.codeblog.dto;


import blog.code.codeblog.enums.UserRoles;

public record UserDTO(String name, String email, String password, UserRoles role) {

}