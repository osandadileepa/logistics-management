package com.quincus.shipment.impl.repository.projection;

import com.quincus.shipment.impl.repository.entity.component.BaseEntity;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.data.jpa.repository.query.QueryUtils.toOrders;

@AllArgsConstructor
public class BaseProjection<T extends BaseEntity> {
    protected final EntityManager entityManager;
    private final Class<T> clazz;

    protected Root<T> applySpecToCriteria(CriteriaQuery<Tuple> query, CriteriaBuilder builder, Specification<T> specs) {
        Root<T> root = query.from(clazz);
        if (isNull(specs)) {
            return root;
        }
        Predicate predicate = specs.toPredicate(root, query, builder);
        if (nonNull(predicate)) {
            query.where(predicate);
        }
        return root;
    }

    protected List<Selection<?>> getSelections(Root<T> root, List<String> fields) {
        List<Selection<?>> selections = new ArrayList<>();
        for (String field : fields) {
            selections.add(root.get(field).alias(field));
        }
        return selections;
    }


    private List<Tuple> getResultList(CriteriaQuery<Tuple> query, Pageable pageable) {
        TypedQuery<Tuple> typedQuery = entityManager.createQuery(query);
        if (nonNull(pageable) && pageable.isPaged()) {
            typedQuery.setFirstResult((int) pageable.getOffset());
            typedQuery.setMaxResults(pageable.getPageSize());
        }
        return typedQuery.getResultList();
    }

    protected List<Tuple> getPageableResultList(CriteriaQuery<Tuple> query, Pageable pageable) {
        return getResultList(query, pageable);
    }

    protected void applySorting(CriteriaBuilder builder, CriteriaQuery<Tuple> query, Root<T> root, Pageable pageable) {
        Sort sort = nonNull(pageable) && pageable.isPaged() ? pageable.getSort() : Sort.unsorted();
        if (sort.isSorted()) {
            query.orderBy(toOrders(sort, root, builder));
        }
    }

    public long getTotalNumberOfPages(long totalResults, long size) {
        if (totalResults % size == 0) {
            return totalResults / size;
        }
        return totalResults / size + 1;
    }
}
