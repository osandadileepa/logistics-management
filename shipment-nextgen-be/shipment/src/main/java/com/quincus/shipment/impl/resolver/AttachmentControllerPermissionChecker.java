package com.quincus.shipment.impl.resolver;

import lombok.AllArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.quincus.shipment.api.constant.AttachmentType.MILESTONE;
import static com.quincus.shipment.api.constant.AttachmentType.NETWORK_LANE;
import static com.quincus.shipment.api.constant.AttachmentType.PACKAGE_JOURNEY_AIR_SEGMENT;

@Component
@AllArgsConstructor
public class AttachmentControllerPermissionChecker {
    private static final String MILESTONE_SHIPMENT_STATUS_VIEW_AUTHORITY = "SHIPMENT_STATUS_VIEW";

    //TODO add authorities for NETWORK_LANE, PACKAGE_JOURNEY_AIR_SEGMENT
    private static final Map<String, String> AUTHORITY_BY_ATTACHMENT_TYPE = Map.of(
            MILESTONE.getValue(), MILESTONE_SHIPMENT_STATUS_VIEW_AUTHORITY
    );
    private final UserDetailsProvider userDetailsProvider;

    public boolean hasDownloadCsvTemplatePermission(String attachmentType) {
        return isUnrestrictedAttachmentType(attachmentType)
                || userDetailsProvider.getQuincusUserDetails().getAuthorities().contains(new SimpleGrantedAuthority(AUTHORITY_BY_ATTACHMENT_TYPE.getOrDefault(attachmentType, "")));
    }

    private boolean isUnrestrictedAttachmentType(String attachmentType) {
        return PACKAGE_JOURNEY_AIR_SEGMENT.getValue().equals(attachmentType) || NETWORK_LANE.getValue().equals(attachmentType);
    }
}
