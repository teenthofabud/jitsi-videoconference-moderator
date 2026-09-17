package com.teenthofabud.videoconference.mdr.server.service.impl;

import com.teenthofabud.videoconference.mdr.error.core.MDRErrorCode;
import com.teenthofabud.videoconference.mdr.server.constants.ServerType;
import com.teenthofabud.videoconference.mdr.server.converter.ServerEntityToVoConverter;
import com.teenthofabud.videoconference.mdr.server.converter.ServerPreferenceFormToEntityConverter;
import com.teenthofabud.videoconference.mdr.server.entity.ServerEntity;
import com.teenthofabud.videoconference.mdr.server.error.ServerException;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Edge cases for reading DEFAULT servers back out. There is no endpoint dedicated to defaults;
 * they surface through {@code getServersByUserId}, which merges them into a user's own
 * preferences, and through {@code getServerById}.
 */
@ExtendWith(MockitoExtension.class)
class DefaultServerRetrievalTest {

    private static final Long USER_ID = 99L;

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

    private ServerEntity defaultServer(Long id, String name, String url) {
        return ServerEntity.builder()
                .id(id).name(name).serverUrl(url).serverType(ServerType.DEFAULT).build();
    }

    private ServerEntity customServer(Long id, Long userId, String name, String url) {
        return ServerEntity.builder()
                .id(id).name(name).serverUrl(url).userId(userId).serverType(ServerType.CUSTOM).build();
    }

    @Nested
    @DisplayName("getServersByUserId")
    class GetServersByUserId {

        @Test
        @DisplayName("a user's own preferences come first, then every default server")
        void customsThenDefaults() {
            when(serverRepository.findAllByUserId(USER_ID)).thenReturn(List.of(
                    customServer(1L, USER_ID, "My box", "https://jitsi.internal")));
            when(serverRepository.findAllByServerType(ServerType.DEFAULT)).thenReturn(List.of(
                    defaultServer(2L, "Jitsi Meet", "https://meet.jit.si")));

            List<ServerVo> servers = serverService.getServersByUserId(USER_ID);

            assertThat(servers).extracting(ServerVo::getId).containsExactly(1L, 2L);
            assertThat(servers).extracting(ServerVo::getServerType)
                    .containsExactly(ServerType.CUSTOM, ServerType.DEFAULT);
        }

        @Test
        @DisplayName("a user with no preferences still receives the default servers")
        void defaultsOnlyForNewUser() {
            when(serverRepository.findAllByUserId(USER_ID)).thenReturn(List.of());
            when(serverRepository.findAllByServerType(ServerType.DEFAULT)).thenReturn(List.of(
                    defaultServer(2L, "Jitsi Meet", "https://meet.jit.si"),
                    defaultServer(3L, "Jitsi Meet EU", "https://meet.example.org")));

            List<ServerVo> servers = serverService.getServersByUserId(USER_ID);

            assertThat(servers).extracting(ServerVo::getId).containsExactly(2L, 3L);
            assertThat(servers).allSatisfy(vo -> assertThat(vo.getUserId()).isNull());
        }

        @Test
        @DisplayName("with no default servers configured only the user's own preferences come back")
        void noDefaultsConfigured() {
            when(serverRepository.findAllByUserId(USER_ID)).thenReturn(List.of(
                    customServer(1L, USER_ID, "My box", "https://jitsi.internal")));
            when(serverRepository.findAllByServerType(ServerType.DEFAULT)).thenReturn(List.of());

            List<ServerVo> servers = serverService.getServersByUserId(USER_ID);

            assertThat(servers).extracting(ServerVo::getId).containsExactly(1L);
        }

        @Test
        @DisplayName("an empty list, not an error, when nothing exists at all")
        void emptyWhenNothingExists() {
            when(serverRepository.findAllByUserId(USER_ID)).thenReturn(List.of());
            when(serverRepository.findAllByServerType(ServerType.DEFAULT)).thenReturn(List.of());

            assertThat(serverService.getServersByUserId(USER_ID)).isEmpty();
        }

        @Test
        @DisplayName("a default server that also carries a user_id is listed once, not twice")
        void defaultOwnedByUserIsDeduplicated() {
            // nothing stops a DEFAULT row from having user_id set, and the two queries then both
            // match it; the merge keeps the first sighting so the plugin dropdown shows no repeat
            ServerEntity odd = ServerEntity.builder()
                    .id(4L).name("Jitsi Meet").serverUrl("https://meet.jit.si")
                    .userId(USER_ID).serverType(ServerType.DEFAULT).build();
            when(serverRepository.findAllByUserId(USER_ID)).thenReturn(List.of(odd));
            when(serverRepository.findAllByServerType(ServerType.DEFAULT)).thenReturn(List.of(odd));

            List<ServerVo> servers = serverService.getServersByUserId(USER_ID);

            assertThat(servers).extracting(ServerVo::getId).containsExactly(4L);
        }

