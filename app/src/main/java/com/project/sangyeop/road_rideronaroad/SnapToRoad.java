package com.project.sangyeop.road_rideronaroad;

import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

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

/**
 * Created by leesa on 2018-03-04.
 */

public class SnapToRoad {

    private final String TMAP_API_KEY = "074f22a2-4edb-4f5d-84a3-c0afb479d173"; // Tmap API의 '이동한 도로'찾기 기능을 사용하기위해, 필요한 키 입니다.
    GoogleMap googleMap; // 코스기록이 그려져 있는 지도입니다.
    PolylineOptions rectOptions = new PolylineOptions().width(10).color(Color.BLUE); // 보정된 코스의 폴리라인(경로)를 저장하고, 지도에 코스기록을 새로 그리기 위해 사용됩니다.

    public SnapToRoad(GoogleMap googleMap) {
        this.googleMap = googleMap;

    }

    /**
     * 도로를 벗어나 기록된 부정확한 정보를,
     * 정확히 도로에 일치시켜서 보정함으로써,
     * 보다 정확한 코스기록을 제공합니다.
     *
     * @param coords : 원본데이터 입니다. 주행한 코스의 정보(LatLng값)들이 저장되어 있으나, 도로를 벗어나 기록된 부정확한 정보입니다.
     */
    public void getJsonData(final String coords) {

        Thread thread = new Thread() {

            public void run() {

                HttpClient httpClient = new DefaultHttpClient();

                String urlString = "https://api2.sktelecom.com/tmap/road/matchToRoads?version=1&appKey=074f22a2-4edb-4f5d-84a3-c0afb479d173";
                try {
                    URI uri = new URI(urlString);

                    HttpPost httpPost = new HttpPost();
                    httpPost.setURI(uri);

                    List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>();

                    nameValuePairs.add(new BasicNameValuePair("responseType", "1"));
                    nameValuePairs.add(new BasicNameValuePair("coords", coords));

                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    HttpResponse response = httpClient.execute(httpPost);

                    JSONObject responsejson;
                    if (response.getEntity() != null) {
                        responsejson = new JSONObject(EntityUtils.toString(response.getEntity(), HTTP.UTF_8));
                        Log.d("체크json리스폰", responsejson+"");
                    } else {
                        return;
                    }

                    String a = responsejson.getJSONObject("resultData").getString("matchedPoints");

                    JSONArray jsonArray = new JSONArray(a);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        String aa = jsonArray.getJSONObject(i).getString("matchedLocation");
                        JSONObject jsonObject1 = new JSONObject(aa);
                        String lat = jsonObject1.getString("latitude");
                        String lng = jsonObject1.getString("longitude");
                        rectOptions.add(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)));
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

        googleMap.clear(); // 지도에서 이전의 부정확한 코스기록 정보를 지우고,
        googleMap.addPolyline(rectOptions); // 보정된 코스기록을 지도에 그립니다.

    }

}
