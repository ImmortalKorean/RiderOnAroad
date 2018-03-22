package com.project.sangyeop.road_rideronaroad;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

/**
 * Created by leesa on 2017-11-21.
 */

public class Friendlist_Adapter extends BaseAdapter {

    private ArrayList<Friendlist_Array> arr;
    private LayoutInflater my_inflater;
    private Activity my_activity;

    public Friendlist_Adapter(Activity act, ArrayList<Friendlist_Array> arr) {

        my_activity = act;
        this.arr = arr;
        my_inflater = (LayoutInflater) my_activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        return arr.size();
    }

    @Override
    public Object getItem(int position) {
        return arr.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertview, ViewGroup parent) {

        final Context context = parent.getContext();

        if (convertview == null) {

            int res = 0;
            res = R.layout.friendlist_format;
            convertview = my_inflater.inflate(res, parent, false);
        }

        ImageView user_profileImg = (ImageView) convertview.findViewById(R.id.friendList_profileImg);
        TextView name = (TextView) convertview.findViewById(R.id.friendList_name);
        TextView status_message = (TextView) convertview.findViewById(R.id.friendList_statusMessage);

//        String serverUri = "http://sangyeop0715.cafe24.com/img/profileImg_";
        String serverUri = "http://sangyeop0715.cafe24.com/img/profileImg_";

        int friend_index_int = arr.get(position).friend_index;
        String friend_index_String = String.valueOf(friend_index_int);

        String friendProfile_ImagePath = serverUri + friend_index_String + ".jpg";

        Log.e("받아오는 file_name : ", friendProfile_ImagePath);

        Glide.with(context)
                .load(friendProfile_ImagePath)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .error(R.drawable.profile)
                .into(user_profileImg);

        name.setText(arr.get(position).friend_nickname);
        status_message.setText(arr.get(position).status_message);

        /**대화명이 한글자도 없으면 표시하지 않는다.*/
        if (arr.get(position).status_message.length() < 1) {
            status_message.setVisibility(View.INVISIBLE);
        } else {
            status_message.setBackgroundResource(R.drawable.inbox2);
        }


        /** 친구 프로필 보기
         *
         *  친구목록 중 하나를 클릭하면,
         *  해당유저의 user_index 값을 전달되며, View_friendProfile 액티비티가 실행됨
         *
         *  friend_index는, DB내 user_info 테이블의 해당유저의 user_index 값과 같음
         *
         *  이를 기준으로 유저 프로필이미지를 불러오거나
         *  해당유저와 채팅할 때, 방에 입장시키기 위한 ID값 역할을 함
         */
        convertview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(context, View_friendProfile.class);
                i.putExtra("friend_index", arr.get(position).friend_index); ////////수저ㅓㅓㅓㅓㅓㅓㅓㅓㅓㅓㅓㅓㅓㅓㅇ
                i.putExtra("friend_nickname", arr.get(position).friend_nickname);
                i.putExtra("status_message", arr.get(position).status_message);
                context.startActivity(i);

            }
        });

        return convertview;


    }


}
