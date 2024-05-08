package com.quincus.networkmanagement.impl.api;

import com.quincus.networkmanagement.api.NodeApi;
import com.quincus.networkmanagement.api.domain.Node;
import com.quincus.networkmanagement.api.filter.NodeSearchFilter;
import com.quincus.networkmanagement.impl.service.NodeExportService;
import com.quincus.networkmanagement.impl.service.NodeService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;

@Service
@AllArgsConstructor
public class NodeApiImpl implements NodeApi {

    private final NodeService nodeService;
    private final NodeExportService nodeExportService;

    @Override
    public Node create(Node node) {
        return nodeService.create(node);
    }

    @Override
    public Node find(String id) {
        return nodeService.find(id);
    }

    @Override
    public Node update(Node node) {
        return nodeService.update(node);
    }

    @Override
    public void delete(String id) {
        nodeService.delete(id);
    }

    @Override
    public Page<Node> list(NodeSearchFilter filter, Pageable pageable) {
        return nodeService.list(filter, pageable);
    }

    @Override
    public void export(NodeSearchFilter filter, PrintWriter writer) {
        nodeExportService.export(filter, writer);
    }

}
