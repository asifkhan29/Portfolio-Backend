package com.student.portfolio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

	  @Bean
	    public OpenAPI customOpenAPI() {
	        return new OpenAPI()
	                .info(new Info().title("Portfolio").version("1.0").description("this is portfolio  project"))
	                .components(new Components()
	                        .addSecuritySchemes("apiKeyAuth", new SecurityScheme()
	                                .type(SecurityScheme.Type.APIKEY)
	                                .name("Authorization")
	                                .in(SecurityScheme.In.HEADER)))
	           
	                .addSecurityItem(new SecurityRequirement().addList("apiKeyAuth"));
	    }
}