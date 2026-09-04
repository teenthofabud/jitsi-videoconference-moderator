package com.teenthofabud.videoconference.mdr.server.repository;

import com.teenthofabud.videoconference.mdr.server.constants.ServerType;
import com.teenthofabud.videoconference.mdr.server.entity.ServerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ServerRepository extends JpaRepository<ServerEntity,Long>{

    boolean existsByUserIdAndServerUrl(Long userId, String serverUrl);

    boolean existsByServerUrlAndServerType(String serverUrl, ServerType type);

    List<ServerEntity> findAllByUserId(Long userId);

    List<ServerEntity> findAllByServerType(ServerType serverType);
}
