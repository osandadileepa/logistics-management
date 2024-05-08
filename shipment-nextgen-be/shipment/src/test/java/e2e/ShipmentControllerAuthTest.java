package e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.quincus.qlogger.api.QLoggerAPI;
import com.quincus.shipment.api.ShipmentApi;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.dto.PackageDimensionUpdateRequest;
import com.quincus.shipment.api.dto.ShipmentMilestoneOpsUpdateRequest;
import com.quincus.shipment.api.dto.UserLocation;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.test_utils.TestUtil;
import com.quincus.shipment.impl.web.ShipmentControllerImpl;
import com.quincus.shipment.impl.web.exception.ShipmentExceptionHandler;
import com.quincus.web.common.config.JacksonConfiguration;
import com.quincus.web.common.exception.CommonExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({MockitoExtension.class})
@WebMvcTest(controllers = {ShipmentControllerImpl.class})
@EnableAutoConfiguration(exclude = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {CommonExceptionHandler.class, ShipmentExceptionHandler.class, ShipmentControllerImpl.class, JacksonConfiguration.class})
class ShipmentControllerAuthTest {
    @MockBean
    ShipmentApi shipmentApi;
    @MockBean
    QLoggerAPI qLoggerAPI;
    @MockBean
    UserDetailsProvider userDetailsProvider;
    @Autowired
    MockMvc mvc;
    ObjectMapper mapper = null;
    @Autowired
    private WebApplicationContext context;

    private TestUtil testUtil = TestUtil.getInstance();

    @BeforeEach
    void init() {
        mapper = new JacksonConfiguration().buildObjectMapper();
        mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .alwaysDo(print())
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void findShipmentWithoutRole() throws Exception {
        given(shipmentApi.find(anyString())).willReturn(new Shipment());

        mvc.perform(MockMvcRequestBuilders
                        .get("/shipments/" + UUID.randomUUID())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "SHIPMENTS_VIEW")
    void findShipmentWithRole() throws Exception {
        given(shipmentApi.find(anyString())).willReturn(new Shipment());

        mvc.perform(MockMvcRequestBuilders
                        .get("/shipments/" + UUID.randomUUID())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void updateShipmentWithoutRole() throws Exception {
        given(shipmentApi.update(any(Shipment.class), anyBoolean())).willReturn(new Shipment());
        mvc.perform(MockMvcRequestBuilders
                        .put("/shipments")
                        .with(csrf())
                        .content(testUtil.validShipmentJson())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "SHIPMENTS_EDIT")
    void updateShipmentWithRole() throws Exception {
        given(shipmentApi.update(any(Shipment.class), anyBoolean())).willReturn(new Shipment());
        mvc.perform(MockMvcRequestBuilders
                        .put("/shipments")
                        .with(csrf())
                        .content(testUtil.validShipmentJson())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void exportShipmentWithoutRole() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .post("/shipments/export")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "SHIPMENTS_EXPORT")
    void exportShipmentWithRole() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .post("/shipments/export")
                        .with(csrf())
                        .content("{}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void findShipmentPackageDimensionWithoutRole() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .get("/shipments/get-by-tracking-id/QC0122110800014-0101/package-dimension")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "DIMS_AND_WEIGHT_VIEW")
    void findShipmentPackageDimension() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .get("/shipments/get-by-tracking-id/QC0122110800014-0101/package-dimension")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void updateShipmentPackageDimensionWithoutRole() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .put("/shipments/get-by-tracking-id/QC0122110800014-0101/package-dimension")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "DIMS_AND_WEIGHT_EDIT")
    void updateShipmentPackageDimension() throws Exception {
        PackageDimensionUpdateRequest requestData = new PackageDimensionUpdateRequest();
        requestData.setOrganizationId("123");
        requestData.setWidth(BigDecimal.valueOf(1));
        requestData.setLength(BigDecimal.valueOf(2));
        requestData.setLength(BigDecimal.valueOf(3));
        requestData.setGrossWeight(BigDecimal.valueOf(4));
        requestData.setUserId(UUID.randomUUID().toString());

        Map<String, PackageDimensionUpdateRequest> data = new HashMap<>();
        data.put("data", requestData);

        String requestContent = mapper.writeValueAsString(data);
        mvc.perform(MockMvcRequestBuilders
                        .put("/shipments/get-by-tracking-id/QC0122110800014-0101/package-dimension")
                        .with(csrf())
                        .content(requestContent)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void updateShipmentMilestoneAdditionalInfoWithoutRole() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .patch("/shipments/get-by-tracking-id/QC0122110800014-0101/milestone-and-additional-info")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "SHIPMENT_STATUS_EDIT")
    void updateShipmentMilestoneAdditionalInfo() throws Exception {
        ShipmentMilestoneOpsUpdateRequest requestData = new ShipmentMilestoneOpsUpdateRequest();
        requestData.setShipmentTrackingId("SHP-123");
        requestData.setMilestoneCode("000");
        requestData.setMilestoneName("Created");
        requestData.setMilestoneTime("2023-01-01T12:00:00Z");
        UserLocation userLocation = new UserLocation();
        userLocation.setLocationId("location-id");
        userLocation.setLocationFacilityName("TESTING FACILITY");
        requestData.setUsersLocation(userLocation);

        Map<String, ShipmentMilestoneOpsUpdateRequest> data = new HashMap<>();
        data.put("data", requestData);

        String requestContent = mapper.writeValueAsString(data);
        mvc.perform(MockMvcRequestBuilders
                        .patch("/shipments/get-by-tracking-id/QC0122110800014-0101/milestone-and-additional-info")
                        .with(csrf())
                        .content(requestContent)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
