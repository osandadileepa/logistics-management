package com.quincus.networkmanagement.api;

import com.quincus.networkmanagement.api.domain.Node;
import com.quincus.networkmanagement.api.filter.NodeSearchFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.PrintWriter;

public interface NodeApi {

    Node create(Node node);

    Node find(String id);

    Node update(Node node);

    void delete(String id);

    Page<Node> list(NodeSearchFilter filter, Pageable pageable);

    void export(NodeSearchFilter filter, PrintWriter writer);
}
