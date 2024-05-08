package com.quincus.finance.costing.weightcalculation.impl.service;

import com.quincus.finance.costing.common.exception.CostingApiException;
import com.quincus.finance.costing.common.web.model.Partner;
import com.quincus.finance.costing.weightcalculation.api.WeightCalculationRuleApi;
import com.quincus.finance.costing.weightcalculation.api.filter.WeightCalculationRuleFilter;
import com.quincus.finance.costing.weightcalculation.api.model.VolumeWeightRule;
import com.quincus.finance.costing.weightcalculation.api.model.WeightCalculationRule;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;

@Service
@Slf4j
@AllArgsConstructor
public class WeightCalculationRuleService {

    public static final String ERR_ACTIVE_RULE_ALREADY_EXIST_FOR_ORG_ID = "An active rule FOR ALL PARTNERS already exists for organizationId: %s";
    public static final String ERR_ACTIVE_RULE_ALREADY_EXIST_FOR_ORG_ID_PARTNER_IDS = "An active rule already exists for one of the partnerIds: %s of organizationId: %s";
    public static final String ERR_RULE_NOT_FOUND = "Weight Calculation Rule with Id %s not found";
    public static final String ERR_DEFAULT_RULE_NOT_FOUND = "Default Weight Calculation Rule for organizationId %s not found";
    public static final String ERR_RULE_TO_APPLY_NOT_FOUND = "Weight Calculation Rule for organizationId %s and partnerId %s not found";
    public static final String ERR_NO_ATTACHED_TEMPLATE = "No attached specialVolumeWeightTemplate file found";
    private final WeightCalculationRuleApi weightCalculationRuleApi;
    private final SpecialVolumeWeightRuleService specialVolumeWeightRuleService;

    @Transactional
    public WeightCalculationRule create(
            WeightCalculationRule weightCalculationRule,
            MultipartFile file
    ) {
        validateWeightCalculationRule(weightCalculationRule);

        attachTemplateToParse(weightCalculationRule, file);
        return weightCalculationRuleApi.create(weightCalculationRule);
    }

    public WeightCalculationRule update(
            WeightCalculationRule weightCalculationRule,
            MultipartFile file) {
        validateWeightCalculationRule(weightCalculationRule);
        attachTemplateToParse(weightCalculationRule, file);
        return weightCalculationRuleApi.update(weightCalculationRule)
                .orElseThrow(() -> new CostingApiException(
                        String.format(ERR_RULE_NOT_FOUND, weightCalculationRule.getId()), HttpStatus.NOT_FOUND)
                );
    }

    public WeightCalculationRule get(String id) {
        return weightCalculationRuleApi.find(id)
                .orElseThrow(() -> new CostingApiException(String.format(ERR_RULE_NOT_FOUND, id), HttpStatus.NOT_FOUND));
    }

    public Page<WeightCalculationRule> search(WeightCalculationRuleFilter filter, Pageable pageable) {
        return weightCalculationRuleApi.search(filter, pageable);
    }

    @Transactional
    public void delete(String id) {
        weightCalculationRuleApi.delete(id);
    }

    public WeightCalculationRule getRuleByOrganizationId(String organizationId) {
        List<WeightCalculationRule> weightCalculationRules = weightCalculationRuleApi.findActiveByOrganizationId(organizationId);
        if (!CollectionUtils.isEmpty(weightCalculationRules)) {
            if (weightCalculationRules.size() > 1) {
                log.warn("There are more than 1 active rule for the organization: {}. The latest active rule will be used.", organizationId);
            }
            return weightCalculationRules.get(0);
        }
        throw new CostingApiException(String.format(ERR_DEFAULT_RULE_NOT_FOUND, organizationId), HttpStatus.NOT_FOUND);
    }

    public WeightCalculationRule getRuleToApply(String ruleId, String organizationId, String partnerId) {
        log.debug("Determining which weight calculation rule to apply");
        if (ruleId != null) {
            return weightCalculationRuleApi.find(ruleId).orElseThrow(() -> new CostingApiException(
                    String.format(ERR_RULE_NOT_FOUND, ruleId), HttpStatus.INTERNAL_SERVER_ERROR)
            );
        }
        return getRuleByOrganizationIdAndPartnerId(organizationId, partnerId);
    }

    private WeightCalculationRule getRuleByOrganizationIdAndPartnerId(String organizationId, String partnerId) {
        Set<String> partnerIds = StringUtils.isBlank(partnerId) ? emptySet() : Set.of(partnerId);
        List<WeightCalculationRule> weightCalculationRules = findRulesByOrganizationIdAndPartnerId(organizationId, partnerIds);
        if (!CollectionUtils.isEmpty(weightCalculationRules)) {
            return weightCalculationRules.get(0);
        }
        throw new CostingApiException(String.format(ERR_RULE_TO_APPLY_NOT_FOUND, organizationId, partnerId), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private List<WeightCalculationRule> findRulesByOrganizationIdAndPartnerId(String organizationId, Set<String> partnerIds) {
        if (CollectionUtils.isEmpty(partnerIds)) {
            return weightCalculationRuleApi.findActiveByOrganizationId(organizationId);
        }
        return weightCalculationRuleApi.findActiveByOrganizationIdAndPartnerIds(organizationId, partnerIds);
    }

    private void attachTemplateToParse(WeightCalculationRule weightCalculationRule, MultipartFile file) {
        if (VolumeWeightRule.SPECIAL.equals(weightCalculationRule.getVolumeWeightRule())) {
            log.debug("volumeWeightRule is set to SPECIAL, parsing attached template file");

            if (file == null) {
                throw new CostingApiException(ERR_NO_ATTACHED_TEMPLATE, HttpStatus.BAD_REQUEST);
            }
            weightCalculationRule.setSpecialVolumeWeightRule(
                    specialVolumeWeightRuleService.parseTemplate(file)
            );
        }
    }

    private void validateWeightCalculationRule(WeightCalculationRule weightCalculationRule) {
        if (!weightCalculationRule.isActive()) {
            log.debug("Skipping validation for inactive weightCalculationRule organizationId:{}, partners:{}",
                    weightCalculationRule.getOrganizationId(), weightCalculationRule.getPartners());
            return;
        }
        String organizationId = weightCalculationRule.getOrganizationId();
        Set<String> partnerIds = weightCalculationRule.getPartners().stream().map(Partner::getId).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(partnerIds)) {
            if (hasExistingActiveRuleForOrg(organizationId)) {
                throw new CostingApiException(String.format(ERR_ACTIVE_RULE_ALREADY_EXIST_FOR_ORG_ID, organizationId), HttpStatus.BAD_REQUEST);
            }
        } else if (hasExistingActiveRuleForOrgAndPartners(organizationId, partnerIds)) {
            throw new CostingApiException(String.format(ERR_ACTIVE_RULE_ALREADY_EXIST_FOR_ORG_ID_PARTNER_IDS, partnerIds, organizationId), HttpStatus.BAD_REQUEST);
        }
    }

    private boolean hasExistingActiveRuleForOrg(String organizationId) {
        return !CollectionUtils.isEmpty(weightCalculationRuleApi.findActiveByOrganizationId(organizationId));
    }

    private boolean hasExistingActiveRuleForOrgAndPartners(String organizationId, Set<String> partnerIds) {
        return !CollectionUtils.isEmpty(weightCalculationRuleApi.findActiveByOrganizationIdAndPartnerIds(organizationId, partnerIds));
    }
}
