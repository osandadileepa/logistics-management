package com.quincus.networkmanagement.impl.service;

import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.quincus.networkmanagement.api.domain.Connection;
import com.quincus.networkmanagement.api.exception.NoRecordsToExportException;
import com.quincus.networkmanagement.api.filter.ConnectionSearchFilter;
import com.quincus.networkmanagement.impl.attachment.connection.ConnectionRecord;
import com.quincus.networkmanagement.impl.attachment.connection.ConnectionRecordMapper;
import com.quincus.networkmanagement.impl.mapper.ConnectionMapper;
import com.quincus.networkmanagement.impl.repository.ConnectionRepository;
import com.quincus.networkmanagement.impl.repository.specification.ConnectionSpecification;
import com.quincus.web.common.exception.model.QuincusException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.PrintWriter;
import java.util.List;

import static com.quincus.networkmanagement.impl.utility.CsvExportUtility.buildConnectionsHeader;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ConnectionExportService {
    private final ConnectionRepository connectionRepository;

    private final ConnectionMapper connectionMapper;

    private final ConnectionRecordMapper connectionRecordMapper;

    public void export(ConnectionSearchFilter filter, PrintWriter writer) {

        ConnectionSpecification specification = new ConnectionSpecification(filter);

        List<Connection> connections = connectionRepository
                .findAll(specification)
                .stream()
                .map(connectionMapper::toDomain)
                .toList();

        if (CollectionUtils.isEmpty(connections)) {
            throw new NoRecordsToExportException("No connection records to export based on your search criteria");
        }

        List<ConnectionRecord> connectionRecords = connections.stream().map(connectionRecordMapper::toRecord).toList();

        try (CSVWriter csvWriter = new CSVWriter(writer)) {
            writer.write(buildConnectionsHeader());
            StatefulBeanToCsv<ConnectionRecord> statefulBeanToCsv = new StatefulBeanToCsvBuilder<ConnectionRecord>(csvWriter)
                    .build();

            statefulBeanToCsv.write(connectionRecords);
            writer.close();
        } catch (Exception e) {
            throw new QuincusException(String.format("Exception occurred while writing record to file: %s", e.getMessage()));
        }
    }
}
