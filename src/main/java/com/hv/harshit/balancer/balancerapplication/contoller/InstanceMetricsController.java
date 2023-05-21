package com.hv.harshit.balancer.balancerapplication.contoller;

import com.hv.harshit.balancer.balancerapplication.contoller.dto.InstanceMetricResponseDto;
import com.hv.harshit.balancer.balancerapplication.service.InstanceMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/~/metrics")
public class InstanceMetricsController {

    private final InstanceMetrics instanceMetrics;

    @GetMapping("/summary")
    public HttpEntity<List<InstanceMetricResponseDto>> getInstanceMetricsSummary(){
        return new ResponseEntity<>(instanceMetrics.getAllInstanceMetrics(), HttpStatus.OK);
    }

}
