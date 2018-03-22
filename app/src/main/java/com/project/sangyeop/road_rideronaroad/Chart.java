package com.project.sangyeop.road_rideronaroad;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.GeoApiContext;
import com.google.maps.RoadsApi;
import com.google.maps.model.SnappedPoint;
import com.skt.Tmap.TMapTapi;
import com.skt.Tmap.TMapView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Chart extends AppCompatActivity implements OnMapReadyCallback {

    String allCoords = "";
    /**
     * Tmap 지도를 띄우기 위한, API KEY 입니다.
     */
    private final String TMAP_API_KEY = "074f22a2-4edb-4f5d-84a3-c0afb479d173";

    TMapTapi tMapTapi;

    /**
     * 속도와 고도를 그려주는 차트입니다.
     */
    LineChart chart;
    LineChart altitudeChart;

    /**
     * 속도/고도 데이터를 저장하는 변수입니다.
     * 이 데이터는 차트 Y축에 그려지게 됩니다.
     */
    List<Entry> speedGraph; // 1번째 Y축
    List<Entry> altitudeGraph; // 2번째 Y축

    /**
     * 코스의 전체적인 이미지를 그리는 캔버스 역할을 합니다.
     */
    GoogleMap courseImage;

    /**
     * 코스의 폴리라인(경로)를 저장하는 역할을 합니다.
     */
    PolylineOptions rectOptions;


    /**
     * 코스의 시작점과 도착점이 저장됩니다.
     */
    LatLngBounds.Builder builder;
    LatLng courseStartingPoint = null;
    LatLng courseDestinationPoint = null;

    /**
     * 코스의 중간지점입니다.
     * <p>
     * 음.. 뭐라 설명해야하나.
     */

    /**
     * 지도의 여백, LatLngBounds 설정을 위한 변수입니다
     * 최대 동서남북 Latlng 값을 저장해서,
     * 코스가 맵 중앙에 위치하기 위해 쓰입니다.
     */

    double eastMost = 0;
    double westMost = 0;
    double northernMost = 0;
    double southernMost = 0;


    TextView drivingThisCourse;
    ImageButton backToInventory;  // 이전화면으로 돌아가는 버튼입니다.

    int courseID;
    private ProgressBar mProgressBar;

    /**
     * The API context used for the Roads and Geocoding web service APIs.
     */
    private GeoApiContext mContext;

    /**
     * The number of points allowed per API request. This is a fixed value.
     */
    private static final int PAGE_SIZE_LIMIT = 100;

    /**
     * Define the number of data points to re-send at the start of subsequent requests. This helps
     * to influence the API with prior data, so that paths can be inferred across multiple requests.
     * You should experiment with this value for your use-case.
     */
    private static final int PAGINATION_OVERLAP = 5;

    /**
     * 위치데이터의 원본을 나타냅니다.
     * 해당 데이터는 도로나 건물을 통과하는,
     * 정제전의 코스경로가 저장되어 있습니다.
     */
    List<com.google.maps.model.LatLng> mCapturedLocations;

    /**
     * 수정된 위치데이터들을 저장합니다
     * 코스가 도로나 빌딩을 통과하지 않고,
     * 오직 도로만 통과하는 코스입니다.
     */
    List<SnappedPoint> mSnappedPoints;

    /**
     * Snap to road 기능을 실행
     */
    AsyncTask<Void, Void, List<SnappedPoint>> mTaskSnapToRoads =
            new AsyncTask<Void, Void, List<SnappedPoint>>() {
                @Override
                protected void onPreExecute() { // 사전 작업
                    mProgressBar.setVisibility(View.VISIBLE); // 프로그래스바를 보이게 한다
                    mProgressBar.setIndeterminate(true); // 프로그래스바 무한진행
                }

                @Override
                protected List<SnappedPoint> doInBackground(Void... params) { // 백그라운드에서
                    try {
                        return snapToRoads(mContext); // snap to read 메소드를 실행, 전달인자는 Context -> 구글맵 web service key 를 지칭한다
                    } catch (final Exception ex) { // 오류시 예외처리
                        ex.printStackTrace();
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(List<SnappedPoint> snappedPoints) { // snap ro road 메소드가 완료되면
                    mSnappedPoints = snappedPoints; //    List<SnappedPoint> 에 저장. SnappedPoint class 는 location(LatLng) index(int) placeID(String)를 가지고있음
                    mProgressBar.setVisibility(View.INVISIBLE); // 작업이 완료됬으므로, 프로그래스바를 보이지 않게 한다.

//                    findViewById(R.id.speed_limits).setEnabled(true); // 속도제한 표시기능이 준비됬으므로, 버튼을 활성화 한다

                    com.google.android.gms.maps.model.LatLng[] mapPoints = // 이 변수는 LatLngBounds 를 설정하기 위해 쓰이고, 또한 스냅된 데이터를 저장하는 데 쓰인다.
                            // 왜 따로 또 저장을 할까? snappedPoints 에 이미 저장되 있는 건데?

                            new com.google.android.gms.maps.model.LatLng[mSnappedPoints.size()]; // 스냅포인트의 사이즈만큼 LatLng 배열의 크기를 생성한다.
                    int i = 0;
                    LatLngBounds.Builder bounds = new LatLngBounds.Builder(); // 코스가 맵에서 한 눈에 보이기 위해서, 모든 스냅포인트들을 include 시킨다. (난 북서 남동만 했는데 ㅠ)
                    for (SnappedPoint point : mSnappedPoints) {
                        mapPoints[i] = new com.google.android.gms.maps.model.LatLng(point.location.lat,
                                point.location.lng);
                        bounds.include(mapPoints[i]);
                        i += 1;
                    }

                    courseImage.clear(); // 기존의 부정확했던 위치데이터 경로를 지우고, 보정된 경로를 그립니다.

                    /**
                     * 출발점과 도착점을 표시합니다.
                     */
                    Marker startingPoint = courseImage.addMarker(new MarkerOptions()
                            .position(courseStartingPoint)
                            .title("출발"));
                    startingPoint.showInfoWindow();

                    Marker destinationPoint = courseImage.addMarker(new MarkerOptions()
                            .position(courseDestinationPoint)
                            .title("Goal"));
                    destinationPoint.showInfoWindow();

                    int padding = 150;
                    courseImage.addPolyline(new PolylineOptions().add(mapPoints).color(Color.BLUE)); // 스냅포인트들을 폴리라인으로 맵에 그려주고, 색깔은 파랑으로 한다.
                    courseImage.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), padding)); // 맵의 전체적인 모습을 볼 수 있도록, 맵의 포커스 줌레벨 바운드링을 한다.

                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        drivingThisCourse = (TextView) findViewById(R.id.drivingThisCourse);

        backToInventory = (ImageButton) findViewById(R.id.backToInventory);

        /**
         * 차트화면을 종료하고, 인벤토리 화면으로 돌아갑니다.
         */
        backToInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mCapturedLocations = new ArrayList<com.google.maps.model.LatLng>();

        mContext = new GeoApiContext.Builder()
                .apiKey("AIzaSyBJ5Jbp-ib_tYhzJjmmG2yuTpEZzH7K71c")
                .build();
        /**
         * 해당코스를 주행합니다.
         * 이 기능이 실행되면, 해당코스의 정보가, 맵에 올려집니다.
         */
        drivingThisCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /**
                 * 해당 코스를 기준으로 경쟁을 하거나, 안내를 받습니다.
                 *
                 * 현재 코스기록이 도로를 벗어나는 문제를 해결 중..
                 */
//                Intent intent = new Intent();
//
//                /**
//                 * 맵의 고유 ID를 전달해 줍니다.
//                 */
//                intent.putExtra("courseID", courseID);
//                setResult(RESULT_OK, intent);
//
//                finish();

                /**
                 * 스냅 투 로드는.. 국내에 적용할수가 없다 OTL... 내 시간
                 */
//                mTaskSnapToRoads.execute();

                /**
                 * 나만의 스냅 투 로드를 실행한다!
                 */

                com.google.maps.model.LatLng start_latLngModel = new com.google.maps.model.LatLng(courseStartingPoint.latitude, courseStartingPoint.longitude);
                com.google.maps.model.LatLng end_latLngModel = new com.google.maps.model.LatLng(courseDestinationPoint.latitude, courseDestinationPoint.longitude);

                SnapToRoad snapToRoad = new SnapToRoad(courseImage);
                snapToRoad.getJsonData(allCoords);

                Marker startingPoint = courseImage.addMarker(new MarkerOptions()
                        .position(courseStartingPoint)
                        .title("출발"));

                Marker destinationPoint = courseImage.addMarker(new MarkerOptions()
                        .position(courseDestinationPoint)
                        .title("Goal"));
                destinationPoint.showInfoWindow();
            }
        });

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.CourseImage);
        mapFragment.getMapAsync(this);

        /**
         * 차트를 초기화 시켜줍니다.
         */
        chart = (LineChart) findViewById(R.id.speedLineChart);
        altitudeChart = (LineChart) findViewById(R.id.altitudeLineChart);

        /**
         * 폴리라인(코스)를 지도에 그리기 위해, 옵션을 설정합니다.
         * 경로는 빨간색으로 표시되며, 너비는 10으로 설정됬습니다.
         */
        rectOptions = new PolylineOptions().width(10).color(Color.DKGRAY);

        /**
         * 구간별 속도와 고도를 저장하기위해, 초기화를 시켜줍니다.
         */
        speedGraph = new ArrayList<Entry>();
        altitudeGraph = new ArrayList<Entry>();

    }

    /**
     * Roads API를 사용하여 포인트를 도로의 가장 가능성있는 위치에 스냅합니다.
     *
     * @param context : 구글 서비스의 API Key 를 지칭합니다.
     * @return : 작업이 완료되고, 맵에 그 결과(스냅된 포인트)를 그리기 위해 사용됩니다.
     * @throws Exception
     */
    private List<SnappedPoint> snapToRoads(GeoApiContext context) throws Exception {
        List<SnappedPoint> snappedPoints = new ArrayList<>();

        int offset = 0;

        /**
         * mCapturedLocations 는 스냅처리 전의, 원본데이터 입니다.
         * 즉 주행하면 기록된 위치데이터들의 집합입니다. 이 데이터는 부정확하기에,
         * 도로를 벗어나거나, 빌딩을 통과! 하는 경우가 많습니다.
         *
         * 이 점을 개선하기 위해, Snap to road 기능을 사용합니다.
         */
        while (offset < mCapturedLocations.size()) {
            // Calculate which points to include in this request. We can't exceed the APIs
            // maximum and we want to ensure some overlap so the API can infer a good location for
            // the first few points in each request.

            // 이 요청에 포함 할 점을 계산합니다. 우리는 API를 초과 할 수 없습니다.
            // 최대치를 유지하고 API가 좋은 위치를 추측 할 수 있도록 일부 겹치는 부분을 확보하려고합니다.
            // 각 요청의 처음 몇 점.

            /**
             *  Snap to road 의 실질적인 계산부분 입니다.
             *  APIs 의 최대값을 초과할 수 없으며 (포인트 갯수 최대 100 ? ? ?)
             *  보다 좋은 결과를 위해, 포인트 중 일부 겹치는 부분이 필요합니다. ( 각 요청의 처음 몇 포인트는)
             */

            if (offset > 0) {
                offset -= PAGINATION_OVERLAP;   // Rewind to include some previous points
                // 일부 이전 점을 포함하도록 되감기

                // 아!! 초반 5개는 그냥 겹쳐버리는 구나?! 저 상수변수 값이 5니니깐
                // 흠.. 아직 잘 모르겠군 그래도 ㅇㅇ
            }
            int lowerBound = offset;

            /**
             *  offset + 페이지 리밋(100) vs 원본데이터 사이즈
             *  중에서 더 작은 값을 최대바운드로 설정합니다.
             *
             *  어디에 쓰이는 변수고, 왜 이렇게 계산하는 지 모르겠다. 아직
             */
            int upperBound = Math.min(offset + PAGE_SIZE_LIMIT, mCapturedLocations.size());

            /**
             * 인덱스.. sublist 흠.. 이거 첨 보네 ㅇㅇ.. 암튼 그만큼 배열크기를 선언한다
             * 왜 그만큼 선언할까?
             * 저 최대값이랑 최소값은 왜 필요하고, 어떻게 계산되는거지?
             * 왜 필요하지?
             */
            // Grab the data we need for this page.
            com.google.maps.model.LatLng[] page = mCapturedLocations
                    .subList(lowerBound, upperBound)
                    .toArray(new com.google.maps.model.LatLng[upperBound - lowerBound]);

            // Perform the request. Because we have interpolate=true, we will get extra data points
            // between our originally requested path. To ensure we can concatenate these points, we
            // only start adding once we've hit the first new point (i.e. skip the overlap).

            // 요청을 수행합니다. interpolate = true이므로 추가 데이터 요소를 얻습니다.
            // 원래 요청한 경로 사이. 이러한 점들을 연결하기 위해 우리는
            // 첫 번째 새 점에 도달하면 추가 시작 (즉, 겹침 건너 뛰기)됩니다.

            /**
             *  요청을 수행합니다. interpolate 가 true 이므로, 원래 요청한 경로 사이의 추가 데이터 요소를 얻습니다.
             *  이러한 점들을 연결하기 위해서, 첫번째 새로운 점에 도달하면 추가를 시작합니다.  (즉, 겹침을 건너뜁니다) 흠?!?!?!!?
             */

            for (int i = 0; i < page.length; i++) {
                Log.d("LatLng", page[i].lat + "/" + page[i].lng);
            }

            SnappedPoint[] points = RoadsApi.snapToRoads(context, true, page).await();

            /**
             * 여기서 자꾸 에러가 나네..  반환되는 게 ㄴ없다는건데..
             */
            Log.d("포인트 사이즈", points.length + "");

            boolean passedOverlap = false;
            for (SnappedPoint point : points) {
                if (offset == 0 || point.originalIndex >= PAGINATION_OVERLAP) {
                    passedOverlap = true;
                }
                if (passedOverlap) {
                    snappedPoints.add(point);
                }
            }

            offset = upperBound;
        }

        return snappedPoints;
    }

    /**
     * 맵이 준비되면 코스정보를 불러옵니다.
     * 코스정보를 맵에 그려줘야 하기 때문에,
     * onCreate에서 정보를 불러오면 문제가 됩니다.
     * 왜냐하면 맵이 준비안된상태에서, 맵에 경로를 그리는 것이 불가능하기 때문입니다.
     *
     * @param googleMap : 코스를 그릴 수 있는 캔버스 역할을 합니다.
     */
    public void onMapReady(GoogleMap googleMap) {

        this.courseImage = googleMap;

        /**
         * 구글맵을 스타일링 해줍니다.
         *
         * 국내에선 구글맵 스타일링 적용이 불가해서, 주석처리했습니다.
         */
//        GoogleMapStyling googleMapStyling = new GoogleMapStyling();
//        googleMapStyling.customizing(this, googleMap);

        loadCourseInfo(); // 코스정보를 불러옵니다.

    }

    /**
     * 코스정보를 불러와주는 메소드 입니다.
     */
    private void loadCourseInfo() {

        /**
         * 이전 화면에서 클릭된 아이템으로 부터,
         * 인덱스 값을 전달 받습니다.
         * 이값은 내부 DB (SQLite)내 저장된 레코드의 고유 인덱스 번호입니다.
         *
         * 이 값을 기준으로, 해당 코스정보들을 불러오게 됩니다.
         */
        Intent intent = getIntent();
        String index = intent.getStringExtra("index");
        courseID = Integer.parseInt(index);

        /**
         * 내부 DB 에 접근해서, 데이터들을 조회합니다.
         */
        DbOpenHelper mDbOpenHelper = new DbOpenHelper(this);
        mDbOpenHelper.open();
        Cursor cursor = mDbOpenHelper.selectCourseInfo(courseID);

        double latitude = 0;
        double longitude = 0;
        Float speed;
        double altitude;

        int time = 0;

        if (cursor.moveToFirst()) {
            /**
             * 레코드 수 만큼 반복하며
             * 좌표(LatLng) 고도 속도를 불러옵니다.
             * 이 데이터는 차트에 그려지게 됩니다.
             */

            do {
                latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
                longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));
                speed = cursor.getFloat(cursor.getColumnIndex("speed"));
                altitude = cursor.getDouble(cursor.getColumnIndex("altitude"));


                if (time == 0) {
                    allCoords += longitude + "," + latitude;
                } else {
                    allCoords += "|" + longitude + "," + latitude;
                }


                /**
                 * 최대 동서남북을 구하기 전, 초기화를 시켜줍니다.
                 * Longitude 가 작을수록 동쪽, 클수록 서쪽입니다.
                 * Latitude 가 작을수록 남쪽, 클수록 북쪽입니다.
                 */
                if (eastMost == 0)
                    eastMost = longitude;

                if (westMost == 0)
                    westMost = longitude;

                if (southernMost == 0)
                    southernMost = latitude;

                if (northernMost == 0)
                    northernMost = latitude;

                if (eastMost > longitude)
                    eastMost = longitude;

                if (westMost < longitude)
                    westMost = longitude;

                if (southernMost > latitude)
                    southernMost = latitude;

                if (northernMost < latitude)
                    northernMost = latitude;

                /**
                 * 출발지점과 도착지점을 구별하기 위한 조건입니다.
                 * 아래 조건이 없다면, 출발지점과 도착지점에는 같은 값이 들어가게 됩니다.
                 */
                if (courseStartingPoint == null) {
                    courseStartingPoint = new LatLng(latitude, longitude);
                }

                rectOptions.add(new LatLng(latitude, longitude));
                mCapturedLocations.add(new com.google.maps.model.LatLng(latitude, longitude));

                speed = speed * 36;
                speed = speed / 10;
                speedGraph.add(new Entry(time, speed));

                altitudeGraph.add(new Entry(time, (int) altitude));

                time++;

            } while (cursor.moveToNext());

        }

        courseDestinationPoint = new LatLng(latitude, longitude);

        Marker startingPoint = courseImage.addMarker(new MarkerOptions()
                .position(courseStartingPoint)
                .title("출발"));

        Marker destinationPoint = courseImage.addMarker(new MarkerOptions()
                .position(courseDestinationPoint)
                .title("Goal"));
        destinationPoint.showInfoWindow();

        Log.d("구글맵 출발점", courseStartingPoint + "");
        Log.d("구글맵 도착점", courseDestinationPoint + "");

        cursor.close();
        mDbOpenHelper.close();

        courseImage.addPolyline(rectOptions);

        LatLng northWest = new LatLng(northernMost, westMost);
        LatLng southEast = new LatLng(southernMost, eastMost);

        builder = new LatLngBounds.Builder();
        builder.include(northWest).include(southEast);

        int padding = 150;

        courseImage.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), padding));

        settingChart();

    }

    /**
     * 데이터를 차트에 그려주는 메소드 입니다.
     */
    public void settingChart() {

        /**
         * 속도/고도 데이터를 초기화하고,
         * 그래프에 그려질 모습을 결정합니다.
         */
        LineDataSet speedDataSet = new LineDataSet(speedGraph, "속도");
        speedDataSet.setColor(Color.RED);
        speedDataSet.setLineWidth(2f);
        speedDataSet.setValueTextColor(Color.RED);

        LineDataSet altitudeDataSet = new LineDataSet(altitudeGraph, "고도");
        altitudeDataSet.setColor(Color.YELLOW);
        altitudeDataSet.setLineWidth(2f);
        altitudeDataSet.setValueTextColor(Color.YELLOW);

        /**
         * 초기화된 속도/고도 데이터를, 차트에 셋팅합니다..
         */
        LineData lineData = new LineData(speedDataSet);
        chart.setData(lineData);

        LineData lineData2 = new LineData(altitudeDataSet);
        altitudeChart.setData(lineData2);

        /**
         * 차트 하단에 Description 텍스트를 표시하지 않습니다.
         */
        chart.getDescription().setEnabled(false);
        altitudeChart.getDescription().setEnabled(false);

        /**
         * 차트를 좌에서 우로, 애니메이션 효과를 줍니다.
         */
        chart.animateX(1000);
        altitudeChart.animateX(1000);

        /**
         * 차트를 아래에서 위로, 애니메이션 효과를 줍니다.
         */
        chart.animateY(3000);
        altitudeChart.animateY(3000);

        /**
         * X축을 설정해 줍니다
         * 이 곳에 시간 단위가 표시됩니다 (시/분/초)
         */

        /**
         * 고도 단위표시를 커스터마이징 합니다. (시 분 초)
         */

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // X축 단위표시를 차트 상단이 아닌, 하단에 표시하게 됩니다.
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(false); // X축의 그리드라인을 그리지 않습니다.

        xAxis.setValueFormatter(new MyValueXFormatter()); // X축의 단위를 '시/분/초'로 formatting 합니다.

        XAxis xAxis2 = altitudeChart.getXAxis();
        xAxis2.setPosition(XAxis.XAxisPosition.BOTTOM); // X축 단위표시를 차트 상단이 아닌, 하단에 표시하게 됩니다.
        xAxis2.setTextSize(10f);
        xAxis2.setTextColor(Color.BLACK);
        xAxis2.setDrawGridLines(false); // X축의 그리드라인을 그리지 않습니다.

        xAxis2.setValueFormatter(new MyValueXFormatter()); // X축의 단위를 '시/분/초'로 formatting 합니다.

        /**
         * Y축의 왼쪽축을 표시하지 않습니다.
         */
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setEnabled(false);

        YAxis leftAxis2 = altitudeChart.getAxisLeft();
        leftAxis2.setEnabled(false);

        /**
         * Y축의 오른쪽을 설정해 줍니다.
         * 이곳에 각각 속도/고도 단위가 표시됩니다. (km/h) / (m)
         */
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setTextColor(Color.RED);
        rightAxis.setAxisMaximum(100); // 최대 최소값도 동적으로 변경해 볼 수 있겠는 걸?
        rightAxis.setAxisMinimum(0);
        rightAxis.setDrawGridLines(false);
        rightAxis.setDrawZeroLine(true);
        rightAxis.setGranularityEnabled(false);
        speedDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
        rightAxis.setValueFormatter(new MyValueYFormatter()); // Y축의 단위를 km/h 로 formatting 합니다.

        YAxis rightAxis2 = altitudeChart.getAxisRight();
        rightAxis2.setTextColor(Color.BLUE);
        rightAxis2.setAxisMaximum(200);
        rightAxis2.setAxisMinimum(0);
        rightAxis2.setDrawGridLines(false);
        rightAxis2.setDrawZeroLine(true);
        rightAxis2.setGranularityEnabled(false);
        altitudeDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
        rightAxis2.setValueFormatter(new MyValueYFormatter2()); // Y축의 단위를 km/h 로 formatting 합니다.

        /**
         * 속도 단위표시를 커스터마이징 합니다. (시속 km/h)
         */
        YAxis yAxis = chart.getAxisRight();
        yAxis.setValueFormatter(new MyValueYFormatter());

        /**
         * 차트를 스타일링 해줍니다.
         */
        List<ILineDataSet> sets = chart.getData()
                .getDataSets();

        for (ILineDataSet iSet : sets) {

            LineDataSet set = (LineDataSet) iSet;
            set.setMode(set.getMode() == LineDataSet.Mode.CUBIC_BEZIER
                    ? LineDataSet.Mode.LINEAR
                    : LineDataSet.Mode.CUBIC_BEZIER);
        }

        for (ILineDataSet iSet : sets) {

            LineDataSet set = (LineDataSet) iSet;
            set.setDrawValues(!set.isDrawValuesEnabled());
        }

        for (ILineDataSet iSet : sets) {

            LineDataSet set = (LineDataSet) iSet;
            if (set.isDrawCirclesEnabled())
                set.setDrawCircles(false);
            else
                set.setDrawCircles(true);
        }

        List<ILineDataSet> sets2 = altitudeChart.getData()
                .getDataSets();

        for (ILineDataSet iSet : sets2) {

            LineDataSet set = (LineDataSet) iSet;
            set.setMode(set.getMode() == LineDataSet.Mode.CUBIC_BEZIER
                    ? LineDataSet.Mode.LINEAR
                    : LineDataSet.Mode.CUBIC_BEZIER);
        }

        for (ILineDataSet iSet : sets2) {

            LineDataSet set = (LineDataSet) iSet;
            set.setDrawValues(!set.isDrawValuesEnabled());
        }

        for (ILineDataSet iSet : sets2) {

            LineDataSet set = (LineDataSet) iSet;
            if (set.isDrawCirclesEnabled())
                set.setDrawCircles(false);
            else
                set.setDrawCircles(true);
        }

        /**
         * 차트를 그려줍니다.
         */
        chart.invalidate();
    }

    class MyValueXFormatter implements IAxisValueFormatter {

        private DecimalFormat mFormat;

        /**
         * 시간 데이터에 대한 formatting 을 해줍니다.
         * 단위는 (시/분/초)로 표시됩니다.
         */
        public MyValueXFormatter() {
            mFormat = new DecimalFormat("#,##0"); // use one decimal
        }

        public String getFormattedValue(float value, AxisBase axis) {

            if (value >= 60) {

                float minute = value / 60;
                int second = (int) value % 60;

                if (second == 0) {
                    return mFormat.format(minute) + ""; // e.g. append a dollar-sign
                } else {
                    return mFormat.format(minute) + ":" + second + ""; // e.g. append a dollar-sign
                }

            } else {
                return mFormat.format(value) + ""; // e.g. append a dollar-sign
            }

        }

    }

    /**
     * 고도(높이) 데이터에 대한 formatting 을 해줍니다.
     * 단위는 m로 표시됩니다.
     */
    class MyValueYFormatter implements IAxisValueFormatter {

        private DecimalFormat mFormat;

        public MyValueYFormatter() {
            mFormat = new DecimalFormat("#,##0"); // use one decimal
        }

        public String getFormattedValue(float value, AxisBase axis) {

            return mFormat.format(value) + " km/h"; // e.g. append a dollar-sign
        }

    }

    /**
     * 고도(높이) 데이터에 대한 formatting 을 해줍니다.
     * 단위는 m로 표시됩니다.
     */
    class MyValueYFormatter2 implements IAxisValueFormatter {

        private DecimalFormat mFormat;

        public MyValueYFormatter2() {
            mFormat = new DecimalFormat("#,##0"); // use one decimal
        }

        public String getFormattedValue(float value, AxisBase axis) {

            return mFormat.format(value) + "m"; // e.g. append a dollar-sign
        }

    }


}
