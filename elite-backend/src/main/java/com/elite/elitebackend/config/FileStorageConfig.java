package com.elite.elitebackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FileStorageConfig implements WebMvcConfigurer {

    @Value("${upload.path:./uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Exponer la carpeta uploads como recurso estático
        String fileUploadPath = System.getProperty("user.dir") + "/uploads";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + fileUploadPath + "/");
    }
}
