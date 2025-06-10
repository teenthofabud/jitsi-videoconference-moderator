package com.teenthofabud.videoconference.mdr.serverpreference.repository;

import com.teenthofabud.videoconference.mdr.serverpreference.entity.ServerPreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServerPreferenceRepository extends JpaRepository<ServerPreferenceEntity,Long> {

    Optional<ServerPreferenceEntity> findByServerUrl(String serverUrl);

    Optional<ServerPreferenceEntity> findByUserId(String userId);
}
