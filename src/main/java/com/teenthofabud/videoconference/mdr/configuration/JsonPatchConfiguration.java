package com.teenthofabud.videoconference.mdr.configuration;

import org.springframework.boot.http.converter.autoconfigure.ServerHttpMessageConvertersCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wiring for JSON patch support.
 * <p>
 * {@link jakarta.json.JsonPatch} is a Jakarta JSON Processing type, so the Jackson converter Spring
 * Boot installs for application/json cannot build one. The converter declared here reads patch
 * documents with JSON-P instead.
 */
@Configuration
public class JsonPatchConfiguration {

    @Bean
    public JsonPatchHttpMessageConverter jsonPatchHttpMessageConverter() {
        return new JsonPatchHttpMessageConverter();
    }

    /**
     * Contributes the patch reader to the server converter stack, ahead of the Jackson converter
     * that claims application/json for every type.
     */
    @Bean
    public ServerHttpMessageConvertersCustomizer jsonPatchConvertersCustomizer(
            JsonPatchHttpMessageConverter jsonPatchHttpMessageConverter) {
        return builder -> builder.addCustomConverter(jsonPatchHttpMessageConverter);
    }
}
