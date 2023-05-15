package com.hv.harshit.balancer.balancerapplication.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Semaphore;

@Slf4j
public class WindowQueue {
    private final Semaphore runningSumSemaphore = new Semaphore(1);
    private final Semaphore windowSemaphore = new Semaphore(1);
    private final long totalLength;
    private final Queue<Long> window;
    private long runningSum = 0;

    public WindowQueue(long totalLength) {
        this.totalLength = totalLength;
        window = new LinkedList<>();
    }

    public void push(Long element){
        acquireLock(runningSumSemaphore);
        acquireLock(windowSemaphore);

        if(window.isEmpty() || window.size() < totalLength){
           runningSum += element;
           window.add(element);
        } else {
            final long frontValue = window.peek();
            runningSum += element - frontValue;

            window.add(element);
            window.poll();
        }

        releaseLock(windowSemaphore);
        releaseLock(runningSumSemaphore);
    }

    public List<Long> elementsInWindow(){
        acquireLock(windowSemaphore);

        final List<Long> elements = new ArrayList<>();
        for(int rotation = 0; rotation < window.size(); rotation++){
            elements.add(window.peek());

            window.add(window.peek());
            window.remove();
        }

        releaseLock(windowSemaphore);

        return elements;
    }

    private void acquireLock(Semaphore semaphore){
        try {
            semaphore.acquire();
        } catch (InterruptedException exception){
            log.info("Waiting for acquiring semaphore");
        }
    }

    private void releaseLock(Semaphore semaphore){
        semaphore.release();
    }

    public double average(){
        if(window.isEmpty()){
            return 0;
        }
        return runningSum/(double) window.size();
    }
}
