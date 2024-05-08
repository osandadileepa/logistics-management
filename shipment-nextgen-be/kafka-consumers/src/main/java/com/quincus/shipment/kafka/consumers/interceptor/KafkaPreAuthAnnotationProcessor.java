package com.quincus.shipment.kafka.consumers.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;

@Aspect
@Component
@Slf4j
public class KafkaPreAuthAnnotationProcessor {

    @Before("@annotation(com.quincus.shipment.kafka.consumers.interceptor.KafkaPreAuthentication)")
    public void initiateKafkaPreAuthentication(final JoinPoint joinPoint) {
        final MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        log.debug("Initiating Kafka pre-authentication for " + methodSignature.getName());
        RequestContextHolder.setRequestAttributes(new KafkaRequestAttribute());
    }

    @After("@annotation(com.quincus.shipment.kafka.consumers.interceptor.KafkaPreAuthentication)")
    public void concludeKafkaPreAuthentication(final JoinPoint joinPoint) {
        final MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        log.debug("Concluding Kafka pre-authentication for " + methodSignature.getName());
        RequestContextHolder.resetRequestAttributes();
    }
}
