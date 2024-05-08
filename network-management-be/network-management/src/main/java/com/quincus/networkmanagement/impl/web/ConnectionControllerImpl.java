package com.quincus.networkmanagement.impl.web;

import com.quincus.networkmanagement.ConnectionController;
import com.quincus.networkmanagement.api.ConnectionApi;
import com.quincus.networkmanagement.api.domain.Connection;
import com.quincus.networkmanagement.api.filter.ConnectionSearchFilter;
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
public class ConnectionControllerImpl implements ConnectionController {

    private ConnectionApi connectionApi;

    @Override
    public Response<Connection> create(Request<Connection> request) {
        return new Response<>(connectionApi.create(request.getData()));
    }

    @Override
    public Response<Connection> find(String id) {
        return new Response<>(connectionApi.find(id));
    }

    @Override
    public Response<Connection> update(String id, Request<Connection> request) {
        Connection connection = request.getData();
        connection.setId(id);
        return new Response<>(connectionApi.update(connection));
    }

    @Override
    public void delete(String id) {
        connectionApi.delete(id);
    }

    @Override
    public Response<SearchFilterResult<Connection>> list(ConnectionSearchFilter filter, Pageable pageable) {
        Page<Connection> result = connectionApi.list(filter, pageable);
        return new Response<>(new SearchFilterResult<>(result, filter));
    }

    @Override
    public void export(ConnectionSearchFilter filter, final HttpServletResponse httpServletResponse) {
        try {
            String fileName = "connections-" + Instant.now().getEpochSecond() + ".csv";
            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            httpServletResponse.setContentType("text/csv");
            httpServletResponse.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            connectionApi.export(filter, httpServletResponse.getWriter());
        } catch (IOException e) {
            throw new QuincusException("Failed to export CSV file");
        }
    }
}
