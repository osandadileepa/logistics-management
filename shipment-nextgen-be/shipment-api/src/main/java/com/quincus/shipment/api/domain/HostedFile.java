package com.quincus.shipment.api.domain;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.quincus.shipment.api.deserializer.HostedFileDeserializer;
import com.quincus.shipment.api.deserializer.OffsetDateTimeDeserializer;
import com.quincus.shipment.api.serializer.OffsetDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;


@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonDeserialize(using = HostedFileDeserializer.class)
public class HostedFile {
    public static final String EXT_JPG = "jpg";
    public static final String EXT_JPEG = "jpeg";
    public static final String EXT_PNG = "png";

    private String id;
    @NotBlank
    private String fileName;
    @NotBlank
    private String fileUrl;
    private String directFileUrl;
    @NotNull
    @Min(0)
    private Long fileSize;
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    @JsonAlias({"file_timestamp", "file_updated"})
    private OffsetDateTime fileTimestamp;
}
