package com.quincus.shipment.kafka.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/kafka/topics")
@Tag(name = "kafka-topics", description = "Utility endpoint for managing of Kafka Topics.")
public interface KafkaAdminController {

    @PostMapping(value = "/{name}")
    @Operation(summary = "Create Topic API", description = "Create a new kafka topic.", tags = "kafka-topics")
    String create(@PathVariable("name") final String name);

    @GetMapping
    @Operation(summary = "List All Topics API", description = "Return a list of existing kafka topics.", tags = "kafka-topics")
    List<String> listAllTopic();

    @DeleteMapping(value = "/{name}")
    @Operation(summary = "Delete Topic API", description = "Delete an existing kafka topic.", tags = "kafka-topics")
    String deleteTopic(@PathVariable("name") final String name);

}
