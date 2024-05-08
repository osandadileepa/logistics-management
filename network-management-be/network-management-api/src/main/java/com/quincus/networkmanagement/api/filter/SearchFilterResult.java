package com.quincus.networkmanagement.api.filter;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@JsonRootName("data")
public class SearchFilterResult<T> {
    private List<T> result;
    private int size;
    private int page;
    private int totalPages;
    private long totalElements;
    private SearchFilter filter;

    public SearchFilterResult(Page<T> page, SearchFilter filter) {
        this.result = page.getContent();
        this.size = page.getSize();
        this.page = page.getNumber();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.filter = filter;
    }
}
