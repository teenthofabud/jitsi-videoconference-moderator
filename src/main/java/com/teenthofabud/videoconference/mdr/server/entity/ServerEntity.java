package com.teenthofabud.videoconference.mdr.server.entity;

import com.teenthofabud.videoconference.mdr.server.constants.ServerType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "server", uniqueConstraints = {
    @UniqueConstraint(
        name = "uk_user_server",
        columnNames = {"user_id", "server_url"}
    )
})
public class ServerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "name", nullable = false, unique = false)
    @EqualsAndHashCode.Include
    String name;

    @Column(name = "server_url", nullable = false, unique = false)
    @EqualsAndHashCode.Include
    String serverUrl;

    @Column(name = "user_id", nullable = true)
    @EqualsAndHashCode.Include
    Long userId;

    @Column(name = "server_type", nullable = false)
    @Enumerated(EnumType.STRING)
    ServerType serverType;

    @Version
    @Column(name = "version", nullable = false)
    Long version;

    @LastModifiedDate
    @Column(name = "last_modified_date", nullable = false)
    java.time.Instant lastModifiedDate;

}
