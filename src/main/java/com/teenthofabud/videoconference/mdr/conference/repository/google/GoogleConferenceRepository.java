package com.teenthofabud.videoconference.mdr.conference.repository.google;

import com.teenthofabud.videoconference.mdr.conference.entity.google.GoogleConferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GoogleConferenceRepository extends JpaRepository<GoogleConferenceEntity,Long> {

    Optional<GoogleConferenceEntity> findByServerPreferenceId(Long serverPreferenceId);
}
