package com.teenthofabud.videoconference.mdr.serverpreference.entity;

import com.teenthofabud.videoconference.mdr.server.entity.ServerEntity;
import com.teenthofabud.videoconference.mdr.serverpreference.constants.ServerPreferenceType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "server_preference")
public class ServerPreferenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "server_url", nullable = false)
    @EqualsAndHashCode.Include
    String serverUrl;

    @Column(name = "user_id", nullable = false, unique = true)
    @EqualsAndHashCode.Include
    String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    ServerEntity server;

    @Enumerated(EnumType.STRING)
    ServerPreferenceType serverPreferenceType;
}
