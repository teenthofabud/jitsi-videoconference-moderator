package com.teenthofabud.videoconference.mdr.serverpreference.entity;

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
public class ServerPreferenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "server_url", nullable = false)
    String serverUrl;

    @Column(name = "user_id", nullable = false)
    String userId;
}
