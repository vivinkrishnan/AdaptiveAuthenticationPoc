package com.adaptive.springboot.adaptiveauthpoc.util;

import java.io.IOException;
import java.net.InetAddress;

import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.web.client.RestTemplate;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;

import io.ipgeolocation.api.Geolocation;
import io.ipgeolocation.api.GeolocationParams;
import io.ipgeolocation.api.IPGeolocationAPI;

public class LocationUtil {

    private static DatabaseReader reader = null;

    /*
    static{
        System.out.println(">>>>>>>>>>>>Static block called");

        File database = new File("C:\\PhD_Projects\\gs-spring-boot\\GeoLite2-City.mmdb");

        //C:\PhD_Projects\gs-spring-boot\GeoLite2-City.mmdb

        try {
            reader = new DatabaseReader.Builder(database).build();
        } catch (IOException e) {
            e.printStackTrace();
        }
    } */
    
    public static JSONObject getDBIPLocation(String ip){
        
        String uri = "http://api.db-ip.com/v2/free/" + ip;
        RestTemplate restTemplate = new RestTemplate();
        String resString = restTemplate.getForObject(uri, String.class);
        JSONObject result = null;
        try {
            result = new JSONObject(resString);
            //System.out.println(">>>>>>> DB IP Info :: " + result  +  "------" + resString) ;
        } catch (JSONException e) {
            e.printStackTrace();
        }
       
        return result;

    }

    public static JSONObject getGeoLocation(String ip){

        JSONObject geoJSON = new JSONObject();
        
        IPGeolocationAPI api = new IPGeolocationAPI("b27212888a85421098434ed9b6759577");

        GeolocationParams geoParams = new GeolocationParams();
        geoParams.setIPAddress(ip);
        geoParams.setFields("geo,time_zone,currency");

        Geolocation geolocation = api.getGeolocation(geoParams);

                // Check if geolocation lookup was successful
        if(geolocation.getStatus() == 200) {
         
            /* 
            System.out.println(">>>>>>> IPAddress " + geolocation.getIPAddress());
            System.out.println(">>>>>>> Country " + geolocation.getCountryName());
            System.out.println(">>>>>>> City " + geolocation.getCity());
            System.out.println(">>>>>>> Currency " + geolocation.getCurrency().getName());
            System.out.println(">>>>>>> Country Code " + geolocation.getCountryCode2());
            System.out.println(">>>>>>> Time " + geolocation.getTimezone().getCurrentTime()); */
            
            try {
                geoJSON.put("city", geolocation.getCity());
                geoJSON.put("country", geolocation.getCountryName());
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {
            System.out.printf("Status Code: %d, Message: %s\n", geolocation.getStatus(), geolocation.getMessage());
        }

        return geoJSON;


    }

    public static JSONObject getMaxKindGeoLocation(String ip){

        CityResponse response;
        JSONObject maxKindGeoLocation = new JSONObject();
            try {
                InetAddress ipAddress = InetAddress.getByName(ip);

                response = reader.city(ipAddress);

                //System.out.println("--------------------------MaxKind Location--------------------------");

                City city = response.getCity();
                //System.out.println(">>>>>>> City" + city.getName());

                maxKindGeoLocation.put("city", city.getName());

                Country country = response.getCountry();

                //System.out.println(">>>>>> Country" + country.getName());
                //System.out.println(">>>>>> ISO Code" + country.getIsoCode());
                maxKindGeoLocation.put("country", country.getName());

                //String postal = response.getPostal().getCode();
                //System.out.println(">>>>>> Postal" + postal);
                //String state = response.getLeastSpecificSubdivision().getName();
                //System.out.println(">>>>>>> State" + state);

                

                //WebServiceClient client = new WebServiceClient.Builder(737994, "URk9c8mutFG0FCOI").host("geolite.info").build();

                //InetAddress ipAddress1 = InetAddress.getByName(ip);

                // You can also use `client.city` or `client.insights`
                // `client.insights` is not available to GeoLite2 users
                //CityResponse response1 = client.city(ipAddress1);

                //System.out.println(">>>>>> City " + response1.getCity().getName());
                //System.out.println(">>>>>> Country " + response1.getCountry().getName());            
                //System.out.println(">>>>>> Subdivision " + response1.getMostSpecificSubdivision());
                //System.out.println(">>>>>> Division name " + response1.getMostSpecificSubdivision().getName());

                //System.out.println("----------------------------------------------------------");

            } catch (IOException | GeoIp2Exception | JSONException e) {
                e.printStackTrace();
            } 

            return maxKindGeoLocation;

    }
}
