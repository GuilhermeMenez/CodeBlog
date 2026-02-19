package blog.code.codeblog.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record PageResponseDTO<T>(
        List<T> content,
        int currentPage,
        int totalPages,
        long totalElements,
        int size,
        boolean first,
        boolean last,
        boolean empty
)  {
}
