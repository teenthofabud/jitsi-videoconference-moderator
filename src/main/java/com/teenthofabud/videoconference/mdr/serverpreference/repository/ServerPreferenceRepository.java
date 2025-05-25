package com.teenthofabud.videoconference.mdr.serverpreference.repository;

import com.teenthofabud.videoconference.mdr.serverpreference.entity.ServerPreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServerPreferenceRepository extends JpaRepository<ServerPreferenceEntity,Long> {

    ServerPreferenceEntity findByServerUrl(String serverUrl);
}
