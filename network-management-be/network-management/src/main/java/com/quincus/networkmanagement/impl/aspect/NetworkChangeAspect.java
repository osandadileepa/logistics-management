package com.quincus.networkmanagement.impl.aspect;

import com.quincus.networkmanagement.impl.service.MmeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@AllArgsConstructor
public class NetworkChangeAspect {
    private final MmeService mmeService;

    @After("@annotation(com.quincus.networkmanagement.impl.annotation.NetworkChange)")
    public void trainModelOnNetworkChange() {
        log.debug("Captured change in network.");
        mmeService.trainModelWithDelay();
    }
}
