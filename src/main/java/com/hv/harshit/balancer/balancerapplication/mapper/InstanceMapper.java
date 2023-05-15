package com.hv.harshit.balancer.balancerapplication.mapper;

import com.hv.harshit.balancer.balancerapplication.model.Instance;
import com.hv.harshit.balancer.balancerapplication.persistance.entity.InstanceEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InstanceMapper {

    InstanceEntity map(Instance instance);

    Instance map(InstanceEntity instanceEntity);
}
