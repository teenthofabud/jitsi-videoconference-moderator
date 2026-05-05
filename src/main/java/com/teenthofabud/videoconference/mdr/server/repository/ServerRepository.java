package com.teenthofabud.videoconference.mdr.server.repository;

import com.teenthofabud.videoconference.mdr.server.entity.ServerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ServerRepository extends JpaRepository<ServerEntity,Long>{

    Optional<ServerEntity> findByOwnerEmail(String email);
}
