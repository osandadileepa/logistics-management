package com.quincus.shipment.impl.attachment;

import com.quincus.shipment.api.constant.AttachmentType;
import com.quincus.web.common.exception.model.QuincusValidationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class AttachmentServiceFactory {
    private final Map<AttachmentType, AbstractAttachmentService<?>> attachmentServiceMap;

    public AttachmentServiceFactory(List<AbstractAttachmentService<?>> abstractAttachmentServiceList) {
        this.attachmentServiceMap = abstractAttachmentServiceList.stream().collect(Collectors.toUnmodifiableMap(
                AbstractAttachmentService::getAttachmentType, Function.identity()
        ));
    }

    public <T extends AbstractAttachmentService<?>> T getAttachmentServiceByType(AttachmentType attachmentType) {
        return (T) attachmentServiceMap.get(attachmentType);
    }

    public <T extends ExportableAttachmentService> T getExportableAttachmentServiceByType(AttachmentType attachmentType) {
        AbstractAttachmentService<?> service = attachmentServiceMap.get(attachmentType);
        if (!(service instanceof ExportableAttachmentService)) {
            throw new QuincusValidationException("The service for " + attachmentType + " is not exportable.");
        }
        return (T) service;
    }

}
