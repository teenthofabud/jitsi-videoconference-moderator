package com.teenthofabud.videoconference.mdr.conference.google.repository;

import com.teenthofabud.videoconference.mdr.conference.constants.ConferenceStatus;
import com.teenthofabud.videoconference.mdr.conference.google.entity.GoogleConferenceEntity;
import com.teenthofabud.videoconference.mdr.support.AbstractPostgresRepositoryTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Exercises {@link GoogleConferenceRepository} against a real PostgreSQL engine so the unique
 * constraint on {@code event_id} declared in {@code sql/ddl/V2__main.sql} is enforced by the
 * database, not merely asserted in Java.
 */
@DisplayName("GoogleConferenceRepository against a real Postgres schema")
class GoogleConferenceRepositoryTest extends AbstractPostgresRepositoryTest {

    @Autowired
    private GoogleConferenceRepository googleConferenceRepository;

    @Test
    @DisplayName("a conference persists and is retrievable by organiser, room and event id")
    void conferencePersistsAndIsRetrievable() {
        GoogleConferenceEntity conference = googleConferenceRepository.save(GoogleConferenceEntity.builder()
                .roomId("room-1")
                .userId(1L)
                .serverId(5L)
                .status(ConferenceStatus.ACTIVE)
                .eventId("evt-1")
                .version(0L)
                .lastModifiedDate(java.time.Instant.parse("2026-01-02T03:04:05Z"))
                .build());

        assertThat(conference.getId()).isNotNull();
        assertThat(conference.getVersion()).isZero();
        assertThat(conference.getLastModifiedDate()).isNotNull();
        assertThat(googleConferenceRepository.findAllByUserId(1L)).containsExactly(conference);
        assertThat(googleConferenceRepository.findByRoomId("room-1")).contains(conference);
        assertThat(googleConferenceRepository.findByUserIdAndEventId(1L, "evt-1")).contains(conference);
    }

    @Test
    @DisplayName("the schema rejects a duplicate event id")
    void duplicateEventIdIsRejectedBySchema() {
        googleConferenceRepository.saveAndFlush(GoogleConferenceEntity.builder()
                .roomId("room-1")
                .userId(1L)
                .serverId(5L)
                .status(ConferenceStatus.ACTIVE)
                .eventId("evt-1")
                .version(0L)
                .lastModifiedDate(java.time.Instant.parse("2026-01-02T03:04:05Z"))
                .build());

        GoogleConferenceEntity duplicate = GoogleConferenceEntity.builder()
                .roomId("room-2")
                .userId(2L)
                .serverId(5L)
                .status(ConferenceStatus.PENDING)
                .eventId("evt-1")
                .version(0L)
                .lastModifiedDate(java.time.Instant.parse("2026-01-02T03:04:05Z"))
                .build();

        assertThatThrownBy(() -> googleConferenceRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
