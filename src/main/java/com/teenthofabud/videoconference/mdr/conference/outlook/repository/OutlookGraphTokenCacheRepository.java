package com.teenthofabud.videoconference.mdr.conference.outlook.repository;

import com.teenthofabud.videoconference.mdr.conference.outlook.entity.OutlookGraphTokenCacheEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OutlookGraphTokenCacheRepository extends JpaRepository<OutlookGraphTokenCacheEntity, Long> {
    Optional<OutlookGraphTokenCacheEntity> findByPendingStateHash(String stateHash);
    Optional<OutlookGraphTokenCacheEntity> findBySubscriptionId(String subscriptionId);
}
