package com.project.sangyeop.road_rideronaroad;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 채팅방에 입장을 하면, (채팅 액티비티로 이동했을 때)
 * 스레드를 이용해서 TCP/IP 통신을 시작합니다.
 * <p>
 * 통신이 시작되면, 채팅방내 있는 유저들과 메시지를 주고 받을 수 있습니다.
 */
public class Chatting extends Activity {

    String mac_my;

    ListView m_ListView;
    Chatting_Adapter m_Adapter;

    ImageButton Button_send;
    EditText editText_massage;
    Handler msghandler;

    SocketClient client;
    ReceiveThread receive;
    SendThread send;
    Socket socket;

//    LinkedList<SocketClient> threadList;

    int roomId;
    SharedPreferences userInfo_sharedPreferences;
    int user_index;
    String user_nickname;

    String default_imgPath;

    int type_count;

    @SuppressLint("HandlerLeak")
    // Todo: 이게 무엇을 뜻하는 지 아직 모르겠습니다. 이걸 지우게 되면, Handler 부분에서 Warning 이 뜹니다.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatting_screen);

        editText_massage = (EditText) findViewById(R.id.editText1); // 메시지 입력란 입니다.
        Button_send = findViewById(R.id.button1); // 메시지를 전송하는 버튼입니다.

        /**
         * 채팅 내용을 출력할 리스트뷰와 어댑터를 설정합니다.
         * 해당 어댑터에서는 자신의 메시지는 오른쪽,
         * 타인의 메시지는 왼쪽에 출력하게 됩니다.
         */
        m_Adapter = new Chatting_Adapter();
        m_ListView = (ListView) findViewById(R.id.listView1);
        m_ListView.setAdapter(m_Adapter);

        /**
         * 채팅방에 입장한 유저의 정보를 불러옵니다.
         */
        userInfo_sharedPreferences = getSharedPreferences("user_info", 0);
        user_index = userInfo_sharedPreferences.getInt("user_index", -1);
        user_nickname = userInfo_sharedPreferences.getString("user_nickname", "null");
        /**
         * Todo : !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! 아래 코드 링크가 잘 못 됫씁니다. 반드시 수정해야함.
         */
        default_imgPath = "http://sangyeop0715.cafe24.com/img/profileImg_";

        /**
         * 이전 화면에서 (채팅방리스트) 어떤 채팅방을 선택했는 지 받아옵니다.
         *
         * roomId 는 외부 DB상에서, 채팅방리스트를 관리하는 Table 을 참고합니다.
         * 그 Table 중 roomId(index) 를 참고합니다. (리스트뷰의 position 값이 아닙니다.)
         *
         */
        Intent intent = getIntent();
        roomId = intent.getIntExtra("roomId", -1);
        Log.d("받아온 roomID", String.valueOf(roomId));

        /**
         * 입장한 채팅방의 채팅기록을 불러오고, 이를 화면에 출력합니다.
         *
         * Todo: 현재는 일일히 서버에서 로딩하고 있습니다. 내부 DB, 가령 SQLite 로 내부DB 를 구현함으로써, 리소스와 데이터 낭비를 줄여야 합니다.
         * Todo: 또한, 해당 채팅방에서는 페이징 처리가 되있지 않습니다. 역시 리소스와 데이터 낭비를 줄이기 위해, 페이징 처리가 구현되야 합니다.
         */
        LoadChattingHistory loadChattingHistory = new LoadChattingHistory();
        loadChattingHistory.execute();

        // 지워도 되나??????????????????????????????????????????????????????????????????????????
//        SharedPreferences sp = getSharedPreferences("token", 0);
//        threadList = new LinkedList<SocketClient>();

        /**
         * 유저의 Mac 주소를 받아옵니다.
         * 이 정보를 기준으로 자신의 메시지를 구분하며,
         * 결과에 따라 메시지를 왼쪽 또는 오른쪽에 표시합니다.
         */
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        mac_my = info.getMacAddress();

        /**
         * 서버와 통신을 시작합니다.
         * 통신이 시작되면, 메시지를 보내거나 받을 수 있습니다.
         */
        String serverIp = "119.205.233.149";
        client = new SocketClient(serverIp, "5001");
        client.start();

