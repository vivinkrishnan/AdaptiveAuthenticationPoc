package com.adaptive.springboot.adaptiveauthpoc.entity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import com.adaptive.springboot.adaptiveauthpoc.weight.Penalty;
import com.adaptive.springboot.adaptiveauthpoc.weight.Status;

public class DBUtil {

    
    public static String getCurrentDateString(){
        java.util.Date dt = new java.util.Date();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        return sdf.format(dt);
    }

    public static Connection getConnection(){
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/adaptive_auth","user","password");
            return connection;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        } 
        return connection;
    }

    public static int insertAudit(JSONObject userDetails,String action, String ip, String geoLocationCity,String coordinates,int geovelocity,String status,String comments){

        Connection connection = null;
        PreparedStatement insertAudit = null;
        int rows = 0;
        //(iduser, username, resource_requested, ipAddress, geolocation_city,geovelocity,coordinates,time_of_attempt,comments

        try{
            connection = DBUtil.getConnection();

            insertAudit = connection.prepareStatement("INSERT INTO audit_trail (iduser, username, resource_requested, ipAddress, geolocation_city,geovelocity,coordinates,time_of_attempt,status,comments) VALUES (?,?,?,?,?,?,?,?,?,?)");

            if(action.equals("register") & status.equals(Status.FAIL.getStatusVal())){
                insertAudit.setString(1, "Null");
            }else{
                insertAudit.setString(1, userDetails.getString("iduser"));
            }
            
            insertAudit.setString(2, userDetails.getString("username"));
            insertAudit.setString(3, action);
            insertAudit.setString(4, ip);
            insertAudit.setString(5, geoLocationCity);
            insertAudit.setInt(6, geovelocity);
            insertAudit.setString(7, coordinates);
            insertAudit.setString(8, getCurrentDateString());
            insertAudit.setString(9, status );
            insertAudit.setString(10, comments);

            rows = insertAudit.executeUpdate();

            return rows;

        }catch(SQLException se){
            se.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }finally{
            try {
                insertAudit.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return rows;         

    }

    public static int registerDevice(JSONObject userObject,JSONObject deviceObject,JSONObject payloadObject,String geoLocationCity,String ip,String action){
        
        int rows = 0;
        Connection connection = null;
        PreparedStatement insertDevice = null,insertUser = null,insertAudit = null; 


        try{
            connection = getConnection();

            connection.setAutoCommit(false);

            insertDevice = connection.prepareStatement("INSERT INTO device (username, localization, device_details, coordinates,ipLocation, user_agent) VALUES (?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);

                insertDevice.setString(1, userObject.getString("username"));
                insertDevice.setString(2, deviceObject.getString("localization"));
                insertDevice.setString(3, deviceObject.getString("details"));
                insertDevice.setString(4, deviceObject.getString("coordinates"));
                insertDevice.setString(5, deviceObject.getString("iplocation"));
                insertDevice.setString(6, deviceObject.getString("useragent"));

                int deviceId = insertDevice.executeUpdate();

                if(deviceId == 1){

                    System.out.println(">>>>>>>> Device inserted successfully");

                    try(ResultSet generatedDeviceKeys = insertDevice.getGeneratedKeys()){
                        if(generatedDeviceKeys.next()){

                            insertUser = connection.prepareStatement("INSERT INTO user (roles, isBlocked, registered_location, registered_device, registered_application,username,credentials) VALUES (?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);

                            insertUser.setString(1, "user");
                            insertUser.setString(2, "no");
                            insertUser.setString(3, payloadObject.getString("registered_location"));
                            insertUser.setString(4, String.valueOf(generatedDeviceKeys.getLong(1)));
                            insertUser.setString(5, payloadObject.getString("app"));
                            insertUser.setString(6, userObject.getString("username"));
                            insertUser.setString(7, userObject.getString("credentials"));
                                
                            int rowInserted = insertUser.executeUpdate();

                            if(rowInserted == 1){
                                System.out.println("Inserted " + rowInserted + " Successfully....");
                                try (ResultSet generatedUserKeys = insertUser.getGeneratedKeys()) {
                                    if (generatedUserKeys.next()) {
                                            
                                        //System.out.println(">>>>>>>>>" + generatedUserKeys.getLong(1));

                                        insertAudit = connection.prepareStatement("INSERT INTO audit_trail (iduser, username, resource_requested, ipAddress, geolocation_city,geovelocity,coordinates,time_of_attempt,status,comments) VALUES (?,?,?,?,?,?,?,?,?,?)");

                                        insertAudit.setString(1, String.valueOf(generatedUserKeys.getLong(1)));
                                        insertAudit.setString(2, userObject.getString("username"));
                                        insertAudit.setString(3, action);
                                        insertAudit.setString(4,ip);
                                        insertAudit.setString(5, geoLocationCity);
                                        insertAudit.setInt(6, 0);
                                        insertAudit.setString(7, deviceObject.getString("coordinates"));
                                        insertAudit.setString(8, getCurrentDateString());
                                        insertAudit.setString(9, Status.SUCCESS.getStatusVal());
                                        insertAudit.setString(10, Status.SUCCESS.getStatusVal());

                                        rows = insertAudit.executeUpdate();

                                            if(rows == 1){
                                                    System.out.println(">>>>>>>>>>> Audit insert success" );
                                            }                    
                                            
                                        } else {
                                            throw new SQLException("Creating user failed, no ID obtained.");
                                        }
                                }catch(SQLException e){
                                    e.printStackTrace();
                                }
                                    
                            }
                        }

                    }                      

                }

            connection.commit();
                    
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException se) {
                se.printStackTrace();
            }            
        }finally{
            if(insertDevice != null && insertUser != null && insertAudit !=null && connection!=null){
                try {
                    insertAudit.close();
                    insertDevice.close();
                    insertUser.close();
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }           
            
        }
            

        return rows;
    }

    public static JSONObject getAuthState(String idUser){

        Connection connection = null;
        PreparedStatement getAuthState = null;
        int idauth_state = 0;
        JSONObject authState = new JSONObject();

        try{

            connection = getConnection();
            getAuthState = connection.prepareStatement("select * from auth_state where iduser=?",ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
            getAuthState.setString(1, idUser);

            ResultSet authData = getAuthState.executeQuery();
            //row = authData.getFetchSize();

            ResultSetMetaData rsmd = authData.getMetaData();

            while(authData.next()) {
              int numColumns = rsmd.getColumnCount();
              for (int i=1; i<=numColumns; i++) {
                String column_name = rsmd.getColumnName(i);
                authState.put(column_name, authData.getObject(column_name));
              }
              
            }

            return authState;

        }catch(SQLException | JSONException se){
            se.printStackTrace();
        }finally{
            try {
                getAuthState.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return authState;
    }

    public static int setChallengeState(JSONObject userDetails, String action, boolean isChallenged, boolean authComplete,int trust,boolean inSufficientTrust, JSONObject authMethods ,int retryCount,String ip, String geoLocationCity, Integer geoVelocity, String coordinates,String status,String comments){

        Connection connection = null;
        PreparedStatement setChallenge = null;
        int row = 0;

        try{
            connection = getConnection();

            //check if authstate exists. If so, update. Else insert. 
            // See if this can be optimized

            JSONObject ifAuthStatePresent = getAuthState(userDetails.getString("iduser"));
            
            if(ifAuthStatePresent.length() ==0){
                setChallenge = connection.prepareStatement("INSERT INTO auth_state(iduser,isChallenged,auth_complete,current_trust,inSufficientTrust,authmethods,retryCount,time_of_attempt)VALUES(?,?,?,?,?,?,?,?)");
            }else{
                setChallenge = connection.prepareStatement("UPDATE auth_state set iduser=?,isChallenged=?,auth_complete=?,current_trust=?,inSufficientTrust=?,authmethods=?,retryCount=?,time_of_attempt=? WHERE idauth_state=? and iduser=?");
            }
            
            setChallenge.setString(1, userDetails.getString("iduser"));
            setChallenge.setBoolean(2, isChallenged);
            setChallenge.setBoolean(3, authComplete);
            setChallenge.setInt(4, trust);
            setChallenge.setBoolean(5, inSufficientTrust);
            setChallenge.setString(6, authMethods.toString());
            setChallenge.setInt(7, retryCount);
            setChallenge.setString(8, getCurrentDateString());

            if(ifAuthStatePresent.length()!=0){
                setChallenge.setInt(9, ifAuthStatePresent.getInt("idauth_state"));
                setChallenge.setString(10, userDetails.getString("iduser"));
            }

            row = setChallenge.executeUpdate();

            //int geoVelocityVal = (geoVelocity)? 1 : 0;

            if(row == 1){
                if(retryCount == Penalty.MaxRetries.getPenalty()){
                    status = Status.BLOCKED.getStatusVal();
                }
                insertAudit(userDetails, action, ip, geoLocationCity, coordinates, geoVelocity, status ,comments);
            }

            return row;

        }catch(SQLException se){
            se.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }finally{
            try {
                if(setChallenge!=null && connection!=null){
                    setChallenge.close();
                    connection.close();
                }                

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return row;
    }
    
    public static JSONObject lastAuditRecord(String idUser){
        JSONObject auditObect = new JSONObject();
        Connection connection = null;
        PreparedStatement getAuditStatement = null;
        //System.out.println("Attempting connection...");

        try {
			
		    connection = getConnection();

			getAuditStatement = connection.prepareStatement("select * from audit_trail where time_of_attempt=(select max(cast(time_of_attempt as datetime)) from audit_trail where iduser= ? and status= ?);",ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);

			//System.out.println(">>>>>> userId :: " + idUser );
			
			getAuditStatement.setString(1, idUser);
            getAuditStatement.setString(2, "Success");

			//System.out.println(">>>>>>> Before firing");

			ResultSet data = getAuditStatement.executeQuery();

            ResultSetMetaData rsmd = data.getMetaData();

            while(data.next()) {
              int numColumns = rsmd.getColumnCount();
              for (int i=1; i<=numColumns; i++) {
                String column_name = rsmd.getColumnName(i);
                auditObect.put(column_name, data.getObject(column_name));
              }
              
            }

            //System.out.println(">>>>>>>>" + auditObect);

		} catch (Exception e) {
			e.printStackTrace();
		}   finally{
			//Close resources in all cases
			try {
                getAuditStatement.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
		}

        return auditObect;

    }

    public static JSONObject getUserRegistrationData(String userName, int iduser){

        JSONObject userObject = new JSONObject();
        Connection connection = null;
		PreparedStatement getUser = null;
        
        System.out.println("Attempting connection...");

		try {
			
		    connection = getConnection();
           // Class.forName("com.mysql.cj.jdbc.Driver");
            //connection =DriverManager.getConnection("jdbc:mysql://localhost:3306/adaptive_auth","user","password"); 

			getUser = connection.prepareStatement("SELECT * FROM user as u,device as d where u.iduser = ? and u.username = ? and u.registered_device = d.iddevice",ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);

			//System.out.println(">>>>>> userId :: " + userName );
			//System.out.println(">>>>>>>> userId int ::" + Integer.parseInt(userName));

            getUser.setInt(1,iduser);
			getUser.setString(2, userName);
            
			//System.out.println(">>>>>>> Before firing");

			ResultSet data = getUser.executeQuery();
            
			//System.out.println(">>>>>>> After firing query");

            ResultSetMetaData rsmd = data.getMetaData();
            while(data.next()) {
              int numColumns = rsmd.getColumnCount();
              //obj = new JSONObject();
              for (int i=1; i<=numColumns; i++) {
                String column_name = rsmd.getColumnName(i);
                userObject.put(column_name, data.getObject(column_name));
                //detailsMap.put(column_name, data.getObject(column_name));
              }
              //json.put(obj);
            }

            return userObject;

		} catch (Exception e) {
			e.printStackTrace();
		}   finally{
			//Close resources in all cases
			try {
                getUser.close();                
			    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
		}  
        return userObject;

    }
    
}
