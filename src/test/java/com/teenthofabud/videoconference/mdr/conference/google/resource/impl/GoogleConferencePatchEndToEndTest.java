package com.teenthofabud.videoconference.mdr.conference.google.resource.impl;

import jakarta.json.JsonObject;
import jakarta.json.JsonPatch;
import com.teenthofabud.videoconference.mdr.conference.constants.ConferenceStatus;
import com.teenthofabud.videoconference.mdr.conference.google.entity.GoogleConferenceEntity;
import com.teenthofabud.videoconference.mdr.conference.google.service.GoogleConferenceService;
import com.teenthofabud.videoconference.mdr.constants.HttpMediaType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.teenthofabud.videoconference.mdr.support.JsonTestSupport.PatchOperation.operation;
import static com.teenthofabud.videoconference.mdr.support.JsonTestSupport.PatchOperation.replace;
import static com.teenthofabud.videoconference.mdr.support.JsonTestSupport.json;
import static com.teenthofabud.videoconference.mdr.support.JsonTestSupport.jsonObject;
import static com.teenthofabud.videoconference.mdr.support.JsonTestSupport.patchBody;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Drives PATCH through the fully wired application, so the JsonPatch binding is proven against the
 * converter stack Spring Boot actually assembles rather than against a hand built one. Security
 * filters are disabled because the subject here is body binding, not authentication.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
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
class GoogleConferencePatchEndToEndTest {

    private static final Long USER_ID = 1L;
    private static final String EVENT_ID = "evt-1";
    private static final String PATCH_BODY = patchBody(replace("/status", "CANCELLED"));

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GoogleConferenceService googleConferenceService;

    @Test
    @DisplayName("a patch sent as application/json is bound and answered with 204")
    void patchIsBoundOverApplicationJson() throws Exception {
        mockMvc.perform(patch("/api/google/conferences/events/" + EVENT_ID)
                        .param("userId", USER_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PATCH_BODY))
                .andExpect(status().isNoContent());

        verify(googleConferenceService)
                .patchGoogleConferenceByUserIdAndEventId(eq(USER_ID), eq(EVENT_ID), org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("a patch sent as application/json-patch+json is bound and answered with 204")
    void patchIsBoundOverTheJsonPatchMediaType() throws Exception {
        mockMvc.perform(patch("/api/google/conferences/events/" + EVENT_ID)
                        .param("userId", USER_ID.toString())
                        .contentType(MediaType.valueOf(HttpMediaType.APPLICATION_JSON_PATCH))
                        .content(PATCH_BODY))
                .andExpect(status().isNoContent());

        verify(googleConferenceService)
                .patchGoogleConferenceByUserIdAndEventId(eq(USER_ID), eq(EVENT_ID), org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("the bound patch carries the operation the client sent")
    void boundPatchCarriesTheOperation() throws Exception {
        mockMvc.perform(patch("/api/google/conferences/events/" + EVENT_ID)
                        .param("userId", USER_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PATCH_BODY))
                .andExpect(status().isNoContent());

        org.mockito.ArgumentCaptor<JsonPatch> captor = org.mockito.ArgumentCaptor.forClass(JsonPatch.class);
        verify(googleConferenceService)
                .patchGoogleConferenceByUserIdAndEventId(eq(USER_ID), eq(EVENT_ID), captor.capture());

        JsonObject before = jsonObject(GoogleConferenceEntity.builder().status(ConferenceStatus.PENDING).version(0L)
                    .lastModifiedDate(java.time.Instant.parse("2026-01-02T03:04:05Z")).build());
        assertThat(captor.getValue().apply(before).getString("status")).isEqualTo("CANCELLED");
    }

    @Test
    @DisplayName("the OpenAPI document still renders after the Jackson 2 mapper bean was removed")
    void openApiDocumentStillRenders() throws Exception {
        // springdoc and swagger-core are Jackson 2 based and were candidates to have been relying
        // on the application's own ObjectMapper bean
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.paths['/api/google/conferences/events/{eventId}'].patch").exists());
    }

    @Test
    @DisplayName("a malformed patch document is answered with 400 MDR-002, not 500")
    void malformedPatchIsABadRequest() throws Exception {
        mockMvc.perform(patch("/api/google/conferences/events/" + EVENT_ID)
                        .param("userId", USER_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(operation("replace", null))))
                .andExpect(status().isBadRequest());
    }
}
