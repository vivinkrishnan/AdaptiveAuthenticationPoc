package com.adaptive.springboot.adaptiveauthpoc.weight;

public enum Device {
    
    screenHeight(1),
    screenWidth(1),
    language(1),
    locale(1),
    timeZone(1),
    timeZoneOffset(1),
    model(1),
    platform(1),
    manufacturer(1),
    uuid(1),
    userAgent(1);

    private int strength;

    Device(){

    }

    Device(int strength){
        this.strength=strength;
    }

    public int getStrength(){
        return this.strength;
    }


}
