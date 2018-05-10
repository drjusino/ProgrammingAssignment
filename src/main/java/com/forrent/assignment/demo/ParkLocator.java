package com.forrent.assignment.demo;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import javax.validation.constraints.NotNull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class ParkLocator {

    // URL variables for pulling from the Google Maps API
    private static final String googleGeocodeURL = "https://maps.googleapis.com/maps/api/geocode/json";
    private static final String googlePlacesURL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";

    //Location Variables
    //Testing Git
    @NotNull
    private String address;
    private List<String> parks;
    private double latitude;
    private double longitude;

    public ParkLocator() {
        parks = new ArrayList<>();
        address = "";
        latitude = 0.0;
        longitude = 0.0;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;

        // Check if the address is empty, and then search for results if it isn't.
        if(address.isEmpty()){
            return;
        } else {
            transferAddressToGPS();
            findNearbyParks();
        }
    }

    public List<String> getParks() {
        return this.parks;
    }

    public void setParks(List<String> parks) {
        this.parks = parks;
    }

    public void setLatitude(double latitude){
        this.latitude = latitude;
    }

    public double getLatitude(double latitude){
        return this.latitude;
    }

    public void setLongitude(double longitude){
        this.longitude = longitude;
    }

    public double getLongitude(double longitude){
        return this.longitude;
    }

    public String toString() {
        return "Location(Address: " + this.address +
                ", Latitude: " + this.latitude + ", Longitude: " + this.longitude +
                ", List of parks: " + parks.toString() + ")";
    }

    private void transferAddressToGPS() {
        //Create the Context Builder
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey("AIzaSyCDghDR7ZlJXnVZDK24uQLBHCIOnyYQd5E")
                .build();

        //Find the proper address for the inputted location.
        GeocodingResult[] gResults = new GeocodingResult[0];
        try {
            gResults = GeocodingApi.geocode(context, address).await();
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Transfer this address to a GPS coordinate
        try {
            // Call upon the Google Maps Geocode API to transfer the address.
            URL url = new URL(googleGeocodeURL + "?address=" + URLEncoder.encode(gResults[0].formattedAddress, "UTF-8") + "&sensor=false" + "&key=AIzaSyCDghDR7ZlJXnVZDK24uQLBHCIOnyYQd5E");
            URLConnection connection = url.openConnection();

            // Open the Byte Stream
            ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
            IOUtils.copy(connection.getInputStream(), output);
            output.close();

            // Convert the received string to a JSONObject
            String jsonString = output.toString();
            JSONObject object = new JSONObject(jsonString);
            if (!object.getString("status").equals("OK")) {
                return;
            }

            // Search for the resulting location from the JSON and assign those values
            JSONObject results = object.getJSONArray("results").getJSONObject(0);
            JSONObject location = results.getJSONObject("geometry").getJSONObject("location");

            latitude = location.getDouble("lat");
            longitude = location.getDouble("lng");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void findNearbyParks() {
        try {
            // Call upon the Google Maps Parks API to find nearby parks.
            URL url = new URL(googlePlacesURL + "?location=" + latitude + "," + longitude + "&type=park" + "&rankby=distance" + "&sensor=false" + "&key=AIzaSyDAd5241fsg4LpWdk3lzF5BPiuFiXZHzuA");
            URLConnection connection = url.openConnection();

            // Open the Byte Stream
            ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
            IOUtils.copy(connection.getInputStream(), output);
            output.close();

            // Convert the received string to a JSONObject
            String jsonString = output.toString();

            JSONObject object = new JSONObject(jsonString);
            if (!object.getString("status").equals("OK")) {
                return;
            }

            // Search through the list to see which ones are within 10 miles and add them to the list.
            int size = object.getJSONArray("results").length();
            if (size > 0) {
                for (int i = 0; i < size; i++) {
                    // Google does not allow the ability to rank by distance and zone by radius at the same time
                    // Because of this, we have to calculate the distance after Google sorts to insure a park is within
                    // 10 miles.
                    JSONObject results = object.getJSONArray("results").getJSONObject(i);
                    JSONObject location = results.getJSONObject("geometry").getJSONObject("location");
                    final int R = 6371; // Radius of the earth

                    double latDistance = Math.toRadians(location.getDouble("lat") - this.latitude);
                    double lonDistance = Math.toRadians(location.getDouble("lng") - this.longitude);
                    double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                            + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(location.getDouble("lat")))
                            * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
                    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
                    double distance = R * c * 1000 / 1609.344; // convert to miles
                    if(distance <= 10){
                        parks.add(results.getString("name") + ", Located at: " + results.get("vicinity"));
                    }
                }
            } else {
                parks.add("There were no results found for this location. Please try a new one.");
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
