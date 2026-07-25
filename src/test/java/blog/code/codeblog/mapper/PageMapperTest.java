package blog.code.codeblog.mapper;

import blog.code.codeblog.dto.PageResponseDTO;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageMapperTest {

    @Test
    void shouldBuildPageResponseFromManualPagination() {
        List<String> content = List.of("a", "b");

        PageResponseDTO<String> response = PageMapper.toPageResponseDTO(content, 1, 2, 5L);

        assertEquals(content, response.content());
        assertEquals(1, response.currentPage());
        assertEquals(3, response.totalPages());
        assertEquals(5L, response.totalElements());
        assertEquals(2, response.size());
        assertFalse(response.first());
        assertFalse(response.last());
        assertFalse(response.empty());
    }

    @Test
    void shouldHandleEmptyContentAndZeroSizeSafely() {
        PageResponseDTO<String> response = PageMapper.toPageResponseDTO(Collections.emptyList(), 0, 0, 0L);

        assertTrue(response.content().isEmpty());
        assertEquals(0, response.currentPage());
        assertEquals(0, response.totalPages());
        assertEquals(0L, response.totalElements());
        assertEquals(0, response.size());
        assertTrue(response.first());
        assertTrue(response.last());
        assertTrue(response.empty());
    }
}

