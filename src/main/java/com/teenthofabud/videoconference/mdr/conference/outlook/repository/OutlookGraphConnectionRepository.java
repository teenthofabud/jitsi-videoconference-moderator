package com.teenthofabud.videoconference.mdr.conference.outlook.repository;

import com.teenthofabud.videoconference.mdr.conference.outlook.entity.OutlookGraphConnectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OutlookGraphConnectionRepository extends JpaRepository<OutlookGraphConnectionEntity, Long> {
    Optional<OutlookGraphConnectionEntity> findByPendingStateHash(String stateHash);
    Optional<OutlookGraphConnectionEntity> findBySubscriptionId(String subscriptionId);
}
