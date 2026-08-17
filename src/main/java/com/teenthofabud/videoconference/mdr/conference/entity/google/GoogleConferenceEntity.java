package com.teenthofabud.videoconference.mdr.conference.entity.google;

import com.teenthofabud.videoconference.mdr.conference.entity.ConferenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@ToString(callSuper = true)
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
@Table(name = "google_conference")
public class GoogleConferenceEntity extends ConferenceEntity {

    @Column(name = "event_id", nullable = false, unique = true)
    String eventId;
}
