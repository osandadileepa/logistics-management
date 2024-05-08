package com.quincus.shipment.impl.enricher;

import com.quincus.shipment.impl.repository.criteria.UserPartnersCriteria;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.web.common.multitenant.QuincusUserPartner;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@Slf4j
@AllArgsConstructor
public class UserPartnerCriteriaEnricher {
    private static final String NO_USER_PARTNER = "User {} has no associated partners";
    private final UserDetailsProvider userDetailsProvider;

    public void enrichCriteriaByPartners(UserPartnersCriteria userPartnersCriteria) {
        List<QuincusUserPartner> userPartners = userDetailsProvider.getCurrentUserPartners();
        if (CollectionUtils.isEmpty(userPartners)) {
            log.debug(NO_USER_PARTNER, userDetailsProvider.getCurrentUserId());
            return;
        }

        List<String> partnerIds = userPartners.stream()
                .filter(Objects::nonNull).map(QuincusUserPartner::getPartnerId).toList();

        userPartnersCriteria.setUserPartners(partnerIds);
    }
}
