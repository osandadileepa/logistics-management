package com.quincus.shipment.impl.web;

import com.quincus.shipment.CleanUpController;
import com.quincus.shipment.impl.service.CleanUpService;
import com.quincus.web.common.utility.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@PreAuthorize("hasAuthority('SUPER-ADMIN')")
public class CleanUpControllerImpl implements CleanUpController {

    private final CleanUpService cleanUpService;

    @Override
    @LogExecutionTime
    public ResponseEntity<Void> cleanUp(String id, String entity) {
        cleanUpService.cleanUpByEntityAndId(entity, id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
