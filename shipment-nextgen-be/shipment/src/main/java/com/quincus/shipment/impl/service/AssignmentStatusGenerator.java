package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.constant.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class AssignmentStatusGenerator {

    public String generateAssignmentStatusByBookingStatus(BookingStatus bookingStatus) {
        switch (bookingStatus) {
            case CANCELLED -> {
                return null;
            }
            case PENDING -> {
                return "Pending";
            }
            case CONFIRMED -> {
                return "Confirmed";
            }
            case REJECTED -> {
                return "Rejected";
            }
            case FAILED -> {
                return "System Error";
            }
            default -> {
                return Strings.EMPTY;
            }
        }
    }

}
