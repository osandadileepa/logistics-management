package com.quincus.shipment.api.dto;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class CustomTreePageImpl<T> extends PageImpl<T> {
    private final long total;

    public CustomTreePageImpl(List<T> content, Pageable pageable, long total) {
        super(content, pageable, total);
        this.total = total;
    }

    @Override
    public long getTotalElements() {
        return this.total;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        CustomTreePageImpl<?> that = (CustomTreePageImpl<?>) o;

        return new EqualsBuilder().appendSuper(super.equals(o)).append(total, that.total).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).appendSuper(super.hashCode()).append(total).toHashCode();
    }
}
