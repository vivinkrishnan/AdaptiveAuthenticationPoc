package com.adaptive.springboot.adaptiveauthpoc.serviceimpl;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.adaptive.springboot.adaptiveauthpoc.entity.DBUtil;
import com.adaptive.springboot.adaptiveauthpoc.service.ResourceRequestService;
import com.adaptive.springboot.adaptiveauthpoc.util.AppMatch;
import com.adaptive.springboot.adaptiveauthpoc.util.AuthenticationMethodList;
import com.adaptive.springboot.adaptiveauthpoc.util.DeviceMatch;
import com.adaptive.springboot.adaptiveauthpoc.util.GeoVelocity;
import com.adaptive.springboot.adaptiveauthpoc.util.LocationMatch;
import com.adaptive.springboot.adaptiveauthpoc.util.LocationUtil;
import com.adaptive.springboot.adaptiveauthpoc.weight.AuthMethods;
import com.adaptive.springboot.adaptiveauthpoc.weight.Match;
import com.adaptive.springboot.adaptiveauthpoc.weight.Penalty;
import com.adaptive.springboot.adaptiveauthpoc.weight.Status;
import com.adaptive.springboot.adaptiveauthpoc.weight.StrengthRequired;

@Service
public class ResourceRequestImpl implements ResourceRequestService {

    public static final String ACTION = "ResourceRequest";

    public static final String APP_MATCH_MISS = "Failed App Match";

    public static final String AUTH_STATE_DOES_NOT_EXIST = "Auth state does not exist";

    public static final String AUTH_STATE_EXPIRED = "Auth state is invalid";

    public static final String GEO_VELOCITY_BREACH = "Geo Velocity Risk";

    public static final String INCORRECT_PASSWORD = "Password Mismatch";

    public static final String RESOURCE_ACCESS_SUCCESS = "Resource Access Success";

    public static final String EXPIRED = "Auth State Expired";

    public static final Integer TIME_OUT = 120;

    public static final Integer MAX_TRIES = 3;

