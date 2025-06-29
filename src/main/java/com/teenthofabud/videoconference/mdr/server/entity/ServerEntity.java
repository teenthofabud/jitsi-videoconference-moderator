package com.teenthofabud.videoconference.mdr.server.entity;

import com.teenthofabud.videoconference.mdr.serverpreference.entity.ServerPreferenceEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "server")
    @ToString.Exclude
    List<ServerPreferenceEntity> serverPreferences = new CopyOnWriteArrayList<>();

    public ServerPreferenceEntity addServerPreference(ServerPreferenceEntity serverPreference){
        serverPreference.setServer(this);
        this.serverPreferences.add(serverPreference);
        return serverPreference;
    }

    public void removeServerPreference(ServerPreferenceEntity serverPreference){
        if(!ObjectUtils.isEmpty(this.serverPreferences)){
            this.serverPreferences.remove(serverPreference);
        }
        serverPreference.setServer(null);
    }
}
