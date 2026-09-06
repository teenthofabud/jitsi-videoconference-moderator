package com.teenthofabud.videoconference.mdr.user.entity;

import com.teenthofabud.videoconference.mdr.conference.entity.ConferenceEntity;
import com.teenthofabud.videoconference.mdr.conference.entity.google.GoogleConferenceEntity;
import com.teenthofabud.videoconference.mdr.server.entity.ServerEntity;
import com.teenthofabud.videoconference.mdr.user.constants.ReferenceType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "principal")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "email", nullable = false, unique = true)
    String email;

    @Column(name = "name", nullable = true)
    String name;

    @Column(name = "is_managed", nullable = false)
    Boolean managed;

}
