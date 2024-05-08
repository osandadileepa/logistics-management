package e2e;

import com.quincus.shipment.impl.attachment.AbstractAttachmentService;
import com.quincus.shipment.impl.attachment.AttachmentServiceFactory;
import com.quincus.shipment.impl.web.AttachmentControllerImpl;
import com.quincus.web.common.config.JacksonConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({MockitoExtension.class})
@WebMvcTest(controllers = {AttachmentControllerImpl.class})
@EnableAutoConfiguration(exclude = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {AttachmentControllerImpl.class, JacksonConfiguration.class})
class AttachmentControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockBean
    private AttachmentControllerImpl attachmentController;
    @MockBean
    private AttachmentServiceFactory attachmentServiceFactory;
    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void init() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .alwaysDo(print())
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @ParameterizedTest
    @ValueSource(strings = {"package-journey-air-segment", "milestone", "network-lane"})
    void downloadTemplateWithoutPermission(String pathParam) throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .get("/attachments/" + pathParam + "/download-csv-template")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @ValueSource(strings = {"package-journey-air-segment", "milestone", "network-lane"})
    @WithMockUser(roles = "SHIPMENT_STATUS_VIEW")
    void downloadTemplateWithPermission(String pathParam) throws Exception {
        AbstractAttachmentService<?> mockService = mock(AbstractAttachmentService.class);
        when(attachmentServiceFactory.getAttachmentServiceByType(any())).thenReturn(mockService);
        mvc.perform(MockMvcRequestBuilders
                        .get("/attachments/" + pathParam + "/download-csv-template")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
