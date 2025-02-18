package org.mrshoffen.cloudstorage;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class SwaggerOpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() throws IOException {
        ClassPathResource resource = new ClassPathResource("openapi.yaml");
        InputStream inputStream = resource.getInputStream();
        String yaml = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        return new OpenAPIV3Parser().readContents(yaml, null, null).getOpenAPI();
    }

}
