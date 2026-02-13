package blog.code.codeblog.dto.authentication;

import java.time.Instant;

public record BlackListedToken(Instant expiresAt) { }
