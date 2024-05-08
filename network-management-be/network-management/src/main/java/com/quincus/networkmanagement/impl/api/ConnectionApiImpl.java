package com.quincus.networkmanagement.impl.api;

import com.quincus.networkmanagement.api.ConnectionApi;
import com.quincus.networkmanagement.api.domain.Connection;
import com.quincus.networkmanagement.api.filter.ConnectionSearchFilter;
import com.quincus.networkmanagement.impl.service.ConnectionExportService;
import com.quincus.networkmanagement.impl.service.ConnectionService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;

@Service
@AllArgsConstructor
public class ConnectionApiImpl implements ConnectionApi {

    private final ConnectionService connectionService;

    private final ConnectionExportService connectionExportService;

    @Override
    public Connection create(Connection connection) {
        return connectionService.create(connection);
    }

    @Override
    public Connection find(String id) {
        return connectionService.find(id);
    }

    @Override
    public Connection update(Connection connection) {
        return connectionService.update(connection);
    }

    @Override
    public void delete(String id) {
        connectionService.delete(id);
    }

    @Override
    public Page<Connection> list(ConnectionSearchFilter filter, Pageable pageable) {
        return connectionService.list(filter, pageable);
    }

    @Override
    public void export(ConnectionSearchFilter filter, PrintWriter writer) {
        connectionExportService.export(filter, writer);
    }
}