        /**
         * receiveThread 를 통해 받은 메세지를 처리합니다.
         * 핸들러를 이요해 메인스레드에서 처리하게 함으로써,
         * 수신한 메시지를 화면에 표시(갱신) 할 수 있습니다.
         */
        msghandler = new Handler() {
            public void handleMessage(Message hdmsg) {
                if (hdmsg.what == 1111) {

                    String s1 = hdmsg.obj.toString(); // 서버로부터 받은 JSON 데이터 입니다. 이 데이터는 메시지에 대한 정보를 담고 있습니다.
                    Log.d("서버로부터 받은 JSON X1: ", s1);

                    String mac_s = null;
                    String msg_s = null;
                    int userindex = 0;
                    String userCome = null;
                    String userImg = null;
                    String username = null;

                    /**
                     * Todo : 현재 불필요한 데이터를 수신하고 있습니다. Json Data form 을 수정해야 합니다.
                     */
                    try {
                        JSONObject jsonObject = new JSONObject(s1);
                        mac_s = jsonObject.getString("mac");
                        msg_s = jsonObject.getString("msg");
                        userindex = jsonObject.getInt("user_index");
                        userCome = jsonObject.getString("user_come");
                        username = jsonObject.getString("user_nickname");
                        userImg = default_imgPath + userindex + ".jpg";

                        Log.e("맥 주소 : ", mac_s);
                        Log.e("메시지 : ", msg_s);
                        Log.e("이름(인덱스)", String.valueOf(userindex));
                        Log.e("입장여부(삭제될 듯) : ", userCome);
                        Log.e("이미지패스", userImg);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (userCome.equals("yes")) { //유저컴이 no면 입장문이므로
                            m_Adapter.add(msg_s, 2, username);
                            m_Adapter.notifyDataSetChanged();
                        } else { // 그외는 대화문이므로
                            if (mac_my.equals(mac_s)) { //맥이 같으니 나
                                m_Adapter.add(msg_s, 1, userindex, userImg, username, type_count);
                                m_Adapter.notifyDataSetChanged();

                                type_count = 0;

                            } else { //맥이 다르니 딴놈임
                                m_Adapter.add(msg_s, 0, userindex, userImg, username, type_count);
                                m_Adapter.notifyDataSetChanged();

                                type_count = 1;
                            }
                        }

                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        /**
         * 메시지를 전송합니다.
         */
        Button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (editText_massage.getText().toString().length() < 1)
                    return;

                if (editText_massage.getText().toString() != null) {

                    /**
                     * Todo: 매번 새로운 객체를 생성해서, 실행하는 건 메모리를 낭비하는 행위인 걸로 알고 있습니다.
                     * Todo: SingleTon 디자인 패턴을 적용해야 할 것 같습니다.
                     */
                    send = new Chatting.SendThread(socket);
                    send.start();

                    /**
                     * 메시지를 전송하고, 재입력을 위해서 메시지 입력란을 비워줍니다.
                     */
                    editText_massage.setText("");
                } else {

                }
            }
        });
    }


    /**
     * TCP/IP 통신을 통해 채팅기능을 수행하는 쓰레드 입니다.
     */
    class SocketClient extends Thread {
        boolean threadAlive;
        String ip, port, mac;
//
//        OutputStream outputStream = null;
//        BufferedReader br = null;

        private DataOutputStream output = null;

        public SocketClient(String ip, String port) {
            threadAlive = true;
            this.ip = ip;
            this.port = port;
        }

