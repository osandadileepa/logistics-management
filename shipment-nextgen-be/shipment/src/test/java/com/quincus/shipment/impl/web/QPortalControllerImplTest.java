package com.quincus.shipment.impl.web;

import com.quincus.shipment.impl.service.QPortalService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QPortalControllerImplTest {
    @InjectMocks
    QPortalControllerImpl qPortalControllerImpl;
    @Mock
    QPortalService qPortalService;

    @Test
    void listAllPackageTypes_shouldReturnResult() {
        when(qPortalService.listPackageTypes()).thenReturn(new ArrayList<>());
        assertThat(qPortalControllerImpl.listPackageTypes()).isNotNull();
    }

    @Test
    void listAllPartners_shouldReturnResult() {
        when(qPortalService.listPartners()).thenReturn(new ArrayList<>());
        assertThat(qPortalControllerImpl.listPartners()).isNotNull();
    }

    @Test
    void listPartnersWithPageParam_shouldReturnResult() {
        int perPage = 10;
        int page = 1;
        when(qPortalService.listPartnersWithSearchAndPagination(perPage, page, null)).thenReturn(new ArrayList<>());
        assertThat(qPortalControllerImpl.listPartners(perPage, page, null)).isNotNull();
    }

    @Test
    void listMilestoneCodes_shouldReturnResult() {
        when(qPortalService.listMilestoneCodes()).thenReturn(new ArrayList<>());
        assertThat(qPortalControllerImpl.listMilestoneCodes()).isNotNull();
    }

    @Test
    void listMilestonesWithPageParam_ShouldReturnResult() {
        int perPage = 10;
        int page = 1;
        when(qPortalService.listMilestonesWithSearchAndPagination(perPage, page, null)).thenReturn(new ArrayList<>());
        assertThat(qPortalControllerImpl.listMilestones(perPage, page, null)).isNotNull();
    }
}
