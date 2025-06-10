package com.teenthofabud.videoconference.mdr.user.repository;

import com.teenthofabud.videoconference.mdr.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity,Long> {

    Optional<UserEntity> findByUserId(String userId);
}
