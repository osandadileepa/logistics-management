package com.quincus.networkmanagement.impl.repository.specification;

import com.quincus.networkmanagement.api.filter.NodeSearchFilter;
import com.quincus.networkmanagement.impl.repository.entity.NodeEntity;
import com.quincus.networkmanagement.impl.repository.entity.NodeEntity_;
import com.quincus.networkmanagement.impl.repository.entity.component.BaseEntity_;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
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

@AllArgsConstructor
public class NodeSpecification implements Specification<NodeEntity> {

    private final transient NodeSearchFilter filter;
    @Getter
    private final List<Predicate> predicates = new ArrayList<>();

    @Override
    public Predicate toPredicate(@NonNull Root<NodeEntity> root, @NonNull CriteriaQuery<?> query, @NonNull CriteriaBuilder builder) {
        addEqualPredicate(root, builder, NodeEntity_.ACTIVE, filter.getActive());
        addEqualPredicate(root, builder, NodeEntity_.NODE_TYPE, filter.getNodeType());
        addNodeCodeLikePredicate(root, builder, filter.getNodeCode());
        addVendorPredicate(root, builder);
        addFacilityPredicate(root, builder);
        addTagsPredicate(root);

        return builder.and(predicates.toArray(new Predicate[0]));
    }

    private void addEqualPredicate(Root<NodeEntity> root, CriteriaBuilder builder, String fieldName, Object value) {
        if (value != null) {
            predicates.add(builder.equal(root.get(fieldName), value));
        }
    }

    private void addNodeCodeLikePredicate(Root<NodeEntity> root, CriteriaBuilder builder, Object value) {
        if (value != null) {
            predicates.add(builder.like(root.get(NodeEntity_.NODE_CODE), "%" + value + "%"));
        }
    }

    private void addFacilityPredicate(Root<NodeEntity> root, CriteriaBuilder builder) {
        if (StringUtils.isNotBlank(filter.getFacilityId())) {
            predicates.add(builder.equal(root.get(NodeEntity_.FACILITY).get(BaseEntity_.ID), filter.getFacilityId()));
        }
    }

    private void addVendorPredicate(Root<NodeEntity> root, CriteriaBuilder builder) {
        if (StringUtils.isNotBlank(filter.getVendorId())) {
            predicates.add(builder.equal(root.get(NodeEntity_.VENDOR).get(BaseEntity_.ID), filter.getVendorId()));
        }
    }

    private void addTagsPredicate(Root<NodeEntity> root) {
        if (ArrayUtils.isNotEmpty(filter.getTags())) {
            List<String> subsetTags = Arrays.asList(filter.getTags());
            Join<NodeEntity, String> joinTags = root.join(NodeEntity_.TAGS);
            predicates.add(joinTags.in(subsetTags));
        }
    }

}
