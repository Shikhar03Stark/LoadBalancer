package com.hv.harshit.balancer.balancerapplication;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;


@Slf4j
@SpringBootApplication
public class BalancerApplication {

    private static ConfigurableApplicationContext run(String[] args){
        return SpringApplication.run(BalancerApplication.class, args);
    }
    public static void main(String[] args) {
        run(args);
    }

    @PreDestroy
    private void onExit(){
        log.info("Exiting Load Balancer. Stopping all instances of the image");
    }

}
