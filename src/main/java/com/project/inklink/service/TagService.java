package com.project.inklink.service;

import com.project.inklink.entity.Tag;
import com.project.inklink.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    public List<Map<String, Object>> getPopularTags(int limit) {
        // Use the fixed repository method
        Pageable pageable = PageRequest.of(0, limit);

        // Option 1: Use the Page<Tag> version
        List<Tag> tags = tagRepository.findPopularTags(pageable).getContent();

        // Option 2: Or use the simpler List<Tag> version
        // List<Tag> tags = tagRepository.findTopPopularTags(pageable);

        return tags.stream().map(tag -> {
            Map<String, Object> tagMap = new HashMap<>();
            tagMap.put("name", tag.getName());
            tagMap.put("storyCount", tag.getStories() != null ? tag.getStories().size() : 0);
            return tagMap;
        }).collect(Collectors.toList());
    }
}