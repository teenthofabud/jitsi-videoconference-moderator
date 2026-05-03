package com.teenthofabud.videoconference.mdr.conference.entity;

import com.teenthofabud.videoconference.mdr.conference.constants.ConferenceStatus;
import com.teenthofabud.videoconference.mdr.server.entity.ServerEntity;
import com.teenthofabud.videoconference.mdr.user.entity.UserEntity;
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

    @Column(name = "organizer_email", nullable = false)
    String organizerEmail;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    ConferenceStatus status;


}
