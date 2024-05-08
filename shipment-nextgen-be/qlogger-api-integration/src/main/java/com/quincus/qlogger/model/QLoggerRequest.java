package com.quincus.qlogger.model;

import com.quincus.shipment.api.domain.Package;
import com.quincus.shipment.api.domain.PackageDimension;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import lombok.Data;

import java.util.List;

@Data
public class QLoggerRequest {
    //common fields defined in https://quincus.atlassian.net/wiki/spaces/AD/pages/1074266113/qLogger+events+categories+definitions#Common-fields
    private String module;
    private String source;
    private String actorId;
    private String actorName;
    private String organizationId;
    private String organizationKey;
    private String occuredAt;
    private String reportedAt;
    private String category;
    private String version;
    private String customData;
    private String clientVersion;

    //See https://quincus.atlassian.net/wiki/spaces/AD/pages/1123418162/qLogger+categories+definitions+Shipment
    private Shipment shipmentAttribute;

    //see https://quincus.atlassian.net/wiki/spaces/AD/pages/1123418162/qLogger+categories+definitions+Shipment#The-shipment_exported-category
    private String shipmentCsvFile;

    private ShipmentJourney segmentJourneyPreviousAttributes;
    private ShipmentJourney segmentJourneyNewAttributes;
    private List<Package> packageAttributes;
    private List<PackageDimension> previousDimensions;
    private List<PackageDimension> newDimensions;
    private PackageJourneySegment oldPackageJourneySegment;
    private PackageJourneySegment newPackageJourneySegment;
}
