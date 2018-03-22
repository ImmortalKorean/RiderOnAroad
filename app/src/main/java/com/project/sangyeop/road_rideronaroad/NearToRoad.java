package com.project.sangyeop.road_rideronaroad;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.model.LatLng;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class NearToRoad {

    private final String TMAP_API_KEY = "074f22a2-4edb-4f5d-84a3-c0afb479d173";
    ArrayList<LatLng> nearToRoadList = new ArrayList<>();

    int speed;
    String roadName;
    int roadCategory;

    public ArrayList<LatLng> getJsonData(final String lat, final String lng) {

        final Thread thread = new Thread() {

            public void run() {

                HttpClient httpClient = new DefaultHttpClient();

                String urlString = "https://api2.sktelecom.com/tmap/road/nearToRoad?version=2&appKey=" + TMAP_API_KEY + "&lat=" + lat + "&lon=" + lng;

                try {
                    URI uri = new URI(urlString);

                    HttpPost httpPost = new HttpPost();
                    httpPost.setURI(uri);

                    List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>();

                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    HttpResponse response = httpClient.execute(httpPost);

                    int code = response.getStatusLine().getStatusCode();
                    String message = response.getStatusLine().getReasonPhrase();
                    String responseString;
                    if (response.getEntity() != null) {
                        responseString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);

                        String string1 = new JSONObject(responseString).getString("resultData");
                        String string2 = new JSONObject(string1).getString("header");
                        JSONObject jsonObject = new JSONObject(string2);
                        speed = Integer.parseInt(jsonObject.getString("speed"));
                        roadName = jsonObject.getString("roadName");
                        roadCategory = Integer.parseInt(jsonObject.getString("roadCategory"));

                    } else {
                        return;
                    }

                } catch (URISyntaxException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        };

        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return nearToRoadList;
    }
}
