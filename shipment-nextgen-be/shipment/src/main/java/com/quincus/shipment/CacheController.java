package com.quincus.shipment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/cache")
@Tag(name = "cache", description = "A utility endpoint for cache related methods.")
public interface CacheController {

    @GetMapping("/info")
    @Operation(summary = "Get Cache Information", description = "Get all the cache information", tags = "cache")
    ResponseEntity<Map<String, Object>> getCacheInfo();

    @DeleteMapping("/delete/{cacheName}")
    @Operation(summary = "Delete Cache by Name", description = "Deletes a cache by its name", tags = "cache")
    ResponseEntity<String> deleteCacheByName(@RequestParam String cacheName);

}