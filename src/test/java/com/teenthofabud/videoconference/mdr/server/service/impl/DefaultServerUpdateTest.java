package com.teenthofabud.videoconference.mdr.server.service.impl;

import com.teenthofabud.videoconference.mdr.error.core.MDRErrorCode;
import com.teenthofabud.videoconference.mdr.server.constants.ServerType;
import com.teenthofabud.videoconference.mdr.server.converter.ServerEntityToVoConverter;
import com.teenthofabud.videoconference.mdr.server.converter.ServerPreferenceFormToEntityConverter;
import com.teenthofabud.videoconference.mdr.server.entity.ServerEntity;
import com.teenthofabud.videoconference.mdr.server.error.ServerException;
import com.teenthofabud.videoconference.mdr.server.form.DefaultServerUpdateForm;
import com.teenthofabud.videoconference.mdr.server.repository.ServerRepository;
import com.teenthofabud.videoconference.mdr.server.vo.ServerVo;
import com.teenthofabud.videoconference.mdr.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.time.Instant;

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
 * Edge cases for {@code updateDefaultServer}, which backs {@code PUT /api/servers/default}.
 */
@ExtendWith(MockitoExtension.class)
class DefaultServerUpdateTest {

    private static final Instant LAST_MODIFIED = Instant.parse("2026-01-02T03:04:05Z");

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

    private DefaultServerUpdateForm form(Long serverId, String name, String url) {
        DefaultServerUpdateForm form = new DefaultServerUpdateForm();
        form.setServerId(serverId);
        form.setName(name);
        form.setServerUrl(url);
        return form;
    }

    private ServerEntity existingDefault(Long id, String name, String url) {
        return ServerEntity.builder()
                .id(id).name(name).serverUrl(url).serverType(ServerType.DEFAULT)
                .version(0L).lastModifiedDate(LAST_MODIFIED).build();
    }

