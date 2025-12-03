package com.prokey.baccaratio.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class CorsConfiguration implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        
        String corsOriginEnv = System.getenv("CORS_ORIGIN");

        
        String[] defaultOrigins = {
                "http://www.baccaratio.devma.de",
                "https://www.baccaratio.devma.de",
                "http://baccaratio.devma.de",
                "https://baccaratio.devma.de",
                "http://www.baccaratio.aporkolab.com",
                "https://www.baccaratio.aporkolab.com",
                "http://baccaratio.aporkolab.com",
                "https://baccaratio.aporkolab.com",
                "http://localhost:4200"
        };

        
        Set<String> allowedOriginsSet = new HashSet<>(Arrays.asList(defaultOrigins));

        
        if (corsOriginEnv != null && !corsOriginEnv.isEmpty()) {
            allowedOriginsSet.addAll(Arrays.asList(corsOriginEnv.split(",")));
        }

        
        registry.addMapping("/**")
                .allowedOrigins(allowedOriginsSet.toArray(new String[0])) 
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)
                .allowedHeaders("*")
                .maxAge(3600);
    }
}