package com.project.sangyeop.road_rideronaroad;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Recording_Driving_Mode extends AppCompatActivity implements OnMapReadyCallback {

    // Keys for storing activity state in the Bundle.
    private final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    private final static String KEY_LOCATION = "location";
    private final static String KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";

    /**
     * 버튼의 활성/비활성화를 위한 변수입니다.
     */
    int BUTTON_MODE = 3;
    final int BUTTON_LOCK_ON = 1;
    final int BUTTON_LOCK_OFF = 2;
    final int RECORDING_IS_OFF = 3;
    final int RECORDING_IS_ON = 4;

    /**
     * Constant used in the location settings dialog.
     */
    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    GoogleMap googlemap;

    ImageButton recordingDrivingCourse;

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Provides access to the Location Settings API.
     */
    private SettingsClient mSettingsClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    private LocationSettingsRequest mLocationSettingsRequest;

    /**
     * Callback for Location events.
     */
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation; // 현재 유저위치

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    private Boolean mRequestingLocationUpdates;

    /**
     * Time when the location was updated represented as a String.
     */
    private String mLastUpdateTime;

    PolylineOptions rectOptions;

    /**
     * 불러온 코스정보를 저장합니다.
     * 구간별, LatLng 고도 속도 Bearing 가 저장됩니다.
     */
    PolylineOptions selectedCourseRoute;

    boolean previousLocation_isInitialized = false;

    /**
     * 이전 위치데이터를 5개 저장하기 위한 변수.
     * 목적은 GPS 특성상, 값이 항상 일정하지 않으므로,
     * 최근 5개값의 평균값을 화면에 표시하기 위함입니다.
     * <p>
     * 따라서 해당 앱에서는 항상 정확한 값이아닌, (속도/베어링)
     * 가장 근사한값을 제공하게 됩니다.
     */
    Location[] previousLocation = new Location[5];
    int previousSpeed;
    int currentSpeed;

    ArrayList<Location> traveledWayPoints = new ArrayList<Location>();

    ImageButton load;
    TextView countDown, speed;

    long totalDrivingTime = 0;
    float totalDrivingMeter = 0;

    SharedPreferences saved_DrivingCourseInformation;
    SharedPreferences.Editor saved_DrivingCourseInformation_Editor;

    ImageButton lockButtons; //

    // record the compass picture angle turned
    private float currentDegree = 0f;

    float badacc = 0;
    float goodacc = 100;
    ArrayList<Float> avg;

    long startTime = 0;
    long finishTime = 0;

    DbOpenHelper mDbOpenHelper;

    final int LOADING_COURSE_INFO = 123;
    final int MY_PERMISSIONS_REQUEST_READ_FINE_LOCATION = 100;

    NearToRoad nearToRoad;
    ArrayList<com.google.maps.model.LatLng> nearToRoadList;

    TextView roadAdress;
    TextView speedLimit;
    MediaPlayer player;
    RelativeLayout alertDisplay;

    ImageView noBike;

    @SuppressLint("MissingPermission")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driving_mode);

        /**
         * 속도제한을 넘겼을 시 출력되는
         * 경고 알람+화면 입니다.
         */
        player = MediaPlayer.create(this, R.raw.alert);
        alertDisplay = findViewById(R.id.alertDisplay);
        noBike = findViewById(R.id.noBike);

        nearToRoad = new NearToRoad();
        nearToRoadList = new ArrayList<>();

        roadAdress = findViewById(R.id.roadAdress);
        speedLimit = findViewById(R.id.speedLimitText);

        // Assume thisActivity is the current activity
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        Log.d("permissionCheck", permissionCheck + "");

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_READ_FINE_LOCATION);

                // MY_PERMISSION_REQUEST_READ_FINE_LOCATION is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        mDbOpenHelper = new DbOpenHelper(this);

        avg = new ArrayList<>();

        saved_DrivingCourseInformation = getSharedPreferences("savedDrivingCourseInformation", MODE_PRIVATE);
        saved_DrivingCourseInformation_Editor = saved_DrivingCourseInformation.edit();

        rectOptions = new PolylineOptions().width(10).color(Color.RED);
        selectedCourseRoute = new PolylineOptions().width(10).color(Color.RED);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        speed = (TextView) findViewById(R.id.speed);

        lockButtons = (ImageButton) findViewById(R.id.lockButton);
        lockButtons.setEnabled(false);
        recordingDrivingCourse = (ImageButton) findViewById(R.id.recordingDrivingCourse);
        load = findViewById(R.id.loadingCourse);
        countDown = (TextView) findViewById(R.id.countDown);

        lockButtons.setOnClickListener(view_onClickListener);
        recordingDrivingCourse.setOnClickListener(view_onClickListener);
        load.setOnClickListener(view_onClickListener);

        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        // Kick off the process of building the LocationCallback, LocationRequest, and
        // LocationSettingsRequest objects.
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();
//        updateUI();

    }// onCreate END

    View.OnClickListener view_onClickListener = new View.OnClickListener() { // 클릭 리스너들

        public void onClick(View v) {

            switch (v.getId()) {

                case R.id.recordingDrivingCourse:

                    /**
                     * 버튼을 재생/정지 모양으로 바꾸기
                     */
                    if (BUTTON_MODE == RECORDING_IS_OFF) {
                        activate_recordingDrivingCourse();
                    } else {
                        confirmRecordingStop();
                    }
                    break;

                case R.id.lockButton:

                    if (BUTTON_MODE == BUTTON_LOCK_ON) {
                        BUTTON_MODE = BUTTON_LOCK_OFF;
                    } else {
                        BUTTON_MODE = BUTTON_LOCK_ON;
                    }
                    updateUI();

                    break;

                case R.id.loadingCourse:

                    Intent i = new Intent(Recording_Driving_Mode.this, DrivingCourseInventory.class);
                    startActivityForResult(i, LOADING_COURSE_INFO);

                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == LOADING_COURSE_INFO) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {

                /**
                 * 차트화면에서 선택된 코스아이템의 ID를 받아옵니다.
                 */
                int courseID = data.getIntExtra("courseID", -1);

                LatLngBounds.Builder builder;
                LatLng courseStartingPoint = null;
                LatLng courseDestinationPoint = null;

                DbOpenHelper mDbOpenHelper = new DbOpenHelper(this);
                mDbOpenHelper.open();
                Cursor cursor = mDbOpenHelper.selectCourseInfo(courseID);

                double latitude = 0;
                double longitude = 0;
                Float speed;
                double altitude;

                int time = 0;

                if (cursor.moveToFirst()) {
                    do {
                        latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
                        longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));
                        speed = cursor.getFloat(cursor.getColumnIndex("speed"));
                        altitude = cursor.getDouble(cursor.getColumnIndex("altitude"));

                        /**
                         * 출발지점과 도착지점을 구별하기 위한 조건입니다.
                         * 아래 조건이 없다면, 출발지점과 도착지점에는 같은 값이 들어갑니다.
                         */
                        if (courseStartingPoint == null) {
                            courseStartingPoint = new LatLng(latitude, longitude);

                            Log.d("시작점 latlng val:", courseStartingPoint + "");
                        }

                        selectedCourseRoute.add(new LatLng(latitude, longitude));

                    } while (cursor.moveToNext());
                }

                courseDestinationPoint = new LatLng(latitude, longitude);

                Log.d("도착점 latlong val:", courseDestinationPoint + "");

                cursor.close();
                mDbOpenHelper.close();

                googlemap.addPolyline(selectedCourseRoute);

                builder = new LatLngBounds.Builder();
                builder.include(courseStartingPoint).include(courseDestinationPoint);

                int padding = 100;

                googlemap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), padding));


            }
        }
    }

    /**
     * 최초로 맵이 준비가 되면, 초기설정들을 해줍니다.
     * <p>
     * 1_맵의 확대/축소, 그리고 패닝을 제한합니다. 또한 맵을 스타일링 합니다.
     * <p>
     * 2_버튼을 누르면, 현재 유저위치로 카메라가 이동하는 기능을 활성화 합니다.
     * 3-1_유저의 위치를 얻고, 맵 중앙에 표시합니다.
     * 3-2_위치가 표시될 때, MOUNTAIN_VIEW 효과가 나타납니다.
     * 3-3_맵의 Tilting 과 Rotate 제스쳐를 비활성화 합니다.
     *
     * @param googleMap
     */
    @SuppressLint("MissingPermission")
    public void onMapReady(final GoogleMap googleMap) {

        this.googlemap = googleMap;

        /**
         * 구글맵을 스타일링 해줍니다.
         */
        GoogleMapStyling googleMapStyling = new GoogleMapStyling();
        googleMapStyling.customizing(this, googleMap);

        /**
         * 1_맵의 확대/축소, 그리고 패닝을 제한합니다.
         *
         * 왜냐하면 아래 설정된 확대/축소를 범위를 벗어나면,
         * 해당 앱에서는 유저에게 제공할 서비스가 전혀 없기 때문입니다.
         * 따라서 유저의 오작동을 방지함으로써, 편의를 제공하는 게 낫습니다.
         *
         * setMaxZoomPreference 는 맵을 확대할 수 있는 범위를 설정합니다.
         * 이는 유저위치를 기준으로, 반경 50m의 주변을 볼 수 있도록 맞춰졌습니다.
         * 더 이상의 확대는, 앱의 특성상 무의미합니다.
         * 왜냐하면 운전자는 주행을 위해서, 가시거리 이상의 환경이 필요합니다.
         * 지나친 확대는, 유저의 눈에 보이는 환경만을 지도에 표시하게 됩니다.
         * 이럴 경우 유저는, 맵을 다시 축소하는 불편을 겪게됩니다.
         *
         * setMinZoomPreference 는 맵을 축소할 수 있는 범위를 설정합니다.
         * 이는 대한민국 전체가, 화면에 나타날 수 있을만큼 맞춰졌습니다.
         * 해당 앱은 대한민국에서만 서비스를 제공하기 때문입니다.
         *
         * setLatLngBoundsForCameraTarget 는 지도의 패닝 범위를 설정합니다.
         * 패닝의 범위는 제주도와 울릉도, 그리고 독도까지 표시될 수 있을만큼 설정됬습니다.
         * 그 외 지역을 지도에 표시하는 것은 불필요 합니다.
         * 왜냐하면 해당 앱은 '대한민국'에서만 서비스를 제공할 수 있기 때문입니다.
         * 그 외 지역에 서비스를 하기 위해선, '다국어 지원'이 필요하지만, 해당 앱에서는 제공되지 않습니다.
         */
//        googleMap.setMaxZoomPreference(18f);
//        googleMap.setMinZoomPreference(7f);


        /**
         * 2_버튼을 누르면, 현재 유저위치로 카메라가 이동하는 기능을 활성화 합니다.
         *
         * 유저는 한번의 버튼클릭으로, 자신의 위치를 알 수 있습니다.
         * 기본적으로 맵의 우측상단에 자동생성 됩니다.
         *
         * (버튼의 위치는 '상대적'이기 때문에 변경될 수 있습니다.)
         * (한 예로, 맵에 설정된 Padding 값이 있다면, 버튼의 위치는 변경됩니다.)
         * (만약 맵의 top_padding이 50dp로 잡혀있다면, 버튼은 기존보다 50dp 아래 나타납니다.)
         *
         * (기본적으로 유저의 현재위치는 맵의 '중앙'에 나타납니다.)
         * (하지만 필요에따라 다른위치로 나타낼 수도 있습니다.)
         * (예를 들어 네비게이션의 앱의 경우, 유저의 현재위치를 맵의 하단에 나타납니다.)
         */
        googleMap.setMyLocationEnabled(true);


        /**
         * 3-1_유저의 위치를 맵 중앙에 표시합니다.
         *
         * 유저의 최근 위치는, 유저의 현재 위치와 가장 근접합니다.
         * 따라서 이를 기준으로 유저위치를 삼습니다.
         *
         * 3-2_위치가 표시될 때, MOUNTAIN_VIEW 효과가 나타납니다.
         */
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            public void onSuccess(Location location) {
                if (location != null) {

                    LatLng currentUserLocation = new LatLng(location.getLatitude(), location.getLongitude());

                    /**
                     * LatLng MOUNTAIN_VIEW :
                     * 이 위치는 지도상 대한민국의 '대략적인' 중심지 입니다.
                     *
                     * 애니메이션이 시작되는 위치이며, 유저의 위치에 따라 애니메이션 효과가 다르게 됩니다.
                     * 예를 들어, 서울에 있을경우와 부산에 있을 경우가 그렇습니다.
                     */

                    /**
                     * 애니메이션 효과가 형편없어서, 주석처리 합니다.
                     */
//                    LatLng MOUNTAIN_VIEW = new LatLng(36.7, 127.9);
//
//                    googlemap.moveCamera(CameraUpdateFactory.newLatLngZoom(MOUNTAIN_VIEW, 7));
//                    googlemap.animateCamera(CameraUpdateFactory.zoomIn());
//                    googlemap.animateCamera(CameraUpdateFactory.zoomTo(17), 2000, null);

                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(currentUserLocation)
                            .zoom(17)
                            .bearing(0)
                            .build();

                    googlemap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                    /**
                     * 3-3_맵의 Tilting 과 Rotate 제스쳐를 비활성화 합니다.
                     * 이 기능들은 앱에서 제공하므로, 유저가 조작할 필요가 없습니다.
                     */
                    googlemap.getUiSettings().setTiltGesturesEnabled(false);
                    googlemap.getUiSettings().setRotateGesturesEnabled(false);
                    googlemap.getUiSettings().setZoomControlsEnabled(true);
                }
            }
        });
    }

    public void activate_recordingDrivingCourse() {

        /**
         * 주행녹화 중에 화면이 꺼지지 않게 합니다.
         */
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        /**
         * 코스기록 전 5초동안 카운트다운이 됩니다.
         * 이로써 유저가 출발전 준비시간을 갖습니다.
         */
        CountDownAnimation countDownAnimation = new CountDownAnimation(countDown, 5);
        countDownAnimation.start();

        BUTTON_MODE = RECORDING_IS_ON;
        updateUI();

        previousLocation_isInitialized = false;
        totalDrivingMeter = 0;
        totalDrivingTime = 0;

        startLocationUpdates();

        startTime = System.currentTimeMillis();
        finishTime = 0;
    }

    /**
     * 코스기록이 완료되면(녹화중지), 코스정보가 저장됩니다.
     * 저장되는 정보는 다음과 같습니다.
     * <p>
     * 총 주행거리와 시간
     * 출발/도착 시간
     * 구간별(거리 또는 시간) 속도/Location(LatLng)
     * <p>
     * 저장된 정보는 코스정보를 간략히 또는 그래프로 나타낼 때 쓰입니다.
     * (데이터베이스에도 저장이 됩니다.)- 아직 미 구 현 상 태 -
     */
    public void inactivate_recordingDrivingCourse() {

        /**
         * 주행녹화가 시작될 때 설정됬던, '화면계속켜짐'을 종료합니다.
         */
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        BUTTON_MODE = RECORDING_IS_OFF;
        updateUI();

        stopLocationUpdates();

        saveDrivingCourseInformation();

        googlemap.clear();

    }

    /**
     * 녹화를 중지하고 코스기록을 저장하기전, 확인과정을 거칩니다.
     */
    public void confirmRecordingStop() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("주행완료");
        builder.setMessage("녹화를 중지합니다.");
        builder.setPositiveButton("녹화완료",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        inactivate_recordingDrivingCourse();
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }


    public void saveDrivingCourseInformation() {

        previousLocation_isInitialized = false;

        finishTime = System.currentTimeMillis();

        totalDrivingTime = (finishTime - startTime) / 1000l;

        startTime = 0;
        finishTime = 0;

        /**
         * SQLite 로 코스정보를 저장합니다.
         */
        mDbOpenHelper = new DbOpenHelper(this);
        mDbOpenHelper.open();
        mDbOpenHelper.create();

        /**
         * 임시로 현재 인덱스는 기록이완료된 시간으로 해놨습니다.
         */
        long time = System.currentTimeMillis();
        SimpleDateFormat dayTime = new SimpleDateFormat("hhmmss");
        String str = dayTime.format(new Date(time));

        Log.d("인덱싱:", str);
        int courseID = Integer.parseInt(str);

        for (int i = 0; i < traveledWayPoints.size(); i++) {

            if (i == traveledWayPoints.size() - 1) {
                mDbOpenHelper.insertColumn(courseID,
                        traveledWayPoints.get(i).getLatitude(),
                        traveledWayPoints.get(i).getLongitude(),
                        traveledWayPoints.get(i).getSpeed(),
                        traveledWayPoints.get(i).getAltitude(),
                        traveledWayPoints.get(i).getBearing(),
                        totalDrivingTime, totalDrivingMeter);
            } else {
                mDbOpenHelper.insertColumn(courseID,
                        traveledWayPoints.get(i).getLatitude(),
                        traveledWayPoints.get(i).getLongitude(),
                        traveledWayPoints.get(i).getSpeed(),
                        traveledWayPoints.get(i).getAltitude(),
                        traveledWayPoints.get(i).getBearing(),
                        0, 0);
            }
        }

        mDbOpenHelper.close();

        Toast.makeText(getApplicationContext(), "코스기록이 저장됐습니다", Toast.LENGTH_SHORT).show();

    }

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                mCurrentLocation = locationResult.getLastLocation();
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

                updateLocationUI();
