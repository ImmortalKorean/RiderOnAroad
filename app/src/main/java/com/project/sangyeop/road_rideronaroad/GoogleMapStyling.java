package com.project.sangyeop.road_rideronaroad;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MapStyleOptions;

/**
 * Created by leesa on 2018-03-03.
 */

public class GoogleMapStyling {

    /**
     * 맵을 검은색으로 스타일링하고, 고속도로를 지도에 표시하지 않습니다.
     *
     * 국내 도로교통법상, 고속도로에서는 바이크 주행이 불법이기 때문입니다.
     * 따라서 고속도로를 지도에 표시하지 않습니다.
     *
     * @param context
     * @param googleMap
     */
    public void customizing(Context context, GoogleMap googleMap) {
        /**
         * 구글맵을 스타일링 합니다.
         */
        try {
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            context, R.raw.style_json));

            if (!success) {
                Log.e("스타일 파싱 실패", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("스타일 리소스를 찾을 수 없습니다", "Can't find style. Error: ", e);
        }
    }

}
