package com.quincus.shipment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/data")
@Tag(name = "data", description = "A utility endpoint for data clean up related method.")
public interface CleanUpController {

    @DeleteMapping("/cleanup")
    @Operation(summary = "Clean up Utility API", description = "Clean ups DB based on entity name.", tags = "data")
    ResponseEntity<Void> cleanUp(@RequestParam("id") final String id, @RequestParam("entity") final String entity);
}
