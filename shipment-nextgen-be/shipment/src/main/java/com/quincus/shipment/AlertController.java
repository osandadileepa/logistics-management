package com.quincus.shipment;

import com.quincus.ext.annotation.UUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

@RequestMapping("/alerts")
@Tag(name = "alerts", description = "This endpoint allows to manage alert related transactions.")
@Validated
public interface AlertController {

    @PatchMapping("/{alertId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @Operation(summary = "Dismiss Shipment Alert API", description = "Dismiss/undo dismiss a shipment alert.", tags = "shipments")
    void dismissAlert(@UUID @PathVariable("alertId") final String alertId,
                      @RequestParam final boolean dismissed);

}
