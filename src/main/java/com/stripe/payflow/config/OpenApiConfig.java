package com.stripe.payflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI payFlowOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("PayFlow API")
                        .description("PayFlow Double-Entry Ledger and Wallet System API Documentation")
                        .version("v1.0.0")
                        .contact(new Contact().name("Risk Engineering Team").email("risk@stripe.payflow.com"))
                        .license(new License().name("Internal Use Only").url("https://payflow.internal/license")));
    }
}