//                updateLocationUIAdvanced(); // 구현 실패..ㅠ
            }
        };
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        /*
         * 위치데이터 업데이트 얼마나 자주 실행할지 설정합니다.
         * 하지만 이 설정이 반드시 그 값을 보장한다는 뜻은 아닙니다.
         * 설정된 값 보다 느리거나, 더 빠르게 업데이트를 받을 수도 있습니다.
         * 또한 위치데이터 업데이트가 불가능한 경우도 있습니다.
         */
        mLocationRequest.setInterval(1000);

        /**
         * 위치데이터 업데이트 간격의 가장 최대주기를 설정합니다.
         * 이것은 정확히, 이 수치보다 빠르게 업데이트를 받을 수 없게 해줍니다.
         * 단위는 millisecond 로, 1000은 1초를 나타냅니다.
         */
        mLocationRequest.setFastestInterval(1000);

        /**
         * 위치데이터의 정확도를 설정합니다.
         */
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    private void startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i("satisfied : ", "All location settings are satisfied.");

                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

//                        updateLocationUI();
                        updateLocationUIAdvanced();
                    }
                })

                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i("val", "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(Recording_Driving_Mode.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i("val", "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e("val", errorMessage);
                                Toast.makeText(Recording_Driving_Mode.this, errorMessage, Toast.LENGTH_LONG).show();
                                mRequestingLocationUpdates = false;
                        }

