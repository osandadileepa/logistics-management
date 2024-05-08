package e2e;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.quincus.shipment.api.PackageDimensionApi;
import com.quincus.shipment.impl.test_utils.TestUtil;
import com.quincus.shipment.impl.web.PackageDimensionControllerImpl;
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

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({MockitoExtension.class})
@WebMvcTest(controllers = {PackageDimensionControllerImpl.class})
@EnableAutoConfiguration(exclude = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {CommonExceptionHandler.class, ShipmentExceptionHandler.class, PackageDimensionControllerImpl.class, JacksonConfiguration.class})
class PackageDimensionControllerImplTest {
    @MockBean
    PackageDimensionApi packageDimensionApi;
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
    void retrievePackageUpdateFileTemplateWithoutRole() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .get("/package-dimensions/bulk-update-file-template")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "DIMS_AND_WEIGHT_VIEW")
    void retrievePackageUpdateFileTemplate() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .get("/package-dimensions/bulk-update-file-template")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void bulkPackageDimensionUpdateImportWithoutRole() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .get("/bulk-package-dimension-update-import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "DIMS_AND_WEIGHT_EDIT")
    void bulkPackageDimensionUpdateImport() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .get("/package-dimensions/bulk-update-file-template")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "DIMS_AND_WEIGHT_EDIT")
    void updateShipmentsPackageDimension() throws Exception {
        given(packageDimensionApi.updateShipmentsPackageDimension(anyList())).willReturn(anyList());
        mvc.perform(MockMvcRequestBuilders
                        .post("/package-dimensions/shipments")
                        .with(csrf())
                        .content(testUtil.validUpdateShipmentsPackageDimensionRQJson())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
