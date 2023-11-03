package com.adaptive.springboot.adaptiveauthpoc.util;

import com.adaptive.springboot.adaptiveauthpoc.weight.StrengthRequired;

public class TrustCalculatorUtil {
    
    public static int getAppTrust(int trust){

        return 0;

    }

    public static int getTrustDeficit(int trust, String action){

        int trust_deficit = 0;

        if(action.equals("login")){
            trust_deficit = StrengthRequired.LoginStrengthRequired.getStrengthRequired() - trust;
        }else{
            trust_deficit = StrengthRequired.ResourceRequestStrengthRequired.getStrengthRequired() - trust;
        }

        return trust_deficit;
    }


}
