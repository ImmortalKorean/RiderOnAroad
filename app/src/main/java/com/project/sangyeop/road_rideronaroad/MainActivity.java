package com.project.sangyeop.road_rideronaroad;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements TabHost.OnTabChangeListener {

    TabHost tabHost;

    Button test_btn_service;

    static final int USER_LOGOUT = 666;

    SharedPreferences userInfo_sharedPreferences; // 로그인한 유저의 정보가 저장된다
    SharedPreferences.Editor userInfoSaver; // 저장정보는 데이터베이스의 user_info 테이블의 (user_index, user_nickname)

    //    ReceiveThread receiveThread;
    Socket socket;
    String mMacAddress;

    private BackPressClose backPressClose;

    int loginStatusCheck;

    ArrayList<Friendlist_Array> friendsArrays;
    Friendlist_Adapter friendsAdapter;
    ListView listView;

    ArrayList<ChattingRoomlist_Array> chatting_room_list_arrays;
    ChattingRoomlist_Adapter chatting_room_list_adapter;
    ListView listV_Chatting_room_list;

    Button btn_logout;

    ImageButton imgBtn_searchOpen, map;
    ImageView logged_user_profileImg;

    TextView txtV_user, logged_user_statusMessage;

    int logged_user_index;
    String user_nickname;
    String user_statusMessage;
    String logged_user_profileImg_path;

    LinearLayout view_logged_userProfile;

    /**
     * 서비스 Class 인, ReceivingMessage 에 대한 변수
     */
    ReceivingMessage receivingMessage; // 서비스 클래스의 객체를 생성해서, 해당 클래스의 메서드를 호출할 수 있습니다.
    boolean isBinded = false; // 바인딩 서비스가 바인딩이 되었는지 여부를 저장합니다.

    /**
     * mybindService()에 전달 된 서비스 바인딩에 대한 콜백을 정의합니다.
     * <p>
     * 콜백이란? :
     * 흠.. 깊히 이해는 못 하겠지만, 역방향 클릭리스너 정도로 이해했다.
     * 우리가 시스템을 클릭리스너로 이벤트를 호출하듯이,
     * 반대로 콜백은 시스템이 우리를 호출하는 느낌같다.
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        /**
         * 서비스와 연결이 설정되면 호출되고, 이에 대한 콜백을 정의합니다.
         * @param name
         * @param service
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            /**
             * 명시적으로 바인딩 했으므로
             * 우리 자신의 프로세스에서 실행중인 서비스.
             * IBinder를 구체적인 클래스에 캐스트하고 직접 액세스합니다.
             *
             * ReceivingMessage 에 바인딩하고, IBinder를 캐스팅하며,
             * ReceivingMessage 인스턴스(객체)를 가져 왔습니다.
             */
            ReceivingMessage.LocalBinder binder = (ReceivingMessage.LocalBinder) service;
            receivingMessage = binder.getService();
            isBinded = true;
            Log.d("체크", isBinded + "(onConn)");
        }

        /**
         * 서비스와의 연결이 갑자기 끊어지면 호출됩니다.
         * @param name
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBinded = false;
            Log.d("체크", isBinded + "(onDisConn)");
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainmenu);

        /**
         * 순전히 test용
         */
        test_btn_service = findViewById(R.id.test_btn_service);
        test_btn_service.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent i = new Intent(MainActivity.this, faceDetect.class);
