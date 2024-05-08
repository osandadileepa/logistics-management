package com.quincus.shipment.impl.attachment;

import com.quincus.shipment.api.filter.ExportFilter;

import java.io.Writer;

public interface ExportableAttachmentService {
    void export(ExportFilter exportFilter, Writer writer);
}
