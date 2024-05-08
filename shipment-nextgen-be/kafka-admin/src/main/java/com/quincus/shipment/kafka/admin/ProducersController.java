package com.quincus.shipment.kafka.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/kafka/producers")
@Tag(name = "kafka-producers", description = "Utility endpoint acting as a kafka producer.")
public interface ProducersController {

    @PostMapping(value = "/{name}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Send to Producer API", description = "Send a message to an existing kafka topic.", tags = "kafka-producers")
    String send(@PathVariable("name") final String name, @RequestBody final String message);
    
}
