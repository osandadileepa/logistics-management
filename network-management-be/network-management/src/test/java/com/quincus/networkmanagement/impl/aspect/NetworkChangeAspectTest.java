package com.quincus.networkmanagement.impl.aspect;

import com.quincus.networkmanagement.impl.service.MmeService;
import com.quincus.networkmanagement.impl.utility.DebounceUtility;
import com.quincus.web.common.multitenant.UserDetailsContextHolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NetworkChangeAspectTest {
    @Mock
    private MmeService mmeService;
    @Mock
    private UserDetailsContextHolder userDetailsContextHolder;
    @Mock
    private DebounceUtility debounceUtility;

    @InjectMocks
    private NetworkChangeAspect networkChangeAspect;

    @Test
    void testTrainModelIsTriggeredOnAspect() {
        networkChangeAspect.trainModelOnNetworkChange();
        verify(mmeService, times(1)).trainModelWithDelay();
    }
}
