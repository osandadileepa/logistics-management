package com.quincus.networkmanagement.impl.service;

import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.quincus.networkmanagement.api.domain.Node;
import com.quincus.networkmanagement.api.exception.NoRecordsToExportException;
import com.quincus.networkmanagement.api.filter.NodeSearchFilter;
import com.quincus.networkmanagement.impl.attachment.node.NodeRecord;
import com.quincus.networkmanagement.impl.attachment.node.NodeRecordMapper;
import com.quincus.networkmanagement.impl.mapper.NodeMapper;
import com.quincus.networkmanagement.impl.repository.NodeRepository;
import com.quincus.networkmanagement.impl.repository.specification.NodeSpecification;
import com.quincus.web.common.exception.model.QuincusException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.PrintWriter;
import java.util.List;

import static com.quincus.networkmanagement.impl.utility.CsvExportUtility.buildNodesHeader;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class NodeExportService {
    private final NodeRepository nodeRepository;

    private final NodeMapper nodeMapper;

    private final NodeRecordMapper nodeRecordMapper;
    
    public void export(NodeSearchFilter filter, PrintWriter writer) {

        NodeSpecification specification = new NodeSpecification(filter);

        List<Node> nodes = nodeRepository
                .findAll(specification)
                .stream()
                .map(nodeMapper::toDomain)
                .toList();

        if (CollectionUtils.isEmpty(nodes)) {
            throw new NoRecordsToExportException("No node records to export based on your search criteria");
        }

        List<NodeRecord> nodeRecords = nodes.stream().map(nodeRecordMapper::toRecord).toList();

        try (CSVWriter csvWriter = new CSVWriter(writer)) {
            writer.write(buildNodesHeader());
            StatefulBeanToCsv<NodeRecord> statefulBeanToCsv = new StatefulBeanToCsvBuilder<NodeRecord>(csvWriter)
                    .build();

            statefulBeanToCsv.write(nodeRecords);
            writer.close();
        } catch (Exception e) {
            throw new QuincusException(String.format("Exception occurred while writing record to file: %s", e.getMessage()));
        }
    }
}
