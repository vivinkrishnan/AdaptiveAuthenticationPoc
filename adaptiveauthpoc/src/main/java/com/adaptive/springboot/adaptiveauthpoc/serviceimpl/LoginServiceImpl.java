package com.adaptive.springboot.adaptiveauthpoc.serviceimpl;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.adaptive.springboot.adaptiveauthpoc.entity.DBUtil;
import com.adaptive.springboot.adaptiveauthpoc.service.LoginService;
import com.adaptive.springboot.adaptiveauthpoc.util.AppMatch;
import com.adaptive.springboot.adaptiveauthpoc.util.AuthenticationMethodList;
import com.adaptive.springboot.adaptiveauthpoc.util.AuthenticationUtil;
import com.adaptive.springboot.adaptiveauthpoc.util.DeviceMatch;
import com.adaptive.springboot.adaptiveauthpoc.util.GeoVelocity;
import com.adaptive.springboot.adaptiveauthpoc.util.LocationMatch;
import com.adaptive.springboot.adaptiveauthpoc.util.LocationUtil;
import com.adaptive.springboot.adaptiveauthpoc.weight.Match;
import com.adaptive.springboot.adaptiveauthpoc.weight.Penalty;
import com.adaptive.springboot.adaptiveauthpoc.weight.Status;
import com.adaptive.springboot.adaptiveauthpoc.weight.StrengthRequired;

@Service
public class LoginServiceImpl implements LoginService{

    public static final String ACTION = "login";

    public static final String APP_MATCH_MISS = "Failed App Match";

    public static final String GEO_VELOCITY_BREACH = "Geo Velocity Risk";

    public static final String INCORRECT_PASSWORD = "Password Mismatch";

    public static final String LOGIN_SUCCESS = "Login Success";

    public static final String EXPIRED = "Auth State Expired";

    public static final Integer TIME_OUT = 120;

    public static final Integer MAX_TRIES = 3;

