package com.adaptive.springboot.adaptiveauthpoc.weight;

public enum Penalty {
    
    GeoVelocityFail(10),
    CredentialsMismatch(3),
    PinNumberMismatch(3),
    SecurityQuestionMismatch(3),
    OTPMismatch(4),
    CAPTCHAMismatch(4),
    GraphicalPasswordMismatch(5),
    TouchMismatch(6),
    DigitalCertificateMismatch(7),
    MaxRetries(3),
    TimeOfLoginMismatch(1),
    CoordinatesToIPLocationMismatch(3);

    private int penalty;

    Penalty(){}

    Penalty(int penalty){
        this.penalty = penalty;
    }

    public int getPenalty(){
        return this.penalty;
    }
}
