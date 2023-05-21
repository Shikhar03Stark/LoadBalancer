package com.hv.harshit.balancer.balancerapplication.service.impl;

import com.hv.harshit.balancer.balancerapplication.contoller.dto.InstanceMetricResponseDto;
import com.hv.harshit.balancer.balancerapplication.enums.WindowSize;
import com.hv.harshit.balancer.balancerapplication.mapper.InstanceMapper;
import com.hv.harshit.balancer.balancerapplication.model.Instance;
import com.hv.harshit.balancer.balancerapplication.persistance.repository.InstanceRepository;
import com.hv.harshit.balancer.balancerapplication.service.InstanceMetrics;
import com.hv.harshit.balancer.balancerapplication.utils.CostCalculator;
import com.hv.harshit.balancer.balancerapplication.utils.WindowQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstanceMetricsImpl implements InstanceMetrics {

    private final InstanceRepository instanceRepository;
    private final InstanceMapper instanceMapper;
    private Map<String, Instance> _instanceMap = new HashMap<>();
    private final Map<String, Long> serveCountMap = new ConcurrentHashMap<>();
    private final Map<String, Long> failCountMap = new ConcurrentHashMap<>();
    private final List<WindowSize> windowSizes = Arrays.asList(WindowSize.TEN, WindowSize.HUNDRED, WindowSize.THOUSAND, WindowSize.TEN_THOUSAND);
    private Map<String, Map<WindowSize, WindowQueue>> _responseTimeQueueMap;

    public Map<String, Map<WindowSize, WindowQueue>> responseTimeQueueMap() {
        if(CollectionUtils.isEmpty(_responseTimeQueueMap)){
            final Map<String, Instance> instances = runningInstances();
            final Map<String, Map<WindowSize, WindowQueue>> map = new ConcurrentHashMap<>();
            instances
                    .keySet()
                    .forEach(containerId -> map.put(containerId, initWindows()));
            this._responseTimeQueueMap = map;
        }
        return this._responseTimeQueueMap;
    }

    private Map<WindowSize, WindowQueue> initWindows() {
        final Map<WindowSize, WindowQueue> map = new ConcurrentHashMap<>();
        windowSizes.forEach(windowSize -> map.put(windowSize, new WindowQueue(windowSize.getLength())));
        return map;
    }

    @Override
    public void setLatestResponseTime(String containerId, long responseTime) {
        final Map<WindowSize, WindowQueue> queueMap = responseTimeQueueMap().get(containerId);
        windowSizes.forEach(windowSize -> queueMap.get(windowSize).push(responseTime));
    }

    @Override
    public void increaseServeCount(String containerId) {
        long previousCount = 0;
        if (serveCountMap.containsKey(containerId)) {
            previousCount = serveCountMap.get(containerId);
        }
        serveCountMap.put(containerId, previousCount + 1);
    }

    @Override
    public void increaseFailCount(String containerId) {
        long previousCount = 0;
        if (failCountMap.containsKey(containerId)) {
            previousCount = failCountMap.get(containerId);
        }
        failCountMap.put(containerId, previousCount + 1);
    }

    @Override
    public double getServeRatio(String containerId) {
        final long serveCount = serveCountMap.getOrDefault(containerId, 0L);
        final long failCount = failCountMap.getOrDefault(containerId, 0L);

        return serveCount/(1.0 + serveCount + failCount);
    }
    @Override
    public double getAvgResponseTime(String containerId, WindowSize windowSize) {
        return responseTimeQueueMap().get(containerId).get(windowSize).average();
    }

    @Override
    public List<WindowSize> getWindowSizes() {
        return windowSizes;
    }

    @Override
    public long getServeCount(String containerId) {
        return serveCountMap.getOrDefault(containerId, 0L);
    }

    @Override
    public long getFailCount(String containerId) {
        return failCountMap.getOrDefault(containerId, 0L);
    }

    @Override
    public InstanceMetricResponseDto getInstanceMetrics(String containerId) {
        final Map<WindowSize, WindowQueue> queueMap = responseTimeQueueMap().get(containerId);
        final Map<WindowSize, Double> avgResponseTime = new HashMap<>();
        windowSizes.forEach(windowSize -> avgResponseTime.put(windowSize, queueMap.get(windowSize).average()));
        return buildResponseDto(containerId, avgResponseTime);
    }

    private InstanceMetricResponseDto buildResponseDto(String containerId, Map<WindowSize, Double> avgResponseTime) {
        final double weightedCost = CostCalculator.weightedCost(containerId, this);
        final double bestCost = CostCalculator.bestCost(containerId, this);

        final Map<String, Double> costMap = new HashMap<>();
        costMap.put("weightedCost", weightedCost);
        costMap.put("bestCost", bestCost);

        return InstanceMetricResponseDto.builder()
                .containerId(containerId)
                .avgResponseTime(avgResponseTime)
                .serveCount(serveCountMap.getOrDefault(containerId, 0L))
                .failCount(failCountMap.getOrDefault(containerId, 0L))
                .serveRatio(getServeRatio(containerId))
                .costs(costMap)
                .build();
    }

    @Override
    public List<InstanceMetricResponseDto> getAllInstanceMetrics() {
        return runningInstances()
                .keySet()
                .stream()
                .map(this::getInstanceMetrics)
                .collect(Collectors.toList());
    }

    private Map<String, Instance> runningInstances() {
        if (CollectionUtils.isEmpty(this._instanceMap)) {
            this._instanceMap = instanceRepository
                    .findAllRunningContainers()
                    .stream()
                    .map(instanceMapper::map)
                    .collect(Collectors.toMap(Instance::getContainerId, instance -> instance));
        }
        return this._instanceMap;
    }
}
