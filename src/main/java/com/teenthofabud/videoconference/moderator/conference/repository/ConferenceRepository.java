package com.teenthofabud.videoconference.moderator.conference.repository;

import com.teenthofabud.videoconference.moderator.conference.entity.ConferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConferenceRepository extends JpaRepository<ConferenceEntity,Long> {

    ConferenceEntity findByUserRef(String userRef);
}
