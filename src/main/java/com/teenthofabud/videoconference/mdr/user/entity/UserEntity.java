package com.teenthofabud.videoconference.mdr.user.entity;

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
@Table(name = "principal")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "subject", nullable = false, unique = true)
    String userId;

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "reference_type", nullable = false)
    @Enumerated(EnumType.STRING)
    ReferenceType referenceType;


}
