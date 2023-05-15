package com.hv.harshit.balancer.balancerapplication.exception;

public class NoRunningInstanceException extends Exception{

    @Override
    public String getMessage() {
        return String.format("No running instances found. %s", super.getMessage());
    }


}
