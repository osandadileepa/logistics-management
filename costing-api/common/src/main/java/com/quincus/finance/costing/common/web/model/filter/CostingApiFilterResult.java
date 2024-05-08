package com.quincus.finance.costing.common.web.model.filter;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@JsonRootName("data")
public class CostingApiFilterResult<T> {
    private List<T> result;
    private int size;
    private int page;
    private int totalPages;
    private long totalElements;

    public CostingApiFilterResult(Page<T> page) {
        this.result = page.getContent();
        this.size = page.getSize();
        this.page = page.getNumber();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
    }

}
