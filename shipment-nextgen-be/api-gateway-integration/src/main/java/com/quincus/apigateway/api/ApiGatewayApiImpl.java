package com.quincus.apigateway.api;

import com.quincus.apigateway.domain.FlightSchedule;
import com.quincus.apigateway.domain.FlightScheduleSearchParameter;
import com.quincus.apigateway.service.ApiGatewayService;
import com.quincus.apigateway.validator.FlightScheduleSearchValidator;
import com.quincus.apigateway.web.model.ApiGatewayWebhookResponse;
import com.quincus.shipment.api.domain.Cost;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.web.common.exception.model.ApiNetworkIssueException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

@Service
@AllArgsConstructor
@Slf4j
public class ApiGatewayApiImpl implements ApiGatewayApi {

    private final ApiGatewayRestClient apiGatewayRestClient;
    private final ApiGatewayService apiGatewayService;
    private final FlightScheduleSearchValidator validator;

    @Override
    @Retryable(value = {ApiNetworkIssueException.class}, maxAttemptsExpression = "${apig-retry.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "${apig-retry.initialDelay}",
                    maxDelayExpression = "${apig-retry.maxDelay}",
                    multiplierExpression = "${apig-retry.multiplier}"
            )
    )
    public List<FlightSchedule> searchFlights(FlightScheduleSearchParameter parameter) {
        validator.validateFlightScheduleSearchParameter(parameter);
        return execute(() -> apiGatewayRestClient.searchFlights(parameter));
    }

    @Override
    @Retryable(value = {ApiNetworkIssueException.class}, maxAttemptsExpression = "${apig-retry.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "${apig-retry.initialDelay}",
                    maxDelayExpression = "${apig-retry.maxDelay}",
                    multiplierExpression = "${apig-retry.multiplier}"
            )
    )
    public ApiGatewayWebhookResponse sendAssignVendorDetails(Shipment shipment, Milestone milestone) {
        return execute(() -> apiGatewayService.sendAssignVendorDetails(shipment, milestone));
    }

    @Override
    @Retryable(value = {ApiNetworkIssueException.class}, maxAttemptsExpression = "${apig-retry.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "${apig-retry.initialDelay}",
                    maxDelayExpression = "${apig-retry.maxDelay}",
                    multiplierExpression = "${apig-retry.multiplier}"
            )
    )
    public ApiGatewayWebhookResponse sendAssignVendorDetailsWithRetry(Shipment shipment, PackageJourneySegment packageJourneySegment) {
        return execute(() -> apiGatewayService.sendAssignVendorDetails(shipment, packageJourneySegment));
    }

    @Override
    @Retryable(value = {ApiNetworkIssueException.class}, maxAttemptsExpression = "${apig-retry.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "${apig-retry.initialDelay}",
                    maxDelayExpression = "${apig-retry.maxDelay}",
                    multiplierExpression = "${apig-retry.multiplier}"
            )
    )
    public ApiGatewayWebhookResponse sendUpdateOrderProgress(Shipment shipment, Milestone milestone) {
        return execute(() -> apiGatewayService.sendUpdateOrderProgress(shipment, milestone));
    }

    @Override
    @Retryable(value = {ApiNetworkIssueException.class}, maxAttemptsExpression = "${apig-retry.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "${apig-retry.initialDelay}",
                    maxDelayExpression = "${apig-retry.maxDelay}",
                    multiplierExpression = "${apig-retry.multiplier}"
            )
    )
    public ApiGatewayWebhookResponse sendUpdateOrderProgress(Shipment shipment, PackageJourneySegment segment, Milestone milestone) {
        return execute(() -> apiGatewayService.sendUpdateOrderProgress(shipment, segment, milestone));
    }

    @Override
    @Retryable(value = {ApiNetworkIssueException.class}, maxAttemptsExpression = "${apig-retry.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "${apig-retry.initialDelay}",
                    maxDelayExpression = "${apig-retry.maxDelay}",
                    multiplierExpression = "${apig-retry.multiplier}"
            )
    )
    public List<ApiGatewayWebhookResponse> sendUpdateOrderAdditionalCharges(Cost cost) {
        return execute(() -> apiGatewayService.sendUpdateOrderAdditionalCharges(cost));
    }

    @Override
    @Retryable(value = {ApiNetworkIssueException.class}, maxAttemptsExpression = "${apig-retry.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "${apig-retry.initialDelay}",
                    maxDelayExpression = "${apig-retry.maxDelay}",
                    multiplierExpression = "${apig-retry.multiplier}"
            )
    )
    public ApiGatewayWebhookResponse sendCheckInDetails(Shipment shipment, Milestone milestone) {
        return execute(() -> apiGatewayService.sendCheckInDetails(shipment, milestone));
    }

    @Override
    @Retryable(value = {ApiNetworkIssueException.class}, maxAttemptsExpression = "${apig-retry.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "${apig-retry.initialDelay}",
                    maxDelayExpression = "${apig-retry.maxDelay}",
                    multiplierExpression = "${apig-retry.multiplier}"
            )
    )
    public ApiGatewayWebhookResponse sendCheckInDetails(Shipment shipment, PackageJourneySegment segment, Milestone milestone) {
        return execute(() -> apiGatewayService.sendCheckInDetails(shipment, segment, milestone));
    }

    private <T> T execute(Supplier<T> function) {
        try {
            return function.get();
        } catch (Exception e) {
            log.error("Error occurred while executing ApiGatewayService", e);
            return null;
        }
    }
}
