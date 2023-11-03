package com.adaptive.springboot.adaptiveauthpoc.serviceimpl;

import java.util.Map;

import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.adaptive.springboot.adaptiveauthpoc.entity.DBUtil;
import com.adaptive.springboot.adaptiveauthpoc.service.RegisterServiceNonMobile;
import com.adaptive.springboot.adaptiveauthpoc.util.AppMatch;
import com.adaptive.springboot.adaptiveauthpoc.util.LocationUtil;
import com.adaptive.springboot.adaptiveauthpoc.weight.Status;

public class RegisterServiceNonMobileImpl implements RegisterServiceNonMobile{

    public static final String ACTION = "register";

    @Override
    public ResponseEntity<?> registerNonMobileUser(Map<String, Object> payload) {

        try{

            JSONObject payloadObject= new JSONObject(payload);
            JSONObject deviceObject = payloadObject.getJSONObject("device");
            JSONObject userObject = payloadObject.getJSONObject("user");
            JSONObject appObject = payloadObject.getJSONObject("app");
            JSONObject pluginObject = payloadObject.getJSONObject("pluginlist");
            String ip = userObject.getString("ip");
            JSONObject geoLocation = LocationUtil.getDBIPLocation(ip);

            deviceObject.put("iplocation", geoLocation);


            try {

                //Registration failure scenario. If the application details do not match,reject immediately.                 
                //if(appObject == null || !appObject.getString("pkgname").equals(ApplicationDetails.PackageName.detail) || !appObject.getString("versionNumber").equals(ApplicationDetails.VersionNumber.detail) || !appObject.getString("appname").equals(ApplicationDetails.Name.detail) || !appObject.getString("versionCode").equals(ApplicationDetails.VersionCode.detail)){
                if(AppMatch.doesNotMatchApplication(appObject, null, ACTION)){    

                    try {

                        //If registration fails, insert details of the failed attempt into the Audit_trail table.
                        int rows = DBUtil.insertAudit(userObject, ACTION, ip, geoLocation.getString("city"), deviceObject.getString("coordinates"), 0, Status.FAIL.getStatusVal(), "Failed app match");

                        if(rows == 1){
                            System.out.println(">>>>>>>>>>> Registration fail Audit insert success" );
                            return new ResponseEntity<>("Registration failed", HttpStatus.BAD_REQUEST);
                        }
                    } catch (Exception e) {
                        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
                    }

                }

                int rows = DBUtil.registerDevice(userObject,deviceObject,payloadObject,geoLocation.getString("city"),ip,ACTION);

                if(rows == 1){
                    //Return a 200 OK
                    return new ResponseEntity<>("Inserted Successfully",HttpStatus.OK);                
                }

                
            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            }

        }catch(JSONException e){
            e.printStackTrace();
        }    
        
        return new ResponseEntity<>("Inserted Successfully",HttpStatus.OK);
    }
    
}
