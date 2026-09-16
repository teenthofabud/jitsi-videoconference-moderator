package com.teenthofabud.videoconference.mdr.conference.repository.google;

import com.teenthofabud.videoconference.mdr.conference.entity.google.GoogleConferenceEntity;
import com.teenthofabud.videoconference.mdr.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GoogleConferenceRepository extends JpaRepository<GoogleConferenceEntity,Long> {

    List<GoogleConferenceEntity> findAllByUserId(Long userId);

    Optional<GoogleConferenceEntity> findByRoomId(String roomId);

    Optional<GoogleConferenceEntity> findByUserIdAndEventId(Long userId,String eventId);

}
