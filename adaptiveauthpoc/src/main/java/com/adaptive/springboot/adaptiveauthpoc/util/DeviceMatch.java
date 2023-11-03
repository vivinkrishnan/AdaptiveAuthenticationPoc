package com.adaptive.springboot.adaptiveauthpoc.util;

import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import com.adaptive.springboot.adaptiveauthpoc.weight.Device;

public class DeviceMatch {
    
    public static int deviceMatch(JSONObject devicePayload,JSONObject deviceRegistered){

        int trust =0;
        int missedCount = 0;

         // if physical attributes do not match , flag it.
         // else proceed to obtain device strength.
        
        try {
            
            JSONObject payloadLocalization = devicePayload.getJSONObject("localization");
            JSONObject registeredLocalization = new JSONObject(deviceRegistered.getString("localization"));
            JSONObject payloadScreensize = new JSONObject(devicePayload.getJSONObject("details").getString("screenSize"));
            JSONObject deviceDetailsRegistered = new JSONObject(deviceRegistered.getString("device_details"));
            JSONObject registeredScreensize = new JSONObject(deviceDetailsRegistered.getString("screenSize"));
            

            if(payloadLocalization.getString("language").equals(registeredLocalization.getString("language"))){
                trust += Device.language.getStrength();
            }else{ missedCount++; }

            if(payloadLocalization.getString("locale").equals(registeredLocalization.getString("locale"))){
                trust += Device.locale.getStrength();
            }else{ missedCount++; }

            if(payloadLocalization.getString("timeZone").equals(registeredLocalization.getString("timeZone")))
                    trust += Device.timeZone.getStrength();
            else
                    missedCount++;        

            if(payloadLocalization.getString("timeZoneOffset").equals(registeredLocalization.getString("timeZoneOffset")))
                    trust += Device.timeZoneOffset.getStrength();    
            else
                    missedCount++;        

            if((payloadScreensize.getString("height").equals(registeredScreensize.getString("height"))) & 
                (payloadScreensize.getString("width").equals(registeredScreensize.getString("width")))
            ){
                trust += Device.screenHeight.getStrength();
                trust += Device.screenWidth.getStrength();
            }else{ missedCount++;}
            
            if(devicePayload.getJSONObject("details").getString("model").equals(deviceDetailsRegistered.getString("model")))
                trust += Device.model.getStrength();
            else
                missedCount++;    
            
            if(devicePayload.getJSONObject("details").getString("platform").equals(deviceDetailsRegistered.getString("platform")))
                trust += Device.platform.getStrength();
            else
                missedCount++;
        
            if(devicePayload.getJSONObject("details").getString("manufacturer").equals(deviceDetailsRegistered.getString("manufacturer")))
                trust += Device.manufacturer.getStrength();
            else
                missedCount++;    

            if(devicePayload.getJSONObject("details").getString("uuid").equals(deviceDetailsRegistered.getString("uuid")))
                trust += Device.uuid.getStrength();
            else
                missedCount++;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return trust-missedCount;
    }


}
