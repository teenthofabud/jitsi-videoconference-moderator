package com.teenthofabud.videoconference.mdr.conference.repository;

import com.teenthofabud.videoconference.mdr.conference.entity.ConferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConferenceRepository extends JpaRepository<ConferenceEntity,Long> {

    List<ConferenceEntity> findByUserId(String userId);
}
