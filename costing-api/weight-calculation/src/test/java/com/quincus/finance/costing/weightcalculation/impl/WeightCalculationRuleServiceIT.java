package com.quincus.finance.costing.weightcalculation.impl;

import com.quincus.finance.costing.common.exception.CostingApiException;
import com.quincus.finance.costing.common.web.model.Partner;
import com.quincus.finance.costing.weightcalculation.api.filter.WeightCalculationRuleFilter;
import com.quincus.finance.costing.weightcalculation.api.model.VolumeWeightRule;
import com.quincus.finance.costing.weightcalculation.api.model.WeightCalculationRule;
import com.quincus.finance.costing.weightcalculation.db.repository.WeightCalculationRuleRepository;
import com.quincus.finance.costing.weightcalculation.impl.config.ITConfiguration;
import com.quincus.finance.costing.weightcalculation.impl.service.WeightCalculationRuleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.quincus.finance.costing.weightcalculation.impl.data.WeightCalculationTestData.dummyPartner;
import static com.quincus.finance.costing.weightcalculation.impl.data.WeightCalculationTestData.dummyRule;
import static com.quincus.finance.costing.weightcalculation.impl.data.WeightCalculationTestData.dummyValidXLSXTemplate;
import static com.quincus.finance.costing.weightcalculation.impl.service.WeightCalculationRuleService.ERR_ACTIVE_RULE_ALREADY_EXIST_FOR_ORG_ID;
import static com.quincus.finance.costing.weightcalculation.impl.service.WeightCalculationRuleService.ERR_ACTIVE_RULE_ALREADY_EXIST_FOR_ORG_ID_PARTNER_IDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ITConfiguration.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class WeightCalculationRuleServiceIT {

    @Autowired
    private WeightCalculationRuleService weightCalculationRuleService;

    @Autowired
    private WeightCalculationRuleRepository weightCalculationRuleRepository;

    @Test
    @DisplayName("GIVEN existing rule with organization id and partner ids WHEN determine rule to apply THEN return expected")
    void returnExpectedWhenGetRuleToApplyAndExistingRuleAndOrganizationIdAndPartnerIds() {
        WeightCalculationRule rule = dummyRule();
        rule.setPartners(Set.of(dummyPartner("0", "Quincus Express")));
        weightCalculationRuleService.create(rule, null);
        List<String> partnerIds = rule.getPartners().stream().map(Partner::getId).toList();


        WeightCalculationRule result = weightCalculationRuleService.getRuleToApply(null, rule.getOrganizationId(), partnerIds.get(0));
        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("createTime", "modifyTime", "id")
                .isEqualTo(rule);
    }

    @Test
    @DisplayName("GIVEN existing rule with organization id only WHEN determine rule to apply THEN return expected")
    void returnExpectedWhenGetRuleToApplyAndExistingRuleAndOrganizationId() {
        WeightCalculationRule rule = dummyRule();
        rule.setPartners(Set.of());
        weightCalculationRuleService.create(rule, null);


        WeightCalculationRule result = weightCalculationRuleService.getRuleToApply(null, rule.getOrganizationId(), "");
        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("createTime", "modifyTime", "id")
                .isEqualTo(rule);
    }

    @Test
    @DisplayName("GIVEN non-existing organization id and partner id WHEN determine rule to apply THEN throw error")
    void throwErrorWhenMissingRuleByOrganizationIdAndPartnerId() {
        WeightCalculationRule rule = dummyRule();
        List<String> partnerIds = rule.getPartners().stream().map(Partner::getId).toList();

        assertThatThrownBy(() -> weightCalculationRuleService.getRuleToApply(null, rule.getOrganizationId(), partnerIds.get(0)))
                .isInstanceOf(CostingApiException.class);
    }

    @Test
    @DisplayName("GIVEN existing rule id WHEN determine rule to apply THEN return expected")
    void returnExpectedWhenGetRuleToApplyAndExistingRuleId() {
        WeightCalculationRule rule = dummyRule();
        rule.setPartners(Set.of());
        WeightCalculationRule createdRule = weightCalculationRuleService.create(rule, null);

        WeightCalculationRule result = weightCalculationRuleService.getRuleToApply(createdRule.getId(), "", "");
        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("createTime", "modifyTime")
                .isEqualTo(createdRule);
    }

    @Test
    @DisplayName("GIVEN non-existing rule id WHEN determine rule to apply THEN throw error")
    void throwErrorWhenMissingRule() {
        assertThatThrownBy(() -> weightCalculationRuleService.getRuleToApply("1", null, ""))
                .isInstanceOf(CostingApiException.class);
    }

    @Test
    @DisplayName("GIVEN non-existing default rule WHEN determine rule to apply THEN throw error")
    void throwErrorWhenMissingDefaultRule() {
        assertThatThrownBy(() -> weightCalculationRuleService.getRuleToApply(null, null, ""))
                .isInstanceOf(CostingApiException.class);
    }

    @Test
    @DisplayName("GIVEN rule with organization id, partnerIds and active status WHEN create rule THEN return save and return expected")
    void returnExpectedWhenCreateActiveRuleWithOrganizationIdAndPartnerIds() {
        WeightCalculationRule rule = dummyRule();

        WeightCalculationRule result = weightCalculationRuleService.create(rule, null);

        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("createTime", "modifyTime", "id")
                .isEqualTo(rule);
    }

    @Test
    @DisplayName("GIVEN rule with existing organization id, partnerIds and active status WHEN create rule THEN throw error")
    void throwErrorWhenActiveRuleWithExistingOrganizationIdAndPartnerIds() {
        WeightCalculationRule rule = dummyRule();
        weightCalculationRuleService.create(rule, null);

        List<String> partnerIds = rule.getPartners().stream().map(Partner::getId).toList();
        assertThatThrownBy(() -> weightCalculationRuleService.create(rule, null))
                .isInstanceOfSatisfying(CostingApiException.class, exception -> {
                    assertThat(exception.getMessage()).isEqualTo(String.format(ERR_ACTIVE_RULE_ALREADY_EXIST_FOR_ORG_ID_PARTNER_IDS, partnerIds, rule.getOrganizationId()));
                });
    }


    @Test
    @DisplayName("GIVEN rule with organization id and active status WHEN create rule THEN return save and return expected")
    void returnExpectedWhenCreateActiveRuleWithOrganizationId() {
        WeightCalculationRule rule = dummyRule();
        rule.setPartners(Set.of());


        WeightCalculationRule result = weightCalculationRuleService.create(rule, null);
        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("createTime", "modifyTime", "id")
                .isEqualTo(rule);
    }


    @Test
    @DisplayName("GIVEN rule with existing organization id, partnerIds and active status WHEN create rule THEN throw error")
    void throwErrorWhenActiveRuleWithExistingOrganizationId() {
        WeightCalculationRule rule = dummyRule();
        rule.setPartners(Set.of());
        weightCalculationRuleService.create(rule, null);


        assertThatThrownBy(() -> weightCalculationRuleService.create(rule, null))
                .isInstanceOfSatisfying(CostingApiException.class, exception -> {
                    assertThat(exception.getMessage()).isEqualTo(String.format(ERR_ACTIVE_RULE_ALREADY_EXIST_FOR_ORG_ID, rule.getOrganizationId()));
                });
    }

    @Test
    @DisplayName("GIVEN valid template WHEN save special volume weight rule THEN return expected")
    void returnExpectedWhenValidTemplate() {
        WeightCalculationRule rule = dummyRule();
        rule.setVolumeWeightRule(VolumeWeightRule.SPECIAL);

        WeightCalculationRule result = weightCalculationRuleService.create(rule, dummyValidXLSXTemplate());

        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("createTime", "modifyTime", "id")
                .isEqualTo(rule);
    }

    @Test
    @DisplayName("GIVEN no template WHEN save special volume weight rule THEN return error")
    void returnErrorWhenNoTemplateSpecial() {
        WeightCalculationRule rule = dummyRule();
        rule.setVolumeWeightRule(VolumeWeightRule.SPECIAL);

        assertThatThrownBy(() -> weightCalculationRuleService.create(rule, null))
                .isInstanceOf(CostingApiException.class);
    }


    @Test
    @DisplayName("GIVEN no template WHEN save standard volume weight rule THEN return expected")
    void returnExpectedWhenNoTemplateStandard() {
        WeightCalculationRule rule = dummyRule();

        WeightCalculationRule result = weightCalculationRuleService.create(rule, null);

        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("createTime", "modifyTime", "id")
                .isEqualTo(rule);
    }

    @Test
    @DisplayName("GIVEN existing rule WHEN update standard volume weight rule THEN return expected")
    void returnExpectedWhenStandardAndExistingRuleThenUpdate() {
        WeightCalculationRule rule = dummyRule();
        rule.setPartners(Set.of());

        WeightCalculationRule createdRule = weightCalculationRuleService.create(rule, null);
        createdRule.setPartners(Set.of(dummyPartner("0", "Quincus Express")));

        WeightCalculationRule result = weightCalculationRuleService.update(createdRule, null);

        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("createTime", "modifyTime", "id")
                .isEqualTo(createdRule);
    }

    @Test
    @DisplayName("GIVEN non-existing rule WHEN update standard volume weight rule THEN return error")
    void returnErrorWhenNonExistingRuleThenUpdate() {
        WeightCalculationRule rule = dummyRule();
        rule.setPartners(Set.of());
        rule.setId("0");

        assertThatThrownBy(() -> weightCalculationRuleService.update(rule, null))
                .isInstanceOf(CostingApiException.class);
    }

    @Test
    @DisplayName("GIVEN existing rule WHEN get standard volume weight rule by organization id THEN return expected")
    void returnExpectedWhenExistingRuleThenGetByOrganizationId() {
        WeightCalculationRule rule = dummyRule();
        rule.setPartners(Set.of());

        WeightCalculationRule createdRule = weightCalculationRuleService.create(rule, null);

        WeightCalculationRule result = weightCalculationRuleService.getRuleByOrganizationId(createdRule.getOrganizationId());

        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("createTime", "modifyTime")
                .isEqualTo(createdRule);
    }

    @Test
    @DisplayName("GIVEN existing rule WHEN get standard volume weight rule by organization id THEN return error")
    void returnErrorWhenExistingRuleThenGetByOrganizationId() {
        assertThatThrownBy(() -> weightCalculationRuleService.getRuleByOrganizationId(UUID.randomUUID().toString()))
                .isInstanceOf(CostingApiException.class);
    }

    @Test
    @DisplayName("GIVEN existing rule WHEN get standard volume weight rule by organization id THEN return expected")
    void returnExpectedWhenExistingRuleThenGet() {
        WeightCalculationRule rule = dummyRule();
        rule.setPartners(Set.of());

        WeightCalculationRule createdRule = weightCalculationRuleService.create(rule, null);

        WeightCalculationRule result = weightCalculationRuleService.get(createdRule.getId());

        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("createTime", "modifyTime")
                .isEqualTo(createdRule);
    }

    @Test
    @DisplayName("GIVEN non-existing rule WHEN get standard volume weight rule by organization id THEN return error")
    void returnErrorWhenExistingRuleThenGet() {
        assertThatThrownBy(() -> weightCalculationRuleService.get(UUID.randomUUID().toString()))
                .isInstanceOf(CostingApiException.class);
    }


    @Test
    @DisplayName("GIVEN existing weight calculation rule id WHEN delete THEN delete")
    void doesNotThrowExceptionDeleteWhenGivenExistingRateCardId() {
        WeightCalculationRule rule = dummyRule();
        rule.setPartners(Set.of());

        WeightCalculationRule createdRule = weightCalculationRuleService.create(rule, null);
        assertDoesNotThrow(() -> weightCalculationRuleService.delete(createdRule.getId()));
    }

    @Test
    @DisplayName("GIVEN non existing weight calculation rule id WHEN delete THEN do nothing")
    void doesNotThrowExceptionDeleteWhenGivenNonExistingRateCardId() {
        assertDoesNotThrow(() -> weightCalculationRuleService.delete(UUID.randomUUID().toString()));
    }

    @Test
    @DisplayName("GIVEN existing rule WHEN search THEN return expected")
    void returnExpectedWhenExistingRuleThenSearch() {
        WeightCalculationRule rule = dummyRule();

        WeightCalculationRule createdRule = weightCalculationRuleService.create(rule, null);

        WeightCalculationRuleFilter weightCalculationRuleFilter = new WeightCalculationRuleFilter();
        weightCalculationRuleFilter.setOrganizationId(rule.getOrganizationId());
        Page<WeightCalculationRule> result = weightCalculationRuleService.search(weightCalculationRuleFilter, Pageable.ofSize(10));
        WeightCalculationRule weightCalculationRule = result.getContent().get(0);

        assertThat(weightCalculationRule)
                .usingRecursiveComparison()
                .ignoringFields("createTime", "modifyTime")
                .isEqualTo(createdRule);
    }

    @Test
    @DisplayName("GIVEN includeDefaultRules is true WHEN search THEN return expected")
    void returnExpectedWhenIncludeDefaultRulesSearch() {
        WeightCalculationRule defaultRule = dummyRule();
        defaultRule.setPartners(Set.of());

        WeightCalculationRule createdRule = weightCalculationRuleService.create(defaultRule, null);

        WeightCalculationRuleFilter weightCalculationRuleFilter = new WeightCalculationRuleFilter();
        weightCalculationRuleFilter.setOrganizationId(defaultRule.getOrganizationId());
        weightCalculationRuleFilter.setIncludeDefaultRules("true");
        Page<WeightCalculationRule> result = weightCalculationRuleService.search(weightCalculationRuleFilter, Pageable.ofSize(10));
        WeightCalculationRule weightCalculationRule = result.getContent().get(0);

        assertThat(weightCalculationRule)
                .usingRecursiveComparison()
                .ignoringFields("createTime", "modifyTime")
                .isEqualTo(createdRule);
    }

    @Test
    @DisplayName("GIVEN includeDefaultRules is false WHEN search THEN return empty result")
    void returnEmptyWhenExcludeDefaultRulesSearch() {
        WeightCalculationRule defaultRule = dummyRule();
        defaultRule.setPartners(Set.of());

        weightCalculationRuleService.create(defaultRule, null);

        WeightCalculationRuleFilter weightCalculationRuleFilter = new WeightCalculationRuleFilter();
        weightCalculationRuleFilter.setOrganizationId(defaultRule.getOrganizationId());
        Page<WeightCalculationRule> result = weightCalculationRuleService.search(weightCalculationRuleFilter, Pageable.ofSize(10));

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("GIVEN name WHEN search THEN return expected")
    void returnExpectedWhenSearchByName() {
        WeightCalculationRule rule = dummyRule();
        rule.setName("Waldo");

        WeightCalculationRule createdRule = weightCalculationRuleService.create(rule, null);

        WeightCalculationRuleFilter weightCalculationRuleFilter = new WeightCalculationRuleFilter();
        weightCalculationRuleFilter.setName("Wal");
        Page<WeightCalculationRule> result = weightCalculationRuleService.search(weightCalculationRuleFilter, Pageable.ofSize(10));
        WeightCalculationRule weightCalculationRule = result.getContent().get(0);

        assertThat(weightCalculationRule)
                .usingRecursiveComparison()
                .ignoringFields("createTime", "modifyTime")
                .isEqualTo(createdRule);
    }
}
