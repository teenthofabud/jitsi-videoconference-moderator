package com.teenthofabud.videoconference.mdr.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@Entity
@EntityListeners(AuditingEntityListener.class)
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

    @Version
    @Column(name = "version", nullable = false)
    Long version;

    @LastModifiedDate
    @Column(name = "last_modified_date", nullable = false)
    java.time.Instant lastModifiedDate;

}
