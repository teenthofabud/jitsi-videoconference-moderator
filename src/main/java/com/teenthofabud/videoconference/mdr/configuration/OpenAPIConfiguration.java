package com.teenthofabud.videoconference.mdr.configuration;

import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;


@Configuration
public class OpenAPIConfiguration {

    @Value("${spring.application.version}")
    private String version;

    @Value("${spring.application.name}")
    private String name;

    @Value("${spring.application.description}")
    private String description;

    @Bean
    public OpenAPI springOpenAPI() {

        return new OpenAPI()
                .info(
                        new Info()
                                .title(name)
                                .contact(contactBuilder())
                                .description(description)
                                .version(version));
    }

    private Contact contactBuilder() {
        Contact contact = new Contact();
        contact.setEmail("anirban.das@t-online.de");
        contact.setName("Anirban Das");
        return contact;
    }

    @Bean
    public GroupedOpenApi groupedOpenApi() {
        String[] paths = {"/**"};
        String[] packagedToMatch = {
                "com.teenthofabud.videoconference.mdr.conference",
                "com.teenthofabud.videoconference.mdr.user",
                "com.teenthofabud.videoconference.mdr.server"

        };
        return GroupedOpenApi
                .builder()
                .group("MDR API")
                .pathsToMatch(paths)
                .packagesToScan(packagedToMatch)
                .addOpenApiCustomizer(openApi ->  {
                    Info info = openApi.getInfo();
                    info.setDescription(description);
                }).build();
    }

}