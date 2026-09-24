package com.teenthofabud.videoconference.mdr.conference.entity;

import com.teenthofabud.videoconference.mdr.conference.constants.ConferenceStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class ConferenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "room_id", nullable = false)
    String roomId;

    // Organizer's userId
    @Column(name = "user_id", nullable = false)
    Long userId;

    @Column(name = "server_id", nullable = false)
    Long serverId;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    ConferenceStatus status;

    @Version
    @Column(name = "version", nullable = false)
    Long version;

    @LastModifiedDate
    @Column(name = "last_modified_date", nullable = false)
    java.time.Instant lastModifiedDate;


}
