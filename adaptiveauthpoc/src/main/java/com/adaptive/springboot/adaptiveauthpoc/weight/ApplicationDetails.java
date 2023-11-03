package com.adaptive.springboot.adaptiveauthpoc.weight;

public enum ApplicationDetails {

    Name("AdaptiveAuthenticationApp"),
    PackageName("com.phd.adaptiveauth"),
    VersionNumber("1.0.0"),
    VersionCode("10000");
    
    private String detail;

    ApplicationDetails(){

    }

    ApplicationDetails(String detail){
        this.detail = detail;
    }

    public String getDetail(){
        return this.detail;
    }
   
}
