package com.teenthofabud.videoconference.mdr.server.repository;

import com.teenthofabud.videoconference.mdr.server.constants.ServerType;
import com.teenthofabud.videoconference.mdr.server.entity.ServerEntity;
import com.teenthofabud.videoconference.mdr.support.AbstractPostgresRepositoryTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Exercises {@link ServerRepository} against a real PostgreSQL engine so the {@code uk_user_server}
 * constraint declared in {@code sql/ddl/V2__main.sql} is actually enforced by the database, not
 * merely asserted in Java.
 */
@DisplayName("ServerRepository against a real Postgres schema")
class ServerRepositoryTest extends AbstractPostgresRepositoryTest {

    @Autowired
    private ServerRepository serverRepository;

    @Test
    @DisplayName("a server persists and is retrievable by user and type")
    void serverPersistsAndIsRetrievable() {
        ServerEntity server = serverRepository.save(ServerEntity.builder()
                .name("Room A")
                .serverUrl("https://meet.example.com/a")
                .userId(42L)
                .serverType(ServerType.CUSTOM)
                .version(0L)
                .lastModifiedDate(java.time.Instant.parse("2026-01-02T03:04:05Z"))
                .build());

        assertThat(server.getId()).isNotNull();
        assertThat(server.getVersion()).isZero();
        assertThat(server.getLastModifiedDate()).isNotNull();
        assertThat(serverRepository.existsByUserIdAndServerUrl(42L, "https://meet.example.com/a")).isTrue();
        assertThat(serverRepository.existsByServerUrlAndServerType("https://meet.example.com/a", ServerType.CUSTOM)).isTrue();
        assertThat(serverRepository.findAllByUserId(42L)).containsExactly(server);
        assertThat(serverRepository.findAllByServerType(ServerType.CUSTOM)).containsExactly(server);
    }

    @Test
    @DisplayName("the schema rejects a duplicate (user_id, server_url) pair")
    void duplicateUserAndUrlIsRejectedBySchema() {
        serverRepository.saveAndFlush(ServerEntity.builder()
                .name("Room A")
                .serverUrl("https://meet.example.com/a")
                .userId(7L)
                .serverType(ServerType.CUSTOM)
                .version(0L)
                .lastModifiedDate(java.time.Instant.parse("2026-01-02T03:04:05Z"))
                .build());

        ServerEntity duplicate = ServerEntity.builder()
                .name("Room A, renamed")
                .serverUrl("https://meet.example.com/a")
                .userId(7L)
                .serverType(ServerType.CUSTOM)
                .version(0L)
                .lastModifiedDate(java.time.Instant.parse("2026-01-02T03:04:05Z"))
                .build();

        assertThatThrownBy(() -> serverRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
