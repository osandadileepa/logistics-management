package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.impl.repository.entity.PartnerEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.Tuple;

import static com.quincus.shipment.impl.repository.constant.NetworkLaneSegmentTupleAlias.PARTNER_CODE;
import static com.quincus.shipment.impl.repository.constant.NetworkLaneSegmentTupleAlias.PARTNER_ID;
import static com.quincus.shipment.impl.repository.constant.NetworkLaneSegmentTupleAlias.PARTNER_NAME;
import static com.quincus.shipment.impl.repository.constant.NetworkLaneSegmentTupleAlias.PARTNER_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NetworkLaneSegmentPartnerTupleMapperTest {
    private final NetworkLaneSegmentPartnerTupleMapper networkLaneSegmentPartnerTupleMapper = new NetworkLaneSegmentPartnerTupleMapper();

    @Test
    void giveTuple_whenValuePresent_thenMapToPartner() {
        Tuple tuple = Mockito.mock(Tuple.class);
        when(tuple.get(PARTNER_ID, String.class)).thenReturn("partner-id");
        when(tuple.get(PARTNER_CODE, String.class)).thenReturn("partner-code");
        when(tuple.get(PARTNER_TYPE, String.class)).thenReturn("partner-type");
        when(tuple.get(PARTNER_NAME, String.class)).thenReturn("partner-name");

        PartnerEntity entity = networkLaneSegmentPartnerTupleMapper.toEntity(tuple);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNotBlank().isEqualTo("partner-id");
        assertThat(entity.getName()).isNotBlank().isEqualTo("partner-name");
        assertThat(entity.getPartnerType()).isNotBlank().isEqualTo("partner-type");
        assertThat(entity.getCode()).isNotBlank().isEqualTo("partner-code");
    }
}
