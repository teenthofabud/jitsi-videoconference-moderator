package com.teenthofabud.videoconference.mdr.user.repository;

import com.teenthofabud.videoconference.mdr.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity,Long> {

    UserEntity findByUserId(String userId);
}
