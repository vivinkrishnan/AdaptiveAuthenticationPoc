package com.adaptive.springboot.adaptiveauthpoc.util;

import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import com.adaptive.springboot.adaptiveauthpoc.weight.ApplicationDetails;

public class AppMatch {

    public static boolean doesNotMatchApplication(JSONObject payloadApp,JSONObject storedApp,String action){

        boolean status=false;

        if(action.equals("register")){

            try {
                return compareWithTemplate(payloadApp);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }else{

           try {
            return compareWithStoredApp(payloadApp, storedApp);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        }

        return status;
    }

    private static boolean compareWithStoredApp(JSONObject payloadApp, JSONObject storedApp) throws JSONException {
        return (payloadApp == null || !payloadApp.getString("pkgname").equals(storedApp.getString("pkgname")) || !payloadApp.getString("versionNumber").equals(storedApp.getString("versionNumber")) || !payloadApp.getString("appname").equals(storedApp.getString("appname")) || !payloadApp.getString("versionCode").equals(storedApp.getString("versionCode")));
    }

    private static boolean compareWithTemplate(JSONObject payloadApp) throws JSONException {
        return (payloadApp == null || !payloadApp.getString("pkgname").equals(ApplicationDetails.PackageName.getDetail()) || !payloadApp.getString("versionNumber").equals(ApplicationDetails.VersionNumber.getDetail()) || !payloadApp.getString("appname").equals(ApplicationDetails.Name.getDetail()) || !payloadApp.getString("versionCode").equals(ApplicationDetails.VersionCode.getDetail()));
    }
    
}
