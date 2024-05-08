package com.quincus.qportal.api;

import com.quincus.qportal.model.QPortalCostType;
import com.quincus.qportal.model.QPortalCurrency;
import com.quincus.qportal.model.QPortalDriver;
import com.quincus.qportal.model.QPortalLocation;
import com.quincus.qportal.model.QPortalMilestone;
import com.quincus.qportal.model.QPortalNotificationRequest;
import com.quincus.qportal.model.QPortalNotificationResponse;
import com.quincus.qportal.model.QPortalOrganization;
import com.quincus.qportal.model.QPortalPackageType;
import com.quincus.qportal.model.QPortalPartner;
import com.quincus.qportal.model.QPortalUser;
import com.quincus.qportal.model.QPortalUserRequest;
import com.quincus.qportal.model.QPortalVehicle;
import com.quincus.qportal.model.QPortalVehicleType;
import com.quincus.web.common.exception.model.ApiNetworkIssueException;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@AllArgsConstructor
@Service
public class QPortalApiImpl implements QPortalApi {

    private final QPortalRestClient qPortalRestClient;

    @Override
    @Cacheable(value = "packageTypes", key = "#organizationId + '-packageTypes'")
    @Retryable(
            value = ApiNetworkIssueException.class,
            maxAttemptsExpression = "${q-retry.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "${q-retry.initialDelay}",
                    maxDelayExpression = "${q-retry.maxDelay}",
                    multiplierExpression = "${q-retry.multiplier}"
            )
    )
    public List<QPortalPackageType> listPackageTypes(@NotBlank String organizationId) {
        return qPortalRestClient.listPackageTypes(organizationId);
    }

    @Override
    @Retryable(
            value = ApiNetworkIssueException.class,
            maxAttemptsExpression = "${q-retry.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "${q-retry.initialDelay}",
                    maxDelayExpression = "${q-retry.maxDelay}",
                    multiplierExpression = "${q-retry.multiplier}"
            )
    )
    public List<QPortalPartner> listPartners(@NotBlank String organizationId) {
        return qPortalRestClient.listPartners(organizationId);
    }

    @Override
    public List<QPortalPartner> listPartnersWithSearchAndPagination(@NotBlank String organizationId, @NotBlank String userId, @NotNull Integer perPage, @NotNull Integer page, String key) {
        return qPortalRestClient.listPartnersWithSearchAndPagination(organizationId, userId, perPage, page, key);
    }

    @Override
    @Cacheable(value = "partnerById", key = "#organizationId + '-partner-' + #partnerId")
    @Retryable(
            value = ApiNetworkIssueException.class,
            maxAttemptsExpression = "${q-retry.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "${q-retry.initialDelay}",
                    maxDelayExpression = "${q-retry.maxDelay}",
                    multiplierExpression = "${q-retry.multiplier}"
            )
    )
    public QPortalPartner getPartner(@NotBlank String organizationId, @NotBlank String partnerId) {
        return qPortalRestClient.getPartnerById(organizationId, partnerId);
    }

    @Override
    @Cacheable(value = "partnerByName", key = "#organizationId + '-partnerByName-' + #partnerName")
    @Retryable(
            value = ApiNetworkIssueException.class,
            maxAttemptsExpression = "${q-retry.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "${q-retry.initialDelay}",
                    maxDelayExpression = "${q-retry.maxDelay}",
                    multiplierExpression = "${q-retry.multiplier}"
            )
    )
    public QPortalPartner getPartnerByName(@NotBlank final String organizationId, @NotBlank final String partnerName) {
        return qPortalRestClient.getPartnerByName(organizationId, partnerName);
    }

    @Override
    @Cacheable(value = "locationById", key = "#organizationId + '-location-' + #locationId")
    @Retryable(
            value = ApiNetworkIssueException.class,
            maxAttemptsExpression = "${q-retry.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "${q-retry.initialDelay}",
                    maxDelayExpression = "${q-retry.maxDelay}",
                    multiplierExpression = "${q-retry.multiplier}"
            )
    )
    public QPortalLocation getLocation(@NotBlank final String organizationId, @NotBlank final String locationId) {
        return qPortalRestClient.getLocation(organizationId, locationId);
    }

    @Override
    @Cacheable(value = "users", key = "#organizationId + '-users'")
    @Retryable(
            value = ApiNetworkIssueException.class,
            maxAttemptsExpression = "${q-retry.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "${q-retry.initialDelay}",
                    maxDelayExpression = "${q-retry.maxDelay}",
                    multiplierExpression = "${q-retry.multiplier}"
            )
    )
    public List<QPortalUser> listUsers(@NotBlank String organizationId) {
        return qPortalRestClient.listUsers(organizationId);
    }

    @Override
    @Cacheable(value = "userById", key = "#organizationId + '-user-' + #userId")
    @Retryable(
            value = ApiNetworkIssueException.class,
            maxAttemptsExpression = "${q-retry.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "${q-retry.initialDelay}",
                    maxDelayExpression = "${q-retry.maxDelay}",
                    multiplierExpression = "${q-retry.multiplier}"
            )
    )
    public QPortalUser getUser(String organizationId, String userId) {
        return qPortalRestClient.getUserById(organizationId, userId);
    }

    @Override
    @Retryable(
            value = ApiNetworkIssueException.class,
            maxAttemptsExpression = "${q-retry.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "${q-retry.initialDelay}",
                    maxDelayExpression = "${q-retry.maxDelay}",
                    multiplierExpression = "${q-retry.multiplier}"
            )
    )
    public QPortalUser getUserWithoutCache(String organizationId, String userId) {
        return qPortalRestClient.getUserById(organizationId, userId);
    }

    @Override
    @Cacheable(value = "currentUserProfile", key = "#token")
    @Retryable(
            value = ApiNetworkIssueException.class,
            maxAttemptsExpression = "${q-retry.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "${q-retry.initialDelay}",
                    maxDelayExpression = "${q-retry.maxDelay}",
                    multiplierExpression = "${q-retry.multiplier}"
            )
    )
    public QPortalUser getCurrentUserProfile(String token) {
        return qPortalRestClient.getCurrentUserProfile(token);
    }

    @Override
    @Cacheable(value = "locationsByName", key = "#organizationId + '-locationsByName-' + #locationName")
    @Retryable(
            value = ApiNetworkIssueException.class,
            maxAttemptsExpression = "${q-retry.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "${q-retry.initialDelay}",
                    maxDelayExpression = "${q-retry.maxDelay}",
                    multiplierExpression = "${q-retry.multiplier}"
            )
    )
    public List<QPortalLocation> getLocationsByName(@NotBlank final String organizationId, @NotBlank final String locationName) {
        return qPortalRestClient.getLocationsByName(organizationId, locationName);
    }

    @Override
    @Retryable(
            value = ApiNetworkIssueException.class,
            maxAttemptsExpression = "${q-retry.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "${q-retry.initialDelay}",
                    maxDelayExpression = "${q-retry.maxDelay}",
                    multiplierExpression = "${q-retry.multiplier}"
            )
    )
    public List<QPortalMilestone> listMilestones(@NotBlank String organizationId) {
        return qPortalRestClient.listMilestones(organizationId);
    }

    @Override
    public List<QPortalMilestone> listMilestonesWithSearchAndPagination(@NotBlank String organizationId, @NotNull Integer perPage, @NotNull Integer page, String key) {
        return qPortalRestClient.listMilestonesWithSearchAndPagination(organizationId, perPage, page, key);
    }

    @Override
    @Cacheable(value = "costTypes", key = "#organizationId + '-costTypes'")
    @Retryable(
            value = ApiNetworkIssueException.class,
            maxAttemptsExpression = "${q-retry.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "${q-retry.initialDelay}",
                    maxDelayExpression = "${q-retry.maxDelay}",
                    multiplierExpression = "${q-retry.multiplier}"
            )
    )
    public List<QPortalCostType> listCostTypes(@NotBlank String organizationId) {
        return qPortalRestClient.listCostTypes(organizationId);
    }

    @Override
    @Cacheable(value = "costType", key = "#organizationId + '-costType-' + #costTypeId")
    @Retryable(
            value = ApiNetworkIssueException.class,
            maxAttemptsExpression = "${q-retry.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "${q-retry.initialDelay}",
                    maxDelayExpression = "${q-retry.maxDelay}",
                    multiplierExpression = "${q-retry.multiplier}"
            )
    )
    public QPortalCostType getCostType(@NotBlank String organizationId, @NotBlank String costTypeId) {
        return qPortalRestClient.getCostType(organizationId, costTypeId);
    }

    @Override
    @Cacheable(value = "drivers", key = "#organizationId + '-drivers'")
    @Retryable(
            value = ApiNetworkIssueException.class,
            maxAttemptsExpression = "${q-retry.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "${q-retry.initialDelay}",
                    maxDelayExpression = "${q-retry.maxDelay}",
                    multiplierExpression = "${q-retry.multiplier}"
            )
    )
    public List<QPortalDriver> listDrivers(@NotBlank String organizationId) {
        return qPortalRestClient.listDrivers(organizationId);
    }

    @Override
    @Cacheable(value = "driversByPartners", key = "#partnerIds + '-drivers'")
    @Retryable(
            value = ApiNetworkIssueException.class,
            maxAttemptsExpression = "${q-retry.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "${q-retry.initialDelay}",
                    maxDelayExpression = "${q-retry.maxDelay}",
                    multiplierExpression = "${q-retry.multiplier}"
            )
    )
    public List<QPortalDriver> listDriversByPartners(String organizationId, List<String> partnerIds) {

        QPortalUserRequest userRequest = new QPortalUserRequest();
        userRequest.setDriver(true);
        userRequest.setAccessiblePartnerIds(partnerIds);

        return qPortalRestClient.listDriversByPartners(organizationId, userRequest);
    }

    @Override
    @Cacheable(value = "currencies", key = "#organizationId + '-currencies'")
    @Retryable(
            value = ApiNetworkIssueException.class,
            maxAttemptsExpression = "${q-retry.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "${q-retry.initialDelay}",
                    maxDelayExpression = "${q-retry.maxDelay}",
                    multiplierExpression = "${q-retry.multiplier}"
            )
    )
    public List<QPortalCurrency> listCurrencies(@NotBlank String organizationId) {
        return qPortalRestClient.listCurrencies(organizationId);
    }

    @Override
    @Cacheable(value = "currencyById", key = "#organizationId + '-currency-' + #currencyId")
    @Retryable(
            value = ApiNetworkIssueException.class,
            maxAttemptsExpression = "${q-retry.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "${q-retry.initialDelay}",
                    maxDelayExpression = "${q-retry.maxDelay}",
                    multiplierExpression = "${q-retry.multiplier}"
            )
    )
    public QPortalCurrency getCurrency(@NotBlank String organizationId, @NotBlank String currencyId) {
        return qPortalRestClient.getCurrency(organizationId, currencyId);
    }

    @Override
    @Cacheable(value = "vehicles", key = "#organizationId + '-vehicles'")
    @Retryable(
            value = ApiNetworkIssueException.class,
            maxAttemptsExpression = "${q-retry.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "${q-retry.initialDelay}",
                    maxDelayExpression = "${q-retry.maxDelay}",
                    multiplierExpression = "${q-retry.multiplier}"
            )
    )
    public List<QPortalVehicle> listVehicles(@NotBlank String organizationId) {
        return qPortalRestClient.listVehicles(organizationId);
    }

    @Override
    @Cacheable(value = "locations", key = "#organizationId + '-locations'")
    @Retryable(
            value = ApiNetworkIssueException.class,
            maxAttemptsExpression = "${q-retry.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "${q-retry.initialDelay}",
                    maxDelayExpression = "${q-retry.maxDelay}",
                    multiplierExpression = "${q-retry.multiplier}"
            )
    )
    public List<QPortalLocation> listLocations(@NotBlank String organizationId) {
        return qPortalRestClient.listLocations(organizationId);
    }

    @Override
    @Cacheable(value = "organization", key = "#organizationId + '-organization'")
    @Retryable(
            value = ApiNetworkIssueException.class,
            maxAttemptsExpression = "${q-retry.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "${q-retry.initialDelay}",
                    maxDelayExpression = "${q-retry.maxDelay}",
                    multiplierExpression = "${q-retry.multiplier}"
            )
    )
    public QPortalOrganization getOrganizationById(String organizationId) {
        return qPortalRestClient.getOrganizationById(organizationId);
    }

    @Override
    @Cacheable(value = "vehicleTypes", key = "#organizationId + '-vehiclesTypes'")
    @Retryable(
            value = ApiNetworkIssueException.class,
            maxAttemptsExpression = "${q-retry.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "${q-retry.initialDelay}",
                    maxDelayExpression = "${q-retry.maxDelay}",
                    multiplierExpression = "${q-retry.multiplier}"
            )
    )
    public List<QPortalVehicleType> listVehicleTypes(String organizationId) {
        return qPortalRestClient.listVehicleTypes(organizationId);
    }

    @Override
    @Retryable(
            value = ApiNetworkIssueException.class,
            maxAttemptsExpression = "${q-retry.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "${q-retry.initialDelay}",
                    maxDelayExpression = "${q-retry.maxDelay}",
                    multiplierExpression = "${q-retry.multiplier}"
            )
    )
    public QPortalNotificationResponse sendNotification(String organizationId, QPortalNotificationRequest qPortalNotificationRequest) {
        return qPortalRestClient.sendNotification(organizationId, qPortalNotificationRequest);
    }
}
