package com.adaptive.springboot.adaptiveauthpoc.weight;

public enum AuthCredentials {
    
    UserNameCredentials("username+password"),
    PinNumber("1234"),
    SecurityQuestion("Mother"),
    OTP("123456"),
    CAPTCHA("abcd"),
    GraphicalPassword("image"),
    Touch("Touch"),
    DigitalCertificate("Certificate");

    AuthCredentials(){}

    private String value;

    AuthCredentials(String value){
        this.value = value;
    }

    public String getValue(){
        return this.value;
    }

}
