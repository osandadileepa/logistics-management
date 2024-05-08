package com.quincus.networkmanagement.impl.repository.specification;

import com.quincus.networkmanagement.api.filter.ConnectionSearchFilter;
import com.quincus.networkmanagement.impl.repository.entity.ConnectionEntity;
import com.quincus.networkmanagement.impl.repository.entity.ConnectionEntity_;
import com.quincus.networkmanagement.impl.repository.entity.component.BaseEntity_;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.quincus.networkmanagement.impl.repository.entity.ConnectionEntity_.ACTIVE;
import static com.quincus.networkmanagement.impl.repository.entity.ConnectionEntity_.CONNECTION_CODE;
import static com.quincus.networkmanagement.impl.repository.entity.ConnectionEntity_.TRANSPORT_TYPE;

@AllArgsConstructor
public class ConnectionSpecification implements Specification<ConnectionEntity> {
    private final transient ConnectionSearchFilter filter;
    @Getter
    private final List<Predicate> predicates = new ArrayList<>();

    @Override
    public Predicate toPredicate(@NonNull Root<ConnectionEntity> root, @NonNull CriteriaQuery<?> query, @NonNull CriteriaBuilder criteriaBuilder) {
        addEqualPredicate(root, criteriaBuilder, ACTIVE, filter.getActive());
        addEqualPredicate(root, criteriaBuilder, TRANSPORT_TYPE, filter.getTransportType());
        addLikePredicate(root, criteriaBuilder, filter.getConnectionCode());

        addVendorPredicate(root, criteriaBuilder);
        addDepartureNodePredicate(root, criteriaBuilder);
        addArrivalNodePredicate(root, criteriaBuilder);
        addTagsPredicate(root);

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    private void addEqualPredicate(Root<ConnectionEntity> root, CriteriaBuilder builder, String fieldName, Object value) {
        if (value != null) {
            predicates.add(builder.equal(root.get(fieldName), value));
        }
    }

    private void addLikePredicate(Root<ConnectionEntity> root, CriteriaBuilder builder, Object value) {
        if (value != null) {
            predicates.add(builder.like(root.get(CONNECTION_CODE), "%" + value + "%"));
        }
    }

    private void addVendorPredicate(Root<ConnectionEntity> root, CriteriaBuilder builder) {
        if (StringUtils.isNotBlank(filter.getVendorId())) {
            predicates.add(builder.equal(root.get(ConnectionEntity_.VENDOR).get(BaseEntity_.ID), filter.getVendorId()));
        }
    }

    private void addDepartureNodePredicate(Root<ConnectionEntity> root, CriteriaBuilder builder) {
        if (StringUtils.isNotBlank(filter.getDepartureNodeId())) {
            predicates.add(builder.equal(root.get(ConnectionEntity_.DEPARTURE_NODE).get(BaseEntity_.ID), filter.getDepartureNodeId()));
        }
    }

    private void addArrivalNodePredicate(Root<ConnectionEntity> root, CriteriaBuilder builder) {
        if (StringUtils.isNotBlank(filter.getArrivalNodeId())) {
            predicates.add(builder.equal(root.get(ConnectionEntity_.ARRIVAL_NODE).get(BaseEntity_.ID), filter.getArrivalNodeId()));
        }
    }

    private void addTagsPredicate(Root<ConnectionEntity> root) {
        if (filter.getTags() != null) {
            List<String> subsetTags = Arrays.asList(filter.getTags());
            Join<ConnectionEntity, String> joinTags = root.join(ConnectionEntity_.TAGS);
            predicates.add(joinTags.in(subsetTags));
        }
    }
}
