package com.adaptive.springboot.adaptiveauthpoc.util;

import java.text.ParseException;
import java.util.Date;

import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

public class GeoVelocity {

    public static Integer calculateGeoVelocity(JSONObject coord1, JSONObject coord2,String timeOfLastAttempt){

            Double distance_covered,time_taken_in_hours, velocity;
            try {
                double lat1 = Double.parseDouble(coord1.getString("latitude"));
                double long1 = Double.parseDouble(coord1.getString("longitude"));
                double lat2 = Double.parseDouble(coord2.getString("latitude"));
                double long2 = Double.parseDouble(coord2.getString("longitude"));

                distance_covered = distance(lat1,lat2,long1,long2);

                System.out.println("Distance Covered ::" + distance_covered);

                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date dateOfLastAttempt = sdf.parse(timeOfLastAttempt);

                System.out.println("Date of last attempt::" + dateOfLastAttempt.getTime());
                
                time_taken_in_hours = (double)(new Date().getTime() - dateOfLastAttempt.getTime())/(60*60*1000);

                velocity = distance_covered/time_taken_in_hours;

                System.out.println("Velocity:::" + velocity);

                //return velocity > 600 ? true: false;
                return velocity.intValue();
      

            } catch (NumberFormatException | JSONException | ParseException e) {
                e.printStackTrace();
            }


            return 1;

    }
    
    public static double distance(double lat1,
                     double lat2, double lon1,
                                  double lon2)
    {
 
        // The math module contains a function
        // named toRadians which converts from
        // degrees to radians.
        lon1 = Math.toRadians(lon1);
        lon2 = Math.toRadians(lon2);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
 
        // Haversine formula
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a = Math.pow(Math.sin(dlat / 2), 2)
                 + Math.cos(lat1) * Math.cos(lat2)
                 * Math.pow(Math.sin(dlon / 2),2);
             
        double c = 2 * Math.asin(Math.sqrt(a));
 
        // Radius of earth in kilometers. Use 3956
        // for miles
        double r = 6371;
 
        // calculate the result
        return(c * r);
    }

    public static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
		if ((lat1 == lat2) && (lon1 == lon2)) {
			return 0;
		}
		else {
			double theta = lon1 - lon2;
			double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
			dist = Math.acos(dist);
			dist = Math.toDegrees(dist);
			dist = dist * 60 * 1.1515;
			if (unit.equals("K")) {
				dist = dist * 1.609344;
			} else if (unit.equals("N")) {
				dist = dist * 0.8684;
			}
			return (dist);
		}
	}
}
