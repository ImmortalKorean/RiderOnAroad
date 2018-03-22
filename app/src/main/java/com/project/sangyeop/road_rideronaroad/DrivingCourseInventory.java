package com.project.sangyeop.road_rideronaroad;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashSet;

public class DrivingCourseInventory extends AppCompatActivity {

    ArrayList<Integer> courseID;
    ArrayList<String> title;
    ArrayList<Float> distance;
    ArrayList<Long> time;
    ArrayList<Boolean> checkBox;

    ArrayList<PolylineOptions> polylineList;
    ArrayList<LatLng> northWestList;
    ArrayList<LatLng> southEastList;

    final int LOADING_COURSE_INFO = 123;

    private ListFragment mList;
    private MapAdapter mAdapter;

    ImageButton backToDrivingMode;
    ImageButton deleteCourseItem;
    static int deleteButton_isClicked = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driving_course_inventory);

        backToDrivingMode = (ImageButton) findViewById(R.id.backToDrivingMode);
        backToDrivingMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        deleteCourseItem = (ImageButton) findViewById(R.id.deleteCourseItem);
        deleteCourseItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deleteButton_isClicked = 1;

                mAdapter.notifyDataSetChanged();

            }
        });

        /**
         * 차트화면에서 응용하자
         */
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
//        collapsingToolbarLayout.setTitle("주행코스 ");
//        collapsingToolbarLayout.setContentScrimColor(getApplicationContext().getResources().getColor(R.color.gray));

        /**
         * 코스정보의 인덱스, 제목, 총 주행거리/시간, 코스각각의 LatLng(폴리라인)을 저장하는 역할을 합니다.
         * 저장된 정보는 리스트뷰 각 아이템에 표시됩니다.
         */
        courseID = new ArrayList<>();
        title = new ArrayList<>();
        distance = new ArrayList<>();
        time = new ArrayList<>();
        checkBox = new ArrayList<>();

        polylineList = new ArrayList<>();
        northWestList = new ArrayList<>();
        southEastList = new ArrayList<>();

        /**
         * 코스정보를 불러와서, 위의 각각 ArrayList 에 저장합니다.
         */
        loadCourseInfo();

        /**
         * 어뎁터를 초기화 시켜준다.
         * 동시에 위치데이터들을 넣어줌
         * 생성자에 있나봐?
         */
        // Set a custom list adapter for a list of locations

        NamedLocation[] LIST_LOCATIONS = new NamedLocation[polylineList.size()];

        for (int i = 0; i < polylineList.size(); i++) {

            LIST_LOCATIONS[i] = new NamedLocation(polylineList.get(i), northWestList.get(i), southEastList.get(i));
        }

        mAdapter = new MapAdapter(this, LIST_LOCATIONS, title, distance, time, courseID, polylineList, checkBox);

        /**
         * 이놈은 리스트뷰를 나타낼 영역
         */
        mList = (ListFragment) getSupportFragmentManager().findFragmentById(R.id.courseRecordList);

        /**
         * 그 영역에 어댑터를 셋팅
         */
        mList.setListAdapter(mAdapter);


        /**
         * ListView에서 MapView를 정리하도록 RecyclerListener를 설정하십시오.
         * 흠...
         */
        // Set a RecyclerListener to clean up MapView from ListView
        AbsListView lv = mList.getListView();
        lv.setRecyclerListener(mRecycleListener);

    }

    /**
     * Adapter that displays a title and {@link com.google.android.gms.maps.MapView} for each item.
     * The layout is defined in <code>lite_list_demo_row.xml</code>. It contains a MapView
     * that is programatically initialised in
     * {@link #getView(int, android.view.View, android.view.ViewGroup)}
     */
    class MapAdapter extends ArrayAdapter<NamedLocation> {

        /**
         * 각 ArrayList 에는, 제목, 총 주행거리/시간, 코스정보의 고유인덱스가 저장됩니다.
         * 이는 리스트뷰 각 아이템의 정보를 표시할 때 쓰입니다.
         */
        ArrayList<String> title;
        ArrayList<Float> distance;
        ArrayList<Long> time;
        ArrayList<Integer> courseID;
        ArrayList<PolylineOptions> polylineList;
        ArrayList<Boolean> isChecked;

        private final HashSet<MapView> mMaps = new HashSet<MapView>();

        public MapAdapter(Context context, NamedLocation[] locations, ArrayList<String> title, ArrayList<Float> distance, ArrayList<Long> time, ArrayList<Integer> courseID, ArrayList<PolylineOptions> polylineList, ArrayList<Boolean> checkbox) {
            super(context, R.layout.inventory_row, R.id.lite_listrow_text, locations);
            this.title = title;
            this.distance = distance;
            this.time = time;
            this.courseID = courseID;
            this.polylineList = polylineList;
            this.isChecked = checkbox;
//            this.isChecked.add(false);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            /**
             * 체크박스값을 유지하기 위한 변수입니다.
             */
            final int checkBoxPosition = position;

            /**
             * 시간을 분/초 단위로 나타내기 위한 변수입니다.
             */
            int minute = (int) (time.get(position) / 60);
            int second = (int) (time.get(position) % 60);

            View row = convertView;
            final ViewHolder holder;
            final Context context = parent.getContext();

            // Check if a view can be reused, otherwise inflate a layout and set up the view holder
            if (row == null) {
                // Inflate view from layout file
                row = getLayoutInflater().inflate(R.layout.inventory_row, null);

                // Set up holder and assign it to the View
                holder = new ViewHolder();
                holder.mapView = (MapView) row.findViewById(R.id.drivingCourse_Image);

                holder.drivingCourse_Title = (TextView) row.findViewById(R.id.drivingCourse_Title);
                holder.drivingCourse_TotalDistance = (TextView) row.findViewById(R.id.drivingCourse_TotalDistance);
                holder.drivingCourse_TotalTime =  row.findViewById(R.id.drivingCourse_TotalTime);
                holder.drivingCourseID =  row.findViewById(R.id.courseID);
                holder.drivingCourse_CheckBox = (CheckBox) row.findViewById(R.id.drivingCourse_CheckBox);

                /**
                 *
                 */
                holder.drivingCourse_CheckBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                /**
                 * 코스기록 아이템 중 하나를 선택하면,
                 * 해당 아이템에 대한 정보를 상세히 보여줍니다.
                 */
                row.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String index = holder.drivingCourseID.getText().toString();
                        Intent intent = new Intent(context, Chart.class);
                        intent.putExtra("index", index);
                        ((Activity) context).startActivityForResult(intent, LOADING_COURSE_INFO);

                    }
                });

                // Set holder as tag for row for more efficient access.
                row.setTag(holder);

                // Initialise the MapView
                holder.initializeMapView();

                // Keep track of MapView
                mMaps.add(holder.mapView);
            } else {
                // View has already been initialised, get its holder
                holder = (ViewHolder) row.getTag();
            }

            // Get the NamedLocation for this item and attach it to the MapView
            NamedLocation item = getItem(position);
            holder.mapView.setTag(item);

            // Ensure the map has been initialised by the on map ready callback in ViewHolder.
            // If it is not ready yet, it will be initialised with the NamedLocation set as its tag
            // when the callback is received.
            if (holder.map != null) {
                // The map is already ready to be used
                setMapLocation(holder.map, item);
            }

            // Set the text label for this item
