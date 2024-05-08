package com.quincus.shipment.api.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.quincus.ext.DateTimeUtil;
import com.quincus.shipment.api.domain.HostedFile;

import java.io.IOException;

import static java.util.Objects.nonNull;

public class HostedFileDeserializer extends StdScalarDeserializer<HostedFile> {

    public HostedFileDeserializer() {
        super(HostedFile.class);
    }

    @Override
    public HostedFile deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        HostedFile hostedFile = new HostedFile();
        //TODO: For backwards compatibility. Remove when deploying to prod.
        if (node.isTextual()) {
            hostedFile.setFileUrl(node.asText());
            return hostedFile;
        }
        JsonNode id = node.get("id");
        if (id != null) {
            hostedFile.setId(id.asText());
        }
        JsonNode fileName = node.get("file_name");
        if (fileName != null) {
            hostedFile.setFileName(fileName.asText());
        }
        JsonNode fileUrl = node.get("file_url");
        if (fileUrl != null) {
            hostedFile.setFileUrl(fileUrl.asText());
        }
        JsonNode fileSize = node.get("file_size");
        if (fileSize != null) {
            hostedFile.setFileSize(fileSize.asLong());
        }
        JsonNode fileTimestamp = nonNull(node.get("file_updated"))?node.get("file_updated"):node.get("file_timestamp");
        if (fileTimestamp != null) {
            hostedFile.setFileTimestamp(DateTimeUtil.toFormattedOffsetDateTime(fileTimestamp.asText()));
        }
        return hostedFile;
    }
}