//                startActivity(i);
//                int a = receivingMessage.getTEST();
//                Toast.makeText(getApplicationContext(), a +"" , Toast.LENGTH_SHORT).show();
            }
        });

        userInfo_sharedPreferences = getSharedPreferences("user_info", 0);
        userInfoSaver = userInfo_sharedPreferences.edit();

        backPressClose = new BackPressClose(this);

        /******************** 뷰 선언 / ClickListenner 연결 [시작] ******************** */

        txtV_user = (TextView) findViewById(R.id.logged_user_nickName_on_mainMenu);
        logged_user_statusMessage = (TextView) findViewById(R.id.logged_user_statusMessage);
        imgBtn_searchOpen = (ImageButton) findViewById(R.id.imgBtn_searchOpen);
        logged_user_profileImg = (ImageView) findViewById(R.id.logged_user_profileImg_on_mainMenu);
        btn_logout = (Button) findViewById(R.id.btn_logout);


        imgBtn_searchOpen.setOnClickListener(View_ClickListener);
        logged_user_profileImg.setOnClickListener(View_ClickListener);
        btn_logout.setOnClickListener(View_ClickListener);

        view_logged_userProfile = (LinearLayout) findViewById(R.id.view_logged_userProfile);
        view_logged_userProfile.setOnClickListener(View_ClickListener);

        /**
         * 메인화면 우측상단의 바이크 아이콘을 누르면,
         * 주행 녹화모드를 할 수 있는 화면으로 넘어갑니다.
         */
        map = (ImageButton) findViewById(R.id.mapActivity);
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, Recording_Driving_Mode.class);
                startActivity(i);
            }
        });

        // 리스트뷰 출력 /////////////////////////////////////////////////////////////////////////////
        friendsArrays = new ArrayList<Friendlist_Array>();

        friendsAdapter = new Friendlist_Adapter(MainActivity.this, friendsArrays);
        listView = (ListView) findViewById(R.id.listview_V);
        listView.setAdapter(friendsAdapter);

        friendsAdapter.notifyDataSetChanged();
        /////////////////////////////////////////////////
        chatting_room_list_arrays = new ArrayList<ChattingRoomlist_Array>();

        chatting_room_list_adapter = new ChattingRoomlist_Adapter(MainActivity.this, chatting_room_list_arrays);
        listV_Chatting_room_list = (ListView) findViewById(R.id.listV_chatting_room_list);
        listV_Chatting_room_list.setAdapter(chatting_room_list_adapter);

        ////////////////////////////////////////////////////////////////////////////////////////////

        // 탭 호스트 시작/////////////////////////////////////////////////////////////////////////////
        tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();

        tabHost.setOnTabChangedListener(MainActivity.this);

//        ImageView img_tab1 = new ImageView(this);
//        ImageView img_tab2 = new ImageView(this);
//        ImageView img_tab3 = new ImageView(this);

//        img_tab1.setImageResource(R.drawable.friend_tap);
//        img_tab2.setImageResource(R.drawable.chat);
//        img_tab3.setImageResource(R.drawable.logout);

        TabHost.TabSpec ts1 = tabHost.newTabSpec("Tab1");
        ts1.setContent(R.id.content1);
//        ts1.setIndicator(img_tab1);
        ts1.setIndicator("탭1");
        tabHost.addTab(ts1);

        TabHost.TabSpec ts2 = tabHost.newTabSpec("Tab2");
        ts2.setContent(R.id.content2);
//        ts2.setIndicator(img_tab2);
        ts2.setIndicator("탭2");
        tabHost.addTab(ts2);

        TabHost.TabSpec ts3 = tabHost.newTabSpec("Tab3");
        ts3.setContent(R.id.content4);
        ts3.setIndicator("탭3");
