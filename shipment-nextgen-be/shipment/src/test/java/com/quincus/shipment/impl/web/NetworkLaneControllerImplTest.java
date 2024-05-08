package com.quincus.shipment.impl.web;

import com.quincus.shipment.api.NetworkLaneApi;
import com.quincus.shipment.api.domain.NetworkLane;
import com.quincus.shipment.api.filter.NetworkLaneFilter;
import com.quincus.shipment.api.filter.NetworkLaneFilterResult;
import com.quincus.shipment.api.filter.ServiceTypeFilter;
import com.quincus.web.common.model.Request;
import com.quincus.web.common.model.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NetworkLaneControllerImplTest {
    @InjectMocks
    private NetworkLaneControllerImpl networkLaneController;
    @Mock
    private NetworkLaneApi networkLaneApi;

    @Test
    void testFindNetworkLaneShouldReturnNetworkLaneApiFindResult() {
        // GIVEN
        ServiceTypeFilter serviceType = new ServiceTypeFilter();
        serviceType.setName("service");
        int perPage = 10;
        int page = 1;
        NetworkLaneFilter filter = new NetworkLaneFilter();
        filter.setServiceTypes(List.of(serviceType));
        filter.setSize(perPage);
        filter.setPageNumber(page);
        Request<NetworkLaneFilter> filterRequest = new Request<>();
        filterRequest.setData(filter);
        NetworkLaneFilterResult filterResult = new NetworkLaneFilterResult(new ArrayList<>());
        when(networkLaneApi.findAll(filter)).thenReturn(filterResult);

        // WHEN:
        Response<NetworkLaneFilterResult> response = networkLaneController.findAll(filterRequest);

        // THEN:
        verify(networkLaneApi).findAll(filter);
        assertThat(response.getData()).isEqualTo(filterResult);
    }

    @Test
    void find_networkLaneFound_shouldReturnNetworkLane() {
        String id = UUID.randomUUID().toString();
        NetworkLane networkLane = new NetworkLane();
        networkLane.setId(id);
        when(networkLaneApi.findById(id)).thenReturn(networkLane);

        Response<NetworkLane> response = networkLaneController.findById(id);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isEqualTo(networkLane);
        verify(networkLaneApi, times(1)).findById(id);
    }

}
