package com.hv.harshit.balancer.balancerapplication.contoller;

import com.hv.harshit.balancer.balancerapplication.service.BalancerService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
@Controller
@RequiredArgsConstructor
public class BalancerController {

    private final BalancerService balancerService;

    @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}, produces = MediaType.ALL_VALUE)
    public HttpEntity<String> proxyRequest(final HttpServletRequest servletRequest) throws IOException {
        final ServletServerHttpRequest serverHttpRequest = new ServletServerHttpRequest(servletRequest);
        final String requestBody = IOUtils.toString(serverHttpRequest.getBody());
        return balancerService.proxyRequest(serverHttpRequest, requestBody);
    }
}
