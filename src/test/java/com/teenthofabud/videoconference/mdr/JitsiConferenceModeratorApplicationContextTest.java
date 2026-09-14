package com.teenthofabud.videoconference.mdr;

import tools.jackson.databind.ObjectMapper;
import jakarta.json.JsonPatch;
import com.teenthofabud.videoconference.mdr.conference.service.GoogleConferenceService;
import com.teenthofabud.videoconference.mdr.configuration.JsonPatchHttpMessageConverter;
import com.teenthofabud.videoconference.mdr.server.service.ServerService;
import com.teenthofabud.videoconference.mdr.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Loads the real application context to prove the wiring the slice tests cannot reach: that the
 * beans every service needs actually resolve, and that the JsonPatch converter really is registered
 * ahead of the Jackson 3 one.
 * <p>
 * The datasource and Auth0 settings are stubbed rather than live. Hibernate is told not to read
 * JDBC metadata and Hikari not to fail when it cannot open a connection, so the context starts
 * without a database; nothing here touches one.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "MODERATOR_DB_HOST=localhost",
        "MODERATOR_DB_PORT=5432",
        "MODERATOR_DB_USERNAME=unused",
        "MODERATOR_DB_PASSWORD=unused",
        "MODERATOR_DB_DRIVER_CLASS=org.postgresql.Driver",
        "MODERATOR_DB_DIALECT=org.hibernate.dialect.PostgreSQLDialect",
        "MODERATOR_HIKARI_MAX_POOL_SIZE=1",
        "MODERATOR_HIKARI_MIN_IDLE_COUNT=0",
        "AUTH0_DOMAIN=example.eu.auth0.com",
        "AUTH0_API_IDENTIFIER=https://example.test/api",
        "spring.datasource.hikari.initialization-fail-timeout=-1",
        "spring.jpa.properties.hibernate.boot.allow_jdbc_metadata_access=false",
        "spring.jpa.properties.hibernate.temp.use_jdbc_metadata_defaults=false"
})
class JitsiConferenceModeratorApplicationContextTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private RequestMappingHandlerAdapter requestMappingHandlerAdapter;

    @Test
    @DisplayName("the context starts and every service resolves its dependencies")
    void contextLoads() {
        assertThat(applicationContext.getBean(UserService.class)).isNotNull();
        assertThat(applicationContext.getBean(ServerService.class)).isNotNull();
        assertThat(applicationContext.getBean(GoogleConferenceService.class)).isNotNull();
    }

    @Test
    @DisplayName("the services are wired to the Jackson 3 mapper Spring Boot auto-configures")
    void servicesUseTheAutoConfiguredJackson3Mapper() {
        assertThat(applicationContext.getBeanNamesForType(ObjectMapper.class)).containsExactly("jacksonJsonMapper");
    }

    @Test
    @DisplayName("no Jackson 2 ObjectMapper bean is defined any more")
    void noJackson2ObjectMapperBeanRemains() {
        assertThat(applicationContext.getBeanNamesForType(com.fasterxml.jackson.databind.ObjectMapper.class)).isEmpty();
    }

    @Test
    @DisplayName("a JSON-P provider is on the classpath, so patch documents can be parsed at all")
    void jsonProcessingProviderIsAvailable() {
        assertThat(jakarta.json.Json.createPatch(jakarta.json.JsonValue.EMPTY_JSON_ARRAY)).isNotNull();
    }

    @Test
    @DisplayName("the patch converter is registered ahead of the Jackson 3 converter")
    void jsonPatchConverterIsRegisteredFirst() {
        List<HttpMessageConverter<?>> converters = requestMappingHandlerAdapter.getMessageConverters();

        assertThat(converters).isNotEmpty();
        assertThat(converters.getFirst()).isInstanceOf(JsonPatchHttpMessageConverter.class);
    }

    @Test
    @DisplayName("the first converter that claims a JsonPatch body is the one that can build it")
    void jsonPatchBodyIsClaimedByTheJackson2BackedConverter() {
        HttpMessageConverter<?> claiming = requestMappingHandlerAdapter.getMessageConverters()
                .stream()
                .filter(converter -> converter.canRead(JsonPatch.class, MediaType.APPLICATION_JSON))
                .findFirst()
                .orElseThrow();

        assertThat(claiming).isInstanceOf(JsonPatchHttpMessageConverter.class);
    }

    @Test
    @DisplayName("ordinary request bodies are left to the default converters")
    void ordinaryBodiesAreNotClaimedByThePatchConverter() {
        HttpMessageConverter<?> claiming = requestMappingHandlerAdapter.getMessageConverters()
                .stream()
                .filter(converter -> converter.canRead(String.class, MediaType.APPLICATION_JSON))
                .findFirst()
                .orElseThrow();

        assertThat(claiming).isNotInstanceOf(JsonPatchHttpMessageConverter.class);
    }
}
