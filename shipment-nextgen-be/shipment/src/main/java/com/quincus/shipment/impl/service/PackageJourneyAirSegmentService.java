package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.dto.FlightDetailsResponse;
import com.quincus.shipment.api.dto.PackageJourneyAirSegmentResponse;
import com.quincus.shipment.api.filter.PackageJourneyAirSegmentFilter;
import com.quincus.shipment.api.filter.PackageJourneyAirSegmentFilterResult;
import com.quincus.shipment.impl.repository.PackageJourneySegmentRepository;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity_;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Tuple;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.quincus.ext.SortUtil.sortListAlphanumerically;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class PackageJourneyAirSegmentService {
    private final UserDetailsProvider userDetailsProvider;
    private final PackageJourneySegmentRepository journeySegmentRepository;

    public PackageJourneyAirSegmentFilterResult findAirlines(final PackageJourneyAirSegmentFilter filter) {
        Page<Tuple> airlineTuples = journeySegmentRepository.findAirlinesByOrganizationId(
                userDetailsProvider.getCurrentOrganizationId(),
                createPageableRequest(filter)
        );
        return createFilterResult(airlineTuples, filter, this::mapToAirlineResponse);
    }

    public PackageJourneyAirSegmentFilterResult findFlightNumbers(final PackageJourneyAirSegmentFilter filter) {
        Page<Tuple> flightNumberTuples = journeySegmentRepository.findFlightNumbersByAirlineAndOrganizationId(
                filter.getAirline(),
                userDetailsProvider.getCurrentOrganizationId(),
                createPageableRequest(filter)
        );
        return createFilterResultWithFlightDetails(flightNumberTuples, filter, tuple -> mapToFlightDetailsResponse(tuple, filter.getAirline()));
    }

    public PackageJourneyAirSegmentFilterResult findAirlinesOrFlightNumbers(final PackageJourneyAirSegmentFilter filter) {
        return createFilterResult(
                findAirlineOrFlightNumber(filter),
                filter,
                tuple -> mapToAirlineDetailsResponse(tuple, filter)
        );
    }

    private Page<Tuple> findAirlineOrFlightNumber(final PackageJourneyAirSegmentFilter filter) {
        return isFindByAirline(filter) ?
                journeySegmentRepository.findAirlinesByKeyAndOrganizationId(
                        filter.getKey(),
                        userDetailsProvider.getCurrentOrganizationId(),
                        createPageableRequest(filter)) :
                journeySegmentRepository.findFlightNumbersByKeyAndOrganizationId(
                        filter.getKey(),
                        userDetailsProvider.getCurrentOrganizationId(),
                        createPageableRequest(filter));
    }

    private boolean isFindByAirline(PackageJourneyAirSegmentFilter filter) {
        return filter.getLevel() == 1;
    }

    private PackageJourneyAirSegmentResponse mapToAirlineDetailsResponse(Tuple airlineOrFlightNumberTuples, PackageJourneyAirSegmentFilter filter) {
        PackageJourneyAirSegmentResponse response = new PackageJourneyAirSegmentResponse();
        String airline = airlineOrFlightNumberTuples.get(PackageJourneySegmentEntity_.AIRLINE, String.class);
        response.id(airline);
        response.name(airline);
        response.code(getAirlineCodeOrAirline(airlineOrFlightNumberTuples));

        if (!isFindByAirline(filter)) {
            FlightDetailsResponse details = new FlightDetailsResponse();
            String flightNumber = airlineOrFlightNumberTuples.get(PackageJourneySegmentEntity_.FLIGHT_NUMBER, String.class);
            details.id(airline + StringUtils.SPACE + flightNumber);
            details.name(flightNumber);
            response.flightNumbers(List.of(details));
        }

        return response;
    }

    private FlightDetailsResponse mapToFlightDetailsResponse(final Tuple flightNumberTuples, final String airline) {
        String flightNumber = flightNumberTuples.get(PackageJourneySegmentEntity_.FLIGHT_NUMBER, String.class);
        return new FlightDetailsResponse()
                .id(airline + StringUtils.SPACE + flightNumber)
                .name(flightNumber);
    }

    private PackageJourneyAirSegmentResponse mapToAirlineResponse(final Tuple airlineTuples) {
        String airline = airlineTuples.get(PackageJourneySegmentEntity_.AIRLINE, String.class);
        return new PackageJourneyAirSegmentResponse()
                .id(airline)
                .name(airline)
                .code(getAirlineCodeOrAirline(airlineTuples));
    }

    private String getAirlineCodeOrAirline(Tuple airlineTuples) {
        return Optional.ofNullable(airlineTuples.get(PackageJourneySegmentEntity_.AIRLINE_CODE, String.class)).orElse(airlineTuples.get(PackageJourneySegmentEntity_.AIRLINE, String.class));
    }

    private Pageable createPageableRequest(final PackageJourneyAirSegmentFilter filter) {
        return PageRequest.of((filter.getPage() - 1), filter.getPerPage());
    }

    private List<PackageJourneyAirSegmentResponse> mergePackageJourneyAirSegmentByAirlineName(List<PackageJourneyAirSegmentResponse> responses) {
        return responses.stream()
                .collect(Collectors.toMap(
                        PackageJourneyAirSegmentResponse::name,
                        Function.identity(),
                        this::mergeFlightDetails
                )).values()
                .stream()
                .sorted(Comparator.comparing(PackageJourneyAirSegmentResponse::name))
                .toList();
    }

    private PackageJourneyAirSegmentResponse mergeFlightDetails(PackageJourneyAirSegmentResponse existingResponse, PackageJourneyAirSegmentResponse newResponse) {
        List<FlightDetailsResponse> existingFlightNumbers = Optional.ofNullable(existingResponse.flightNumbers()).map(ArrayList::new).orElse(new ArrayList<>());
        Optional.ofNullable(newResponse.flightNumbers()).ifPresent(existingFlightNumbers::addAll);
        sortListAlphanumerically(existingFlightNumbers, FlightDetailsResponse::name);
        existingResponse.flightNumbers(existingFlightNumbers);
        return existingResponse;
    }

    private PackageJourneyAirSegmentFilterResult createFilterResult(Page<Tuple> tuples, PackageJourneyAirSegmentFilter filter, Function<Tuple, PackageJourneyAirSegmentResponse> responseMapper) {
        List<PackageJourneyAirSegmentResponse> responses = tuples.stream().map(responseMapper).toList();
        return new PackageJourneyAirSegmentFilterResult(mergePackageJourneyAirSegmentByAirlineName(responses))
                .filter(filter)
                .totalPages(tuples.getTotalPages())
                .totalElements(tuples.getTotalElements())
                .page(filter.getPage());
    }

    private PackageJourneyAirSegmentFilterResult createFilterResultWithFlightDetails(Page<Tuple> tuples, PackageJourneyAirSegmentFilter filter, Function<Tuple, FlightDetailsResponse> responseMapper) {
        List<FlightDetailsResponse> responses = tuples.stream().map(responseMapper).collect(Collectors.toCollection(ArrayList::new));
        sortListAlphanumerically(responses, FlightDetailsResponse::name);
        return new PackageJourneyAirSegmentFilterResult(responses)
                .filter(filter)
                .totalPages(tuples.getTotalPages())
                .totalElements(tuples.getTotalElements())
                .page(filter.getPage());
    }
}