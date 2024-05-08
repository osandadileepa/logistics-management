package com.quincus.shipment.impl.repository.criteria;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Optional;

@Getter
@Setter
public abstract class AbstractPageableCriteria<T> {
    private static final String DESC = "desc";
    private final PageRequest defaultPageRequest;
    private final Sort defaulSort;
    protected String sortDir;
    protected Integer page;
    protected Integer perPage;
    protected String sortBy;

    protected AbstractPageableCriteria(final PageRequest defaultPageRequest, final Sort defaulSort) {
        this.defaultPageRequest = defaultPageRequest;
        this.defaulSort = defaulSort;
    }

    public PageRequest pageRequest() {
        return Optional.ofNullable(page)
                .flatMap(p -> Optional.ofNullable(perPage)
                        .map(pp -> PageRequest.of(p - 1, pp, sortBy())))
                .orElse(defaultPageRequest);
    }

    private Sort sortBy() {
        Sort.Direction direction = DESC.equals(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return StringUtils.isEmpty(sortBy) || StringUtils.isEmpty(sortDir)
                ? defaulSort
                : Sort.by(direction, sortBy);
    }

    public abstract T buildSpecification();

}
