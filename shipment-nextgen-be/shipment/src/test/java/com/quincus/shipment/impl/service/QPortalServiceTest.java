package com.quincus.shipment.impl.service;

import com.quincus.qportal.api.QPortalApi;
import com.quincus.qportal.model.QPortalMilestone;
import com.quincus.qportal.model.QPortalPartner;
import com.quincus.shipment.impl.mapper.qportal.QPortalMilestoneMapper;
import com.quincus.shipment.impl.mapper.qportal.QPortalPartnerMapper;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QPortalServiceTest {
    @InjectMocks
    private QPortalService qPortalService;
    @Mock
    private QPortalApi qPortalApi;
    @Mock
    private UserDetailsProvider userDetailsProvider;
    @Mock
    private QPortalPartnerMapper qPortalPartnerMapper;
    @Mock
    private QPortalMilestoneMapper qPortalMilestoneMapper;

    @Test
    void testListPartnersV1_triggerQPortalApiGetPartner() {
        String currentUserOrgId = "org-123";
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(currentUserOrgId);
        List<QPortalPartner> qPortalPartnerList = new ArrayList<>();
        qPortalPartnerList.add(new QPortalPartner());

        when(qPortalApi.listPartners(currentUserOrgId)).thenReturn(qPortalPartnerList);
        qPortalService.listPartners();

        verify(qPortalApi, times(1)).listPartners(currentUserOrgId);
        verify(qPortalPartnerMapper, times(qPortalPartnerList.size())).toPartner(any());
    }

    @Test
    void testListMilestone_triggerQPortalApiGetMilestoneWithNoPagination() {
        String currentUserOrgId = "org-123";
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(currentUserOrgId);
        List<QPortalMilestone> qPortalMilestones = new ArrayList<>();
        qPortalMilestones.add(new QPortalMilestone());

        when(qPortalApi.listMilestones(currentUserOrgId)).thenReturn(qPortalMilestones);
        qPortalService.listMilestones();

        verify(qPortalMilestoneMapper, times(qPortalMilestones.size())).toMilestoneLookup(any());
        verify(qPortalApi, times(1)).listMilestones(currentUserOrgId);
    }

    @Test
    void testListPartnersWithPageParam_triggerQPortalApiGetPartnerV2WithPageParams() {
        String currentUserOrgId = "org-123";
        int perPage = 10;
        int page = 1;
        String key = "test";
        String userId = UUID.randomUUID().toString();
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(currentUserOrgId);
        when(userDetailsProvider.getCurrentUserId()).thenReturn(userId);
        List<QPortalPartner> qPortalPartnerList = new ArrayList<>();
        qPortalPartnerList.add(new QPortalPartner());

        when(qPortalApi.listPartnersWithSearchAndPagination(currentUserOrgId, userId, perPage, page, key)).thenReturn(qPortalPartnerList);
        qPortalService.listPartnersWithSearchAndPagination(perPage, page, key);

        verify(qPortalApi, times(1)).listPartnersWithSearchAndPagination(currentUserOrgId, userId, perPage, page, key);
        verify(qPortalPartnerMapper, times(qPortalPartnerList.size())).toPartner(any());
    }
}