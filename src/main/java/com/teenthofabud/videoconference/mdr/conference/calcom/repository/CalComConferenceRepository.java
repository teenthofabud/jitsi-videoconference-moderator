package com.teenthofabud.videoconference.mdr.conference.calcom.repository;

import com.teenthofabud.videoconference.mdr.conference.calcom.entity.CalComConferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CalComConferenceRepository extends JpaRepository<CalComConferenceEntity, Long> {

    Optional<CalComConferenceEntity> findByBookingUid(String bookingUid);

    Optional<CalComConferenceEntity> findByUserIdAndBookingUid(Long userId, String bookingUid);
}
