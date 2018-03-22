package com.project.sangyeop.road_rideronaroad;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by leesa on 2018-02-01.
 */

public class CourseRecordAdapter extends RecyclerView.Adapter<CourseRecordAdapter.ViewHolder> {

    final int LOADING_COURSE_INFO = 123;

    /**
     * 흠...;;
     */
    private final HashSet<MapView> mMaps = new HashSet<MapView>();


    /**
     * 코스기록 아이템을 설명하는 항목 입니다.
     * 제목, 총주행거리, 총주행시간, 그리고 코스기록_인덱스 입니다.
     */
    private ArrayList<String> title;
    private ArrayList<Float> distance;
    private ArrayList<Long> time;
    private ArrayList<Integer> courseID;
    private ArrayList<LatLng> latLngs;


    public CourseRecordAdapter(ArrayList<String> title, ArrayList<Float> distance, ArrayList<Long> time, ArrayList<Integer> courseID) {
        this.title = title;
        this.distance = distance;
        this.time = time;
        this.courseID = courseID;
    }

    public CourseRecordAdapter(ArrayList<String> title, ArrayList<Float> distance, ArrayList<Long> time, ArrayList<Integer> courseID, ArrayList<LatLng> latLngs) {
        this.title = title;
        this.distance = distance;
        this.time = time;
        this.courseID = courseID;
        this.latLngs = latLngs;
    }

    /**
     * 아이템별로 정보를 나타내는 역할을 합니다.
     * 표시되는 정보는 코스의 간략한 이미지,
     * 제목, 총 주행거리와 총주행시간 입니다.
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        /**
         * 시간을 분/초 단위로 나타내기 위한 변수입니다.
         */
        int minute = (int) (time.get(position) / 60);
        int second = (int) (time.get(position) % 60);

        holder.drivingCourse_Title.setText(title.get(position));

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
         * 각 아이템의 맵뷰를 초기화 시켜줍니다.
         */

        holder.initializeMapView();
//
        mMaps.add(holder.mapView);

        if (holder.map != null) {
//        holder.map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngs.get(position), 15));
//        Log.d("안되냐","!");
//            setMapLocation(holder.map.moveCamera(););
        }


    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    class ViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback {

        public MapView mapView;

        public TextView drivingCourse_Title;
        public TextView drivingCourse_TotalDistance;
        public TextView drivingCourse_TotalTime;
        public TextView drivingCourseID;

        private Context context = null;

        GoogleMap map;

        @Override
        public void onMapReady(GoogleMap googleMap) {
            MapsInitializer.initialize(context);
            map = googleMap;
            Log.d("몇번되냐", "1");
//            setMapLocation(map);
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
            }
        }

        public ViewHolder(View v) {
            super(v);

            context = v.getContext();

            mapView = (MapView) v.findViewById(R.id.drivingCourse_Image);
            drivingCourse_Title = (TextView) v.findViewById(R.id.drivingCourse_Title);
            drivingCourse_TotalDistance = (TextView) v.findViewById(R.id.drivingCourse_TotalDistance);
            drivingCourse_TotalTime = (TextView) v.findViewById(R.id.drivingCourse_TotalTime);
            drivingCourseID = (TextView) v.findViewById(R.id.courseID);

            /**
             * 코스기록 아이템 중 하나를 선택하면,
             * 해당 아이템에 대한 정보를 상세히 보여줍니다.
             */
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String index = drivingCourseID.getText().toString();
                    Intent intent = new Intent(context, Chart.class);
                    intent.putExtra("index", index);
                    ((Activity) context).startActivityForResult(intent, LOADING_COURSE_INFO);

                }
            });
        }
    }

    /**
     * RecyclerView에 표시항 각행의 포맷을 선택합니다.
     *
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.course_record_row, parent, false);

        ViewHolder vh = new ViewHolder(v);

        return vh;
    }

    /**
     * 전체 아이템의 갯수를 반환합니다.
     *
     * @return
     */
    @Override
    public int getItemCount() {
        return title.size();
    }

    public void setMapLocation(GoogleMap map) {

        LatLng seoul = new LatLng(37.483972, 126.977813);

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 18f));

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL); // 굳이 해줘야 할까?

        Log.d("로그", "z");
    }

}