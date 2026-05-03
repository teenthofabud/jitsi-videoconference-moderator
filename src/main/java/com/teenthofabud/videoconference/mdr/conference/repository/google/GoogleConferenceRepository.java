package com.teenthofabud.videoconference.mdr.conference.repository.google;

import com.teenthofabud.videoconference.mdr.conference.entity.google.GoogleConferenceEntity;
import com.teenthofabud.videoconference.mdr.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GoogleConferenceRepository extends JpaRepository<GoogleConferenceEntity,Long> {

    List<GoogleConferenceEntity> findAllByOrganizerEmail(String email);

    Optional<GoogleConferenceEntity> findByRoomId(String roomId);

    Optional<GoogleConferenceEntity> findByOrganizerEmailAndEventId(String email,String eventId);

}
