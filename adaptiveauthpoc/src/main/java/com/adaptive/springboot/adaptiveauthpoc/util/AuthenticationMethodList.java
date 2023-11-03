package com.adaptive.springboot.adaptiveauthpoc.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import com.adaptive.springboot.adaptiveauthpoc.weight.AuthMethods;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AuthenticationMethodList {
    
    public static HashMap<String,Integer> getAuthenticationMethodsForUser(String currentAuthMethod,String idUser,Boolean isChallenged){

        HashMap<String,Integer> authHashMap = new HashMap<>();

        AuthMethods.stream().filter(aM -> !(aM.toString().equals(currentAuthMethod)))
        .forEach(aM -> authHashMap.put(aM.toString(), aM.getStrength()));

        return authHashMap;
    }

    public static HashMap<String,Integer> getAuthenticationMethodsForUser(JSONObject authMethods,String idUser,Boolean isChallenged){
      
        HashMap<String,Integer> authHashMap = new HashMap<>();

        if(authMethods !=null && authMethods.length()!=0){
            try {
                Iterator<?> keys = authMethods.keys();
                while(keys.hasNext()) {
                    String key = (String)keys.next();
                    
                    System.out.println(key + "--"  + authMethods.getString(key));
                    
                    AuthMethods.stream().filter(aM -> !(aM.toString().equals(key)))
                    .forEach(aM -> authHashMap.put(aM.toString(), aM.getStrength()));
                    
                }

                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
       
        return authHashMap;
    }

    public static String getNewAuthMethod(HashMap<String, Integer> authMethodsMap, int trustDeficit,JSONObject usedAuthMethods) throws JSONException{

        int minValue = Integer.MAX_VALUE;
        String newAuthMethod = "";

        /* 
        newAuthMethod = authMethodsMap.entrySet().stream()
                        .filter(p -> p.getValue() >= trustDeficit)
                        .sorted(Map.Entry.comparingByValue())
                        .findFirst()
                        .map(Object::toString)
                        .orElse("null"); */

        //(usedAuthMethods !=null) &  (usedAuthMethods.get(entry.getKey())!=null) &                 
                        

        for (Entry<String, Integer> entry : authMethodsMap.entrySet()) {    
            
            if((entry.getValue() >= trustDeficit)){
                if(minValue > entry.getValue()){
                    minValue = entry.getValue();
                    newAuthMethod = entry.getKey();        
                }        
            }
        }
                        


        return newAuthMethod;

    }
}
