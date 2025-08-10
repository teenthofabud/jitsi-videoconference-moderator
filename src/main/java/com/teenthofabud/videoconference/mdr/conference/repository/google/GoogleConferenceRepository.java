package com.teenthofabud.videoconference.mdr.conference.repository.google;

import com.teenthofabud.videoconference.mdr.conference.entity.google.GoogleConferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GoogleConferenceRepository extends JpaRepository<GoogleConferenceEntity,Long> {

    List<GoogleConferenceEntity> findAllByServerPreferenceId(Long serverPreferenceId);

    Optional<GoogleConferenceEntity> findByServerPreferenceIdAndEventId(Long serverPreferenceId, String roomId);
    
}
