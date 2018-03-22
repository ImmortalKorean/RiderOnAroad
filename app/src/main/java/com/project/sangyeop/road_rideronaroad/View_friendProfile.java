package com.project.sangyeop.road_rideronaroad;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

public class View_friendProfile extends AppCompatActivity {

    ImageView view_friendprofileImg, view_friendBackground;
    ImageButton backtoMainmenu;
    TextView freechat_with_friend, view_friendnickName, view_friendnick_statusMessage;

    int user_index;
    int friend_index;

    String frined_profileImg_path;
    String friend_backgroundImg_path;
    String friend_nickName, status_message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_friendprofile);

        //유저인덱스는 항상 sharedPreference에 저장되기에, 불러오기만 하면된다.
        SharedPreferences userInfo_sharedPreferences = getSharedPreferences("user_info", 0);
        user_index = userInfo_sharedPreferences.getInt("user_index", -1);

        Log.d("유저인덱스 : ", String.valueOf(user_index));

        //친구인덱스는 액티비티가 전환되기전 전달받는다.
        Intent i = getIntent();
        friend_index = i.getIntExtra("friend_index", -1);
        friend_nickName = i.getStringExtra("friend_nickname");
        status_message = i.getStringExtra("status_message");
        //인덱스는 - (음수)가 될수 없으므로, -1은 절대로 입력될 수 없다.

        Log.d("유저의 인덱스", String.valueOf(user_index));
        Log.d("친구의 인덱스", String.valueOf(friend_index));

        view_friendnickName = (TextView) findViewById(R.id.view_friendnickName);
        view_friendnick_statusMessage = (TextView) findViewById(R.id.view_friendnick_statusMessage);

        freechat_with_friend = (TextView) findViewById(R.id.freechat_with_friend);

        view_friendnickName.setText(friend_nickName);
        view_friendnick_statusMessage.setText(status_message);
        freechat_with_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /** 친구와 1:1 프리채팅 시작
                 *
                 *  서버에 기존방이 있으면 방을 만들지 않고, 방 ID값만 받아온다.
                 *
                 *  서버에 기존방이 없으면 방을 만들고, 방 ID값을 받아온다.
                 *  방에 입장시키기 위해, 서버에 두 유저의 index값을 전달한다.
                 *
                 *  서버는 방을 만들고 두 유저의 인덱스값을 참고해 입장시키고, 방ID를 보내준다.
                 *  클라이언트는 그 값을 받는다.
                 *  방 ID는 채팅방별로 일어나는 일들을 저장하는 기준이 된다.
                 *
                 *  과정이 완료되면 Chatting 액티비티가 실행됨
                 *
                 */

                Start_freeChatting start_freeChatting = new Start_freeChatting();
                start_freeChatting.execute();

            }
        });

        backtoMainmenu = (ImageButton) findViewById(R.id.finish_viewFriendProfile);
        backtoMainmenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        view_friendprofileImg = (ImageView) findViewById(R.id.view_friendProfileImg);
        view_friendBackground = (ImageView) findViewById(R.id.view_friendBackground);

        frined_profileImg_path = "http://sangyeop0715.cafe24.com/img/profileImg_" + String.valueOf(friend_index) + ".jpg";
        friend_backgroundImg_path = "http://sangyeop0715.cafe24.com/img/backgroundImg_" + String.valueOf(friend_index) + ".jpg";

        Log.e("받아오는 profileImg : ", frined_profileImg_path);
        Log.e("받아오는 background : ", friend_backgroundImg_path);

        Glide.with(View_friendProfile.this)
                .load(frined_profileImg_path)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .error(R.drawable.profile)
                .into(view_friendprofileImg);

        Glide.with(View_friendProfile.this)
                .load(friend_backgroundImg_path)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .error(R.drawable.profile)
                .into(view_friendBackground);
    }

    public class Start_freeChatting extends AsyncTask<Void, Void, Void> {

        String data = ""; //서버로부터 오는 데이터를 담을 변수

        @Override
        protected Void doInBackground(Void... voids) {

            /** 유저인덱스와 친구인덱스 전송
             *
             *  intergers[]에는 첫째로 유저인덱스, 둘째로 친구인덱스가 들어가있다.
             *
             */

            String indexes = "user_index=" + user_index + "&friend_index=" + friend_index + "";
            Log.d("서버로 가는 user_index", String.valueOf(user_index));
            Log.d("서버로 가는 friend_index", String.valueOf(friend_index));

            try {// 서버연결
                URL url = new URL("http://yeop0715.cafe24.com/start_freechatting.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.connect();

                // 안드로이드 -> 서버로 para 전달
                OutputStream outs = conn.getOutputStream();
                outs.write(indexes.getBytes("UTF-8"));
                outs.flush();
                outs.close();

                // 서버 -> 안드로이드로 para 전달
                InputStream is = null;
                BufferedReader in = null;
                data = ""; //

                is = conn.getInputStream();
                in = new BufferedReader(new InputStreamReader(is), 8 * 1024);
                String line = null;
                StringBuffer buff = new StringBuffer();
                while ((line = in.readLine()) != null) {
                    buff.append(line + "\n");
                }
                data = buff.toString().trim();
                Log.e("인덱스주고 서버로부터 받은 roomId:", data);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Intent chatting = new Intent(View_friendProfile.this, Chatting.class);

            int roomId = Integer.parseInt(data);
//            String roomId = data;

            chatting.putExtra("roomId", roomId);
            Log.d("ChattingN에 전달되는 roomId:", String.valueOf(roomId));

            startActivity(chatting);

            /** 메인화면으로 복귀
             *
             * 친구와 채팅방을 생성된 후 채팅이 종료되면
             * 친구프로필 보기상태로 돌아오게 된다.
             *
             * 그래서 채팅방이 생성됨과 동시에 유저프로필 액티비티(다음 액티비티 실행전 액티비티)는 종료된다.
             * 아니면 유지시키는 게 맞나?
             *
             * 카톡은 아예 채팅방리스트로 넘어감 (탭2)
             * 심지어 방이 만들고 바로사라져도 (두 유저간 채팅이 없으면, 방을 무효화 시키는 모양이다)
             *
             * 이건 나중에 startactivityResult 로 request code를 받아서,
             * 그리고 이 view_profiel activity도 startactivityREsult로 실행되서, request 랑 result code를 반환하고
             * mainactivity에서 activityonResult 에서 처리하면 되겠네.
             *
             * 이렇게 주석을 쓴 이유는.. 지금 만들기 귀찮아서
             */
            //
            finish();
        }
    }
}
