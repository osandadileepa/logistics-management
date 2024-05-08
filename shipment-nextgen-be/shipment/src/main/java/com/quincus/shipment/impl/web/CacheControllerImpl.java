package com.quincus.shipment.impl.web;

import com.quincus.shipment.CacheController;
import com.quincus.web.common.utility.annotation.LogExecutionTime;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@PreAuthorize("hasAuthority('SUPER-ADMIN')")
public class CacheControllerImpl implements CacheController {

    private CacheManager cacheManager;

    @Override
    @LogExecutionTime
    public ResponseEntity<Map<String, Object>> getCacheInfo() {
        if (cacheManager == null) return ResponseEntity.ok(Collections.emptyMap());
        Map<String, Object> cacheInfo = new HashMap<>();
        for (String cacheName : cacheManager.getCacheNames()) {
            Cache cache = cacheManager.getCache(cacheName);
            cacheInfo.put(cacheName, cache);
        }
        return ResponseEntity.ok(cacheInfo);
    }

    @Override
    @LogExecutionTime
    public ResponseEntity<String> deleteCacheByName(String cacheName) {
        Cache cache = Optional.ofNullable(cacheManager).map(cm -> cm.getCache(cacheName)).orElse(null);
        if (cache == null) {
            return ResponseEntity.ok(String.format("No cache `%s` found!", cacheName));
        }
        cache.clear();
        return ResponseEntity.ok(String.format("Cache `%s` has been successfully removed!", cacheName));
    }

}