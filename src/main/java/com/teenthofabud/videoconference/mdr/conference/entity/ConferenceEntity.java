package com.teenthofabud.videoconference.mdr.conference.entity;

import com.teenthofabud.videoconference.mdr.conference.constants.ConferenceStatus;
import com.teenthofabud.videoconference.mdr.conference.converter.ConferenceStatusAttributeConverter;
import jakarta.persistence.*;
import lombok.*;

@Entity
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ConferenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "room_id", nullable = false)
    String roomId;

    @Column(name = "user_id", nullable = false)
    String userId;

    @Column(name = "server_url", nullable = false)
    String serverUrl;

    @Column(name = "status", nullable = false)
    @Convert(converter = ConferenceStatusAttributeConverter.class  )
    ConferenceStatus status;


}
