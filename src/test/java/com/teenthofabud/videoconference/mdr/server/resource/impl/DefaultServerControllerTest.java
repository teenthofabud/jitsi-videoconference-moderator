package com.teenthofabud.videoconference.mdr.server.resource.impl;

import com.teenthofabud.videoconference.mdr.error.core.MDRErrorCode;
import com.teenthofabud.videoconference.mdr.server.constants.ServerType;
import com.teenthofabud.videoconference.mdr.server.error.ServerException;
import com.teenthofabud.videoconference.mdr.server.form.DefaultServerForm;
import com.teenthofabud.videoconference.mdr.server.form.DefaultServerUpdateForm;
import com.teenthofabud.videoconference.mdr.server.form.ServerPreferenceUpdateForm;
import com.teenthofabud.videoconference.mdr.server.service.ServerService;
import com.teenthofabud.videoconference.mdr.server.vo.ServerVo;
import com.teenthofabud.videoconference.mdr.support.MockMvcTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static com.teenthofabud.videoconference.mdr.support.JsonTestSupport.json;
import static com.teenthofabud.videoconference.mdr.support.JsonTestSupport.truncated;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HTTP level edge cases for creating and reading default servers.
 */
@ExtendWith(MockitoExtension.class)
class DefaultServerControllerTest {

    private static final String BASE = "/api/servers";

