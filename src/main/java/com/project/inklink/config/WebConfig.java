package com.project.inklink.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve static files from frontend directory
        registry.addResourceHandler("/**")
                .addResourceLocations(
                        "classpath:/frontend/",
                        "classpath:/static/",
                        "classpath:/public/",
                        "file:./frontend/"
                )
                .setCachePeriod(3600);

        // Ensure HTML files are served correctly
        registry.addResourceHandler("*.html")
                .addResourceLocations("classpath:/frontend/");
    }
}