//            holder.title.setText(item.name);

            /**
             * 거리를 m또는 km로 나타내기 위한 변수입니다.
             * 1000m 미만이면, m로 표시하고,
             * 그렇지 않으면 1.1km와 같이 소수점 한자리까지만 km로 표시합니다.
             */
            if (distance.get(position) >= 1000) {
                String meter = String.format("%.1f", distance.get(position) / 1000);
                holder.drivingCourse_TotalDistance.setText("총 주행거리 : " + meter + "km");
            } else {
                String meter = String.format("%.0f", distance.get(position));
                holder.drivingCourse_TotalDistance.setText("총 주행거리 : " + meter + "m");
            }

            /**
             * 시간을 분/초 단위로 나타냅니다.
             */
            if (minute == 0) {
                holder.drivingCourse_TotalTime.setText("총 주행시간 : " + second + "초");
            } else {
                holder.drivingCourse_TotalTime.setText("총 주행시간 : " + minute + "분 " + second + "초");
            }

            /**
             * 코스의 인덱스를 나타냅니다.
             * 하지만 화면에 표시되지는 않습니다.
             * 이 값은 아이템을 클릭햇을 때, 참고용으로만 씁니다.
             */
            holder.drivingCourseID.setText(String.valueOf(courseID.get(position)));

            /**
             * 휴지통 버튼을 클릭했을 때, 숨겨진 체크박스가 표시됩니다.
             * 유저는 체크박스를 통해, 코스 아이템을 삭제할 수 있습니다.
             */

            if (deleteButton_isClicked == 0) {
                holder.drivingCourse_CheckBox.setVisibility(View.INVISIBLE);
            } else {
                holder.drivingCourse_CheckBox.setVisibility(View.VISIBLE);
            }


            return row;
        }

        /**
         * Retuns the set of all initialised {@link MapView} objects.
         *
         * @return All MapViews that have been initialised programmatically by this adapter
         */
        public HashSet<MapView> getMaps() {
            return mMaps;
        }
    }

    /**
     * Displays a {@link LiteListDemoActivity.NamedLocation} on a
     * {@link com.google.android.gms.maps.GoogleMap}.
     * Adds a marker and centers the camera on the NamedLocation with the normal map type.
     */
    private static void setMapLocation(GoogleMap map, NamedLocation data) {

        LatLng seoul = new LatLng(37.481179, 126.952763);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 13f));

        PolylineOptions rectOptions = data.polylineOptions;

        map.addPolyline(rectOptions);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(data.northWest).include(data.southEast);

        int padding = 50;

        map.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), padding));

        /**
         * 코스의 전체적인 이미지를 보여주는 맵뷰입니다.
         * 이 맵뷰를 NormalMode 로 설정하고,
         * 모든 제스처와 툴바를 Off 시킵니다.
         */
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setMapToolbarEnabled(false);
        map.getUiSettings().setAllGesturesEnabled(false);
    }

    /**
     * Holder for Views used in the {@link LiteListDemoActivity.MapAdapter}.
     * Once the  the <code>map</code> field is set, otherwise it is null.
     * When the {@link #onMapReady(com.google.android.gms.maps.GoogleMap)} callback is received and
     * the {@link com.google.android.gms.maps.GoogleMap} is ready, it stored in the {@link #map}
     * field. The map is then initialised with the NamedLocation that is stored as the tag of the
     * MapView. This ensures that the map is initialised with the latest data that it should
     * display.
     */
    class ViewHolder implements OnMapReadyCallback {

        MapView mapView;

        TextView drivingCourse_Title;
        TextView drivingCourse_TotalDistance;
        TextView drivingCourse_TotalTime;
        TextView drivingCourseID;
        CheckBox drivingCourse_CheckBox;

        GoogleMap map;

        @Override
        public void onMapReady(GoogleMap googleMap) {
            MapsInitializer.initialize(getApplicationContext());
            map = googleMap;
            NamedLocation data = (NamedLocation) mapView.getTag();
            if (data != null) {
                setMapLocation(map, data);
            }

            /**
             * 구글맵을 스타일링 해줍니다.
             */
            GoogleMapStyling googleMapStyling = new GoogleMapStyling();
            googleMapStyling.customizing(DrivingCourseInventory.this, googleMap);
        }

        /**
         * Initialises the MapView by calling its lifecycle methods.
         */
        public void initializeMapView() {
            if (mapView != null) {
                // Initialise the MapView
                mapView.onCreate(null);
                // Set the map ready callback to receive the GoogleMap object
                mapView.getMapAsync(this);
                mapView.setClickable(false);
            }
        }

    }

    /**
     * RecycleListener that completely clears the {@link com.google.android.gms.maps.GoogleMap}
     * attached to a row in the ListView.
     * Sets the map type to {@link com.google.android.gms.maps.GoogleMap#MAP_TYPE_NONE} and clears
     * the map.
     */
    private AbsListView.RecyclerListener mRecycleListener = new AbsListView.RecyclerListener() {

        @Override
        public void onMovedToScrapHeap(View view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            if (holder != null && holder.map != null) {
                // Clear the map and free up resources by changing the map type to none
                holder.map.clear();
                holder.map.setMapType(GoogleMap.MAP_TYPE_NONE);
            }

        }
    };

    /**
     * Location represented by a position ({@link com.google.android.gms.maps.model.LatLng} and a
     * name ({@link java.lang.String}).
     */
    public static class NamedLocation {

        public PolylineOptions polylineOptions;

        public LatLng northWest;
        public LatLng southEast;

        NamedLocation(PolylineOptions polylineOptions, LatLng northWest, LatLng southEast) {

            this.polylineOptions = polylineOptions;
            this.northWest = northWest;
            this.southEast = southEast;
        }
    }

    public void loadCourseInfo() {

        DbOpenHelper mDbOpenHelper = new DbOpenHelper(this);
        mDbOpenHelper.open();
        Cursor cursor = mDbOpenHelper.selectpreview();

        if (cursor.moveToFirst()) {
            do {
                Long time = cursor.getLong(cursor.getColumnIndex("totalTime"));
                Float distance = cursor.getFloat(cursor.getColumnIndex("totalDistance"));
                int courseID = cursor.getInt(cursor.getColumnIndex("courseID"));

                title.add("2018-03-11");
                this.distance.add(distance);
                this.time.add(time);
                this.courseID.add(courseID);
                this.checkBox.add(false);

                Log.d("인덱스:", courseID + "");
                Log.d("시간:", time + "");
                Log.d("거리:", distance + "");

                loadCourseRouteInfo(courseID);

            } while (cursor.moveToNext());
        }

        cursor.close();
        mDbOpenHelper.close();

    }

    public void loadCourseRouteInfo(int courseID) {

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

        /**
         * 레코드 수 만큼 반복으하며
         * 좌표(LatLng) 고도 속도를 불러옵니다.
         * 이 데이터는 차트에 그려지게 됩니다.
         */
        int cnt = polylineList.size();

        polylineList.add(new PolylineOptions().width(10).color(Color.RED));

        double eastMost = 0;
        double westMost = 0;
        double northernMost = 0;
        double southernMost = 0;

        /**
         * 리스트 안에 리스트라..
         */
        if (cursor.moveToFirst()) {
            do {
                latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
                longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));

                polylineList.get(cnt).add(new LatLng(latitude, longitude));

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

                /**
                 *
                 */

                if (eastMost > longitude)
                    eastMost = longitude;

                if (westMost < longitude)
                    westMost = longitude;

                if (southernMost > latitude)
                    southernMost = latitude;

                if (northernMost < latitude)
                    northernMost = latitude;


            } while (cursor.moveToNext());
        }

        LatLng northWest = new LatLng(northernMost, westMost);
        LatLng southEast = new LatLng(southernMost, eastMost);

        northWestList.add(northWest);
        southEastList.add(southEast);

        cursor.close();
        mDbOpenHelper.close();

    }

    public void onDestroy() {
        super.onDestroy();

        deleteButton_isClicked = 0;
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're resp`ding to
        if (requestCode == LOADING_COURSE_INFO) {
            if (resultCode == RESULT_OK) {
                int courseID = data.getIntExtra("courseID", -1);

                /**
                 * 차트화면에서 받은 인텐트값을 전달해 줍니다.
                 * 전달되는 값은 코스정보가 저장된 테이블의 고유 ID(인덱스) 입니다.
                 *
                 * 이 정보를 바탕으로, 코스정보를 불러오며, 주행화면에 코스정보를 그리게 됩니다.
                 */
                Intent intent = new Intent();
                intent.putExtra("courseID", courseID);

                setResult(RESULT_OK, intent);
                finish();

                // Do something with the contact here (bigger example below)
            }
        }
    }
}
