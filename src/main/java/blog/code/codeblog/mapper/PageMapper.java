package blog.code.codeblog.mapper;

import blog.code.codeblog.dto.PageResponseDTO;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;


@UtilityClass
public class PageMapper {

    public static <T, R> PageResponseDTO<R> toPageResponseDTO(Page<T> page, Function<T, R> mapper) {

        return PageResponseDTO.<R>builder()
                .content(page.getContent().stream()
                        .map(mapper)
                        .toList())
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .size(page.getSize())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }

    public static <T> PageResponseDTO<T> toPageResponseDTO(List<T> content, int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return PageResponseDTO.<T>builder()
                .content(content)
                .currentPage(page)
                .totalPages(totalPages)
                .totalElements(totalElements)
                .size(size)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .empty(content.isEmpty())
                .build();
    }



}
