package com.quincus.shipment.kafka.consumers.utility;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KakfaPayloadFieldExtractorTest {


    @Test
    void testExtractField_InvalidJson() {
        String json = "invalid-json";

        String orgId = KakfaPayloadFieldExtractor.extractField(json, "organization_id");

        assertThat(orgId).isNull();

    }

    @Test
    void testExtractField_OrgIdInRootLevel() {
        String json = "{\"organization_id\": \"test-org-id\", \"id\": \"test-order-id\"}";

        String orgId = KakfaPayloadFieldExtractor.extractField(json, "organization_id");

        assertThat(orgId).isEqualTo("test-org-id");
    }

    @Test
    void testExtractField_OrgIdInNestedNode() {
        String json = "{\"id\": \"123\", \"changes\": {\"organization_id\": \"test-org-id\", \"id\": \"test-order-id\"}}";

        String orgId = KakfaPayloadFieldExtractor.extractField(json, "organization_id");

        assertThat(orgId).isEqualTo("test-org-id");
    }

    @Test
    void testExtractField_OrgIdInNestedNodeEmpty() {
        String json = "{\"id\": \"123\", \"changes\": {\"organization_id\": \"\", \"id\": \"test-order-id\"}}";

        String orgId = KakfaPayloadFieldExtractor.extractField(json, "organization_id");

        assertThat(orgId).isEmpty();
    }
}
