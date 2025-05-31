package com.teenthofabud.videoconference.mdr.user.entity;

import com.teenthofabud.videoconference.mdr.serverpreference.entity.ServerPreferenceEntity;
import com.teenthofabud.videoconference.mdr.user.constants.ReferenceType;
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
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    String userId;

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "referenceType", nullable = false)
    ReferenceType referenceType;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    ServerPreferenceEntity serverPreference;

}
