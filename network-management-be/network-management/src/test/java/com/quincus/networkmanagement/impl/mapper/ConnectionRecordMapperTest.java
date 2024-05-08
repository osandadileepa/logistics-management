package com.quincus.networkmanagement.impl.mapper;

import com.quincus.networkmanagement.api.domain.Connection;
import com.quincus.networkmanagement.impl.attachment.connection.ConnectionRecord;
import com.quincus.networkmanagement.impl.attachment.connection.ConnectionRecordMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Spy;

import static com.quincus.networkmanagement.impl.data.NetworkManagementRecordTestData.dummyAirConnectionRecord;
import static com.quincus.networkmanagement.impl.data.NetworkManagementRecordTestData.dummyGroundConnectionRecord;
import static com.quincus.networkmanagement.impl.data.NetworkManagementTestData.dummyAirConnection;
import static com.quincus.networkmanagement.impl.data.NetworkManagementTestData.dummyGroundConnection;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;

class ConnectionRecordMapperTest {
    @Spy
    private ConnectionRecordMapper mapper = Mappers.getMapper(ConnectionRecordMapper.class);

    @Test
    @DisplayName("given ground connectionRecord when map record to domain then return expected connection")
    void returnExpectedWhenMapGroundConnectionRecordToDomain() {
        ConnectionRecord record = dummyGroundConnectionRecord();
        assertThatNoException().isThrownBy(() -> mapper.toDomain(record));
    }

    @Test
    @DisplayName("given air connectionRecord when map record to domain then return expected connection")
    void returnExpectedWhenMapAirConnectionRecordToDomain() {
        ConnectionRecord record = dummyAirConnectionRecord();
        assertThatNoException().isThrownBy(() -> mapper.toDomain(record));
    }

    @Test
    @DisplayName("given ground connection when map domain to record then return expected connection record")
    void returnExpectedWhenMapGroundConnectionToRecord() {
        Connection domain = dummyGroundConnection();
        assertThatNoException().isThrownBy(() -> mapper.toRecord(domain));
    }

    @Test
    @DisplayName("given air connection when map domain to record then return expected connection record")
    void returnExpectedWhenMapAirConnectionToRecord() {
        Connection domain = dummyAirConnection();
        assertThatNoException().isThrownBy(() -> mapper.toRecord(domain));
    }

}
