package com.project.inklink.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String DEV_SERVER_DESCRIPTION = "Development Server";
    private static final String PROD_SERVER_DESCRIPTION = "Production Server";
    private static final String MIT_LICENSE_URL = "https://choosealicense.com/licenses/mit/";

    @Value("${app.openapi.dev-url:http://localhost:8080}")
    private String devUrl;

    @Value("${app.openapi.prod-url:https://api.inklink.com}")
    private String prodUrl;

    @Value("${app.openapi.enable-prod-server:false}")
    private boolean enableProdServer;

    @Value("${app.version:v1.0.0}")
    private String appVersion;

    @Bean
    public OpenAPI inkLinkOpenAPI() {
        return new OpenAPI()
                .info(buildApiInfo())
                .servers(buildServers());
    }

    private Info buildApiInfo() {
        return new Info()
                .title("InkLink API")
                .version(appVersion)
                .description("""
                        Story Sharing Platform API Documentation
                        
                        This API provides endpoints for managing stories, user profiles, 
                        comments, and interactions on the InkLink platform.
                        
                        ## Features
                        - User authentication and authorization
                        - Story creation, reading, updating, and deletion
                        - Commenting system
                        - User profile management
                        - Search and filtering capabilities
                        """)
                .contact(buildContactInfo())
                .license(buildLicenseInfo())
                .termsOfService("https://inklink.com/terms");
    }

    private Contact buildContactInfo() {
        return new Contact()
                .name("InkLink Support Team")
                .email("support@inklink.com")
                .url("https://inklink.com/contact")
                .extensions(java.util.Map.of(
                        "supportHours", "Mon-Fri 9:00-17:00 EST",
                        "responseTime", "Within 24 hours"
                ));
    }

    private License buildLicenseInfo() {
        return new License()
                .name("MIT License")
                .url(MIT_LICENSE_URL);
    }

    private List<Server> buildServers() {
        Server devServer = new Server()
                .url(devUrl)
                .description(DEV_SERVER_DESCRIPTION)
                .extensions(java.util.Map.of(
                        "environment", "development",
                        "status", "active"
                ));

        if (enableProdServer) {
            Server prodServer = new Server()
                    .url(prodUrl)
                    .description(PROD_SERVER_DESCRIPTION)
                    .extensions(java.util.Map.of(
                            "environment", "production",
                            "status", "active"
                    ));
            return List.of(prodServer, devServer);
        }

        return List.of(devServer);
    }
}