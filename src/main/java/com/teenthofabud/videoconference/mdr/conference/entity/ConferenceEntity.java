package com.teenthofabud.videoconference.mdr.conference.entity;

import com.teenthofabud.videoconference.mdr.conference.constants.ConferenceStatus;
import com.teenthofabud.videoconference.mdr.conference.converter.ConferenceStatusAttributeConverter;
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

    @Column(name = "room_id", nullable = false, unique = true)
    String roomId;

    @Column(name = "event_id", nullable = false, unique = true)
    String eventId;

    @Column(name = "server_preference_id", nullable = false, unique = true)
    Long serverPreferenceId;

    @Column(name = "status", nullable = false)
    @Convert(converter = ConferenceStatusAttributeConverter.class  )
    ConferenceStatus status;


}
