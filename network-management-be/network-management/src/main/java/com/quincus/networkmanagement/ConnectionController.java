package com.quincus.networkmanagement;

import com.quincus.networkmanagement.api.domain.Connection;
import com.quincus.networkmanagement.api.filter.ConnectionSearchFilter;
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

@RequestMapping("/connections")
@Tag(name = "connections", description = "This endpoint allows to manage connection related transactions")
public interface ConnectionController {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create Connection API", description = "Create a new connection", tags = "connections")
    Response<Connection> create(@Valid @RequestBody final Request<Connection> request);

    @GetMapping("/{id}")
    @Operation(summary = "Find Connection API", description = "Find an existing connection", tags = "connections")
    Response<Connection> find(@PathVariable("id") final String id);

    @PutMapping("/{id}")
    @Operation(summary = "Update Connection API", description = "Update an existing connection", tags = "connections")
    Response<Connection> update(@PathVariable("id") final String id, @Valid @RequestBody final Request<Connection> request);

    @DeleteMapping("/{id}")
    @Operation(summary = "Update Connection API", description = "Delete an existing connection", tags = "connections")
    void delete(@PathVariable("id") final String id);

    @GetMapping
    @Operation(summary = "List Connections API", description = "List existing Connections based on search criteria", tags = "connections")
    Response<SearchFilterResult<Connection>> list(ConnectionSearchFilter filter,
                                                  @PageableDefault(size = 5, sort = {"createTime"}, direction = Sort.Direction.DESC) Pageable pageable);

    @GetMapping("/export")
    @Operation(summary = "Export Connections API", description = "Export Connections CSV based on search criteria", tags = "connections")
    void export(ConnectionSearchFilter filter, final HttpServletResponse httpServletResponse);
}
