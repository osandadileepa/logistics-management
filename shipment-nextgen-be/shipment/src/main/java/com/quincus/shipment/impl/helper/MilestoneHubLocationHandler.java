package com.quincus.shipment.impl.helper;

import com.quincus.qportal.model.QPortalLocation;
import com.quincus.shipment.api.domain.Coordinate;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.service.QPortalService;
import com.quincus.web.common.multitenant.QuincusUserLocation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;


@Component
@AllArgsConstructor
@Slf4j
public class MilestoneHubLocationHandler {

    private final QPortalService qPortalService;
    private final UserDetailsProvider userDetailsProvider;

    /**
     * When milestones have hubId (Facility Id) from 3rd party, enrich from QPortal LocationIds (CountryId,StateId,CityId,Lat,Lon)
     *
     * @param milestone - milestone
     */
    public void enrichMilestoneHubIdWithLocationIds(Milestone milestone) {
        if (milestone == null || StringUtils.isBlank(milestone.getHubId())) {
            return;
        }
        QPortalLocation qPortalLocation = qPortalService.getLocation(milestone.getHubId());
        if (qPortalLocation == null) {
            log.warn("Invalid HubId : {}", milestone.getHubId());
            return;
        }
        milestone.setHubCountryId(qPortalLocation.getCountryId());
        milestone.setHubStateId(qPortalLocation.getStateProvinceId());
        milestone.setHubCityId(qPortalLocation.getCityId());
        milestone.setHubTimeZone(qPortalLocation.getTimezoneTimeInGmt());
        Coordinate coordinate = new Coordinate();
        coordinate.setLat(Optional.ofNullable(qPortalLocation.getLat()).map(BigDecimal::new).orElse(null));
        coordinate.setLon(Optional.ofNullable(qPortalLocation.getLon()).map(BigDecimal::new).orElse(null));
        milestone.setMilestoneCoordinates(coordinate);
    }

    /**
     * This method is for milestone that uses start facility id as hubId but since there are start facility that is of address
     * we will be using cityId to enrich timezone
     *
     * @param milestone - with hubCityId
     */
    public void enrichMilestoneHubLocationDetailsByHubCityId(Milestone milestone) {
        if (milestone == null || StringUtils.isBlank(milestone.getHubCityId())) {
            return;
        }
        QPortalLocation qPortalLocation = qPortalService.getLocation(milestone.getHubCityId());
        if (qPortalLocation == null) {
            log.warn("Invalid HubCityId : {}", milestone.getHubCityId());
            return;
        }
        milestone.setHubCountryId(qPortalLocation.getCountryId());
        milestone.setHubStateId(qPortalLocation.getStateProvinceId());
        milestone.setHubTimeZone(qPortalLocation.getTimezoneTimeInGmt());
        Coordinate coordinate = new Coordinate();
        coordinate.setLat(Optional.ofNullable(qPortalLocation.getLat()).map(BigDecimal::new).orElse(null));
        coordinate.setLon(Optional.ofNullable(qPortalLocation.getLon()).map(BigDecimal::new).orElse(null));
        milestone.setMilestoneCoordinates(coordinate);
    }

    /**
     * This method will set milestone hubId with user location/facility and populate hubLocationIds (CountryId,StateId,CityId)
     * This will be sent to QShip milestone message for SHPV2-5221
     *
     * @param milestone - milestone
     */
    public void configureMilestoneHubWithUserHubInfo(Milestone milestone) {
        QuincusUserLocation quincusUserLocation = userDetailsProvider.getUserCurrentLocation();
        if (StringUtils.isBlank(quincusUserLocation.locationId())) {
            return;
        }
        milestone.setHubId(quincusUserLocation.locationId());
        enrichMilestoneHubIdWithLocationIds(milestone);
    }
}
