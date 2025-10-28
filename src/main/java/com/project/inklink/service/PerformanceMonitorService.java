package com.project.inklink.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class PerformanceMonitorService {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitorService.class);

    private final ConcurrentHashMap<String, AtomicLong> endpointCallCounts = new ConcurrentHashMap<>();
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong totalErrors = new AtomicLong(0);

    public void recordEndpointCall(String endpoint) {
        endpointCallCounts.computeIfAbsent(endpoint, k -> new AtomicLong(0)).incrementAndGet();
        totalRequests.incrementAndGet();
    }

    public void recordError(String endpoint) {
        totalErrors.incrementAndGet();
        logger.warn("Error recorded for endpoint: {}", endpoint);
    }

    @Scheduled(fixedRate = 60000) // Run every minute
    public void logPerformanceMetrics() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        long heapUsed = memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
        long heapMax = memoryBean.getHeapMemoryUsage().getMax() / (1024 * 1024);
        int activeThreads = threadBean.getThreadCount();
        int peakThreads = threadBean.getPeakThreadCount();

        logger.info("=== PERFORMANCE METRICS ===");
        logger.info("Total Requests: {}", totalRequests.get());
        logger.info("Total Errors: {}", totalErrors.get());
        logger.info("Heap Memory: {} MB / {} MB", heapUsed, heapMax);
        logger.info("Active Threads: {}, Peak: {}", activeThreads, peakThreads);

        // Log top 5 endpoints
        logger.info("--- Top Endpoints ---");
        endpointCallCounts.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue().get(), e1.getValue().get()))
                .limit(5)
                .forEach(entry ->
                        logger.info("{} - {} calls", entry.getKey(), entry.getValue().get()));

        logger.info("======================");
    }

    public ConcurrentHashMap<String, AtomicLong> getEndpointStatistics() {
        return new ConcurrentHashMap<>(endpointCallCounts);
    }

    public long getTotalRequests() {
        return totalRequests.get();
    }

    public long getTotalErrors() {
        return totalErrors.get();
    }

    public double getErrorRate() {
        long requests = totalRequests.get();
        long errors = totalErrors.get();
        return requests > 0 ? (double) errors / requests * 100 : 0.0;
    }
}