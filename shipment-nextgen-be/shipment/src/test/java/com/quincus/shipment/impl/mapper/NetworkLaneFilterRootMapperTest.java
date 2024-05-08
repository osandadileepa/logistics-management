package com.quincus.shipment.impl.mapper;

import com.quincus.order.api.domain.Destination;
import com.quincus.order.api.domain.Origin;
import com.quincus.order.api.domain.Root;
import com.quincus.shipment.api.filter.NetworkLaneFilter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NetworkLaneFilterRootMapperTest {

    private final NetworkLaneFilterRootMapper networkLaneFilterRootMapper = new NetworkLaneFilterRootMapper();

    @Test
    void givenRootMessage_whenMapOrderMessageToNetworkLaneFilter_properlyMapToNetworkLaneFilter() {
        Root orderMessage = new Root();
        Origin origin = new Origin();
        origin.setId("origin_facility_id");
        origin.setCountryId("origin_country_id");
        origin.setStateId("origin_state_id");
        origin.setCityId("origin_city_id");

        Destination destination = new Destination();
        destination.setId("destination_facility_id");
        destination.setCountryId("destination_country_id");
        destination.setStateId("destination_state_id");
        destination.setCityId("destination_city_id");

        orderMessage.setOrigin(origin);
        orderMessage.setDestination(destination);
        orderMessage.setServiceType("Express");
        NetworkLaneFilter filter = networkLaneFilterRootMapper.mapOrderMessageToNetworkLaneFilter(orderMessage);
        assertThat(filter.getPageNumber()).isZero();
        assertThat(filter.getSize()).isEqualTo(3);
        assertThat(filter.getServiceTypes().get(0).getName()).isEqualTo("Express");
        assertThat(filter.getOriginLocations().getCountryExtIds().get(0)).isEqualTo("origin_country_id");
        assertThat(filter.getOriginLocations().getStateExtIds().get(0)).isEqualTo("origin_state_id");
        assertThat(filter.getOriginLocations().getCityExtIds().get(0)).isEqualTo("origin_city_id");
        assertThat(filter.getDestinationLocations().getCountryExtIds().get(0)).isEqualTo("destination_country_id");
        assertThat(filter.getDestinationLocations().getStateExtIds().get(0)).isEqualTo("destination_state_id");
        assertThat(filter.getDestinationLocations().getCityExtIds().get(0)).isEqualTo("destination_city_id");
    }

    @Test
    void givenRootMessageMissingOrigin_whenMapOrderMessageToNetworkLaneFilter_shouldReturnNull() {
        Root orderMessage = new Root();

        Destination destination = new Destination();
        destination.setId("destination_facility_id");
        destination.setId("destination_facility_id");
        destination.setCountryId("destination_country_id");
        destination.setStateId("destination_state_id");
        destination.setCityId("destination_city_id");

        orderMessage.setDestination(destination);
        orderMessage.setServiceType("Express");
        NetworkLaneFilter filter = networkLaneFilterRootMapper.mapOrderMessageToNetworkLaneFilter(orderMessage);
        assertThat(filter).isNull();
    }

    @Test
    void givenRootMessageMissingDestination_whenMapOrderMessageToNetworkLaneFilter_shouldReturnNull() {
        Root orderMessage = new Root();
        Origin origin = new Origin();
        origin.setId("origin_facility_id");
        origin.setCountryId("origin_country_id");
        origin.setStateId("origin_state_id");
        origin.setCityId("origin_city_id");

        orderMessage.setOrigin(origin);
        orderMessage.setServiceType("Express");
        NetworkLaneFilter filter = networkLaneFilterRootMapper.mapOrderMessageToNetworkLaneFilter(orderMessage);
        assertThat(filter).isNull();
    }

    @Test
    void givenRootMessageMissingServiceType_whenMapOrderMessageToNetworkLaneFilter_shouldReturnNull() {
        Root orderMessage = new Root();
        Origin origin = new Origin();
        origin.setId("origin_facility_id");
        origin.setCountryId("origin_country_id");
        origin.setStateId("origin_state_id");
        origin.setCityId("origin_city_id");

        Destination destination = new Destination();
        destination.setId("destination_facility_id");
        destination.setCountryId("destination_country_id");
        destination.setStateId("destination_state_id");
        destination.setCityId("destination_city_id");

        orderMessage.setOrigin(origin);
        orderMessage.setDestination(destination);

        NetworkLaneFilter filter = networkLaneFilterRootMapper.mapOrderMessageToNetworkLaneFilter(orderMessage);
        assertThat(filter).isNull();
    }
}
