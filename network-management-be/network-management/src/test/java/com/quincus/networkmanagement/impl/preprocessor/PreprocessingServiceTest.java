package com.quincus.networkmanagement.impl.preprocessor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quincus.networkmanagement.api.domain.Connection;
import com.quincus.networkmanagement.api.domain.Node;
import com.quincus.networkmanagement.impl.preprocessor.generator.GraphGenerator;
import com.quincus.networkmanagement.impl.service.ConnectionService;
import com.quincus.networkmanagement.impl.service.NodeService;
import com.quincus.web.common.multitenant.UserDetailsContextHolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.quincus.networkmanagement.impl.data.NetworkManagementTestData.dummyConnection;
import static com.quincus.networkmanagement.impl.data.NetworkManagementTestData.dummyNode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {PreprocessingService.class})
class PreprocessingServiceTest {
    @Mock
    private ConnectionService connectionService;
    @Mock
    private GraphGenerator graphGenerator;
    @Mock
    private NodeService nodeService;
    @Mock
    private UserDetailsContextHolder userDetailsContextHolder;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private ResourceLoader resourceLoader;

    @InjectMocks
    private PreprocessingService preprocessingService;

    @Test
    void testGenerateTrainingInputTest() throws IOException {
        String organizationId = "ORGANIZATION-ID";
        List<Node> mockNodes = List.of(dummyNode(), dummyNode());
        List<Connection> mockConnections = List.of(dummyConnection(mockNodes.get(0), mockNodes.get(1)));
        ObjectNode mockGraph = new ObjectNode(JsonNodeFactory.instance);
        ObjectNode mockSettings = new ObjectNode(JsonNodeFactory.instance);
        Resource resource = new ClassPathResource("mme/test.json");

        when(nodeService.getAllActiveNodesByOrganization(organizationId)).thenReturn(mockNodes);
        when(connectionService.getAllActiveConnectionsByOrganization(organizationId)).thenReturn(mockConnections);
        when(graphGenerator.toGraph(mockNodes, mockConnections, null)).thenReturn(mockGraph);
        when(userDetailsContextHolder.getCurrentProfile()).thenReturn("local");
        when(resourceLoader.getResource(any(String.class))).thenReturn(resource);
        when(objectMapper.readTree(any(InputStream.class))).thenReturn(mockSettings);

        ObjectNode result = preprocessingService.generateTrainingInput(organizationId, null);

        assertThat(result).isNotNull().isExactlyInstanceOf(ObjectNode.class);

        verify(nodeService).getAllActiveNodesByOrganization(organizationId);
        verify(connectionService).getAllActiveConnectionsByOrganization(organizationId);
        verify(graphGenerator).toGraph(mockNodes, mockConnections, null);
        verify(userDetailsContextHolder).getCurrentProfile();
        verify(resourceLoader).getResource(any(String.class));
        verify(objectMapper).readTree(any(InputStream.class));
    }
}

