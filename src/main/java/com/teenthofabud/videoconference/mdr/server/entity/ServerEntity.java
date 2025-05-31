package com.teenthofabud.videoconference.mdr.server.entity;

import com.teenthofabud.videoconference.mdr.serverpreference.entity.ServerPreferenceEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ServerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "name", nullable = false, unique = true)
    String name;

    @Column(name = "server_url", nullable = false, unique = true)
    String serverUrl;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "server_url")
    List<ServerPreferenceEntity> serverPreference;
}
