package com.teenthofabud.videoconference.mdr.conference.outlook.repository;

import com.teenthofabud.videoconference.mdr.conference.outlook.entity.OutlookConferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OutlookConferenceRepository extends JpaRepository<OutlookConferenceEntity, Long> {

    Optional<OutlookConferenceEntity> findByUserIdAndDraftTrackingId(Long userId, String draftTrackingId);

    Optional<OutlookConferenceEntity> findByDraftTrackingId(String draftTrackingId);

    List<OutlookConferenceEntity> findAllByUserId(Long userId);
}