//                        updateLocationUI();
                        updateLocationUIAdvanced();
                    }
                });
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mRequestingLocationUpdates = false;
                    }
                });
    }

    /**
     * 주행 중 잘못된 조작을 '완화'하기 위해서, 버튼을 비활성화 시킵니다.
     * 예를 들어 유저의 오작동 또는 우천시 빗방울에 의해서, 화면이 오작동 할 수 있기 때문입니다.
     * <p>
     * updateUI()는 버튼을 활성화 또는 비활성화 시키며, 상태에 따라 배경색을 바꿉니다.
     * 버튼이 비활성화 된 상태에서는, 클릭을 할 수 없습니다.
     * 버튼 잠금, 주행녹화 시작/정지 , 코스 불러오기 버튼에 적용됩니다.
     */
    private void updateUI() {

        /**
         * 활성화는 O, 비활성화는 X로 표기했습니다.
         */
        switch (BUTTON_MODE) {

            /**
             * 주행녹화가 아직 시작되지 않은 상태입니다.
             *
             * 잠금 기능      : X
             * 녹화 시작/정지 :  O
             * 코스 불러오기  :  O
             */
            case RECORDING_IS_OFF:

                lockButtons.setEnabled(false);
                recordingDrivingCourse.setEnabled(true);
                load.setEnabled(true);

                googlemap.getUiSettings().setAllGesturesEnabled(true);
                googlemap.getUiSettings().setZoomControlsEnabled(true);
                googlemap.getUiSettings().setZoomControlsEnabled(true);
                googlemap.getUiSettings().setCompassEnabled(true);
                googlemap.getUiSettings().setMyLocationButtonEnabled(true);

                lockButtons.setImageDrawable(getResources().getDrawable(R.drawable.ic_lock_open_white_24dp));
                lockButtons.setBackgroundResource(R.drawable.mybutton_off);
                recordingDrivingCourse.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_black_24dp));
                recordingDrivingCourse.setBackgroundResource(R.drawable.mybutton);
                load.setBackgroundResource(R.drawable.mybutton);

                break;

            /**
             * 주행녹화가 시작된 상황입니다.
             *
             * 잠금 기능      : O
             * 녹화 시작/정지 :  O
             * 코스 불러오기  :  X
             */
            case RECORDING_IS_ON:

                lockButtons.setEnabled(true);
                recordingDrivingCourse.setEnabled(true);
                load.setEnabled(false);

                googlemap.getUiSettings().setAllGesturesEnabled(true);
                googlemap.getUiSettings().setZoomControlsEnabled(true);
                googlemap.getUiSettings().setZoomControlsEnabled(true);
                googlemap.getUiSettings().setCompassEnabled(true);
                googlemap.getUiSettings().setMyLocationButtonEnabled(true);

                lockButtons.setImageDrawable(getResources().getDrawable(R.drawable.ic_lock_open_white_24dp));
                lockButtons.setBackgroundResource(R.drawable.mybutton);
                recordingDrivingCourse.setImageDrawable(getResources().getDrawable(R.drawable.ic_stop_white_24dp));
                recordingDrivingCourse.setBackgroundResource(R.drawable.mybutton);
                load.setBackgroundResource(R.drawable.mybutton_off);

                break;

            /**
             * 주행녹화 중, 잠금버튼을 활성화 한 상태입니다. (잠금기능이 아직 시작되지 않음)
             *
             * 잠금 기능      : O
             * 녹화 시작/정지 :  X
             * 코스 불러오기  :  X
             */
            case BUTTON_LOCK_ON:

                lockButtons.setEnabled(true);
                recordingDrivingCourse.setEnabled(false);
                load.setEnabled(false);

                googlemap.getUiSettings().setAllGesturesEnabled(false);
                googlemap.getUiSettings().setZoomControlsEnabled(false);
                googlemap.getUiSettings().setCompassEnabled(false);
                googlemap.getUiSettings().setMyLocationButtonEnabled(false);

                lockButtons.setImageDrawable(getResources().getDrawable(R.drawable.ic_lock_white_24dp));
                lockButtons.setBackgroundResource(R.drawable.mybutton);
                recordingDrivingCourse.setImageDrawable(getResources().getDrawable(R.drawable.ic_stop_white_24dp));
                recordingDrivingCourse.setBackgroundResource(R.drawable.mybutton_off);
                load.setBackgroundResource(R.drawable.mybutton_off);

                break;

            /**
             * 주행녹화 중, 잠금버튼을 '비'활성화 한 상태입니다.
             *
             * 잠금 기능      : O
             * 녹화 시작/정지 :  O
             * 코스 불러오기  :  X
             */
            case BUTTON_LOCK_OFF:

                lockButtons.setEnabled(true);
                recordingDrivingCourse.setEnabled(true);
                load.setEnabled(false);
                googlemap.getUiSettings().setAllGesturesEnabled(true);
                googlemap.getUiSettings().setZoomControlsEnabled(true);

                googlemap.getUiSettings().setZoomControlsEnabled(true);
                googlemap.getUiSettings().setCompassEnabled(true);
                googlemap.getUiSettings().setMyLocationButtonEnabled(true);

                lockButtons.setImageDrawable(getResources().getDrawable(R.drawable.ic_lock_open_white_24dp));
                lockButtons.setBackgroundResource(R.drawable.mybutton);
                recordingDrivingCourse.setImageDrawable(getResources().getDrawable(R.drawable.ic_stop_white_24dp));
                recordingDrivingCourse.setBackgroundResource(R.drawable.mybutton);
                load.setBackgroundResource(R.drawable.mybutton_off);
                break;
        }
    }

    /**
     * 변경된 단말의 위치정보를 수신하고,
     * 해당 정보를 바탕으로, UI를 변경하거나 정보를 화면에표시합니다.
     * <p>
     * 아래와 같은 기능을 수행합니다.
     * <p>
     * - 지도회전이 기존보다 정확하고, 매끄럽게 진행됩니다.
     * - 유저위치를 정확히 도로에만 표시하고, 정확한 주행한 코스를 폴리리라인 통해 나타냅니다.
     */
    private void updateLocationUIAdvanced() {


         /*
         * 위치정확도가 너무 낮으면, 해당 위치데이터를 무시합니다.
         * 위치정확도 수치가 1이면, 최대 +-1m의 위치오차 나타냅니다.
         * 주행테스트 결과, 위치정확도의 제일 양호한 값은 4 였습니다.
         * 하지만 종종 24 또는 34(실내), 또는 48까지 떨어졌습니다.
         * 여러번 주행하며 평균정확도를 계산해 본 결과 12에 수렴했습니다.
         *
         * 따라서 해당 앱에서는 정확도가 15이하인, 위치데이터만 처리합니다.
         * 정확도 10 이하로도 주행테스트를 해봤으나,
         * 위치데이터가 너무 부족해서, 원만한 폴리라인(경로)를 그릴 수 없었습니다.
         */
        if (mCurrentLocation != null && mCurrentLocation.getAccuracy() <= 15) {

            /*
             * 유저가 도로에 있더라도 수신된 위치정보는,
             * 대게 어긋나거나, 도로를 벗어나는 모습을 보여주게 됩니다. 심한 경우 빌딩을 관통! 하는 모습을 보여줍니다.
             * 따라서 보다 정확한 유저의 위치정보를 얻기 위해서,
             * 수신한 위치정보를 기준으로, '가장 가까운 도로'를 찾습니다.
             */

            // if(...?) .. 가장 가까운 도로를 찾았으나,
            // 유저가 주행하고 있는 도로가 아니라면, 또는 이미 그 도로를 지나쳤다면,
            // 다시 가장 가까운 도로를 탐색합니다.

            nearToRoadList = nearToRoad.getJsonData(mCurrentLocation.getLatitude() + "", mCurrentLocation.getLongitude() + "");

            Location location = mCurrentLocation;

            float minDistance = 1000;

            for (int i = 0; i < nearToRoadList.size(); i++) {
                LatLng latLng = new LatLng(nearToRoadList.get(i).lat, nearToRoadList.get(i).lng);
                Marker marker = googlemap.addMarker(new MarkerOptions()
                        .position(latLng));

                rectOptions.add(latLng);
                googlemap.addPolyline(rectOptions);

                Log.d("초기화된 location lat/lng", location.getLatitude() + "/" + location.getLongitude());

                Log.d("조정할 latlng ", nearToRoadList.get(i).lat + "/" + nearToRoadList.get(i).lng);

                location.setLatitude(nearToRoadList.get(i).lat);
                location.setLongitude(nearToRoadList.get(i).lng);

                Log.d("조정된 location lat/lng", location.getLatitude() + "/" + location.getLongitude());

                minDistance = mCurrentLocation.distanceTo(location);

                Log.d("비교할 lat/lng", nearToRoadList.get(i).lat + "/" + nearToRoadList.get(i).lng);
                Log.d("비교 latlng:", location.getLatitude() + "/" + location.getLongitude());
                Log.d("현재 latlng:", mCurrentLocation.getLatitude() + "/" + mCurrentLocation.getLongitude());
                Log.d("도로까지 거리", minDistance + "");

            }
        }
    }

    @SuppressLint("NewApi")
    /**
     * 변경된 단말의 위치정보를 수신하고,
     * 해당 정보를 바탕으로, UI를 변경하거나 정보를 화면에표시합니다.
     *
     * - 유저지도상에 폴리라인을 그립니다.
     * - 지도회전
     * - 유저위치를 정확히 도로에만 표시
     */
    private void updateLocationUI() {

        /*
         * 위치정확도가 너무 낮으면, 해당 위치데이터를 무시합니다.
         * 위치정확도 수치가 1이면, 최대 +-1m의 위치오차 나타냅니다.
         * 주행테스트 결과, 위치정확도의 제일 양호한 값은 4 였습니다.
         * 하지만 종종 24 또는 34(실내), 또는 48까지 떨어졌습니다.
         * 여러번 주행하며 평균정확도를 계산해 본 결과 12에 수렴했습니다.
         *
         * 따라서 해당 앱에서는 정확도가 15이하인, 위치데이터만 처리합니다.
         *
         * 정확도 10이하로도 필터링 테스트 해봤으나,
         * 위치데이터가 부족해서, 원만한 폴리라인(경로)를 그릴 수 없었습니다.
         */

        /**
         * 위치데이터 값이 없거나, 정확도가 15m를 초과하면 무시합니다.
         */
        if (mCurrentLocation != null && mCurrentLocation.getAccuracy() <= 15) {

            nearToRoad.getJsonData(String.valueOf(mCurrentLocation.getLatitude()), String.valueOf(mCurrentLocation.getLongitude()));

            roadAdress.setText(nearToRoad.roadName);
            speedLimit.setText(nearToRoad.speed + "");

            /**
             * 이전 데이터의 최초의 값은, 현재 데이터의 값으로 초기화 준다.
             * 왜냐하면 위치데이터의 맨 처음값은, 항상 null 이기 때문이다.
             * 이전 데이터를 받은 적이 없으므로, 초기화가 안되있는 상태이기 때문이다.
             */
            if (!previousLocation_isInitialized) {
                previousLocation[0] = mCurrentLocation;

                previousSpeed = (int) (mCurrentLocation.getSpeed() * 3.6);

                previousLocation_isInitialized = true;
            }

            /**
             * 최근 5개의 값을 저장합니다.
             * 이는 위치데이터의 평균값을 구하기 위함입니다.
             * 왜냐하면 위치데이터 특성상, 항상 정확한 값을 얻을 수가 없기 때문입니다.
             * 따라서 해당 앱에서는 정확한 값이 아닌,
             * 제일 근접/근사한 값을 제공합니다.
             *
             * previousLocation 배열은, 데이터는 코스기록이 시작될 때 초기화 될 수 있습니다.
             * 왜냐하면 녹화가 시작되기 전에는, 데이터가 없으므로 초기화가 불가능하기 때문입니다. (위치데이터를 받은 적이 없으므로)
             * 아래는 최근 5개의 값들을 초기화 시켜주는 코드입니다.
             * 이는 카운트다운 5초동안 실행되며,
             * 보통 5개 값모두, 유저의 시작지점을 저장하게 됩니다.
             * 그리고 이중 가장 정확한 값들을 기준으로, 화면에 표시하거나 저장합니다.
             */
            for (int i = 0; i < previousLocation.length; i++) {
                if (previousLocation[i] == null) {
                    previousLocation[i] = mCurrentLocation;
                    previousLocation_isInitialized = true;
                }
            }
            /**
             * 가장 오래된 위치데이터를 지우고,
             * 최근 위치데이터를 저장합니다.
             * 밀어내기식..!
             */
            previousLocation[0] = previousLocation[1];
            previousLocation[1] = previousLocation[2];
            previousLocation[2] = previousLocation[3];
            previousLocation[3] = previousLocation[4];
            previousLocation[4] = mCurrentLocation;

            /**
             * 이전과 현재 지점사이의 거리를 구한다.
             */
            float meterBetweenPreviousLocationAndCurrentLocation = previousLocation[3].distanceTo(mCurrentLocation);

            /**
             * 시속(km/h) = 거리/시간
             * 아래에서 거리* 3.6을 해준이유는 다음과 같다.
             *
             * 현재 위치데이터 측정시간은 매 1초마다 계산된다.
             * 그리고 이전과 현재구간의 거리는 Meter 로 계산된다,
             * 즉 매순간 초속 몇미터인지를 계산하고 있는 것이다.
             *
             * 따라서 시간당 km 수를 구하기 위해서는,
             * 먼저 3600초(1시간)을 곱해야 한다.
             * 그리고 여기에 1000을 나눠줘야, km단위의 시속을 알 수 있다.
             *
             * 따라서 1*3600/1000 => 3.6 이다.
             */

            float currentSpeed_KmPerHour = (float) (meterBetweenPreviousLocationAndCurrentLocation * 3.6);

            LatLng currentLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

            /**
             * 위치데이터를 경로에 추가합니다.
             */
            rectOptions.add(currentLatLng);
            googlemap.addPolyline(rectOptions);
            traveledWayPoints.add(mCurrentLocation);

            totalDrivingTime += 1;
            totalDrivingMeter += meterBetweenPreviousLocationAndCurrentLocation;

            /**
             * 최근 5개의 위치데이터의 속도/베어링 평균값을 계산합니다.
             */

            float speeds = 0;
            float bearings = 0;

            for (int i = 0; i < previousLocation.length; i++) {
                speeds += previousLocation[i].getSpeed();
                bearings += previousLocation[i].getBearing();

                if (previousLocation[i].getAccuracy() <= goodacc) {

                    goodacc = previousLocation[i].getAccuracy();

                } else if (previousLocation[i].getAccuracy() > badacc) {

                    badacc = previousLocation[i].getAccuracy();
                }

            }

            avg.add(mCurrentLocation.getAccuracy());

            float avgACC = 0;
            for (int i = 0; i < avg.size(); i++) {
                avgACC += avg.get(i);
            }

            float averageSpeed = speeds / previousLocation.length;
            float averageBearing = bearings / previousLocation.length;

            Log.d("avgSpeed val", averageSpeed + "");
            Log.d("avgBearing val", averageBearing + "");

            currentSpeed = (int) (mCurrentLocation.getSpeed() * 3.6);


            /**
             * 속도제한 알람
             *
             * 현재 주행하고 있는 도로의 속도제한보다,
             * 유저가 주행하고 있는 속도가 빠르면,
             * 비프음이 울리면서, 화면에 적색으로 깜빡입니다.
             *
             */
            if (currentSpeed >= nearToRoad.speed) {
                player.start();
                alertDisplay.setVisibility(View.VISIBLE);
                speed.setText(currentSpeed + "");
                speed.setTextColor(Color.RED);
            } else {
                alertDisplay.setVisibility(View.INVISIBLE);
                speed.setText(currentSpeed + "");
                speed.setTextColor(Color.BLACK);
            }

            /**
             * 주행불가능한 도로
             *
             * 고속도로나 자동차전용도로에서 주행하고 있으면,
             * 경고가 울립니다.
             */
            if (nearToRoad.roadCategory == 0 || nearToRoad.roadCategory == 1) {
                player.start();
                alertDisplay.setVisibility(View.VISIBLE);
                noBike.setVisibility(View.VISIBLE);
            } else {
                alertDisplay.setVisibility(View.INVISIBLE);
                noBike.setVisibility(View.INVISIBLE);
            }


            /**
             * 속도계 애니메이션
             * 잠시 보류
             */
//            startCountAnimation(previousSpeed, currentSpeed);


            /**
             * 주행속도별 지도의 크기(줌레벨)를 변경합니다.
             * 저속일 때는 '좁은 범위를 자세히' 보여주고, 고속일 때는 '넓은 범위를 간략히' 보여줍니다.
             * 중간속도일 때는, 저속과 고속사이의 상태를 보여줍니다.
             *
             * 또한 유저의 진행방향에 따라, 맵을 회전시킵니다.
             */
            CameraPosition cameraPosition;

            /**
             * 코스 보정이 안되서, 축척을 크게 잡아, 코스가 어긋나는 것을 감췄습니다..
             * Todo: 코스녹화시 코스경로가 도로를 벗어나는 문제를 해결하면, 속도별 축척을 다르게 표시할 것
             * Todo: 애니메이트카메라로 이동시켰을 때, 맵 로딩이 지연되는 현상이 발생. 해결하면 moveCamera를 animateCamera로 변경할 것
             *
             */
            if (currentSpeed_KmPerHour < 45) { // 45km/h 이하일 때

                cameraPosition = new CameraPosition.Builder()
                        .target(currentLatLng)
                        .bearing(averageBearing)
                        .zoom(16)
                        .build();

                googlemap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                /**
                 *
                 * 아직 해결 못함..
                 */
            } else if (currentSpeed_KmPerHour < 75) { // 75km/h 이하일 때

                cameraPosition = new CameraPosition.Builder()
                        .target(currentLatLng)
                        .bearing(averageBearing)
                        .zoom(16)
                        .build();

                googlemap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            } else { // 75km/h 이상일 때

                cameraPosition = new CameraPosition.Builder()
                        .target(currentLatLng)
                        .bearing(averageBearing)
                        .zoom(16)
                        .build();

                googlemap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        }
    }

    /**
     * 속도계를 숫자형태로 나타냅니다.
     * 숫자값이 바뀔 때, 카운트 애니메이션 효과를 줍니다.
     * 예를들어 1에서 10으로 바뀔 때, 1씩 차례차례 올라가는 애니메이션 효과를 보여줍니다.
     *
     * @param previousSpeed
     * @param currentSpeed
     */
    private void startCountAnimation(int previousSpeed, int currentSpeed) {
        ValueAnimator animator = ValueAnimator.ofInt(previousSpeed, currentSpeed);
        animator.setDuration(1000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                speed.setText(animation.getAnimatedValue().toString());
            }
        });
        animator.start();
    }


    private String getArrivalTime() {

        // 시스템으로부터 현재시간(ms) 가져오기
        long now = System.currentTimeMillis();
        // Data 객체에 시간을 저장한다.
        Date date = new Date(now);
        // 각자 사용할 포맷을 정하고 문자열로 만든다.
//        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        String strNow = sdfNow.format(date);

        // 1. 위 코드를 2줄로 줄였다
        //        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//        time = sdfNow.format(new Date(System.currentTimeMillis()));

        // 2. 위 코드를 1줄로 줄였다. 하지만 위에 코드를 이해하지 못한 상태라면 가독성이 떨어질 수 있다.
//        time = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(System.currentTimeMillis()));

        return strNow;


    }

    /**
     * 주행녹화 액티비티가 종료되면, LocationUpdate 를 중지시킵니다.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopLocationUpdates();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

}
