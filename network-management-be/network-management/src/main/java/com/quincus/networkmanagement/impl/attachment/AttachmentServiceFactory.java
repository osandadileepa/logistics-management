package com.quincus.networkmanagement.impl.attachment;

import com.quincus.networkmanagement.api.constant.AttachmentType;
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
}

