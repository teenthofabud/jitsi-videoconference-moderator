package com.teenthofabud.videoconference.mdr.conference.entity;

import com.teenthofabud.videoconference.mdr.conference.constants.ConferenceStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;


@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
@MappedSuperclass
public abstract class ConferenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "room_id", nullable = false)
    String roomId;

    @Column(name = "server_preference_id", nullable = false)
    Long serverPreferenceId;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    ConferenceStatus status;


}
