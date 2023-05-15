package com.hv.harshit.balancer.balancerapplication.persistance.entity;

import com.hv.harshit.balancer.balancerapplication.enums.ContainerStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "instance")
@NoArgsConstructor
public class InstanceEntity{
    @Id
    private String containerId;
    private String containerName;
    @Enumerated(EnumType.STRING)
    private ContainerStatus status;
    private int exposedPort;
    private int hostBindPort;
    private long createdAt;
}
