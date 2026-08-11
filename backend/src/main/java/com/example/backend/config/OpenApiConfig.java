package com.example.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * Adds a JWT "Authorize" button to Swagger UI. After calling
 * POST /api/auth/login there, copy the returned token, click Authorize,
 * paste it in (no need to type "Bearer " - Swagger adds that prefix), and
 * every protected endpoint tried from the UI will send it automatically.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI courseEnrolmentOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Course Enrolment API")
                        .description("Authentication endpoints for the Course Enrolment System capstone project")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME_NAME, new SecurityScheme()
                                .name(BEARER_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
