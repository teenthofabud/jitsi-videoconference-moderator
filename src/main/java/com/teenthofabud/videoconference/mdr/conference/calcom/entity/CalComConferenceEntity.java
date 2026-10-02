package com.teenthofabud.videoconference.mdr.conference.calcom.entity;

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
@Table(name = "calcom_conference")
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
public class CalComConferenceEntity extends ConferenceEntity {

    /** Cal.com event.uid; the stable reference used by webhook integrations. */
    @Column(name = "booking_uid", nullable = false, unique = true)
    String bookingUid;

    /** Cal.com internal booking ID. */
    @Column(name = "booking_id", unique = true)
    Long bookingId;
}
