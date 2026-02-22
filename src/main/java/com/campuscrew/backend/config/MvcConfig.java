package com.campuscrew.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // This tells Spring: "If a webpage asks for an image starting with /uploads/, 
        // go look in the 'uploads' folder on the physical hard drive."
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
