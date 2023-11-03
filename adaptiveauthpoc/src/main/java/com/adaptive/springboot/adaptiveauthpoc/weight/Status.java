package com.adaptive.springboot.adaptiveauthpoc.weight;

public enum Status {

    FAIL("FAIL"),
    SUCCESS("SUCCESS"),
    CHALLENGE("CHALLENGE"),
    NEWAUTHMETHOD("NEW AUTH METHOD"),
    BLOCKED("BLOCKED"),
    EXPIRED("EXPIRED");

    private String statusVal;

    Status(){}

    Status(String statusVal){
        this.statusVal = statusVal;
    }

    public String getStatusVal(){
        return statusVal;
    }


}