    private void saveReturnsTheArgument() {
        when(serverRepository.save(any(ServerEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Nested
    @DisplayName("happy path")
    class HappyPath {

        @Test
        @DisplayName("the name and URL are replaced and the row stays DEFAULT with no owner")
        void updatesNameAndUrl() throws ServerException {
            when(serverRepository.findById(1L)).thenReturn(Optional.of(existingDefault(1L, "Old", "https://old.example.org")));
            when(serverRepository.existsByServerUrlAndServerType("https://meet.jit.si", ServerType.DEFAULT)).thenReturn(false);
            saveReturnsTheArgument();

            ServerVo vo = serverService.updateDefaultServer(form(1L, "Jitsi Meet", "https://meet.jit.si"));

            assertThat(vo.getId()).isEqualTo(1L);
            assertThat(vo.getName()).isEqualTo("Jitsi Meet");
            assertThat(vo.getServerUrl()).isEqualTo("https://meet.jit.si");
            assertThat(vo.getServerType()).isEqualTo(ServerType.DEFAULT);
            assertThat(vo.getUserId()).isNull();
        }

        @Test
        @DisplayName("the submitted URL is sanitised before it is stored")
        void urlIsSanitised() throws ServerException {
            when(serverRepository.findById(1L)).thenReturn(Optional.of(existingDefault(1L, "Old", "https://old.example.org")));
            when(serverRepository.existsByServerUrlAndServerType("https://meet.jit.si", ServerType.DEFAULT)).thenReturn(false);
            saveReturnsTheArgument();

            ServerVo vo = serverService.updateDefaultServer(form(1L, "Jitsi Meet", "  HTTP://Meet.Jit.Si//  "));

            assertThat(vo.getServerUrl()).isEqualTo("https://meet.jit.si");
        }

        @Test
        @DisplayName("renaming without changing the URL skips the duplicate check")
        void renameOnlySkipsTheDuplicateCheck() throws ServerException {
            when(serverRepository.findById(1L)).thenReturn(Optional.of(existingDefault(1L, "Old", "https://meet.jit.si")));
            saveReturnsTheArgument();

            ServerVo vo = serverService.updateDefaultServer(form(1L, "Jitsi Meet", "https://meet.jit.si"));

            assertThat(vo.getName()).isEqualTo("Jitsi Meet");
            verify(serverRepository, never()).existsByServerUrlAndServerType(anyString(), any());
        }

        @Test
        @DisplayName("a cosmetic URL change does not trip the duplicate check against the server itself")
        void cosmeticUrlChangeIsNotATrueChange() throws ServerException {
            // the stored URL is compared against the sanitised submission, so "HTTPS://MEET.JIT.SI/"
            // resolves to the value already held and the existence check is skipped
            when(serverRepository.findById(1L)).thenReturn(Optional.of(existingDefault(1L, "Old", "https://meet.jit.si")));
            saveReturnsTheArgument();

            ServerVo vo = serverService.updateDefaultServer(form(1L, "Jitsi Meet", "HTTPS://MEET.JIT.SI/"));

            assertThat(vo.getServerUrl()).isEqualTo("https://meet.jit.si");
            verify(serverRepository, never()).existsByServerUrlAndServerType(anyString(), any());
        }

        @Test
        @DisplayName("updating a default server never touches the user service")
        void doesNotValidateAnyUser() throws ServerException {
            when(serverRepository.findById(1L)).thenReturn(Optional.of(existingDefault(1L, "Old", "https://meet.jit.si")));
            saveReturnsTheArgument();

            serverService.updateDefaultServer(form(1L, "Jitsi Meet", "https://meet.jit.si"));

            verifyNoInteractions(userService);
        }
    }

    @Nested
    @DisplayName("rejections")
    class Rejections {

        @Test
        @DisplayName("an unknown server id is MDR_NOT_FOUND")
        void unknownIdIsNotFound() {
            when(serverRepository.findById(404L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> serverService.updateDefaultServer(form(404L, "Jitsi Meet", "https://meet.jit.si")))
                    .isInstanceOf(ServerException.class)
                    .hasMessage("Default server doesn't exist with the id  404")
                    .extracting(e -> ((ServerException) e).getError())
                    .isEqualTo(MDRErrorCode.MDR_NOT_FOUND);

            verify(serverRepository, never()).save(any());
        }

        @Test
        @DisplayName("a CUSTOM server cannot be rewritten through the default server endpoint")
        void aCustomServerCannotBeRewritten() {
            // without this guard the endpoint would let any caller edit another user's preference
            ServerEntity someoneElsesPreference = ServerEntity.builder()
                    .id(7L).name("Alice box").serverUrl("https://alice.internal")
                    .userId(42L).serverType(ServerType.CUSTOM)
                    .version(0L).lastModifiedDate(LAST_MODIFIED).build();
            when(serverRepository.findById(7L)).thenReturn(Optional.of(someoneElsesPreference));

            assertThatThrownBy(() -> serverService.updateDefaultServer(form(7L, "Hijacked", "https://attacker.example.org")))
                    .isInstanceOf(ServerException.class)
                    .extracting(e -> ((ServerException) e).getError())
                    .isEqualTo(MDRErrorCode.MDR_NOT_FOUND);

            verify(serverRepository, never()).save(any());
        }

        @Test
        @DisplayName("the rejection for a CUSTOM server is indistinguishable from an unknown id")
        void aCustomServerLooksLikeAnUnknownId() {
            // so the endpoint cannot be used to enumerate which ids exist
            when(serverRepository.findById(7L)).thenReturn(Optional.of(ServerEntity.builder()
                    .id(7L).name("Alice box").serverUrl("https://alice.internal")
                    .userId(42L).serverType(ServerType.CUSTOM)
                    .version(0L).lastModifiedDate(LAST_MODIFIED).build()));
            when(serverRepository.findById(404L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> serverService.updateDefaultServer(form(7L, "Hijacked", "https://attacker.example.org")))
                    .hasMessage("Default server doesn't exist with the id  7");
            assertThatThrownBy(() -> serverService.updateDefaultServer(form(404L, "Hijacked", "https://attacker.example.org")))
                    .hasMessage("Default server doesn't exist with the id  404");
        }

        @Test
        @DisplayName("moving a default server onto a URL another default already uses is MDR_EXISTS")
        void duplicateUrlIsRejected() {
            when(serverRepository.findById(1L)).thenReturn(Optional.of(existingDefault(1L, "Old", "https://old.example.org")));
            when(serverRepository.existsByServerUrlAndServerType("https://meet.jit.si", ServerType.DEFAULT)).thenReturn(true);

            assertThatThrownBy(() -> serverService.updateDefaultServer(form(1L, "Jitsi Meet", "https://meet.jit.si")))
                    .isInstanceOf(ServerException.class)
                    .hasMessage("Default server already exists with the url https://meet.jit.si")
                    .extracting(e -> ((ServerException) e).getError())
                    .isEqualTo(MDRErrorCode.MDR_EXISTS);

            verify(serverRepository, never()).save(any());
        }

        @Test
        @DisplayName("the duplicate rejection names the URL that was compared, not the one being replaced")
        void duplicateRejectionNamesTheNewUrl() {
            when(serverRepository.findById(1L)).thenReturn(Optional.of(existingDefault(1L, "Old", "https://old.example.org")));
            when(serverRepository.existsByServerUrlAndServerType("https://meet.jit.si", ServerType.DEFAULT)).thenReturn(true);

            assertThatThrownBy(() -> serverService.updateDefaultServer(form(1L, "Jitsi Meet", "  HTTP://Meet.Jit.Si/  ")))
                    .hasMessage("Default server already exists with the url https://meet.jit.si");
        }

        @Test
        @DisplayName("a null server id falls through to the not found branch")
        void nullServerIdIsNotFound() {
            when(serverRepository.findById(null)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> serverService.updateDefaultServer(form(null, "Jitsi Meet", "https://meet.jit.si")))
                    .isInstanceOf(ServerException.class)
                    .hasMessage("Default server doesn't exist with the id  null");
        }

        @Test
        @DisplayName("a null URL is compared before sanitisation and reaches the update")
        void nullUrlIsPassedThrough() throws ServerException {
            when(serverRepository.findById(1L)).thenReturn(Optional.of(existingDefault(1L, "Old", "https://old.example.org")));
            when(serverRepository.existsByServerUrlAndServerType(eq(null), eq(ServerType.DEFAULT))).thenReturn(false);
            saveReturnsTheArgument();

            ServerVo vo = serverService.updateDefaultServer(form(1L, "Jitsi Meet", null));

            assertThat(vo.getServerUrl()).isNull();
        }
    }
}
