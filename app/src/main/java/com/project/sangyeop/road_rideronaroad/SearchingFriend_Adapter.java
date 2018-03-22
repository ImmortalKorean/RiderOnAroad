package com.project.sangyeop.road_rideronaroad;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


/**
 * Created by leesa on 2017-12-14.
 */

public class SearchingFriend_Adapter extends BaseAdapter {

    ArrayList<SearchingFriend_Array> searchingResult_arrays;
    LayoutInflater layoutInflater;
    Activity activity;

    public SearchingFriend_Adapter(Activity act, ArrayList<SearchingFriend_Array> arr) {

        this.activity = act;
        this.searchingResult_arrays = arr;
        this.layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        return searchingResult_arrays.size();
    }

    @Override
    public Object getItem(int position) {
        return searchingResult_arrays.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final Context context = parent.getContext();

        if (convertView == null) {

            int res = 0;
            res = R.layout.serarchingresult_format;
            convertView = layoutInflater.inflate(res, parent, false);

        }

        ImageView user_profileImg = (ImageView) convertView.findViewById(R.id.SearchingResult_userProfileImg);
        TextView user_name = (TextView) convertView.findViewById(R.id.SearchingResult_userNickname);
        final ImageButton addFriend = (ImageButton) convertView.findViewById(R.id.searchingResult_addFriend);

        String serverUri = "http://sangyeop0715.cafe24.com/img/profileImg_";
        int profileId = searchingResult_arrays.get(position).friend_index;
        String profileid_s = String.valueOf(profileId);

        String imgPath = serverUri + profileid_s + ".jpg";

        Log.e("받아오는 file_name : ", imgPath);

        Glide.with(context)
                .load(imgPath)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .error(R.drawable.profile)
                .into(user_profileImg);

        user_name.setText(searchingResult_arrays.get(position).friend_name);


        addFriend.setOnClickListener(new View.OnClickListener() { //
            @Override
            public void onClick(View v) {

// 다이얼로그 에러남 집중력 다 떨어짐 수고요
                //                ConfirmAddBeforeADD(context, searchingResult_arrays.get(position).friend_index);

                AddFriend addFriend = new AddFriend(context);
                addFriend.execute(searchingResult_arrays.get(position).friend_index);

            }
        });

        return convertView;

    }

//    public void ConfirmAddBeforeADD(Context context, int friend_index ) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setTitle("친구추가");
//        builder.setMessage("추가할래 정말이놈아?");
//        builder.setPositiveButton("예",
//                new DialogInterface.OnClickListener() {

//                    public void onClick(DialogInterface dialog, int which) {
//                        Toast.makeText(context,"예를 선택했습니다.",Toast.LENGTH_LONG).show();
//                    }
//                });
//        builder.setNegativeButton("아니오",
//                new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        Toast.makeText(getApplicationContext(),"아니오를 선택했습니다.",Toast.LENGTH_LONG).show();
//                    }
//                });
//        builder.show();
//    }

    class AddFriend extends AsyncTask<Integer, Void, Void> {

        Context context;

        AddFriend(Context ctx) {
            this.context = ctx;
        }

        @Override
        protected Void doInBackground(Integer... friend_index) {

            SharedPreferences userInfo_sharedPreferences = context.getSharedPreferences("user_info", 0);
            int user_index = userInfo_sharedPreferences.getInt("user_index", -1);
            int friendIndex = friend_index[0];

            String data = ""; // 서버에서 받는 데이터

            Log.d("유저정보", String.valueOf(user_index));
            Log.d("친구정보", String.valueOf(friendIndex));

            String param = "user_index=" + user_index + "&friend_Index=" + friendIndex + "";
            Log.e("POST", param); // 서버로 보내는 데이터 확인

            try {// 서버연결

                URL url = new URL("http://yeop0715.cafe24.com/add_friend.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); // 와 이거 뭐지
                conn.setRequestMethod("POST");
                conn.setDoInput(true);

                conn.connect();

                // 안드로이드 -> 서버로 para 전달
                OutputStream outs = conn.getOutputStream();
                outs.write(param.getBytes("UTF-8")); // 음 ㅈㄴ 모르겠군..
                outs.flush(); // 이거 알고 있었는데...
                outs.close();

                // 서버 -> 안드로이드로 para 전달
                InputStream is = null;
                BufferedReader in = null;
                data = ""; // 음 ㅈㄴ 모르겠군2

                is = conn.getInputStream();
                in = new BufferedReader(new InputStreamReader(is), 8 * 1024);
                String line = null;
                StringBuffer buff = new StringBuffer();
                while ((line = in.readLine()) != null) {
                    buff.append(line + "\n");
                }
                data = buff.toString().trim();

                Log.e("RECV DATA", data); //서버에서 받은 data변수에 데이터값 확인

            } catch (MalformedURLException e) { // 망할 예외처리들은 왜케 많아 .. 뭐가 뭔지도 잘 모르겠어
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

    }

    protected void onPostExecute(Void v) {

        Log.d("done", "yes");

    }


}
