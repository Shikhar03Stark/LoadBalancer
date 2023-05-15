package com.hv.harshit.balancer.balancerapplication.enums;

public enum WindowSize {
    TEN(10L),
    HUNDRED(100L),
    THOUSAND(1000L),
    TEN_THOUSAND(10000L),
    HUNDRED_THOUSAND(100000L),
    MILLION(1000000);

    private final long length;
    WindowSize(long length){
        this.length = length;
    }

    public long getLength() {
        return length;
    }
}
