package com.flopkart.common.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Should calculate totalPages, isFirst and isLast correctly")
    void shouldCalculatePaginationProperties() {
        List<String> items = List.of("Item 1", "Item 2", "Item 3");
        PageResponse<String> page = PageResponse.of(items, 0, 3, 10);

        assertThat(page.getContent()).containsExactly("Item 1", "Item 2", "Item 3");
        assertThat(page.getPageNumber()).isEqualTo(0);
        assertThat(page.getPageSize()).isEqualTo(3);
        assertThat(page.getTotalElements()).isEqualTo(10);
        assertThat(page.getTotalPages()).isEqualTo(4); // ceil(10/3) = 4
        assertThat(page.isFirst()).isTrue();
        assertThat(page.isLast()).isFalse();
        assertThat(page.isEmpty()).isFalse();
    }

    @Test
    @DisplayName("Should detect last page correctly")
    void shouldDetectLastPage() {
        List<String> items = List.of("Item 10");
        PageResponse<String> page = PageResponse.of(items, 3, 3, 10);

        assertThat(page.getPageNumber()).isEqualTo(3);
        assertThat(page.isFirst()).isFalse();
        assertThat(page.isLast()).isTrue();
    }

    @Test
    @DisplayName("Should handle empty content")
    void shouldHandleEmptyContent() {
        PageResponse<String> page = PageResponse.of(List.of(), 0, 10, 0);

        assertThat(page.isEmpty()).isTrue();
        assertThat(page.getTotalPages()).isEqualTo(0);
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Should serialize to JSON properly")
    void shouldSerializeJson() throws Exception {
        PageResponse<String> page = PageResponse.of(List.of("A", "B"), 0, 2, 2);

        String json = objectMapper.writeValueAsString(page);
        assertThat(json).contains("\"pageNumber\":0");
        assertThat(json).contains("\"pageSize\":2");
        assertThat(json).contains("\"totalElements\":2");
        assertThat(json).contains("\"first\":true");
        assertThat(json).contains("\"last\":true");
    }
}
