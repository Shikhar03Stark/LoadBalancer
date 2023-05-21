package com.hv.harshit.balancer.balancerapplication.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.concurrent.Semaphore;

@Slf4j
public final class SemaphoreUtils {

    public static void acquireLock(Semaphore semaphore){
        try {
            semaphore.acquire();
        } catch (InterruptedException exception){
            log.info("Waiting for acquiring semaphore. stackTrace={}", ExceptionUtils.getStackTrace(exception));
        }
    }

   public static void releaseLock(Semaphore semaphore){
        semaphore.release();
    }

}
