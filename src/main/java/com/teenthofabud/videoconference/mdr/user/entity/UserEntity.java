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

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "reference_type", nullable = false)
    @Enumerated(EnumType.STRING)
    ReferenceType referenceType;

    @OneToOne(mappedBy = "owner", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private ServerEntity preferredServer;

    @OneToMany(cascade = CascadeType.PERSIST, mappedBy = "createdBy")
    @ToString.Exclude
    List<GoogleConferenceEntity> conferences = new ArrayList<>();

    public void addConference(GoogleConferenceEntity conference){
        if(conference == null)
            return;
        conferences.add(conference);
        conference.setCreatedBy(this);
    }

    public void removeConference(GoogleConferenceEntity conference){
        if(conference == null)
            return;
        conferences.remove(conference);
        conference.setCreatedBy(null);
    }

}
