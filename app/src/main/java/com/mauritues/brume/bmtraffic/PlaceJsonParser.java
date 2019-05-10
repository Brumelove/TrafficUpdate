package com.mauritues.brume.bmtraffic;

import android.annotation.SuppressLint;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlaceJsonParser {

    public List<HashMap<String,String>> parse(JSONObject jObject){

        JSONArray jPlaces = null;
        try {
            /** Retrieves all the elements in the 'places' array */
            jPlaces = jObject.getJSONArray("predictions");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        /** Invoking getPlaces with the array of json object
         * where each json object represent a place
         */
        return getPlaces(jPlaces);
    }

    private List<HashMap<String, String>> getPlaces(JSONArray jPlaces){
        int placesCount = jPlaces.length();
        List<HashMap<String, String>> placesList = new ArrayList<HashMap<String,String>>();
        HashMap<String, String> place = null;

        /** Taking each place, parses and adds to list object */
        for(int i=0; i<placesCount;i++){
            try {
                /** Call getPlace with place JSON object to parse the place */
                place = getPlace((JSONObject)jPlaces.get(i));
                placesList.add(place);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return placesList;
    }

    /** Parsing the Place JSON object */
    private HashMap<String, String> getPlace(JSONObject jPlace){

        HashMap<String, String> place = new HashMap<String, String>();

        String id="";
        String reference="";
        String description="";
        String placeId = "";
        try {

            description = jPlace.getString("description");
            id = jPlace.getString("id");
            reference = jPlace.getString("reference");
            //System.out.println("Okay checking and checking " + jPlace.getString("place_id"));
            placeId = jPlace.getString("place_id");

            String longlatResponse = downloadUrl("https://maps.googleapis.com/maps/api/place/details/json?placeid="+placeId+"&key="+SessionVariables.MAP_VIEW_KEY);
            System.out.println("Longlat response " + longlatResponse);

            //trying to get latitude an dlongitude
            JSONObject testJson = new JSONObject(longlatResponse).getJSONObject("result");
            JSONObject geometry = testJson.getJSONObject("geometry");
            JSONObject location = geometry.getJSONObject("location");
            double latitude = location.getDouble("lat");
            double longitude = location.getDouble("lng");

            System.out.println("Latitude and Longitude  " + latitude + "   --   " +longitude);
            LatLng latLng = new LatLng(latitude, longitude);

            SessionVariables.Locations.put(description, latLng);

            place.put("description", description);
            place.put("_id",id);
            place.put("reference",reference);
            place.put("place_id", placeId);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return place;
    }



    @SuppressLint("LongLogTag")
    private String downloadUrl(String strUrl) {
       try {
           String data = "";
           InputStream iStream = null;
           HttpURLConnection urlConnection = null;
           try {
               URL url = new URL(strUrl);

               // Creating an http connection to communicate with url
               urlConnection = (HttpURLConnection) url.openConnection();

               // Connecting to url
               urlConnection.connect();

               // Reading data from url
               iStream = urlConnection.getInputStream();

               BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

               StringBuffer sb = new StringBuffer();

               String line = "";
               while ((line = br.readLine()) != null) {
                   sb.append(line);
               }

               data = sb.toString();

               System.out.println("Yap yap,  here's your data " + data);

               br.close();

           } catch (Exception e) {
               Log.d("Exception while downloading url", e.toString());
           } finally {
               iStream.close();
               urlConnection.disconnect();
           }
           return data;
       }
       catch (IOException e){

       }
       return "";
    }

}
