package com.project.sangyeop.road_rideronaroad;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by leesa on 2018-03-15.
 */

public class ReceivingMessage extends Service {

    String testString="zzz";
    /**
     * 메시지 수신대기를 하기 위한 변수입니다.
     */
    Socket socket;
    SocketReceiving client;
    ReceiveMsg receive;
    String serverIp = "119.205.233.149";

    Handler msghandler;


    // 바인된 된 클라이언트(다른 컴포넌트/View)에게 주는 Binder
    private final IBinder iBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        ReceivingMessage getService() {

            /**
             * 서비스 클래스의 객체를 반환함으로써,
             * 클라이언트(다른 컴포넌트/View)가
             * 해당 서비스 클래스의 메서드를 호출할 수 있습니다.
             */

            return ReceivingMessage.this;
        }
    }

    /**
     * 서비스 객체와 화면단의 액티비티 사이에서
     * 데이터를 주고받을 때 사용하는 메서드 입니다.
     *
     * @param intent
     * @return : 이 값은 기본적으로 nullable 이지만,
     * 바인딩 서비스를 위해서는 ibinder 을 반환해야 합니다.
     */
    public IBinder onBind(Intent intent) {

        return iBinder;
    }

    /**
     * 서비스가 최초로 호출될 때 '한번만' 실행됩니다.
     * 주로 여기서, 서비스 객체의 사전설정을 합니다.
     */
    public void onCreate() {
        super.onCreate();

        msghandler = new Handler() {
            public void handleMessage(Message hdmsg) {
                if (hdmsg.what == 1111) {

                    String s1 = hdmsg.obj.toString(); // 서버로부터 받은 JSON 데이터 입니다. 이 데이터는 메시지에 대한 정보를 담고 있습니다.
                    Log.d("서버로부터 받은 JSON  : ", s1);

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
                        username = jsonObject.getString("user_nickname");

                        Log.e("맥 주소 : ", mac_s);
                        Log.e("메시지 : ", msg_s);
                        Log.e("이름(인덱스)", String.valueOf(userindex));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    /**
     * 서비스가 호출될 때마다 실행됩니다.
     */
    public int onStartCommand(Intent intent, int flags, int startId) {

        return flags; // 무슨 값을 넣어야 하냐
    }

    /**
     * 서비스가 종료될 때 실행됩니다.
     */
    public void onDestry() {

    }

    public int getTEST() {
        client = new SocketReceiving(serverIp, "5001");
        client.start();
        Toast.makeText(getApplicationContext(),"서비스가 시작됨"+testString,Toast.LENGTH_SHORT).show();
        return -44444;
    }

    /**
     * TCP/IP 통신을 통해 채팅기능을 수행하는 쓰레드 입니다.
     */
    class SocketReceiving extends Thread {
        boolean threadAlive;
        String ip, port, mac;

        private DataOutputStream output = null;

        public SocketReceiving(String ip, String port) {
            threadAlive = true;
            this.ip = ip;
            this.port = port;
        }

        public void run() {

            try {
                // 연결후 바로 ReceiveThread 시작
                socket = new Socket(ip, Integer.parseInt(port));
                output = new DataOutputStream(socket.getOutputStream());
                receive = new ReceiveMsg(socket);
                receive.start();

//                //MAC 주소를 받아오기 위한 설정
//                WifiManager mng = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
//                WifiInfo info = mng.getConnectionInfo();
//                mac = info.getMacAddress(); // Todo : 현재 코드를 제대로 이해하지 못한 상태입니다. ㅠ 왜 여기서 mac 주소를 따로 또 받는지 알수가 없습니다.
//
//                //MAC 전송
//                output.writeUTF(mac);
//
//                String entry_sentence;
//
//                entry_sentence = user_index + "님이(가) 입장하셨습니다.";
//
//                String msg = "{\"mac\"" + ":" + "\"" + mac + "\"" + ","
//                        + "\"room_ID\"" + ":" + "\"" + roomId + "\"" + ","
//                        + "\"msg\"" + ":" + "\"" + entry_sentence + "\"" + ","
//                        + "\"user_come\"" + ":" + "\"" + "yes" + "\"" + ","
//                        + "\"user_img\"" + ":" + "\"" + 444 + "\"" + ","
//                        + "\"user_nickname\"" + ":" + "\"" + user_nickname + "\"" + ","
//                        + "\"user_index\"" + ":" + "\"" + user_index + "\"" + "}";
//
//                output.writeInt(roomId);
//                Log.d("roomID =========", String.valueOf(roomId));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 통신이 연결된 동안 메시지를 수신하는 쓰레드입니다.
     */
    class ReceiveMsg extends Thread {
        private Socket socket = null;
        DataInputStream input;

        public ReceiveMsg(Socket socket) {
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
                    testString = msg;
                    Log.d("자받아지냐", msg);

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


}
