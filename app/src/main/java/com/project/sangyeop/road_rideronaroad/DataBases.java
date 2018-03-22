package com.project.sangyeop.road_rideronaroad;

/**
 * Created by leesa on 2018-02-19.
 */

import android.provider.BaseColumns;

public final class DataBases {

    public static final class CreateDB implements BaseColumns {
        public static final String COURSE_ID = "courseID";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String SPEED = "speed";
        public static final String ALTITUDE = "altitude";
        public static final String BEARING = "bearing";
        public static final String TOTAL_TIME = "totalTime";
        public static final String TOTLA_DISTNACE = "totalDistance";
        public static final String _TABLENAME0 = "courseInfo";
        public static final String _CREATE0 = "create table if not exists " + _TABLENAME0 + "("

                + _ID + " integer primary key autoincrement, "
                + COURSE_ID + " integer not null , "
                + LATITUDE + " double not null , "
                + LONGITUDE + " double not null , "
                + SPEED + " float not null , "
                + ALTITUDE + " double not null , "
                + BEARING + " float not null , "
                + TOTAL_TIME + " long  , "
                + TOTLA_DISTNACE + " float  );";
    }
}