package e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.quincus.shipment.api.dto.MilestoneResponse;
import com.quincus.shipment.impl.service.QPortalService;
import com.quincus.shipment.impl.web.QPortalControllerImpl;
import com.quincus.shipment.impl.web.exception.ShipmentExceptionHandler;
import com.quincus.web.common.config.JacksonConfiguration;
import com.quincus.web.common.exception.CommonExceptionHandler;
import com.quincus.web.common.exception.model.ApiCallException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = {QPortalControllerImpl.class}, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ContextConfiguration(classes = {CommonExceptionHandler.class, ShipmentExceptionHandler.class, QPortalControllerImpl.class, JacksonConfiguration.class})
class QPortalControllerTest {

    @MockBean
    QPortalService qPortalService;

    ObjectMapper mapper = null;

    @BeforeEach
    void init() {
        mapper = new JacksonConfiguration().buildObjectMapper();
        mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
    }

    @Test
    void givenValidOrganizationId_WhenListMilestoneCodes_ThenReturnResponse(@Autowired MockMvc mvc) throws Exception {
        when(qPortalService.listMilestoneCodes()).thenReturn(List.of(new MilestoneResponse()));

        String organizationId = UUID.randomUUID().toString();

        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .get("/qportal/milestone-codes")
                        .param("organizationId", organizationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).isNotNull();
    }

    @Test
    void givenInvalidOrganizationId_WhenListMilestoneCodes_ThenThrowApiException(@Autowired MockMvc mvc) throws Exception {
        String organizationId = "Invalid Organization ID";
        String errorMessage = "Unable to retrieve milestones from QPortal with organization id `%s`";
        given(qPortalService.listMilestoneCodes())
                .willThrow(new ApiCallException(errorMessage, HttpStatus.NOT_FOUND));

        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .get("/qportal/milestone-codes")
                        .param("organizationId", organizationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).contains(errorMessage);
    }

}
