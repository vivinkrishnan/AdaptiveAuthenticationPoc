package com.adaptive.springboot.adaptiveauthpoc.weight;

import java.util.stream.Stream;

public enum AuthMethods {

    UserNameCredentials(5),
    PinNumber(5),
    SecurityQuestion(5),
    OTP(6),
    CAPTCHA(6),
    GraphicalPassword(7),
    Touch(7),
    DigitalCertificate(8);

    private int strength;

    AuthMethods(){}
    
    AuthMethods(int strength){
        this.strength = strength;
    }

    public int getStrength(){
        return this.strength;
    }

    public static Stream<AuthMethods> stream() {
        return Stream.of(AuthMethods.values()); 
    }
}
