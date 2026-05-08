package com.teenthofabud.videoconference.mdr.server.entity;

import com.teenthofabud.videoconference.mdr.conference.entity.ConferenceEntity;
import com.teenthofabud.videoconference.mdr.conference.entity.google.GoogleConferenceEntity;
import com.teenthofabud.videoconference.mdr.server.constants.ServerType;
import com.teenthofabud.videoconference.mdr.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "server")
public class ServerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "name", nullable = false, unique = true)
    @EqualsAndHashCode.Include
    String name;

    @Column(name = "server_url", nullable = false, unique = true)
    @EqualsAndHashCode.Include
    String serverUrl;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserEntity owner;

    @Column(name = "server_type", nullable = false)
    @Enumerated(EnumType.STRING)
    ServerType serverType;

}