        public void run() {

            try {
                // 연결후 바로 ReceiveThread 시작
                socket = new Socket(ip, Integer.parseInt(port));
                output = new DataOutputStream(socket.getOutputStream());
                receive = new Chatting.ReceiveThread(socket);
                receive.start();

                //MAC 주소를 받아오기 위한 설정
                WifiManager mng = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                WifiInfo info = mng.getConnectionInfo();
                mac = info.getMacAddress(); // Todo : 현재 코드를 제대로 이해하지 못한 상태입니다. ㅠ 왜 여기서 mac 주소를 따로 또 받는지 알수가 없습니다.

                //MAC 전송
                output.writeUTF(mac);

                String entry_sentence;

                entry_sentence = user_index + "님이(가) 입장하셨습니다.";

                String msg = "{\"mac\"" + ":" + "\"" + mac + "\"" + ","
                        + "\"room_ID\"" + ":" + "\"" + roomId + "\"" + ","
                        + "\"msg\"" + ":" + "\"" + entry_sentence + "\"" + ","
                        + "\"user_come\"" + ":" + "\"" + "yes" + "\"" + ","
                        + "\"user_img\"" + ":" + "\"" + 444 + "\"" + ","
                        + "\"user_nickname\"" + ":" + "\"" + user_nickname + "\"" + ","
                        + "\"user_index\"" + ":" + "\"" + user_index + "\"" + "}";

                output.writeInt(roomId);
                Log.d("roomID =========", String.valueOf(roomId));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 통신이 연결된 동안 메시지를 수신하는 쓰레드입니다.
     */
    class ReceiveThread extends Thread {
        private Socket socket = null;
        DataInputStream input;

        public ReceiveThread(Socket socket) {
            this.socket = socket;
            try {
                input = new DataInputStream(socket.getInputStream());
            } catch (IOException e) {
            }
        }

        public void run() {

            try {
                while (input != null) {

                    String msg = input.readUTF();
                    Log.d("서버로 부터 받은 String msg Z", msg);

                    if (msg != null) {

                        try {
                            Message hdmsg = msghandler.obtainMessage();
                            hdmsg.what = 1111;
                            hdmsg.obj = msg;
                            msghandler.sendMessage(hdmsg);

                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 메시지를 보내기 위한 쓰레드입니다. 메시지 전송이 끝나고 나면, 기능이 종료됩니다.
     */
    class SendThread extends Thread {
        private Socket socket;
        String sendmsg = editText_massage.getText().toString();

        DataOutputStream output;

        public SendThread(Socket socket) {
            this.socket = socket;
            try {
                output = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {

            try {

                //메세지 전송부 (누군지 식별하기 위한 바업으로 mac을 사용)
                String mac = null;
                WifiManager mng = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                WifiInfo info = mng.getConnectionInfo();
                mac = info.getMacAddress();
                mac_my = info.getMacAddress();

                if (output != null) {
                    if (sendmsg != null) {
//                        output.writeUTF(mac + " : " + sendmsg); //유저아이디 여기다가

                        SharedPreferences sp = getSharedPreferences("token", 0);
                        String s1 = sp.getString("key", "null");

                        String user_img = sp.getString("imgPath", "없시유");


                        String message = "{\"mac\"" + ":" + "\"" + mac + "\"" + ","
                                + "\"room_ID\"" + ":" + "\"" + roomId + "\"" + ","
                                + "\"msg\"" + ":" + "\"" + sendmsg + "\"" + ","
                                + "\"user_come\"" + ":" + "\"" + "no" + "\"" + ","
                                + "\"user_img\"" + ":" + "\"" + 444 + "\"" + ","
                                + "\"user_nickname\"" + ":" + "\"" + user_nickname + "\"" + ","
                                + "\"user_index\"" + ":" + "\"" + user_index + "\"" + "}";

                        output.writeUTF(message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }
        }


    }

    /**
     * 입장한 채팅방의 내역을 불러옵니다.
     * <p>
     * Todo : 일일히 서버에서 불러오는 미련한 짓을 하는 중이다. SQLite 과 페이징 처리를 구현해야한다.
     */
    class LoadChattingHistory extends AsyncTask<Void, Void, Void> {

        String data = "";

        @Override
        protected Void doInBackground(Void... voids) {

            String param = "room_ID=" + roomId + "";

            Log.e("POST", param); // 서버로 보내지는 데이터 확인

            try {// 서버연결

                URL url = new URL("http://yeop0715.cafe24.com/loadchatting_history.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.connect();

                // 안드로이드 -> 서버로 para 전달
                OutputStream outs = conn.getOutputStream();
                outs.write(param.getBytes("UTF-8"));
                outs.flush();
                outs.close();

                // 서버 -> 안드로이드로 para 전달
                InputStream is = null;
                BufferedReader in = null;
                data = "";

                is = conn.getInputStream();
                in = new BufferedReader(new InputStreamReader(is), 8 * 1024);
                String line = null;
                StringBuffer buff = new StringBuffer();
                while ((line = in.readLine()) != null) {
                    buff.append(line + "\n");
                }
                data = buff.toString().trim();

                Log.e("서버로부터 받은 데이터는 채팅 ==>", data); //서버에서 받은 data변수에 데이터값 확인

                String result = "";
                JSONArray jsonArray = new JSONArray(data);

                int user_index;
                String user_nickname;
                String message = null;
                String userImg = null;
                String mac = null;
                String time = null;

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    user_index = jsonObject.getInt("user_index");
                    user_nickname = jsonObject.getString("user_nickname");
                    message = jsonObject.getString("message");
                    mac = jsonObject.getString("mac_address");
                    time = jsonObject.getString("message_time");

                    Log.d("유저 인덱스", String.valueOf(user_index));
                    Log.d("유저 이름", user_nickname);
                    Log.d("메시지 : ", user_nickname);

                    userImg = default_imgPath + user_index + ".jpg";

                    String date = time;

                    SimpleDateFormat original_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    SimpleDateFormat new_format = new SimpleDateFormat("HH:mm");

                    String new_date = null;
                    try {
                        Date original_date = original_format.parse(date);
                        new_date = new_format.format(original_date);

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    try {
                        if (mac_my.equals(mac)) { //맥이 같으니 나
                            m_Adapter.add(message, 1, user_index, userImg, user_nickname, type_count, new_date);
                            type_count = 0;
                        } else { //맥이 다르니 딴놈임
                            m_Adapter.add(message, 0, user_index, userImg, user_nickname, type_count, new_date);
                            type_count = 1;
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
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
            m_Adapter.notifyDataSetChanged();
        }
    }

}


