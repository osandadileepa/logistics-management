package com.quincus.shipment.impl.repository.specification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.filter.CostAmountRange;
import com.quincus.shipment.api.filter.CostDateRange;
import com.quincus.shipment.impl.repository.criteria.CostCriteria;
import com.quincus.shipment.impl.repository.entity.CostEntity;
import com.quincus.shipment.impl.repository.entity.CostEntity_;
import com.quincus.shipment.impl.repository.entity.component.CostTypeEntity_;
import com.quincus.shipment.impl.repository.entity.component.MultiTenantEntity_;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@Slf4j
public class CostSpecification extends BaseSpecification<CostEntity> {
    private static final String ADD_USER_LOCATIONS_COVERAGE_PREDICATE = "addUserLocationsCoveragePredicate";
    private static final String ADD_USER_PARTNERS_PREDICATE = "addUserPartnersPredicate";
    private static final String ADD_BETWEEN_COST_AMOUNT_PREDICATE = "addBetweenCostAmountPredicate";
    private static final String ADD_BETWEEN_ISSUED_DATE_PREDICATE = "addBetweenIssuedDatePredicate";
    private static final String LOWER_SQL_FUNCTION = "LOWER";
    private static final String JSON_CONTAINS_SQL_FUNCTION = "JSON_CONTAINS";
    private static final String JSON_WILD_CARD = "$";
    private static final String ADD_COST_TYPE_PREDICATE = "addCostTypePredicate";
    private static final String ADD_DRIVER_NAME_PREDICATE = "addDriverNamePredicate";
    private static final String ADD_PARTNER_NAME_PREDICATE = "addPartnerNamePredicate";
    private final transient CostCriteria criteria;
    private final ObjectMapper objectMapper;

    @Override
    public Predicate toPredicate(@NonNull Root<CostEntity> root, @NonNull CriteriaQuery<?> query, @NonNull CriteriaBuilder criteriaBuilder) {
        addCostTypePredicate(root, criteria.getCostTypes());
        addDriverNamePredicate(root, criteria.getDrivers());
        addPartnerNamePredicate(root, criteria.getVendors());
        addOrganizationPredicate(root, criteriaBuilder, criteria.getOrganizationId());
        addBetweenIssuedDatePredicate(root, criteriaBuilder, criteria.getIncurredDateRange());
        addBetweenCostAmountPredicate(root, criteriaBuilder, criteria.getCostAmountRange());
        addUserLocationsCoveragePredicate(root, criteriaBuilder, criteria.getUserLocationsCoverage());
        addUserPartnersPredicate(root, criteria.getPartnerId(), criteria.getUserPartners(), criteriaBuilder);
        return buildPredicate(query, criteriaBuilder);
    }

    private void addOrganizationPredicate(Root<CostEntity> root, CriteriaBuilder criteriaBuilder, String organizationId) {
        addEqualsPredicate(root, ADD_ORGANIZATION_PREDICATE, criteriaBuilder, organizationId, MultiTenantEntity_.ORGANIZATION_ID);
    }

    private void addPartnerNamePredicate(Root<CostEntity> root, List<?> partners) {
        addInPredicate(root, ADD_PARTNER_NAME_PREDICATE, partners, CostEntity_.PARTNER_NAME);
    }

    private void addDriverNamePredicate(Root<CostEntity> root, List<?> drivers) {
        addInPredicate(root, ADD_DRIVER_NAME_PREDICATE, drivers, CostEntity_.DRIVER_NAME);
    }

    private void addCostTypePredicate(Root<CostEntity> root, List<?> costTypes) {
        if (CollectionUtils.isNotEmpty(costTypes)) {
            Expression<String> costTypeName = root.get(CostEntity_.COST_TYPE).get(CostTypeEntity_.NAME);
            addPredicate(ADD_COST_TYPE_PREDICATE, costTypeName.in(costTypes));
        }
    }

    private void addUserLocationsCoveragePredicate(Root<CostEntity> root, CriteriaBuilder builder, Set<String> userLocationsCoverage) {
        if (CollectionUtils.isNotEmpty(userLocationsCoverage)) {
            List<Predicate> keysPredicates = new ArrayList<>();
            userLocationsCoverage.forEach(k -> keysPredicates.add(createJSONContainsPredicate(k, builder, root)));
            addPredicate(CostSpecification.ADD_USER_LOCATIONS_COVERAGE_PREDICATE, builder.or(keysPredicates.toArray(new Predicate[0])));
        }
    }


