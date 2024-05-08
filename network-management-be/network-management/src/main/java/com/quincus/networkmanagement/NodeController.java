package com.quincus.networkmanagement;

import com.quincus.networkmanagement.api.domain.Node;
import com.quincus.networkmanagement.api.filter.NodeSearchFilter;
import com.quincus.networkmanagement.api.filter.SearchFilterResult;
import com.quincus.web.common.model.Request;
import com.quincus.web.common.model.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RequestMapping("/nodes")
@Tag(name = "nodes", description = "This endpoint allows to manage node related transactions")
public interface NodeController {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create Node API", description = "Create a new node", tags = "nodes")
    Response<Node> create(@Valid @RequestBody final Request<Node> request);

    @GetMapping("/{id}")
    @Operation(summary = "Find Node API", description = "Find an existing node", tags = "nodes")
    Response<Node> find(@PathVariable("id") final String id);

    @PutMapping("/{id}")
    @Operation(summary = "Update Node API", description = "Update an existing node", tags = "nodes")
    Response<Node> update(@PathVariable("id") final String id, @Valid @RequestBody final Request<Node> request);

    @DeleteMapping("/{id}")
    @Operation(summary = "Update Node API", description = "Delete an existing node", tags = "nodes")
    void delete(@PathVariable("id") final String id);

    @Operation(summary = "List Nodes API", description = "List existing nodes depending on search criteria", tags = "nodes")
    @GetMapping
    Response<SearchFilterResult<Node>> list(
            NodeSearchFilter filter,
            @PageableDefault(size = 5, sort = {"createTime"}, direction = Sort.Direction.DESC) Pageable pageable
    );

    @GetMapping("/export")
    @Operation(summary = "Export Nodes API", description = "Export Nodes CSV based on search criteria", tags = "nodes")
    void export(NodeSearchFilter filter, final HttpServletResponse httpServletResponse);
}
