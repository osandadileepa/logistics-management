package com.quincus.shipment.impl.validator;


import com.quincus.shipment.api.domain.ProofOfCost;
import com.quincus.shipment.api.exception.ProofOfCostException;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class ProofOfCostValidator {
    //TODO move this to FileName annotation
    public static final String ERR_MSG_INVALID_FILE_FORMAT_AND_FILE_SIZE = "There is an error in your file. Please check if this file is in JPG/JPEG/PNG format and not more than 24MB.";
    private static final String[] ALLOWED_FILE_FORMAT = {"png", "jpg", "jpeg", "jpe"};

    public void validate(final List<ProofOfCost> proofOfCosts) {
        Optional.ofNullable(proofOfCosts)
                .ifPresent(list -> list.stream()
                        .map(ProofOfCost::getFileName)
                        .forEach(this::validateFileFormat));
    }
    
    private void validateFileFormat(final String filename) {
        if (!FilenameUtils.isExtension(filename, Arrays.stream(ALLOWED_FILE_FORMAT).toList())) {
            throw new ProofOfCostException(ERR_MSG_INVALID_FILE_FORMAT_AND_FILE_SIZE);
        }
    }
}
