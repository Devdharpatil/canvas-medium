package com.canvamedium.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration for OpenAPI documentation.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configures the OpenAPI documentation for the CanvaMedium API.
     *
     * @return Configured OpenAPI instance
     */
    @Bean
    public OpenAPI canvaMediumOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CanvaMedium API")
                        .description("RESTful API for CanvaMedium - a platform blending Canva's design experience with Medium's reading flow")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("CanvaMedium Team")
                                .email("contact@canvamedium.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Development Server")
                ));
    }
} 