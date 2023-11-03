package com.adaptive.springboot.adaptiveauthpoc.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.adaptive.springboot.adaptiveauthpoc.entity.DBUtil;
import com.adaptive.springboot.adaptiveauthpoc.service.LoginService;
import com.adaptive.springboot.adaptiveauthpoc.service.RegisterService;
import com.adaptive.springboot.adaptiveauthpoc.service.ResourceRequestService;

@RestController
public class AdaptiveAuthenticationController {

    @Autowired
    private RegisterService registerService;

    @Autowired
    private LoginService loginService;

    @Autowired
    private ResourceRequestService resourceService;

    //Add a requestMapping ? /api/v1/adaptive_auth ?
    
    @GetMapping("/getUser/{id}")
    public ResponseEntity<Map<String,String>> getUserDetails(@PathVariable(value = "id") String userId) throws SQLException{

        Map<String,String> respMap = new HashMap<>();
        Connection connection = null;
		PreparedStatement getUser = null;
		System.out.println("Attempting connection...");

		try {
			
		    //Class.forName("com.mysql.cj.jdbc.Driver");
            //connection =DriverManager.getConnection("jdbc:mysql://localhost:3306/adaptive_auth","user","password"); 
            
            connection = DBUtil.getConnection();

			getUser = connection.prepareStatement("SELECT * FROM user u,device d where u.idUser = ? and u.registered_device = d.iddevice",ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);

			System.out.println(">>>>>> userId :: " + userId );
			System.out.println(">>>>>>>> userId int ::" + Integer.parseInt(userId));

			getUser.setInt(1, Integer.parseInt(userId));

			System.out.println(">>>>>>> Before firing");

			ResultSet data = getUser.executeQuery();
            
            JSONArray json = new JSONArray();

            ResultSetMetaData rsmd = data.getMetaData();
            while(data.next()) {
              int numColumns = rsmd.getColumnCount();
              JSONObject obj = new JSONObject();
              for (int i=1; i<=numColumns; i++) {
                String column_name = rsmd.getColumnName(i);
                obj.put(column_name, data.getObject(column_name));
              }
              json.put(obj);
            }

            System.out.println(">>>>>>>>" + json);

            JSONObject jso = new JSONObject();
            jso.put("Details", json.getJSONObject(0));

            System.out.println(">>>>>" + jso);

            System.out.println("\n");
            System.out.println(">>>>>>>" + jso.getJSONObject("Details").getString("device_details"));

			System.out.println(">>>>>>> After firing query");
			
            /* 
			if(data.first()){

                System.out.println(">>>>>>> Row count :::" + data.getRow());

				respMap.put("roles", data.getString("roles"));
				respMap.put("isBlocked", data.getString("isBlocked"));
				respMap.put("registered_location", data.getString("registered_location"));
				respMap.put("registered_device", data.getString("registered_device"));
				return new ResponseEntity<>(respMap,HttpStatus.OK);

			} else{
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}   */

		} catch (Exception e) {
			e.printStackTrace();
		}   finally{
			//Close resources in all cases
            if(getUser != null && connection != null){
                getUser.close();
			connection.close();
            }			
		}  

        return new ResponseEntity<>(respMap,HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String,Object> payload){

        if(payload == null || payload.size() == 0){
            return new ResponseEntity<>("Invalid payload", HttpStatus.BAD_REQUEST);
        }

        return registerService.registerUser(payload);
		
        
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String,Object> payload){

        if(payload == null || payload.size() == 0){
            return new ResponseEntity<>("Invalid payload", HttpStatus.BAD_REQUEST);
        }

        return loginService.loginUser(payload);

    }

    @PostMapping("/resource")
    public ResponseEntity<?> resource(@RequestBody Map<String,Object> payload){

        if(payload == null || payload.size() == 0){
            return new ResponseEntity<>("Invalid payload", HttpStatus.BAD_REQUEST);
        } 

        return resourceService.resourceRequest(payload);

    }

}


