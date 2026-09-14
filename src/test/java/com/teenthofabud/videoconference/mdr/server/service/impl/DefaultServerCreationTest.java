package com.teenthofabud.videoconference.mdr.server.service.impl;

import com.teenthofabud.videoconference.mdr.error.core.MDRErrorCode;
import com.teenthofabud.videoconference.mdr.server.constants.ServerType;
import com.teenthofabud.videoconference.mdr.server.converter.ServerEntityToVoConverter;
import com.teenthofabud.videoconference.mdr.server.converter.ServerPreferenceFormToEntityConverter;
import com.teenthofabud.videoconference.mdr.server.entity.ServerEntity;
import com.teenthofabud.videoconference.mdr.server.error.ServerException;
import com.teenthofabud.videoconference.mdr.server.form.DefaultServerForm;
import com.teenthofabud.videoconference.mdr.server.repository.ServerRepository;
import com.teenthofabud.videoconference.mdr.server.vo.ServerVo;
import com.teenthofabud.videoconference.mdr.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Edge cases for {@code createDefaultServer}, which backs {@code POST /api/servers/default}.
 * A DEFAULT server is global: it carries no user_id and is offered to every user.
 */
@ExtendWith(MockitoExtension.class)
class DefaultServerCreationTest {

    @Mock
    private ServerRepository serverRepository;
    @Mock
    private UserService userService;

    private ServerServiceImpl serverService;

    @BeforeEach
    void setUp() {
        serverService = new ServerServiceImpl(
                serverRepository,
                new ServerPreferenceFormToEntityConverter(),
                new ServerEntityToVoConverter(),
                userService);
    }

    private DefaultServerForm form(String name, String url) {
        DefaultServerForm form = new DefaultServerForm();
        form.setName(name);
        form.setServerUrl(url);
        return form;
    }

    private void savePersistsWithId(Long id) {
        when(serverRepository.save(any(ServerEntity.class))).thenAnswer(invocation -> {
            ServerEntity entity = invocation.getArgument(0);
            entity.setId(id);
            return entity;
        });
    }

    private ServerEntity captureSaved() {
        ArgumentCaptor<ServerEntity> captor = ArgumentCaptor.forClass(ServerEntity.class);
        verify(serverRepository).save(captor.capture());
        return captor.getValue();
    }

    @Nested
    @DisplayName("happy path")
    class HappyPath {

        @Test
        @DisplayName("a default server is stored as DEFAULT with no owning user")
        void storedAsDefaultWithoutOwner() throws ServerException {
            when(serverRepository.existsByServerUrlAndServerType("https://meet.jit.si", ServerType.DEFAULT)).thenReturn(false);
            savePersistsWithId(1L);

            ServerVo vo = serverService.createDefaultServer(form("Jitsi Meet", "https://meet.jit.si"));

            ServerEntity saved = captureSaved();
            assertThat(saved.getServerType()).isEqualTo(ServerType.DEFAULT);
            assertThat(saved.getUserId()).isNull();
            assertThat(saved.getName()).isEqualTo("Jitsi Meet");
            assertThat(saved.getServerUrl()).isEqualTo("https://meet.jit.si");

            assertThat(vo.getId()).isEqualTo(1L);
            assertThat(vo.getServerType()).isEqualTo(ServerType.DEFAULT);
            assertThat(vo.getUserId()).isNull();
        }

        @Test
        @DisplayName("creating a default server never touches the user service")
        void doesNotValidateAnyUser() throws ServerException {
            when(serverRepository.existsByServerUrlAndServerType(anyString(), eq(ServerType.DEFAULT))).thenReturn(false);
            savePersistsWithId(1L);

            serverService.createDefaultServer(form("Jitsi Meet", "https://meet.jit.si"));

            verifyNoInteractions(userService);
        }

        @Test
        @DisplayName("the submitted form is left untouched - only a sanitised copy is persisted")
        void formIsNotMutated() throws ServerException {
            when(serverRepository.existsByServerUrlAndServerType(anyString(), eq(ServerType.DEFAULT))).thenReturn(false);
            savePersistsWithId(1L);

            DefaultServerForm form = form("Jitsi Meet", "HTTP://Meet.Jit.Si/");
            serverService.createDefaultServer(form);

            assertThat(form.getServerUrl()).isEqualTo("HTTP://Meet.Jit.Si/");
            assertThat(captureSaved().getServerUrl()).isEqualTo("https://meet.jit.si");
        }

        @Test
        @DisplayName("a duplicate name is allowed - only the URL is deduplicated")
        void duplicateNameIsAllowed() throws ServerException {
            when(serverRepository.existsByServerUrlAndServerType("https://meet.example.org", ServerType.DEFAULT)).thenReturn(false);
            savePersistsWithId(2L);

            ServerVo vo = serverService.createDefaultServer(form("Jitsi Meet", "https://meet.example.org"));

            assertThat(vo.getName()).isEqualTo("Jitsi Meet");
        }
    }

    @Nested
    @DisplayName("URL sanitisation")
    class UrlSanitisation {

        @ParameterizedTest(name = "{0} is stored as {1}")
        @CsvSource({
                "https://meet.jit.si,          https://meet.jit.si",
                "http://meet.jit.si,           https://meet.jit.si",
                "meet.jit.si,                  https://meet.jit.si",
                "MEET.JIT.SI,                  https://meet.jit.si",
                "HTTPS://MEET.JIT.SI,          https://meet.jit.si",
                "https://meet.jit.si/,         https://meet.jit.si",
                "https://meet.jit.si///,       https://meet.jit.si",
                "'  https://meet.jit.si  ',    https://meet.jit.si",
                "'  HTTP://Meet.Jit.Si//  ',   https://meet.jit.si"
        })
        void urlIsNormalisedBeforePersisting(String submitted, String stored) throws ServerException {
            when(serverRepository.existsByServerUrlAndServerType(stored, ServerType.DEFAULT)).thenReturn(false);
            savePersistsWithId(1L);

            serverService.createDefaultServer(form("Jitsi Meet", submitted));

            assertThat(captureSaved().getServerUrl()).isEqualTo(stored);
        }

