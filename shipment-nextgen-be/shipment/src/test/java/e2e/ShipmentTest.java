package e2e;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.quincus.qlogger.api.QLoggerAPI;
import com.quincus.shipment.api.AlertApi;
import com.quincus.shipment.api.ShipmentApi;
import com.quincus.shipment.api.constant.TriggeredFrom;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.exception.PackageJourneySegmentException;
import com.quincus.shipment.api.exception.ShipmentInvalidStatusException;
import com.quincus.shipment.api.exception.ShipmentNotFoundException;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.test_utils.TestUtil;
import com.quincus.shipment.impl.web.ShipmentControllerImpl;
import com.quincus.shipment.impl.web.exception.ShipmentExceptionHandler;
import com.quincus.web.common.config.JacksonConfiguration;
import com.quincus.web.common.exception.CommonExceptionHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = {ShipmentControllerImpl.class}, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ContextConfiguration(classes = {CommonExceptionHandler.class, ShipmentExceptionHandler.class, ShipmentControllerImpl.class, JacksonConfiguration.class})
class ShipmentTest {
    @MockBean
    ShipmentApi shipmentApi;

    @MockBean
    AlertApi alertApi;

    @MockBean
    QLoggerAPI qLoggerAPI;

    @MockBean
    UserDetailsProvider userDetailsProvider;
    private TestUtil testUtil = TestUtil.getInstance();

    @Test
    void create_FacilityIDsWronglySet_ShouldReturnBadRequest(@Autowired MockMvc mvc) throws Exception {
        PackageJourneySegmentException packageJourneySegmentException = new PackageJourneySegmentException();
        packageJourneySegmentException.addError(0, "start_facility", "error");
        given(shipmentApi.create(any(Shipment.class)))
                .willThrow(packageJourneySegmentException);

        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .post("/shipments")
                        .content(testUtil.invalidShipmentJson())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).contains("error");
    }

    @Test
    void update_ShipmentNotFound_ShouldReturnNotFound(@Autowired MockMvc mvc) throws Exception {
        given(shipmentApi.update(any(Shipment.class), anyBoolean()))
                .willThrow(new ShipmentNotFoundException("update shipment not found "));
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .put("/shipments")
                        .content(testUtil.invalidShipmentJson())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).contains("update shipment not found");
    }

    @Test
    void cancel_ShipmentAlreadyCancelled_ShouldReturnBadRequest(@Autowired MockMvc mvc) throws Exception {
        doThrow(new ShipmentInvalidStatusException("already cancelled.")).when(shipmentApi).cancel(anyString(), any(TriggeredFrom.class));
        String shipmentId = UUID.randomUUID().toString();
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .patch("/shipments/cancel/" + shipmentId)
                        .content(testUtil.invalidShipmentJson())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).contains("already cancelled");
    }

    @Test
    void add_InvalidJSONRequest_ShouldReturnBadRequest(@Autowired MockMvc mvc) throws Exception {
        given(shipmentApi.create(any(Shipment.class)))
                .willAnswer(invocationOnMock -> {
                    throw new JsonParseException(null, "error");
                });
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .post("/shipments")
                        .content(testUtil.invalidShipmentJson())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).contains("error");
    }

    @Test
    void add_ThrowsJacksonException_ShouldReturnBadRequest(@Autowired MockMvc mvc) throws Exception {
        given(shipmentApi.create(any(Shipment.class)))
                .willAnswer(invocationOnMock -> {
                    throw new JsonMappingException(null, "error");
                });
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .post("/shipments")
                        .content(testUtil.invalidShipmentJson())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).contains("error");
    }
}