    @Mock
    private ServerService serverService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ServerController controller = new ServerController();
        ReflectionTestUtils.setField(controller, "serverService", serverService);
        mockMvc = MockMvcTestSupport.standaloneMockMvc(controller);
    }

    private ServerVo defaultVo(Long id, String name, String url) {
        return ServerVo.builder().id(id).name(name).serverUrl(url).serverType(ServerType.DEFAULT)
                .version(0L).lastModifiedDate(Instant.parse("2026-01-02T03:04:05Z")).build();
    }

    private DefaultServerForm createForm(String name, String url) {
        DefaultServerForm form = new DefaultServerForm();
        form.setName(name);
        form.setServerUrl(url);
        return form;
    }

    private DefaultServerUpdateForm updateForm(Long serverId, String name, String url) {
        DefaultServerUpdateForm form = new DefaultServerUpdateForm();
        form.setServerId(serverId);
        form.setName(name);
        form.setServerUrl(url);
        return form;
    }

    @Nested
    @DisplayName("POST /api/servers/default")
    class Create {

        @Test
        @DisplayName("a default server is created with 201 and a Location header")
        void createsDefaultServer() throws Exception {
            when(serverService.createDefaultServer(any(DefaultServerForm.class)))
                    .thenReturn(defaultVo(1L, "Jitsi Meet", "https://meet.jit.si"));

            mockMvc.perform(post(BASE + "/default")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(createForm("Jitsi Meet", "https://meet.jit.si"))))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "http://localhost/api/servers/default/1"))
                    .andExpect(jsonPath("$.serverType").value("DEFAULT"))
                    .andExpect(jsonPath("$.userId").doesNotExist());
        }

        @Test
        @DisplayName("a missing server name is rejected with 400 MDR-002")
        void missingNameIsRejected() throws Exception {
            mockMvc.perform(post(BASE + "/default")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(createForm(null, "https://meet.jit.si"))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value(MDRErrorCode.MDR_ATTRIBUTE_INVALID.getErrorCode()))
                    .andExpect(jsonPath("$.message").value("server name can't be null"));

            verifyNoInteractions(serverService);
        }

        @Test
        @DisplayName("a missing server URL is rejected with 400 MDR-002")
        void missingUrlIsRejected() throws Exception {
            mockMvc.perform(post(BASE + "/default")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(createForm("Jitsi Meet", null))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("server URL can't be null"));

            verifyNoInteractions(serverService);
        }

        @Test
        @DisplayName("a duplicate default server is surfaced as 409 MDR-005")
        void duplicateIsConflict() throws Exception {
            when(serverService.createDefaultServer(any(DefaultServerForm.class)))
                    .thenThrow(new ServerException(MDRErrorCode.MDR_EXISTS,
                            "Default server already exists with the url https://meet.jit.si"));

            mockMvc.perform(post(BASE + "/default")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(createForm("Jitsi Meet", "https://meet.jit.si"))))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value(MDRErrorCode.MDR_EXISTS.getErrorCode()))
                    .andExpect(jsonPath("$.message").value("Default server already exists with the url https://meet.jit.si"));
        }

        @Test
        @DisplayName("a malformed body is rejected with 400 MDR-002")
        void malformedBodyIsRejected() throws Exception {
            mockMvc.perform(post(BASE + "/default")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(truncated(json(createForm("Jitsi Meet", "https://meet.jit.si")))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Request body could not be read"));

            verifyNoInteractions(serverService);
        }
    }

    @Nested
    @DisplayName("PUT /api/servers/default")
    class Update {

        @Test
        @DisplayName("a default server is updated with 200 and the new representation")
        void updatesDefaultServer() throws Exception {
            when(serverService.updateDefaultServer(any(DefaultServerUpdateForm.class)))
                    .thenReturn(defaultVo(1L, "Jitsi Meet", "https://meet.jit.si"));

            mockMvc.perform(put(BASE + "/default")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(updateForm(1L, "Jitsi Meet", "https://meet.jit.si"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Jitsi Meet"))
                    .andExpect(jsonPath("$.serverType").value("DEFAULT"))
                    .andExpect(jsonPath("$.userId").doesNotExist());
        }

        @Test
        @DisplayName("the body is bound onto the update form")
        void bindsTheUpdateForm() throws Exception {
            when(serverService.updateDefaultServer(any(DefaultServerUpdateForm.class)))
                    .thenReturn(defaultVo(1L, "Jitsi Meet", "https://meet.jit.si"));

            mockMvc.perform(put(BASE + "/default")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(updateForm(1L, "Jitsi Meet", "https://meet.jit.si"))))
                    .andExpect(status().isOk());

            ArgumentCaptor<DefaultServerUpdateForm> captor = ArgumentCaptor.forClass(DefaultServerUpdateForm.class);
            verify(serverService).updateDefaultServer(captor.capture());
            assertThat(captor.getValue().getServerId()).isEqualTo(1L);
            assertThat(captor.getValue().getName()).isEqualTo("Jitsi Meet");
            assertThat(captor.getValue().getServerUrl()).isEqualTo("https://meet.jit.si");
        }

        @Test
        @DisplayName("a missing server id is rejected with 400 MDR-002")
        void missingServerIdIsRejected() throws Exception {
            mockMvc.perform(put(BASE + "/default")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(updateForm(null, "Jitsi Meet", "https://meet.jit.si"))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value(MDRErrorCode.MDR_ATTRIBUTE_INVALID.getErrorCode()))
                    .andExpect(jsonPath("$.message").value("server ID can't be null"));

            verifyNoInteractions(serverService);
        }

        @Test
        @DisplayName("a missing name or URL is rejected with 400 MDR-002")
        void missingNameOrUrlIsRejected() throws Exception {
            mockMvc.perform(put(BASE + "/default")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(updateForm(1L, null, "https://meet.jit.si"))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("server name can't be null"));

            mockMvc.perform(put(BASE + "/default")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(updateForm(1L, "Jitsi Meet", null))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("server URL can't be null"));

            verifyNoInteractions(serverService);
        }

        @Test
        @DisplayName("an unknown default server is surfaced as 404 MDR-006")
        void unknownServerIsNotFound() throws Exception {
            when(serverService.updateDefaultServer(any(DefaultServerUpdateForm.class)))
                    .thenThrow(new ServerException(MDRErrorCode.MDR_NOT_FOUND,
                            "Default server doesn't exist with the id  404"));

            mockMvc.perform(put(BASE + "/default")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(updateForm(404L, "Jitsi Meet", "https://meet.jit.si"))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value(MDRErrorCode.MDR_NOT_FOUND.getErrorCode()));
        }

        @Test
        @DisplayName("a URL already taken by another default server is surfaced as 409 MDR-005")
        void duplicateUrlIsConflict() throws Exception {
            when(serverService.updateDefaultServer(any(DefaultServerUpdateForm.class)))
                    .thenThrow(new ServerException(MDRErrorCode.MDR_EXISTS,
                            "Default server already exists with the url https://meet.jit.si"));

            mockMvc.perform(put(BASE + "/default")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(updateForm(1L, "Jitsi Meet", "https://meet.jit.si"))))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value(MDRErrorCode.MDR_EXISTS.getErrorCode()));
        }

        @Test
        @DisplayName("updating a user preference still goes to PUT /api/servers, not to /default")
        void preferenceUpdateKeepsItsOwnRoute() throws Exception {
            when(serverService.updateServerPreference(any()))
                    .thenReturn(ServerVo.builder().id(9L).name("My box").serverUrl("https://jitsi.internal")
                            .userId(7L).serverType(ServerType.CUSTOM).build());

            ServerPreferenceUpdateForm preference = new ServerPreferenceUpdateForm();
            preference.setServerId(9L);
            preference.setUserId(7L);
            preference.setName("My box");
            preference.setServerUrl("https://jitsi.internal");

            mockMvc.perform(put(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(preference)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.serverType").value("CUSTOM"));

            verify(serverService, never()).updateDefaultServer(any());
        }
    }

    @Nested
    @DisplayName("GET /api/servers")
    class Read {

        @Test
        @DisplayName("a user's list carries the default servers alongside their own preferences")
        void listsDefaultsWithPreferences() throws Exception {
            when(serverService.getServersByUserId(7L)).thenReturn(List.of(
                    ServerVo.builder().id(1L).name("My box").serverUrl("https://jitsi.internal")
                            .userId(7L).serverType(ServerType.CUSTOM).build(),
                    defaultVo(2L, "Jitsi Meet", "https://meet.jit.si")));

            mockMvc.perform(get(BASE).param("userId", "7"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].serverType").value("CUSTOM"))
                    .andExpect(jsonPath("$[1].serverType").value("DEFAULT"));
        }

        @Test
        @DisplayName("a missing userId is rejected with 400 MDR-004")
        void missingUserIdIsRejected() throws Exception {
            mockMvc.perform(get(BASE))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value(MDRErrorCode.MDR_ATTRIBUTE_MISSING.getErrorCode()))
                    .andExpect(jsonPath("$.message").value("Required request parameter userId is missing"));

            verify(serverService, never()).getServersByUserId(any());
        }

        @Test
        @DisplayName("a userId that is not a number is rejected with 400 MDR-002 naming the parameter")
        void nonNumericUserIdIsRejected() throws Exception {
            mockMvc.perform(get(BASE).param("userId", "abc"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value(MDRErrorCode.MDR_ATTRIBUTE_INVALID.getErrorCode()))
                    .andExpect(jsonPath("$.message").value("abc is not a valid value for userId"));

            verify(serverService, never()).getServersByUserId(any());
        }

        @Test
        @DisplayName("a server id that is not a number is rejected with 400 rather than 500")
        void nonNumericServerIdIsRejected() throws Exception {
            mockMvc.perform(get(BASE + "/not-a-number"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value(MDRErrorCode.MDR_ATTRIBUTE_INVALID.getErrorCode()));

            verifyNoInteractions(serverService);
        }

        @Test
        @DisplayName("an unknown server id is surfaced as 404 MDR-006")
        void unknownServerIdIsNotFound() throws Exception {
            when(serverService.getServerById(404L))
                    .thenThrow(new ServerException(MDRErrorCode.MDR_NOT_FOUND, "Server not found with id: 404"));

            mockMvc.perform(get(BASE + "/404"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value(MDRErrorCode.MDR_NOT_FOUND.getErrorCode()));
        }
    }
}
