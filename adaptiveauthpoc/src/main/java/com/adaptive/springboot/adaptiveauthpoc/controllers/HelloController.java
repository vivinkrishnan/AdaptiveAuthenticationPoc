package com.adaptive.springboot.adaptiveauthpoc.controllers;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.adaptive.springboot.adaptiveauthpoc.entity.DBUtil;
import com.adaptive.springboot.adaptiveauthpoc.interfaces.TestWiring;
import com.adaptive.springboot.adaptiveauthpoc.util.LocationUtil;
import com.adaptive.springboot.adaptiveauthpoc.weight.AuthMethods;

@RestController
public class HelloController {

	@Autowired
    private TestWiring tWiring;

	@GetMapping("/")
	public String index() {
		return "Greetings from Spring Boot!";
	}

	@GetMapping("/test")
    public String test(){


        JSONObject x = new JSONObject();

        System.out.println("x size::" + x.length() );

        try {
            //x.put("hello", "world");
            //x.put("new","word");
            /* 
            UserNameCredentials(5),
            PinNumber(5),
            SecurityQuestion(5),
            OTP(6),
            CAPTCHA(6),
            GraphicalPassword(7),
            Touch(7),
            DigitalCertificate(8); */


            x.put("UserNameCredentials","UserNameCredentials");
            x.put("PinNumber","PinNumber");
            x.put("SecurityQuestion","SecurityQuestion");
            x.put("OTP", "OTP");
            x.put("GraphicalPassword","GraphicalPassword");
            
            System.out.println("x size::" + x.length() );

            try {
                Iterator<?> keys = x.keys();
                while(keys.hasNext()) {
                    String key = (String)keys.next();
                    
                    System.out.println(key + "--"  + x.getString(key));

                    AuthMethods am = AuthMethods.valueOf(key);
                    System.out.println(am.getStrength());
                    
                }

                
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        JSONObject auditObject = DBUtil.lastAuditRecord("23");


        System.out.println("Last Audit Record :: " + auditObject);

        try {
            if(auditObject.length()!=0){
                System.out.println("Audit coordinates ::" + auditObject.getString("coordinates"));
            }
            
        } catch (JSONException e) {
            
            e.printStackTrace();
        }

        return "Hello World";
    }

    @PostMapping("/testJSON")
    public String testJSON(@RequestBody Map<String,Object> payload,@RequestHeader("User-Agent") String userAgent){
    //public String testJSON(@RequestBody String payload ){

        //System.out.println(payload);

        try {
                JSONObject jsonObject= new JSONObject(payload);

                JSONObject deviceObject = jsonObject.getJSONObject("device");

                System.out.println(jsonObject.get("app"));

                System.out.println(jsonObject.get("user"));

                System.out.println(jsonObject.get("device"));

                //JSONObject ip = jsonObject.getJSONObject("user");

                System.out.println(">>>>>>> IP:" + jsonObject.getJSONObject("user").get("ip"));
                String ip = jsonObject.getJSONObject("user").get("ip").toString();

                //JSONObject maxKindGeoLocation = MyController.getMaxKindGeoLocation(ip);

                //System.out.println(">>>>>>> maxKindGeolocation" + maxKindGeoLocation);

                JSONObject geoLocation = LocationUtil.getGeoLocation(ip);

                System.out.println(">>>>>>> IP Geo Location" + geoLocation);

                deviceObject.put("location", geoLocation);

                System.out.println(">>>>>>>" + deviceObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return "Received JSON";
    }

	@PostMapping("/insertTest")
    public ResponseEntity<?> addTestUser(@RequestBody Map<String,Object> payload){

        System.out.println(">>>>>> " + payload);
        JSONObject jsonObject = new JSONObject(payload);

        Connection connection = null;
		PreparedStatement insertUser = null;
        Reader reader = null;
		System.out.println("Attempting connection...");
         
        try{

            try {
                //Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DBUtil.getConnection();

                insertUser = connection.prepareStatement("INSERT INTO test (testcol, testcol1) VALUES (?,?)");

                insertUser.setString(1, jsonObject.getString("testcol"));
                reader = new StringReader(jsonObject.getString("device"));
                insertUser.setClob(2, reader);
                
                int rowInserted = insertUser.executeUpdate();

                System.out.println("Inserted " + rowInserted + " Successfully....");

                return new ResponseEntity<>("Inserted Successfully",HttpStatus.OK);
                //Return a 200 OK
            } catch (SQLException e) {
                e.printStackTrace();
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            }
			
		}
		catch (Exception e) {
			//Trying to create a user that already exists
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		finally{
			//Close resources in all cases
			try {
                insertUser.close();
                connection.close();
                reader.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
		}

    }

	@GetMapping("/testWiring")
    public String testWiring(){

        String retString = tWiring.getStringFrom();

        return retString;

    }
}
