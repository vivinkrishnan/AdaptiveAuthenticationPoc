package com.adaptive.springboot.adaptiveauthpoc.weight;

public enum StrengthRequired {

    LoginStrengthRequired(20),
    ResourceRequestStrengthRequired(20);

    private int strengthRequired;

    StrengthRequired(){}

    StrengthRequired(int strengthRequired){
        this.strengthRequired = strengthRequired;
    }

    public int getStrengthRequired(){
        return this.strengthRequired;
    }
    
}
