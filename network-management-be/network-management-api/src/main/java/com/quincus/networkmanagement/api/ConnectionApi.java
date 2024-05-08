package com.quincus.networkmanagement.api;

import com.quincus.networkmanagement.api.domain.Connection;
import com.quincus.networkmanagement.api.filter.ConnectionSearchFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.PrintWriter;

public interface ConnectionApi {

    Connection create(Connection connection);

    Connection find(String id);

    Connection update(Connection connection);

    void delete(String id);

    Page<Connection> list(ConnectionSearchFilter filter, Pageable pageable);

    void export(ConnectionSearchFilter filter, PrintWriter writer);
}
