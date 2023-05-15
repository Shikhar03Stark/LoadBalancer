package com.hv.harshit.balancer.balancerapplication.service;

import com.hv.harshit.balancer.balancerapplication.model.BalancerDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;

public interface BalancerService {

    BalancerDetail getBalancerDetails();

    ResponseEntity<String> proxyRequest(ServerHttpRequest request, String body);
}