    @Override
    public ResponseEntity<?> loginUser(Map<String, Object> payload) {
        
        int trust = 0,appTrust =0, retryCount =0, trustStored =0, trustRequired = 0;
        boolean isChallenged,authComplete,inSufficientTrust = false; 
        boolean challengeResponse = false;
        JSONObject authState = new JSONObject();    

        try {

            trustRequired = StrengthRequired.LoginStrengthRequired.getStrengthRequired();

            JSONObject payloadObject = new JSONObject(payload);

            JSONObject app = payloadObject.getJSONObject("app");

            JSONObject device = payloadObject.getJSONObject("device");
            //String deviceCoordinates = device.getString("coordinates");
            JSONObject deviceCoordinates = device.getJSONObject("coordinates");
            
            //Check if this is a challenge response.
            if(payloadObject.has("challengeResponse")){
                challengeResponse = payloadObject.getBoolean("challengeResponse");
            }                 

            JSONObject userDetails = DBUtil.getUserRegistrationData(payloadObject.getJSONObject("user").getString("userName"),payloadObject.getInt("iduser"));
            
            //If user entry does not exist, then reject and send response to client.
            if(userDetails.length()==0){
                return new ResponseEntity<>("User Registration does not exist", HttpStatus.BAD_REQUEST);
            }

            JSONObject storedApp = new JSONObject(userDetails.getString("registered_application"));

            String ip = payloadObject.getJSONObject("user").getString("ip");

            JSONObject geoLocation = LocationUtil.getDBIPLocation(ip);
            String geoLocationCity = geoLocation.getString("city");
     
            //Non-mobile client or different application, reject request
            //Check if app details match. If not, reject
            //if(app == null || !app.getString("pkgname").equals(storedApp.getString("pkgname")) || !app.getString("versionNumber").equals(storedApp.getString("versionNumber")) || !app.getString("appname").equals(storedApp.getString("appname")) || !app.getString("versionCode").equals(storedApp.getString("versionCode"))  ){

            if(AppMatch.doesNotMatchApplication(app, storedApp, ACTION)){    

                try {

                    int rows = DBUtil.insertAudit(userDetails, ACTION, ip, geoLocation.getString("city"),  device.getString("coordinates"),0, Status.FAIL.getStatusVal() ,APP_MATCH_MISS);

                    if(rows == 1){
                        System.out.println(">>>>>>>>>>> Login fail Audit insert success" );
                        return new ResponseEntity<>("Login failed - Application Does not match", HttpStatus.BAD_REQUEST);
                    }
                } catch (Exception e) {
                    //connection.rollback();
                    e.printStackTrace();
                    return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
                }

                return new ResponseEntity<>("Application Does not match",HttpStatus.BAD_REQUEST);

            }else{
                
                // App trust factor achieved
                appTrust = Match.AppMatch.getWeight();
                System.out.println(trust);
                trust += appTrust;

                System.out.println("Trust with appMatch:: "  + trust );

                //GeoVelocity check
                //if geovelocity exceeds last recorded location, then flag. Check audit_trail table for last recorded geolocation. Geovelocity check should be done 
                //based on geo-coordinates and IP resolved location. If IP location does not match geo-location, yet another flag.

                JSONObject lastAuditRecord = DBUtil.lastAuditRecord(userDetails.getString("iduser"));
            
                //boolean geoVelocityRisk = false;      
                int geoVelocityRisk = 0;      

                if(lastAuditRecord.length() == 0){
                    //geoVelocityRisk = GeoVelocity.calculateGeoVelocity(deviceCoordinates, deviceCoordinates, DBUtil.getCurrentDateString());
                    geoVelocityRisk = GeoVelocity.calculateGeoVelocity(deviceCoordinates, deviceCoordinates, DBUtil.getCurrentDateString());

                }else{
                    JSONObject coordinatesFromAudit = new JSONObject(lastAuditRecord.getString("coordinates"));
                    geoVelocityRisk = GeoVelocity.calculateGeoVelocity(coordinatesFromAudit, deviceCoordinates, lastAuditRecord.getString("time_of_attempt"));
                }

                System.out.println(geoVelocityRisk);       
                
                if(geoVelocityRisk > 600){
                    trust -= Penalty.GeoVelocityFail.getPenalty();

                    System.out.println("Trust with geo velocty risk:: " + trust);

                    //Should the client be challenged right away? 
                    //Instead, can the trust be lowered and the rest of the checks performed?
                    
                    //int rows = DBUtil.setChallengeState(userDetails, ACTION, trust, ip, geoLocation.getString("city"), 1, device.getString("coordinates"), GEO_VELOCITY_BREACH);

                    //if(rows == 1){
                    //    System.out.println(">>>>>>>>>>> Saved Auth State. Sending Challenge to user" );
                        //Choose an alternate authentication method from the available methods.
                    //    return new ResponseEntity<>("Alternate Authentication method", HttpStatus.UNAUTHORIZED);
                    //}    

                }

                //No geovelocity penalty
                //Check if location city from GPS co-ordinates and GeoLocation city from IP Address matches. 
                //If so, add that to the trust factor, If not, impose a penalty for the mismatch.

                if(LocationMatch.ifLocationMatch(deviceCoordinates, geoLocationCity)){
                    trust += Match.LocationMatch.getWeight();

                    System.out.println("Trust with location match GPS - IP :: " + trust );

                }else{
                    trust -= Penalty.CoordinatesToIPLocationMismatch.getPenalty();

                    System.out.println("Trust with no location match GPS - IP :: " + trust );
                }
                
                //Proceed to device check                              
                //device should have slightly lenient checks as OS updates can change some parameters. Similarly location can vary.
                //match the static attributes

                 /* 
                    Device Match should return more than just boolean.
                    If the device match detects wide variance in the device attributes( beyond a threshold), it should save the state and initiate
                    device authentication challenge. The challenge should be the response to the device, after saving the current trust and auth state.
                    Once the device responds, the system should pickup from where it left of ( after having subjected the request to the usual strict checks).
                    If device attribute variance is still detected, then the device trust value ( weight or penalty) should be applied.                
                */ 
                
                trust  += DeviceMatch.deviceMatch(device, userDetails);


               

                System.out.println("Trust after device match :: " + trust );

                //Check if the time of login falls in the range that is in the established behavioural profile.
                // To do this, all previous login/ resource requests's timestamp should be considered to understand the range. 
                // This will need ML. Might not be possible with static rules. 
                
                String currentAuthMethod = payloadObject.getString("authmethod");
                String credentials = payloadObject.getJSONObject("user").getString("credentials");
                JSONObject authMethods = new JSONObject();
                authMethods.put(currentAuthMethod , currentAuthMethod);
                JSONObject usedAuthMethods = new JSONObject();


                //Check if the flow should proceed into challenge Response handling or first time login. 
                //If challenge response, check if this is from an earlier wrong credentials or from insufficient trust.

                //First time login
                if(!challengeResponse){

                    if(AuthenticationUtil.credentialsMatch(currentAuthMethod, credentials)){
                  
                        //if credentials match, then proceed to device check
                        //add trust gained from authentication strength
                        trust += AuthenticationUtil.getAuthStrength(currentAuthMethod);
    
                        //trust = Math.max(trust,trustStored);
    
                        System.out.println("Trust with credentials match :: " + trust );
    
                    }else{
                        //impose a penalty for the credentials mismatch
                        trust -= Penalty.CredentialsMismatch.getPenalty();
    
                        System.out.println("Trust with no credentials match :: " + trust );
    
                        //update retry count
                        retryCount++;
                        JSONObject incorrectCredentials = new JSONObject();
                        incorrectCredentials.put("message", INCORRECT_PASSWORD);
                        incorrectCredentials.put("retryAttempt", retryCount);
    
                        isChallenged = true;
                        authComplete = false;
                        inSufficientTrust = (trust >= trustRequired) ? true : false;
    
                        int rows = DBUtil.setChallengeState(userDetails, ACTION, isChallenged, authComplete, trust, inSufficientTrust, authMethods ,retryCount,ip, geoLocationCity, geoVelocityRisk, deviceCoordinates.toString(), Status.CHALLENGE.getStatusVal(),INCORRECT_PASSWORD);
    
                        if(rows == 1){
                            if(retryCount == MAX_TRIES){
                                return new ResponseEntity<>("Blocked", HttpStatus.FORBIDDEN);
                            }else{
                                return new ResponseEntity<>(incorrectCredentials.toString(),HttpStatus.UNAUTHORIZED);
                            }
                            
                        }                   
    
                    }   
                    
                }else{

                    //Optimize this call. See if this can be avoided.
                    authState = DBUtil.getAuthState(payloadObject.get("iduser").toString());
                    

                    String time_of_attempt = "";

                    if(authState!=null && authState.length()!=0){
                        retryCount = authState.getInt("retryCount");
                        trustStored = authState.getInt("current_trust");
                        time_of_attempt = authState.getString("time_of_attempt");
                        usedAuthMethods = authState.getJSONObject("authmethods");
                    }                    

                    if(!time_of_attempt.equals("")){
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date dateOfLastAttempt = sdf.parse(time_of_attempt);
                        
                        Double time_expired = (double)(new Date().getTime() - dateOfLastAttempt.getTime())/(60*1000);

                        if(time_expired > 5){

                            //Update Audit trail and challengestate about this expired auth state.
                            isChallenged = true;
                            authComplete = false;
                            inSufficientTrust = false;

                            int rows = DBUtil.setChallengeState(userDetails, ACTION, isChallenged, authComplete, trust, inSufficientTrust, authMethods ,retryCount,ip, geoLocationCity, geoVelocityRisk, deviceCoordinates.toString(), Status.EXPIRED.getStatusVal(),EXPIRED);

                            if(rows == 1){
                                return new ResponseEntity<>("Auth State Expired. Relogin", HttpStatus.UNAUTHORIZED) ;                                
                            }

                        }
                    }                    

                    if(retryCount >= MAX_TRIES){
                        return new ResponseEntity<>("Blocked", HttpStatus.FORBIDDEN);
                    }

                    //Auth state not expired and not blocked.
                    //Match credentials of new auth method 

                    if(AuthenticationUtil.credentialsMatch(currentAuthMethod, credentials)){
                  
                        //if credentials match, then proceed to device check
                        //add trust gained from authentication strength
                        
                        if(trust >= trustStored){
                            trust = trustStored + AuthenticationUtil.getAuthStrength(currentAuthMethod);
                        }else{
                            trust += AuthenticationUtil.getAuthStrength(currentAuthMethod);
                        }
                        
                        System.out.println("Trust with credentials match :: " + trust );
    
                    }else{
                        //impose a penalty for the credentials mismatch
                        trust -= Penalty.CredentialsMismatch.getPenalty();
    
                        System.out.println("Trust with no credentials match :: " + trust );
    
                        //update retry count
                        retryCount++;
                        JSONObject incorrectCredentials = new JSONObject();
                        incorrectCredentials.put("message", INCORRECT_PASSWORD);
                        incorrectCredentials.put("retryAttempt", retryCount);
    
                        isChallenged = true;
                        authComplete = false;
                        inSufficientTrust = (trust >= trustRequired) ? true : false;
    
                        //trustStored = Math.min(trustStored,trust);
                        trust = Math.min(trustStored, trust);
    
                        int rows = DBUtil.setChallengeState(userDetails, ACTION, isChallenged, authComplete, trust, inSufficientTrust, usedAuthMethods ,retryCount,ip, geoLocationCity, geoVelocityRisk, deviceCoordinates.toString(), Status.CHALLENGE.getStatusVal(),INCORRECT_PASSWORD);
    
                        if(rows == 1){
                            if(retryCount == MAX_TRIES){
                                return new ResponseEntity<>("Blocked", HttpStatus.FORBIDDEN);
                            }else{
                                return new ResponseEntity<>(incorrectCredentials.toString(),HttpStatus.UNAUTHORIZED);
                            }
                            
                        }                   
    
                    }   

                    
                }

                System.out.println("Trust acquired :: " + trust );
                System.out.println("Trust required :: " + trustRequired );
                
                if(trust >= trustRequired){

                    //Trust required to achieve successful login achieved. 
                    //Update authstate and record success in Audit Trail
                    //Return login success indication to client.

                    isChallenged = false;
                    authComplete = true;
                    inSufficientTrust = false;

                    int rows = DBUtil.setChallengeState(userDetails, ACTION, isChallenged, authComplete, trust, inSufficientTrust, authMethods ,retryCount, ip, geoLocationCity, geoVelocityRisk, deviceCoordinates.toString(), Status.SUCCESS.getStatusVal(), LOGIN_SUCCESS);

                    if(rows == 1){
                        return new ResponseEntity<>(LOGIN_SUCCESS,HttpStatus.OK);
                    }                   

                }else{

                    isChallenged = true;
                    authComplete = false;
                    inSufficientTrust = false;

                    //int trustDeficit = TrustCalculatorUtil.getTrustDeficit(trust, ACTION);
                    int trustDeficit = trustRequired - trust;

                    HashMap<String,Integer> availableAuthMethodsMap = new HashMap<String,Integer>();
                    
                    availableAuthMethodsMap=AuthenticationMethodList.getAuthenticationMethodsForUser(currentAuthMethod, payloadObject.getString("iduser"), isChallenged);

                    usedAuthMethods.put(currentAuthMethod, currentAuthMethod);
                    String newAuthMethod = AuthenticationMethodList.getNewAuthMethod(availableAuthMethodsMap, trustDeficit,usedAuthMethods);

                    System.out.println("Chosen new auth method :: " + newAuthMethod);

                    //authMethods.put(newAuthMethod, newAuthMethod);
                    usedAuthMethods.put(newAuthMethod, newAuthMethod);

                    int rows = DBUtil.setChallengeState(userDetails, ACTION, isChallenged, authComplete, trust, inSufficientTrust, usedAuthMethods ,retryCount, ip, geoLocationCity, geoVelocityRisk, deviceCoordinates.toString(), Status.CHALLENGE.getStatusVal(), Status.NEWAUTHMETHOD.getStatusVal());

                    if(rows == 1){
                        return new ResponseEntity<>(newAuthMethod,HttpStatus.UNAUTHORIZED);
                    }

                }               

            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>("Login success",HttpStatus.OK);
        

    }
    
}
