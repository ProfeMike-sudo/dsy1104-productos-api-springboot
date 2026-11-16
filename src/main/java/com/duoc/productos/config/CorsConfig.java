package com.duoc.productos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// CORS: el navegador deja leer la respuesta solo desde los orígenes exactos de cors.origenes.
// No autentica usuarios ni reemplaza reglas de acceso.
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${cors.origenes}")
    private String[] origenes;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(origenes)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}