    private void addUserPartnersPredicate(Root<CostEntity> root, String partnerId, List<String> userPartners,
                                          CriteriaBuilder builder) {
        Predicate partnersPredicate;
        if (StringUtils.isNotEmpty(partnerId)) {
            partnersPredicate = createPartnersExclusivePredicate(root, partnerId, userPartners);
        } else {
            partnersPredicate = createPartnersInclusivePredicate(root, userPartners, builder);
        }
        addPredicate(CostSpecification.ADD_USER_PARTNERS_PREDICATE, partnersPredicate);
    }

    private Predicate createPartnersInclusivePredicate(Root<CostEntity> root, List<String> userPartners,
                                                       CriteriaBuilder criteriaBuilder) {
        Predicate noPartnerPredicate = criteriaBuilder.or(criteriaBuilder.equal(root.get(CostEntity_.PARTNER_ID), ""),
                criteriaBuilder.isNull(root.get(CostEntity_.PARTNER_ID)));
        if (CollectionUtils.isEmpty(userPartners)) {
            // UserPartners is Empty: retrieve all shipments with no associated partners
            return noPartnerPredicate;
        }
        // UserPartners is not empty: retrieve all shipments with no associated partners and particular partner
        return criteriaBuilder.or(root.get(CostEntity_.PARTNER_ID).in(userPartners), noPartnerPredicate);
    }

    private Predicate createPartnersExclusivePredicate(Root<CostEntity> root, String partnerId,
                                                       List<String> userPartners) {
        List<String> partnersList = new ArrayList<>();
        partnersList.add(partnerId);
        if (CollectionUtils.isNotEmpty(userPartners)) {
            partnersList.addAll(userPartners);
        }
        return root.get(CostEntity_.PARTNER_ID).in(partnersList);
    }


    private void addBetweenCostAmountPredicate(Root<CostEntity> root, CriteriaBuilder builder, CostAmountRange costAmountRange) {
        if (costAmountRange == null) {
            return;
        }
        BigDecimal minValue = costAmountRange.getMinCostAmount();
        BigDecimal maxValue = costAmountRange.getMaxCostAmount();
        if (minValue != null && maxValue != null) {
            addPredicate(CostSpecification.ADD_BETWEEN_COST_AMOUNT_PREDICATE, builder.between(root.get(CostEntity_.COST_AMOUNT), minValue, maxValue));
        } else if (minValue != null) {
            addPredicate(CostSpecification.ADD_BETWEEN_COST_AMOUNT_PREDICATE, builder.greaterThanOrEqualTo(root.get(CostEntity_.COST_AMOUNT), minValue));
        } else if (maxValue != null) {
            addPredicate(CostSpecification.ADD_BETWEEN_COST_AMOUNT_PREDICATE, builder.lessThanOrEqualTo(root.get(CostEntity_.COST_AMOUNT), maxValue));
        }
    }

    private void addBetweenIssuedDatePredicate(Root<CostEntity> root, CriteriaBuilder builder, CostDateRange date) {
        if (date != null) {
            LocalDateTime from = date.getIncurredDateFrom();
            LocalDateTime to = date.getIncurredDateTo();
            addPredicate(CostSpecification.ADD_BETWEEN_ISSUED_DATE_PREDICATE, builder.between(root.get(CostEntity_.ISSUED_DATE), from, to));
        }
    }

    private Predicate createJSONContainsPredicate(String value, CriteriaBuilder criteriaBuilder, Root<CostEntity> root) {
        Expression<Boolean> expression = criteriaBuilder.function(
                JSON_CONTAINS_SQL_FUNCTION, Boolean.class,
                criteriaBuilder.function(LOWER_SQL_FUNCTION, String.class, root.get(CostEntity_.locationExternalIds)),
                criteriaBuilder.function(LOWER_SQL_FUNCTION, String.class, criteriaBuilder.literal(convertValueAsJsonString(value))),
                criteriaBuilder.literal(JSON_WILD_CARD));
        return criteriaBuilder.isTrue(expression);
    }

    private String convertValueAsJsonString(String value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.debug(String.format("Unable to convert value: `%s` to jsonString ", value));
        }
        return null;
    }

}
