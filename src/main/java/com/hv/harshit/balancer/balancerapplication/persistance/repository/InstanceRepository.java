package com.hv.harshit.balancer.balancerapplication.persistance.repository;

import com.hv.harshit.balancer.balancerapplication.persistance.entity.InstanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstanceRepository extends JpaRepository<InstanceEntity, String> {

    @Modifying(flushAutomatically = true)
    @Query(value = "update instance set status = 'STOPPED' where container_id = :containerId", nativeQuery = true)
    void markStopped(@Param("containerId") String containerId);

    @Query(value = "select * from instance where status = 'RUNNING'", nativeQuery = true)
    List<InstanceEntity> findAllRunningContainers();
}
