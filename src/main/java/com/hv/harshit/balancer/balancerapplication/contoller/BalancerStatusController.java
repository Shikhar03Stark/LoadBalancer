package com.hv.harshit.balancer.balancerapplication.contoller;

import com.hv.harshit.balancer.balancerapplication.model.BalancerDetail;
import com.hv.harshit.balancer.balancerapplication.service.BalancerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/~/balancer")
@RequiredArgsConstructor
public class BalancerStatusController {

    private final BalancerService balancerService;

    @GetMapping("/info")
    public HttpEntity<BalancerDetail> getBalancerDetail(){
        return new ResponseEntity<>(balancerService.getBalancerDetails(), HttpStatus.OK);
    }
}