//        ts3.setIndicator(img_tab3);
        tabHost.addTab(ts3);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        final int TabHeight = metrics.heightPixels;

        //탭 높이 조절
        tabHost.getTabWidget().getChildTabViewAt(0).getLayoutParams().height = (TabHeight * 15) / 200;
        tabHost.getTabWidget().getChildTabViewAt(1).getLayoutParams().height = (TabHeight * 15) / 200;
        tabHost.getTabWidget().getChildTabViewAt(2).getLayoutParams().height = (TabHeight * 15) / 200;

        // 로그인한 유저 정보를 불러온다.
        loginStatusCheck = userInfo_sharedPreferences.getInt("user_index", -1);

        /**
         * 유저 로그인 상태를 체크하고,
         * 로그인이 되어있지 않다면, 로그인을 요구합니다.
         * 로그인을 하지않으면, 앱을 실행할 수 없게 앱을 종료시킵니다.
         */
        if (loginStatusCheck < 0) { // 인덱스 값이 음수라는 건,

            unmybindService();

            Intent needToLogin = new Intent(MainActivity.this, User_Login.class);
            startActivityForResult(needToLogin, USER_LOGOUT);
        } else {
            mybindService();
            Log.d("체크", "유저가 로그인 상태므로 바인딩 시킴" + isBinded + "바인딩");
        }

    }

    /**
     * 각 뷰에 대한 클릭리스너 이벤트 입니다.
     */
    View.OnClickListener View_ClickListener = new View.OnClickListener() { // 클릭 리스너들
        @Override
        public void onClick(View v) {

            switch (v.getId()) {

                case R.id.logged_user_profileImg_on_mainMenu:
                case R.id.view_logged_userProfile:

                    Intent getImage = new Intent(MainActivity.this, View_userProfile.class);

                    startActivity(getImage);

                    break;


                case R.id.btn_logout:

                    /** 유저 로그아웃
                     *
                     *  로그아웃 버튼을 누르면, sharedPreference의 user_info가 초기화됨.
                     *  초기화는 user_info의 user_index가 -1로 설정
                     *
                     *  user_index는 데이터베이스에서, user_info TABLE의 첫번째 column과 같다.
                     *  따라서 -1이면, 정상적인 유저가 아니라는 뜻이 된다. (Sequence_INDEX는 음수를 가질 수 없으므로)
                     *
                     */
                    userInfoSaver.putInt("user_index", -1);
                    userInfoSaver.commit();

                    Intent user_logout = new Intent(MainActivity.this, User_Login.class);
                    startActivityForResult(user_logout, USER_LOGOUT);

                    unmybindService(); // 유저가 로그아웃 했으므로, 채팅메시지 수신대기를 중지합니다.

                    break;

                case R.id.imgBtn_searchOpen:

                    Intent userFinder = new Intent(MainActivity.this, SearchingFriend.class);
                    startActivity(userFinder);

                    break;
            }
        }
    };

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) { // 로그인이 필요한 상태에서 로그인 성공시, 앱을 계속 진행시킨다.

            switch (requestCode) {

                case USER_LOGOUT:

                    String user_name = userInfo_sharedPreferences.getString("user_nickname", "null");
                    txtV_user.setText(user_name);

                    tabHost.setCurrentTab(0); // 첫번째 탭의 내용을 표시합니다. 본인과 친구프로필
                    /**
                     * 유저가 로그인 했으므로,
                     * 유저가 속해있는 채팅방의 메시지를 수신대기 합니다.
                     *
                     * 메시지를 수신하게 되면, 알림메시지를 띄우게 됩니다.
                     */
                    mybindService(); // 아.. 작동안됨

                    break;

            }
        } else {
            switch (requestCode) { // 로그인이 필요한 상태에서 로그인을 하지 않고 나가버렸을 때, 앱을 종료시킵니다.

                case USER_LOGOUT:

                    /**
                     * 유저가 로그아웃 했으므로, 연결된 서비스를 종료합니다.
                     *
                     * 여기 Switch case 구문은,
                     * 유저가 로그아웃을 하고, 다시 로그인을 하지 않고 앱을 종료시키는 상홥입니다.
                     * 따라서 이전에 로그아웃을 했으므로, 서비스가 언바운딩 되었겠지만
                     * 서비스 언바운딩에 신중함을 기하기 위해서,
                     * 다시 한번 서비스 바인딩 여부를 확인을 합니다.
                     *
                     * 서비스가 바인딩되어 있지 않다면,
                     * 아래 해당 메소드는 어떤 기능도 수행하지 않습니다.
                     */
                    unmybindService(); // 아.. 안됨

                    finish();

                    break;
            }
        }

    }

    @Override
    public void onBackPressed() {
        backPressClose.onBackPressed();
    }

    public void onResume() {
        super.onResume();
        Log.d("Resume : ", "!!!!!!!!!!!!!");
        /** 유저정보와 생명주기
         *
         *  onCreate에서 초기화 시키면, 이전 사용자의 로그아웃시, 다음사용자는 이전사용자의 값과 충돌이 일어난다.
         *
         */

        logged_user_index = userInfo_sharedPreferences.getInt("user_index", -1);
        user_nickname = userInfo_sharedPreferences.getString("user_nickname", "null");
        user_statusMessage = userInfo_sharedPreferences.getString("status_message", "null");
        logged_user_profileImg_path = "http://sangyeop0715.cafe24.com/img/profileImg_" + logged_user_index + ".jpg";

        LoadFriendList loadFriendList = new LoadFriendList();
        loadFriendList.execute();

        logged_user_statusMessage.setText(user_statusMessage);
        logged_user_statusMessage.setBackgroundResource(R.drawable.inbox2);
        txtV_user.setText(user_nickname);

        try { //임시방편임 수정요망
            Thread.sleep(100);

//            Glide.with(MainActivity.this)
//                    .load(logged_user_profileImg_path)
//                    .diskCacheStrategy(DiskCacheStrategy.NONE)
//                    .skipMemoryCache(true)
//                    .error(R.drawable.profile)
//                    .into(logged_user_profileImg);

            SharedPreferences sharedPreferences = getSharedPreferences("user_info", 0);
            String encodedString = sharedPreferences.getString("user_img", null);

            Log.d("체크 이미지 스트링", encodedString);

            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            logged_user_profileImg.setImageBitmap(bitmap);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onTabChanged(String tabId) { // 탭 위젯별 클릭리스너

        switch (tabId) {

            case "Tab1":

                break;

            case "Tab2":

                LoadChattingRooms loadChattingRooms = new LoadChattingRooms();
                loadChattingRooms.execute();

                break;

            case "Tab3":

                break;
        }


    }

//    class SocketClient extends Thread {
//        boolean threadAlive;
//        String ip, port, mac;
//
//        OutputStream outputStream = null;
//        BufferedReader bufferedReader = null;
//
//        private DataOutputStream dataOutputStream = null;
//
//        public SocketClient(String ip, String port) {
//            threadAlive = true;
//            this.ip = ip;
//            this.port = port;
//        }
//
//        public void run() {
//
//            try {
//                socket = new Socket(ip, Integer.parseInt(port));
//                dataOutputStream = new DataOutputStream(socket.getOutputStream());
//                receiveThread = new ReceiveThread(socket);
//                receiveThread.start();
//
//                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
//                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//                mMacAddress = wifiInfo.getMacAddress();
//
//                dataOutputStream.writeUTF(mMacAddress);
//                dataOutputStream.writeInt(-1);
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

//    class ReceiveThread extends Thread {
//        private Socket socket = null;
//        DataInputStream dataInputStream;
//
//        public ReceiveThread(Socket socket) { // 생성자
//            this.socket = socket;
//            try {
//                dataInputStream = new DataInputStream(socket.getInputStream());
//            } catch (IOException e) {
//            }
//        }
//
//        public void run() {
//
//            try {
//
//                int roomTotalSize = 0;
//                roomTotalSize = dataInputStream.readInt();
//                Log.d("roomTotalSize = ", String.valueOf(roomTotalSize));
//
//                int roomId;
//
//                chatting_room_list_arrays.clear();
//
//                for (int i = 0; i < roomTotalSize; i++) {
//                    roomId = dataInputStream.readInt();
//                    chatting_room_list_arrays.add(new ChattingRoomlist_Array(roomId, "ㅋㅋㅋ"));
//                }
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//        }
//    }

    class LoadFriendList extends AsyncTask<Void, Integer, Void> {

        // 어느 유저의 친구목록을 불러올지 알기위해, 고유값인 user_index 값을 전달
        int user_index = userInfo_sharedPreferences.getInt("user_index", -1);

        String data = "";

        @Override
        protected Void doInBackground(Void... voids) {

            String param = "user_index=" + user_index + "";
            Log.e("POST", param); // 서버로 보내지는 데이터 확인

            try {// 서버연결

                /** load_friendlist.php 파일에서..
                 *
                 *  과연 join을 안 시키고, select를 두번이나 하는 게 맞는방법일까?
                 *
                 *  php파일에서 문제가많음.. 비효율적인 것 같음
                 */

                URL url = new URL("http://yeop0715.cafe24.com/load_friendlist.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); // 컨텐트 타입?
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

                Log.e("서버로부터 받은 데이터는 ==>", data); //서버에서 받은 data변수에 데이터값 확인

                String result = "";
                JSONArray jsonArray = new JSONArray(data);

                int friend_Index;
                String friend_Nickname;
                String status_message;

                friendsArrays.clear();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    friend_Index = jsonObject.getInt("friend_ID");
                    friend_Nickname = jsonObject.getString("friend_Nickname");
                    status_message = jsonObject.getString("status_message");

                    Log.d("friend_ID", String.valueOf(friend_Index));
                    Log.d("friend_Nickname", friend_Nickname);
                    Log.d("status_message", status_message);

                    friendsArrays.add(new Friendlist_Array(friend_Index, friend_Nickname, status_message)); // 인덱스를 조인해서 이름을 가져오자

                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(Void v) {

//            searchingResult_adapter.notifyDataSetChanged(); // 검색결과 갱신

            friendsAdapter.notifyDataSetChanged();
            Log.d("친구들불러오기", "성공");
        }
    }

    class LoadChattingRooms extends AsyncTask<Void, Void, Void> {

        String data = "";
        int user_index = userInfo_sharedPreferences.getInt("user_index", -1); // 현재유저의 정보

        @Override
        protected Void doInBackground(Void... voids) {

            String param = "user_index=" + user_index + "";
            Log.e("POST", param); // 서버로 보내지는 데이터 확인

            try {// 서버연결

                URL url = new URL("http://yeop0715.cafe24.com/Load_ChattingRooms.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); // 컨텐트 타입?
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

                Log.e("서버로부터 받은채팅룸 ==>", data); //서버에서 받은 data변수에 데이터값 확인

                String result = "";
                JSONArray jsonArray = new JSONArray(data);

                int roomlistID;
                int roomID;
                int opponent_index;
                String opponent_nickname;
                String last_message;
                String last_time;

                chatting_room_list_arrays.clear();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    roomlistID = jsonObject.getInt("roomlist_ID"); // 제이슨에서 제외했나보네 . 0불려진다
                    opponent_index = jsonObject.getInt("opponent_index");
                    opponent_nickname = jsonObject.getString("opponent_nickname");
                    last_message = jsonObject.getString("last_message");
                    last_time = jsonObject.getString("last_time");
//                    roomlistID = user_index; //임시방편으로 유저인덱스로. 어차피 둘다 같은 값이다


                    roomID = jsonObject.getInt("room_ID");

                    Log.d("roomlist_ID", String.valueOf(roomlistID));
                    Log.d("room_ID", String.valueOf(roomID));
                    Log.d("상대방 인덱스", String.valueOf(opponent_index));
                    Log.d("상대방 닉네임", opponent_nickname);
                    Log.d("최근 메시지", last_message);
                    Log.d("최근 시각", last_time);

                    chatting_room_list_arrays.add(new ChattingRoomlist_Array(roomlistID, roomID, opponent_index, opponent_nickname, last_message, last_time));


                    //친구 인덱스
                    //날짜
                    //최근 채팅내용(이건 db가 되야되는 데..)

                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(Void v) {

            chatting_room_list_adapter.notifyDataSetChanged();
            Log.d("채팅룸불러오기", "성공");
        }
    }

    /**
     * ReceivingMessage 서비스를 바인딩 합니다.
     */
    public void mybindService() {
        Intent intent = new Intent(this, ReceivingMessage.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        isBinded = true;
        Log.d("서비스가 바인딩 상태", isBinded + "");
    }

    /**
     * 바인딩된 ReceivingMessage 서비스를 언바인딩(연결해제/종료) 시킵니다.
     */
    public void unmybindService() {
        if (isBinded) {
            unbindService(serviceConnection);
            isBinded = false;
            Log.d("서비스가 바인딩 상태", isBinded + "");
        }
    }


}