        @Test
        @DisplayName("deduplication keeps preferences ahead of defaults and drops only true repeats")
        void deduplicationPreservesOrdering() {
            ServerEntity shared = ServerEntity.builder()
                    .id(4L).name("Shared").serverUrl("https://shared.example.org")
                    .userId(USER_ID).serverType(ServerType.DEFAULT).build();
            when(serverRepository.findAllByUserId(USER_ID)).thenReturn(List.of(
                    customServer(1L, USER_ID, "My box", "https://jitsi.internal"), shared));
            when(serverRepository.findAllByServerType(ServerType.DEFAULT)).thenReturn(List.of(
                    shared, defaultServer(2L, "Jitsi Meet", "https://meet.jit.si")));

            List<ServerVo> servers = serverService.getServersByUserId(USER_ID);

            assertThat(servers).extracting(ServerVo::getId).containsExactly(1L, 4L, 2L);
        }

        @Test
        @DisplayName("two distinct servers sharing a name and URL are both kept")
        void distinctRowsAreNotCollapsed() {
            // deduplication is by primary key, so genuinely separate rows survive even when their
            // visible attributes are identical
            when(serverRepository.findAllByUserId(USER_ID)).thenReturn(List.of());
            when(serverRepository.findAllByServerType(ServerType.DEFAULT)).thenReturn(List.of(
                    defaultServer(2L, "Jitsi Meet", "https://meet.jit.si"),
                    defaultServer(3L, "Jitsi Meet", "https://meet.jit.si")));

            List<ServerVo> servers = serverService.getServersByUserId(USER_ID);

            assertThat(servers).extracting(ServerVo::getId).containsExactly(2L, 3L);
        }

        @Test
        @DisplayName("a default and a preference may share a name - the list is not disambiguated")
        void nameCollisionsAreNotResolved() {
            when(serverRepository.findAllByUserId(USER_ID)).thenReturn(List.of(
                    customServer(1L, USER_ID, "Jitsi Meet", "https://jitsi.internal")));
            when(serverRepository.findAllByServerType(ServerType.DEFAULT)).thenReturn(List.of(
                    defaultServer(2L, "Jitsi Meet", "https://meet.jit.si")));

            List<ServerVo> servers = serverService.getServersByUserId(USER_ID);

            assertThat(servers).extracting(ServerVo::getName).containsExactly("Jitsi Meet", "Jitsi Meet");
            assertThat(servers).extracting(ServerVo::getServerUrl)
                    .containsExactly("https://jitsi.internal", "https://meet.jit.si");
        }

        @Test
        @DisplayName("an unknown user id is not rejected - the defaults still come back")
        void unknownUserStillGetsDefaults() {
            when(serverRepository.findAllByUserId(-1L)).thenReturn(List.of());
            when(serverRepository.findAllByServerType(ServerType.DEFAULT)).thenReturn(List.of(
                    defaultServer(2L, "Jitsi Meet", "https://meet.jit.si")));

            List<ServerVo> servers = serverService.getServersByUserId(-1L);

            assertThat(servers).hasSize(1);
            verifyNoInteractions(userService);
        }

        @Test
        @DisplayName("a null user id is tolerated at the service layer")
        void nullUserIdIsTolerated() {
            // the controller declares userId as a required request param, so this only matters for
            // direct service callers
            when(serverRepository.findAllByUserId(null)).thenReturn(List.of());
            when(serverRepository.findAllByServerType(ServerType.DEFAULT)).thenReturn(List.of(
                    defaultServer(2L, "Jitsi Meet", "https://meet.jit.si")));

            assertThat(serverService.getServersByUserId(null)).hasSize(1);
        }

        @Test
        @DisplayName("another user's preferences are never included")
        void otherUsersPreferencesAreExcluded() {
            when(serverRepository.findAllByUserId(USER_ID)).thenReturn(List.of());
            when(serverRepository.findAllByServerType(ServerType.DEFAULT)).thenReturn(List.of(
                    defaultServer(2L, "Jitsi Meet", "https://meet.jit.si")));

            List<ServerVo> servers = serverService.getServersByUserId(USER_ID);

            assertThat(servers).extracting(ServerVo::getServerType).containsOnly(ServerType.DEFAULT);
            verify(serverRepository).findAllByUserId(USER_ID);
        }
    }

    @Nested
    @DisplayName("getServerById")
    class GetServerById {

        @Test
        @DisplayName("a default server is readable by id")
        void defaultServerIsReadableById() throws ServerException {
            when(serverRepository.findById(2L)).thenReturn(Optional.of(
                    defaultServer(2L, "Jitsi Meet", "https://meet.jit.si")));

            ServerVo vo = serverService.getServerById(2L);

            assertThat(vo.getServerType()).isEqualTo(ServerType.DEFAULT);
            assertThat(vo.getUserId()).isNull();
            assertThat(vo.getServerUrl()).isEqualTo("https://meet.jit.si");
        }

        @Test
        @DisplayName("an unknown id is MDR_NOT_FOUND")
        void unknownIdIsNotFound() {
            when(serverRepository.findById(404L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> serverService.getServerById(404L))
                    .isInstanceOf(ServerException.class)
                    .hasMessage("Server not found with id: 404")
                    .extracting(e -> ((ServerException) e).getError())
                    .isEqualTo(MDRErrorCode.MDR_NOT_FOUND);
        }

        @Test
        @DisplayName("a null id is passed to the repository rather than short circuited")
        void nullIdIsNotShortCircuited() {
            when(serverRepository.findById(null)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> serverService.getServerById(null))
                    .isInstanceOf(ServerException.class)
                    .hasMessage("Server not found with id: null");
        }
    }
}
