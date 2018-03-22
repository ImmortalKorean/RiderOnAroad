//package com.project.sangyeop.road_rideronaroad;
//
//import android.content.Context;
//import android.graphics.Color;
//import android.location.Location;
//import android.os.Build;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.view.WindowManager;
//import android.widget.Button;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.skt.Tmap.TMapGpsManager;
//import com.skt.Tmap.TMapPoint;
//import com.skt.Tmap.TMapPolyLine;
//import com.skt.Tmap.TMapView;
//
//import java.util.ArrayList;
//
//public class TmapDrivingMode extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback {
//
//    // tMap 지도를 띄우기 위해 필요한, API KEY 입니다.
//    static private final String TMAP_API_KEY = "074f22a2-4edb-4f5d-84a3-c0afb479d173";
//
//    //태스트용들
//    TextView textView;
//    Button test_tmap_line;
//
//
//    static TMapView tMapView;
//
//    TMapPolyLine tMapPolyLine; // 지도상의 경로를 표시할 선의 색깔이나 굵기를 설정합니다.
//    ArrayList<TMapPoint> tMapPoints; // 경로상의 위치정보들이 저장되고, 맵상에 폴리라인(경로)을 그릴 때 씁니다.
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_tmap_driving_mode);
//
//        /**
//         * 주행녹화 중에 화면이 꺼지지 않게 합니다.
//         */
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//
//        /**
//         * ㄴ테스팅용 지울거임 ㅁㄴㅇㄻㄴㅇㄻㄴㅇㄻㄴㅇㄻㄴㅇㄻㄴㅇㄻㄴㅇㄻㄴㅇㄻㄴㅇㄻㄴㅇㄻㄴㅇㄹㄴㅇㄹ
//         */
//        textView = findViewById(R.id.test_latlng_show);
//        test_tmap_line = findViewById(R.id.test_tmap_line);
//        test_tmap_line.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                // 패스포인트 메서드는 뭘까
//                // tMapPolyLine.addPassPoint();
//
//                // 저장된 위치정보들을 기준으로, 지도상에 선을 그립니다. 이 선은 유저가 주행한 코스를 보여주게 됩니다.
//                for (int i = 0; i < tMapPoints.size(); i++) {
//                    tMapPolyLine.addLinePoint(tMapPoints.get(i));
//                }
//
//                tMapView.addTMapPolyLine("Line1", tMapPolyLine);
//
//                Toast.makeText(getApplicationContext(), "짜ㅣㄴ", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        // 위치정보들을 저장하는 역할을 합니다.
//        tMapPoints = new ArrayList<TMapPoint>();
//
//        // 지도상의 경로를 표시할 선의 크기는 2로 하고, 색상은 적색으로 합니다.
//        tMapPolyLine = new TMapPolyLine();
//        tMapPolyLine.setLineColor(Color.RED);
//        tMapPolyLine.setLineWidth(2f);
//
//        /*
//        TMapView 객체를 생성하고, Layout 에 추가함으로써, tMap 지도를 화면에 표시할 수 있습니다.
//        TMAP_API_KEY 로 인증을 하고, 지도를 초기화 해줍니다.
//         */
//        LinearLayout tMapViewContainer = findViewById(R.id.tMapViewContainer);
//        tMapView = new TMapView(this);
//
//        tMapView.setSKTMapApiKey(TMAP_API_KEY);
//        tMapViewContainer.addView(tMapView);
//
//        tMapView.setIconVisibility(true); // 현재위치를 표시하는  아이콘을 활성화 합니다.
////        tMapView.setTrackingMode(true); // 현재위치를 트랭킥합니다
////        tMapView.setCompassMode(true); // 유저의 진행방향에 따라, 맵을 회전시킵니다. .. 가속도계를 이용해서 회전시키는 것 같다. 도로 기준이 아니라. 단말의 기울기를 기준으로 ㅠ
////        tMapView.setSightVisible(true); // 유저가 보고있는 방향을 표시합니다.
//
//        tMapView.setTrackingMode(true); // 화면중심에 유저의 현위치를 계속 나타내주는, 트래킹 모드를 활성화 합니다.
//        tMapView.setZoomLevel(17);
//
//        // 유저의 위치정보를 얼마나 자주 탐색할지 설정하고, GPS를 활성합니다.
//        TMapGpsManager gps = new TMapGpsManager(this);
//        gps.setProvider(gps.GPS_PROVIDER);
//        gps.setMinDistance(1f); // 최소 1m 이상 위치가 변경되면, 해당 위치정보를 얻습니다.
//        gps.setMinTime(500l); // 최대 1초마다, 유저의 위치정보를 얻습니다.
////        gps.OpenGps();
//
//        tMapView.setLocationPoint(126.9301319,37.4814859);
//        tMapView.setCenterPoint(126.9301319,37.4814859);
//
//    }
//
//    /**
//     * 변경된 유저의 위치정보를 얻습니다.
//     *
//     * @param location : 유저의 현재위치가 저장됩니다. 이 위치는 유저의 변경된 새로운 위치정보를 뜻합니다.
//     */
//    public void onLocationChange(Location location) {
//
//        textView.setText(location.getLatitude() + "/" + location.getLongitude() +
//                "\nAccuracy : " + location.getAccuracy() +
//                "\nbearing : " + location.getBearing() +
//                "\nspeed : " + location.getSpeed()
//        );
//
//        tMapView.setLocationPoint(location.getLongitude(), location.getLatitude()); // 유저의 현재위치를 맵에 표시하고,
//        tMapView.setCenterPoint(location.getLongitude(), location.getLatitude(), true); // 그 지점을 맵뷰 중앙 표시합니다.
//
//        tMapView.rotate(location.getBearing()); // 지도를 인자만큼 회전시킵니다.
//
//        tMapPoints.add(new TMapPoint(location.getLatitude(), location.getLongitude())); // 위치정보들을 저장합니다. 이는 나중에 맵상에 경로를 표시할 때 쓰입니다.
//
//
//    }
//}
