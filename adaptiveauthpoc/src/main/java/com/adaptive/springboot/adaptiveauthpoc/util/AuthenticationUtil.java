package com.adaptive.springboot.adaptiveauthpoc.util;

import com.adaptive.springboot.adaptiveauthpoc.weight.AuthCredentials;
import com.adaptive.springboot.adaptiveauthpoc.weight.AuthMethods;

public class AuthenticationUtil {
    
    public static int getAuthStrength(String authMethod){
        int auth_strength = 0;

        switch(authMethod){

            case "UserNameCredentials" :
                    auth_strength += AuthMethods.UserNameCredentials.getStrength();
            case "PinNumber":
                    auth_strength += AuthMethods.PinNumber.getStrength();
            case "SecurityQuestion" :
                    auth_strength += AuthMethods.SecurityQuestion.getStrength();
            case "OTP" :
                    auth_strength += AuthMethods.OTP.getStrength();
            case "CAPTCHA" :
                    auth_strength += AuthMethods.CAPTCHA.getStrength();
            case "GraphicalPassword" :
                    auth_strength += AuthMethods.GraphicalPassword.getStrength();
            case "Touch" :
                    auth_strength += AuthMethods.Touch.getStrength();
            case "DigitalCertificate" :
                    auth_strength += AuthMethods.DigitalCertificate.getStrength();
            default : auth_strength = 0;
        }

        return auth_strength;
    }

    public static boolean credentialsMatch(String authMethod,String credentials){
        boolean match = false;

        switch(authMethod){
            case "UserNameCredentials" :
                    match = credentials.equals(AuthCredentials.UserNameCredentials.getValue());
                    break;
            case "PinNumber":
                    match = credentials.equals(AuthCredentials.PinNumber.getValue());
                    break;
            case "SecurityQuestion" :
                    match = credentials.equals(AuthCredentials.SecurityQuestion.getValue());
                    break;
            case "OTP" :
                    match = credentials.equals(AuthCredentials.OTP.getValue());
                    break;
            case "CAPTCHA" :
                    match = credentials.equals(AuthCredentials.CAPTCHA.getValue());
                    break;
            case "GraphicalPassword" :
                    match = credentials.equals(AuthCredentials.GraphicalPassword.getValue());
                    break;
            case "Touch" :
                    match = credentials.equals(AuthCredentials.Touch.getValue());
                    break;
            case "DigitalCertificate" :
                    match = credentials.equals(AuthCredentials.DigitalCertificate.getValue());
                    break;
            default : match = false;
                    break;

        }

       return match; 
    }
}


    