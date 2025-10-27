package com.project.inklink.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class QueryPerformanceAspect {

    private static final Logger logger = LoggerFactory.getLogger(QueryPerformanceAspect.class);
    private static final long SLOW_QUERY_THRESHOLD = 1000; // 1 second

    @Around("execution(* com.project.inklink.repository.*.*(..))")
    public Object logQueryPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        try {
            return joinPoint.proceed();
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            String methodName = joinPoint.getSignature().getName();

            if (executionTime > SLOW_QUERY_THRESHOLD) {
                logger.warn("SLOW QUERY DETECTED: {} took {} ms", methodName, executionTime);
            } else {
                logger.debug("Query {} executed in {} ms", methodName, executionTime);
            }
        }
    }
}