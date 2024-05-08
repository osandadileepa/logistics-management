package com.quincus.networkmanagement.impl.preprocessor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quincus.networkmanagement.api.constant.MmeGraphProperty;
import com.quincus.networkmanagement.api.domain.Connection;
import com.quincus.networkmanagement.api.domain.Node;
import com.quincus.networkmanagement.impl.preprocessor.generator.GraphGenerator;
import com.quincus.networkmanagement.impl.service.ConnectionService;
import com.quincus.networkmanagement.impl.service.NodeService;
import com.quincus.web.common.multitenant.UserDetailsContextHolder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class PreprocessingService {
    private static final JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;
    private final NodeService nodeService;
    private final ConnectionService connectionService;
    private final GraphGenerator graphGenerator;
    private final ObjectMapper objectMapper;
    private final UserDetailsContextHolder userDetailsContextHolder;
    private final ResourceLoader resourceLoader;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ObjectNode generateTrainingInput(String organizationId, Long dateOfTraining) {

        organizationId = StringUtils.isBlank(organizationId) ? userDetailsContextHolder.getCurrentOrganizationId() : organizationId;

        List<Node> nodes = nodeService.getAllActiveNodesByOrganization(organizationId);
        List<Connection> connections = referenceDepartureAndArrivalNodes(nodes, connectionService.getAllActiveConnectionsByOrganization(organizationId));

        ObjectNode result = jsonNodeFactory.objectNode();
        result.set(MmeGraphProperty.GRAPH, graphGenerator.toGraph(nodes, connections, dateOfTraining));
        result.set(MmeGraphProperty.SETTINGS, getSettings());

        return result;
    }

    /**
     * Reference departure and arrival nodes of connections
     * Also, excludes a connection if either node is invalid (e.g. inactive)
     */
    private List<Connection> referenceDepartureAndArrivalNodes(List<Node> nodes, List<Connection> connections) {
        Map<String, Node> nodeMap = nodes.stream().collect(Collectors.toMap(Node::getNodeCode, node -> node));
        List<Connection> result = new ArrayList<>();

        connections.forEach(
                c -> {
                    if (nodeMap.containsKey(c.getDepartureNode().getNodeCode()) &&
                            nodeMap.containsKey(c.getArrivalNode().getNodeCode())
                    ) {
                        c.setDepartureNode(nodeMap.get(c.getDepartureNode().getNodeCode()));
                        c.setArrivalNode(nodeMap.get(c.getArrivalNode().getNodeCode()));
                        result.add(c);
                    }
                }
        );
        return result;
    }

    private ObjectNode getSettings() {
        String filePath = "classpath:mme/settings-" + userDetailsContextHolder.getCurrentProfile() + ".json";
        Resource resource = resourceLoader.getResource(filePath);
        ObjectNode settings = jsonNodeFactory.objectNode();

        try {
            settings = (ObjectNode) objectMapper.readTree(resource.getInputStream());
        } catch (Exception ignore) {
            log.warn("Failed to read MME settings file {} ", filePath, ignore);
        }

        return settings;
    }
}
