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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by leesa on 2017-11-21.
 */

public class ChattingRoomlist_Adapter extends BaseAdapter {

    private ArrayList<ChattingRoomlist_Array> arr;
    private LayoutInflater my_inflater;
    private Activity my_activity;

    public ChattingRoomlist_Adapter(Activity act, ArrayList<ChattingRoomlist_Array> arr) {

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
            res = R.layout.chatting_roomlist_format;
            convertview = my_inflater.inflate(res, parent, false);
        }

        ImageView roomImg = (ImageView) convertview.findViewById(R.id.chatting_room_list_ProfileImgV);
        TextView roomTitle = (TextView) convertview.findViewById(R.id.chatting_room_list_UserName);
        TextView chatTime = (TextView) convertview.findViewById(R.id.chatting_room_list_chatTime);
        TextView chatContent = (TextView) convertview.findViewById(R.id.chatting_roomlist_chatContent);

        roomTitle.setText(arr.get(position).roomTitle);
        chatContent.setText(arr.get(position).last_message);


        String date = arr.get(position).last_time;

        SimpleDateFormat original_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat new_format = new SimpleDateFormat("aa HH:mm", Locale.KOREAN);

        String new_date = null;
        try {
            Date original_date = original_format.parse(date);
            new_date = new_format.format(original_date);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        chatTime.setText(new_date);


        String serverUri = "http://sangyeop0715.cafe24.com/img/profileImg_";
        String friend_index_String = String.valueOf(arr.get(position).friend_index);

        String friendProfile_ImagePath = serverUri + friend_index_String + ".jpg";

        Log.d("friendImgPath 123: ", friendProfile_ImagePath);

        Glide.with(context)
                .load(friendProfile_ImagePath)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .error(R.drawable.profile)
                .into(roomImg);

        convertview.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent i = new Intent(context, Chatting.class);

                i.putExtra("roomId", arr.get(position).roomId);

                context.startActivity(i);

            }
        });

        return convertview;

    }
}
