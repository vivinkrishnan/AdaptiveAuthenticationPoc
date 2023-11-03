package com.adaptive.springboot.adaptiveauthpoc.util;

import org.springframework.boot.configurationprocessor.json.JSONObject;

public class AverageOfAttributes {
    
    public static String getMostUsedCity(String userid){

        //select geolocation_city,count(geolocation_city) as 'city_count' from audit_trail where iduser=27 group by geolocation_city order by city_count desc limit 1;

        return "";
    }
    

    public static JSONObject getPreferredTimeRange(String userid){

        JSONObject timeJsonObject = new JSONObject();


        return timeJsonObject;

    } 



}
