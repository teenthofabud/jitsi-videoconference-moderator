package com.teenthofabud.videoconference.mdr.user.repository;

import com.teenthofabud.videoconference.mdr.support.AbstractPostgresRepositoryTest;
import com.teenthofabud.videoconference.mdr.user.entity.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Exercises {@link UserRepository} against a real PostgreSQL engine so the unique constraint on
 * {@code email} declared in {@code sql/ddl/V2__main.sql} is enforced by the database, not merely
 * asserted in Java.
 */
@DisplayName("UserRepository against a real Postgres schema")
class UserRepositoryTest extends AbstractPostgresRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("a user persists and is retrievable by email")
    void userPersistsAndIsRetrievableByEmail() {
        UserEntity user = userRepository.save(UserEntity.builder()
                .email("organiser@example.com")
                .name("Organiser")
                .managed(false)
                .version(0L)
                .lastModifiedDate(java.time.Instant.parse("2026-01-02T03:04:05Z"))
                .build());

        assertThat(user.getId()).isNotNull();
        assertThat(user.getVersion()).isZero();
        assertThat(user.getLastModifiedDate()).isNotNull();
        assertThat(userRepository.findByEmail("organiser@example.com")).contains(user);
    }

    @Test
    @DisplayName("the schema rejects a duplicate email")
    void duplicateEmailIsRejectedBySchema() {
        userRepository.saveAndFlush(UserEntity.builder()
                .email("organiser@example.com")
                .name("Organiser")
                .managed(false)
                .version(0L)
                .lastModifiedDate(java.time.Instant.parse("2026-01-02T03:04:05Z"))
                .build());

        UserEntity duplicate = UserEntity.builder()
                .email("organiser@example.com")
                .name("Same email, different person")
                .managed(true)
                .version(0L)
                .lastModifiedDate(java.time.Instant.parse("2026-01-02T03:04:05Z"))
                .build();

        assertThatThrownBy(() -> userRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
