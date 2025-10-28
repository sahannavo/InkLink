package com.project.inklink.service;

import com.project.inklink.entity.Story;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);

    private final ConcurrentHashMap<String, AtomicLong> storyViewCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> searchQueryCounts = new ConcurrentHashMap<>();

    public void logStoryView(Story story) {
        String storyKey = "story_" + story.getId();
        storyViewCounts.computeIfAbsent(storyKey, k -> new AtomicLong(0)).incrementAndGet();

        logger.info("Story viewed: ID={}, Title={}, Views={}",
                story.getId(), story.getTitle(), storyViewCounts.get(storyKey).get());
    }

    public void logStoryCreation(Story story) {
        logger.info("Story created: ID={}, Title={}, Author={}",
                story.getId(), story.getTitle(), story.getAuthor().getUsername());
    }

    public void logStoryUpdate(Story story, String originalTitle) {
        logger.info("Story updated: ID={}, OriginalTitle={}, NewTitle={}",
                story.getId(), originalTitle, story.getTitle());
    }

    public void logStoryPublication(Story story) {
        logger.info("Story published: ID={}, Title={}, PublishedAt={}",
                story.getId(), story.getTitle(), LocalDateTime.now());
    }

    public void logStoryDeletion(Story story) {
        logger.info("Story deleted: ID={}, Title={}, Author={}",
                story.getId(), story.getTitle(), story.getAuthor().getUsername());
    }

    public void logSearchQuery(String query) {
        searchQueryCounts.computeIfAbsent(query.toLowerCase(), k -> new AtomicLong(0)).incrementAndGet();

        logger.debug("Search query executed: '{}', Count={}",
                query, searchQueryCounts.get(query.toLowerCase()).get());
    }

    public Long getStoryViewCount(Long storyId) {
        String storyKey = "story_" + storyId;
        AtomicLong count = storyViewCounts.get(storyKey);
        return count != null ? count.get() : 0L;
    }

    public Long getSearchQueryCount(String query) {
        AtomicLong count = searchQueryCounts.get(query.toLowerCase());
        return count != null ? count.get() : 0L;
    }

    public ConcurrentHashMap<String, AtomicLong> getPopularSearches(int limit) {
        return new ConcurrentHashMap<>(
                searchQueryCounts.entrySet().stream()
                        .sorted((e1, e2) -> Long.compare(e2.getValue().get(), e1.getValue().get()))
                        .limit(limit)
                        .collect(ConcurrentHashMap::new,
                                (map, entry) -> map.put(entry.getKey(), entry.getValue()),
                                ConcurrentHashMap::putAll)
        );
    }
}