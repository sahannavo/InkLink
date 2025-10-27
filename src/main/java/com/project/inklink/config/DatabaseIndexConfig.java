package com.project.inklink.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class DatabaseIndexConfig {

    // Indexes will be created through @Table and @Column annotations in entities
    // Additional performance configurations can be added here
}