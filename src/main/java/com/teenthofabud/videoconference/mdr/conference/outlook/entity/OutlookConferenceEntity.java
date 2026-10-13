package com.teenthofabud.videoconference.mdr.conference.outlook.entity;

import com.teenthofabud.videoconference.mdr.conference.entity.ConferenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "outlook_conference")
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
public class OutlookConferenceEntity extends ConferenceEntity {

    @Column(name = "draft_tracking_id", nullable = false, unique = true)
    String draftTrackingId;

    @Column(name = "event_id", unique = true)
    String eventId;
}