    @Override
    public ResponseEntity<?> resourceRequest(Map<String, Object> payload) {

        int trust = 0,appTrust =0, retryCount =0, trustRequired = 0;
        boolean isChallenged,authComplete,inSufficientTrust = false; 
        boolean authContext = false;
        JSONObject authState = new JSONObject(); 
        JSONObject authMethods = new JSONObject();   

        try {

            trustRequired = StrengthRequired.ResourceRequestStrengthRequired.getStrengthRequired();

            JSONObject payloadObject = new JSONObject(payload);

            JSONObject app = payloadObject.getJSONObject("app");

            JSONObject device = payloadObject.getJSONObject("device");
            //String deviceCoordinates = device.getString("coordinates");
            JSONObject deviceCoordinates = device.getJSONObject("coordinates");

            int geoVelocityRisk = 0;
            int trustStored = 0;
            
            //Check if this is a challenge response.
            if(payloadObject.has("authContext")){
                authContext = true;
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


                //GeoVelocity check
                //if geovelocity exceeds last recorded location, then flag. Check audit_trail table for last recorded geolocation. Geovelocity check should be done 
                //based on geo-coordinates and IP resolved location. If IP location does not match geo-location, yet another flag.

                JSONObject lastAuditRecord = DBUtil.lastAuditRecord(userDetails.getString("iduser"));
                
                //boolean geoVelocityRisk = false;        
                    

                if(lastAuditRecord.length() == 0){
                    geoVelocityRisk = GeoVelocity.calculateGeoVelocity(deviceCoordinates, deviceCoordinates, DBUtil.getCurrentDateString());
                }else{
                    JSONObject coordinatesFromAudit = new JSONObject(lastAuditRecord.getString("coordinates"));
                    geoVelocityRisk = GeoVelocity.calculateGeoVelocity(coordinatesFromAudit, deviceCoordinates, lastAuditRecord.getString("time_of_attempt"));
                }

                System.out.println(geoVelocityRisk);       
                    
                if(geoVelocityRisk > 600){
                    trust -= Penalty.GeoVelocityFail.getPenalty();

                    //Should the client be challenged right away? 
                    //Instead, can the trust be lowered and the rest of the checks performed?
                        
                    //int rows = DBUtil.setChallengeState(userDetails, ACTION, trust, ip, geoLocation.getString("city"), 1, device.getString("coordinates"), GEO_VELOCITY_BREACH);

                    //if(rows == 1){
                    //    System.out.println(">>>>>>>>>>> Saved Auth State. Sending Challenge to user" );
                        //Choose an alternate authentication method from the available methods.
                        //    return new ResponseEntity<>("Alternate Authentication method", HttpStatus.UNAUTHORIZED);
                    //}    

                }


                //Check if auth_context exists and if so, is valid.

                //Optimize this call. See if this can be avoided.
                authState = DBUtil.getAuthState(payloadObject.get("iduser").toString());

                //An authentication state does not exist. User should be asked to login first.
                if(authState.length() == 0){

                    isChallenged = true;
                    authComplete = false;
                    inSufficientTrust = false;


                    int rows = DBUtil.insertAudit(userDetails, ACTION, ip, geoLocation.getString("city"),  device.getString("coordinates"),geoVelocityRisk, Status.FAIL.getStatusVal() ,AUTH_STATE_DOES_NOT_EXIST);
                    // int rows = DBUtil.setChallengeState(userDetails, ACTION, isChallenged, authComplete, trust, inSufficientTrust, authMethods ,retryCount,ip, geoLocationCity, geoVelocityRisk, deviceCoordinates.toString(), Status.EXPIRED.getStatusVal(),EXPIRED);

                    if(rows == 1){
                        return new ResponseEntity<>("Auth State does not exist. Login", HttpStatus.UNAUTHORIZED) ;                                
                    }

                }else{

                    trustStored = authState.getInt("current_trust");

                    String time_of_attempt = "";
                    
                    if(authState!=null && authState.length()!=0){
                        retryCount = authState.getInt("retryCount");
                        time_of_attempt = authState.getString("time_of_attempt");
                        authMethods = new JSONObject(authState.getString("authmethods"));
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

                            int rows = DBUtil.insertAudit(userDetails, ACTION, ip, geoLocation.getString("city"),  device.getString("coordinates"),geoVelocityRisk, Status.FAIL.getStatusVal() ,AUTH_STATE_EXPIRED);
                            // int rows = DBUtil.setChallengeState(userDetails, ACTION, isChallenged, authComplete, trust, inSufficientTrust, authMethods ,retryCount,ip, geoLocationCity, geoVelocityRisk, deviceCoordinates.toString(), Status.EXPIRED.getStatusVal(),EXPIRED);

                            if(rows == 1){
                                return new ResponseEntity<>("Auth State expired. Login", HttpStatus.UNAUTHORIZED) ;                                
                            }

                        }
                    }                    

                    if(retryCount >= MAX_TRIES){
                        return new ResponseEntity<>("Blocked", HttpStatus.FORBIDDEN);
                    }

                    //If auth state valid, not expired, and not blocked, add the trust weight of the authmethods to trust.
                    if(authMethods !=null && authMethods.length()!=0){
                        try {
                            Iterator<?> keys = authMethods.keys();
                            while(keys.hasNext()) {
                                String key = (String)keys.next();
                                
                                System.out.println(key + "--"  + authMethods.getString(key));
                                
                                //System.out.println(AuthMethods.)
                                AuthMethods am = AuthMethods.valueOf(key);

                                trust += am.getStrength();
                                
                            }            
                            
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    
                }
                    
            }


            

            //No geovelocity penalty
            //Check if location city from GPS co-ordinates and GeoLocation city from IP Address matches. 
            //If so, add that to the trust factor

            if(LocationMatch.ifLocationMatch(deviceCoordinates, geoLocationCity)){
                trust += Match.LocationMatch.getWeight();
            }
                
            //Proceed to device check                              
            //device should have slightly lenient checks as OS updates can change some parameters. Similarly location can vary.
            //match the static attributes
                
            trust  += DeviceMatch.deviceMatch(device, userDetails);

            //Check if the time of login falls in the range that is in the established behavioural profile.
            // To do this, all previous login/ resource requests's timestamp should be considered to understand the range. 
            // This will need ML. Might not be possible with static rules. 

            trust = Math.min(trustStored,trust);
    
            if(trust >= trustRequired){

                //Trust required to achieve successful login achieved. 
                //Update authstate and record success in Audit Trail
                //Return login success indication to client.

                isChallenged = false;
                authComplete = true;
                inSufficientTrust = false;

                int rows = DBUtil.insertAudit(userDetails, ACTION, ip, geoLocation.getString("city"),  device.getString("coordinates"),geoVelocityRisk, Status.SUCCESS.getStatusVal(), RESOURCE_ACCESS_SUCCESS);

                if(rows == 1){
                    return new ResponseEntity<>(RESOURCE_ACCESS_SUCCESS,HttpStatus.OK);
                }                   

            }else{

                isChallenged = true;
                authComplete = false;
                inSufficientTrust = true;

                //int trustDeficit = TrustCalculatorUtil.getTrustDeficit(trust, ACTION);
                int trustDeficit = trustRequired - trust;

                HashMap<String,Integer> availableAuthMethodsMap = new HashMap<String,Integer>();
                    
                availableAuthMethodsMap=AuthenticationMethodList.getAuthenticationMethodsForUser(authMethods, payloadObject.getString("iduser"), isChallenged);

                String newAuthMethod = AuthenticationMethodList.getNewAuthMethod(availableAuthMethodsMap, trustDeficit,authMethods);

                authMethods.put(newAuthMethod, newAuthMethod);

                int rows = DBUtil.setChallengeState(userDetails, ACTION, isChallenged, authComplete, trust, inSufficientTrust, authMethods ,retryCount, ip, geoLocationCity, geoVelocityRisk, deviceCoordinates.toString(), Status.CHALLENGE.getStatusVal(), Status.NEWAUTHMETHOD.getStatusVal());

                if(rows == 1){
                    return new ResponseEntity<>(newAuthMethod,HttpStatus.UNAUTHORIZED);
                }

            }

        }catch(JSONException je){
            je.printStackTrace();
        }catch(ParseException pe){
            pe.printStackTrace();
        }

        return new ResponseEntity<>("Login success",HttpStatus.OK);

    }

}
