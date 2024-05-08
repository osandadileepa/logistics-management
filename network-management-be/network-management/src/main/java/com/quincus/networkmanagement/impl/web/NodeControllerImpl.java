package com.quincus.networkmanagement.impl.web;

import com.quincus.networkmanagement.NodeController;
import com.quincus.networkmanagement.api.NodeApi;
import com.quincus.networkmanagement.api.domain.Node;
import com.quincus.networkmanagement.api.filter.NodeSearchFilter;
import com.quincus.networkmanagement.api.filter.SearchFilterResult;
import com.quincus.web.common.exception.model.QuincusException;
import com.quincus.web.common.model.Request;
import com.quincus.web.common.model.Response;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;

@AllArgsConstructor
@RestController
public class NodeControllerImpl implements NodeController {

    private NodeApi nodeApi;

    @Override
    public Response<Node> create(Request<Node> request) {
        return new Response<>(nodeApi.create(request.getData()));
    }

    @Override
    public Response<Node> find(String id) {
        return new Response<>(nodeApi.find(id));
    }

    @Override
    public Response<Node> update(String id, Request<Node> request) {
        Node node = request.getData();
        node.setId(id);
        return new Response<>(nodeApi.update(node));
    }

    @Override
    public void delete(String id) {
        nodeApi.delete(id);
    }

    @Override
    public Response<SearchFilterResult<Node>> list(NodeSearchFilter filter, Pageable pageable) {
        Page<Node> result = nodeApi.list(filter, pageable);
        return new Response<>(new SearchFilterResult<>(result, filter));
    }

    @Override
    public void export(NodeSearchFilter filter, final HttpServletResponse httpServletResponse) {
        try {
            String fileName = "nodes-" + Instant.now().getEpochSecond() + ".csv";
            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            httpServletResponse.setContentType("text/csv");
            httpServletResponse.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            nodeApi.export(filter, httpServletResponse.getWriter());
        } catch (IOException e) {
            throw new QuincusException("Failed to export CSV file");
        }
    }
}