        @Test
        @DisplayName("a path is preserved, only trailing slashes are trimmed")
        void pathIsPreserved() throws ServerException {
            when(serverRepository.existsByServerUrlAndServerType("https://example.org/jitsi", ServerType.DEFAULT)).thenReturn(false);
            savePersistsWithId(1L);

            serverService.createDefaultServer(form("Hosted", "https://example.org/jitsi/"));

            assertThat(captureSaved().getServerUrl()).isEqualTo("https://example.org/jitsi");
        }

        @Test
        @DisplayName("the duplicate check runs against the sanitised URL, so cosmetic variants collide")
        void duplicateCheckUsesSanitisedUrl() {
            when(serverRepository.existsByServerUrlAndServerType("https://meet.jit.si", ServerType.DEFAULT)).thenReturn(true);

            assertThatThrownBy(() -> serverService.createDefaultServer(form("Jitsi Meet", "HTTP://Meet.Jit.Si///")))
                    .isInstanceOf(ServerException.class);

            verify(serverRepository).existsByServerUrlAndServerType("https://meet.jit.si", ServerType.DEFAULT);
        }

        @Test
        @DisplayName("a URL that is only slashes collapses to the bare https scheme")
        void slashOnlyUrlCollapses() throws ServerException {
            // "///" -> trimmed to "" -> no scheme -> "https://"; nothing rejects this today
            when(serverRepository.existsByServerUrlAndServerType("https://", ServerType.DEFAULT)).thenReturn(false);
            savePersistsWithId(1L);

            serverService.createDefaultServer(form("Broken", "///"));

            assertThat(captureSaved().getServerUrl()).isEqualTo("https://");
        }

        @Test
        @DisplayName("a blank URL is passed through unsanitised rather than rejected")
        void blankUrlIsPassedThrough() throws ServerException {
            // sanitizeUrl() short circuits on blank input, so the blank value reaches the insert and
            // only the NOT NULL / length constraints of the database would object
            when(serverRepository.existsByServerUrlAndServerType("   ", ServerType.DEFAULT)).thenReturn(false);
            savePersistsWithId(1L);

            serverService.createDefaultServer(form("Blank", "   "));

            assertThat(captureSaved().getServerUrl()).isEqualTo("   ");
        }

        @Test
        @DisplayName("a null URL is passed through - @NotNull on the form is the only guard")
        void nullUrlIsPassedThrough() throws ServerException {
            when(serverRepository.existsByServerUrlAndServerType(null, ServerType.DEFAULT)).thenReturn(false);
            savePersistsWithId(1L);

            serverService.createDefaultServer(form("Null url", null));

            assertThat(captureSaved().getServerUrl()).isNull();
        }
    }

    @Nested
    @DisplayName("duplicates")
    class Duplicates {

        @Test
        @DisplayName("a second default server on the same URL is rejected as MDR_EXISTS")
        void duplicateDefaultIsRejected() {
            when(serverRepository.existsByServerUrlAndServerType("https://meet.jit.si", ServerType.DEFAULT)).thenReturn(true);

            assertThatThrownBy(() -> serverService.createDefaultServer(form("Jitsi Meet", "https://meet.jit.si")))
                    .isInstanceOf(ServerException.class)
                    .hasMessage("Default server already exists with the url https://meet.jit.si")
                    .extracting(e -> ((ServerException) e).getError())
                    .isEqualTo(MDRErrorCode.MDR_EXISTS);

            verify(serverRepository, never()).save(any());
        }

        @Test
        @DisplayName("the rejection message names the sanitised URL that was actually compared")
        void rejectionMessageEchoesSanitisedUrl() {
            when(serverRepository.existsByServerUrlAndServerType("https://meet.jit.si", ServerType.DEFAULT)).thenReturn(true);

            assertThatThrownBy(() -> serverService.createDefaultServer(form("Jitsi Meet", "  HTTP://Meet.Jit.Si/  ")))
                    .hasMessage("Default server already exists with the url https://meet.jit.si");
        }

        @Test
        @DisplayName("an existing CUSTOM server on the same URL does not block a DEFAULT server")
        void customServerOnSameUrlDoesNotBlock() throws ServerException {
            // the existence check is scoped to ServerType.DEFAULT, and the uk_user_server unique
            // index is on (user_id, server_url), so a user preference and a default can coexist
            when(serverRepository.existsByServerUrlAndServerType("https://meet.jit.si", ServerType.DEFAULT)).thenReturn(false);
            savePersistsWithId(5L);

            ServerVo vo = serverService.createDefaultServer(form("Jitsi Meet", "https://meet.jit.si"));

            assertThat(vo.getServerType()).isEqualTo(ServerType.DEFAULT);
        }

        @Test
        @DisplayName("two default servers may share a URL if they differ after sanitisation")
        void differentUrlsAreIndependent() throws ServerException {
            when(serverRepository.existsByServerUrlAndServerType("https://meet.jit.si/room", ServerType.DEFAULT)).thenReturn(false);
            savePersistsWithId(6L);

            ServerVo vo = serverService.createDefaultServer(form("Jitsi Meet room", "https://meet.jit.si/room"));

            assertThat(vo.getServerUrl()).isEqualTo("https://meet.jit.si/room");
        }
    }
}
