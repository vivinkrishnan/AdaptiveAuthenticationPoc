package com.adaptive.springboot.adaptiveauthpoc.util;

import javax.print.attribute.URISyntax;

import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.web.client.RestTemplate;

public class LocationMatch {
    
    public static boolean ifLocationMatch(JSONObject coOrds, String currentLocation){

        boolean match = false;
        String uri = "https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=";

        try {
            StringBuilder uriString = new StringBuilder(uri);
            uriString.append(coOrds.getString("latitude"));
            uriString.append("&longitude=");
            uriString.append(coOrds.getString("longitude"));

            RestTemplate restTemplate = new RestTemplate();
            String resString = restTemplate.getForObject(uriString.toString(), String.class);
            
            JSONObject result = new JSONObject(resString);
            String resultCity = result.getString("city");

            //int containsLocation = resultCity.contains(currentLocation);
            String bangalore = "Bangalore", bengaluru = "Bengaluru";

            if(resultCity.equals(bangalore) || resultCity.equals(bengaluru) || resultCity.contains(bangalore) || resultCity.contains(bengaluru)){
                resultCity = bangalore;
            }
            if(currentLocation.equals(bangalore) || currentLocation.equals(bengaluru) || currentLocation.contains(bangalore) || currentLocation.contains(bengaluru)){
                currentLocation = bangalore;
            }

            match = resultCity.contains(currentLocation) || currentLocation.contains(resultCity);

            //boolean test = ifLocationMatchGoogle(coOrds, currentLocation);

            
        } catch (JSONException e) {
            e.printStackTrace();
        }
       
        
        return match;

    }

    public static boolean ifLocationMatchGoogle(JSONObject coOrds, String currentLocation){

        boolean match = false;
        //String uri = "https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=";
        String uri = "https://maps.googleapis.com/maps/api/geocode/json?latlng=";

        //32.9765956,77.6830188&key=

        try {
            StringBuilder uriString = new StringBuilder(uri);
            uriString.append(coOrds.getString("latitude"));
            uriString.append(",");
            uriString.append(coOrds.getString("longitude"));
            uriString.append("&key=AIzaSyATvE06lqrrN1QwEhCQ7CqDzOBxAsoL74E");
            

            RestTemplate restTemplate = new RestTemplate();
            String resString = restTemplate.getForObject(uriString.toString(), String.class);
            
            JSONObject result = new JSONObject(resString);
            String compoundCode = result.getJSONObject("plus_code").getString("compound_code");
            String resultCity = compoundCode.substring(9,compoundCode.indexOf(","));

            //int containsLocation = resultCity.contains(currentLocation);
            String bangalore = "Bangalore", bengaluru = "Bengaluru";

            if(resultCity.equals(bangalore) || resultCity.equals(bengaluru) || resultCity.contains(bangalore) || resultCity.contains(bengaluru)){
                resultCity = bangalore;
            }
            if(currentLocation.equals(bangalore) || currentLocation.equals(bengaluru) || currentLocation.contains(bangalore) || currentLocation.contains(bengaluru)){
                currentLocation = bangalore;
            }

            match = resultCity.contains(currentLocation) || currentLocation.contains(resultCity);
            
        } catch (JSONException e) {
            e.printStackTrace();
        }
       
        
        return match;

    }
}
