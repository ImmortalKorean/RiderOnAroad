package com.project.sangyeop.road_rideronaroad;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;
import com.nhn.android.naverlogin.ui.view.OAuthLoginButton;

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
import java.net.URL;

public class User_Login extends AppCompatActivity {

    String accessToken;

    //네이버 로그인을 위한 변수와 요소들
    private OAuthLoginButton btOAuthLoginButton;
    private OAuthLogin mOAuthLoginModule;
    Context mContext = User_Login.this;
    //네이버 로그인을 위한 핸들러(네이버 로그인 버튼의 동작을 정의한다.)
    TextView input_id, input_pw;
    Button btn_login, goto_join;
    String s_id, s_pw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_login);

        input_id = (TextView) findViewById(R.id.input_id);
        input_pw = (TextView) findViewById(R.id.input_pw);
        btn_login = (Button) findViewById(R.id.btn_login);
        goto_join = (Button) findViewById(R.id.goto_join);

        btn_login.setOnClickListener(btn_ClickListener);
        goto_join.setOnClickListener(btn_ClickListener);

        //네이버 로그인 인스턴스 초기화
        mOAuthLoginModule = OAuthLogin.getInstance();
        mOAuthLoginModule.init(
                User_Login.this
                , "_kbsBGJ_IrLFK0_lmqCC"
                , "I0KWlp5RJX"
                , "네이버 아이디로 로그인"

                //,OAUTH_CALLBACK_INTENT
                // SDK 4.1.4 버전부터는 OAUTH_CALLBACK_INTENT변수를 사용하지 않습니다.

        );

        //네이버 로그인 버튼 구현
        btOAuthLoginButton = (OAuthLoginButton) findViewById(R.id.buttonOAuthLoginImg);
        btOAuthLoginButton.setOAuthLoginHandler(mOAuthLoginHandler);
    }

    Button.OnClickListener btn_ClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {

                case R.id.btn_login:

                    try { // 아이디란에 null값 예외처리
                        s_id = input_id.getText().toString();
                        s_pw = input_pw.getText().toString();
                    } catch (NullPointerException e) {
                        Log.e("err", e.getMessage());  // 이케하면 ㅇㅇ 뭐라 뜨나
                    }

                    loginDB lDB = new loginDB();
                    lDB.execute();

                    break;

                case R.id.goto_join:

                    Intent i = new Intent(User_Login.this, User_Signup.class);
                    startActivity(i);
            }
        }
    };

    public class loginDB extends AsyncTask<Void, Integer, Void> {

        int login_success_code;
        int user_index;
        String user_nickname;
        String status_message;

        String data = "";

        @Override
        protected Void doInBackground(Void... voids) {


            String param = "u_id=" + s_id + "&u_pw=" + s_pw + "";
            // 마지막에 ""는 왜 넣었을까.. 문자열로 만들어 줄려고???

            Log.e("POST", param); // 로그로 아디값 확인하려고 ㅇㅇ

            try {// 서버연결

                URL url = new URL("http://yeop0715.cafe24.com/user_login.php");
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

                Log.e("Received DATA : ", data); //서버에서 받은 data변수에 데이터값 확인

                String result = "";
                JSONArray jsonArray = new JSONArray(data);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    login_success_code = jsonObject.getInt("login_success_code");
                    user_index = jsonObject.getInt("user_index");
                    user_nickname = jsonObject.getString("user_nickname");
                    status_message = jsonObject.getString("status_message");

                    Log.d("유저 인덱스", String.valueOf(user_index));
                    Log.d("유저 닉네임", user_nickname);

                }


            } catch (MalformedURLException e) { // 망할 예외처리들은 왜케 많아 .. 뭐가 뭔지도 잘 모르겠어
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            SharedPreferences sharedPreferences_userInfo = getSharedPreferences("user_info", 0);
            SharedPreferences.Editor userInfoSaver = sharedPreferences_userInfo.edit();
            userInfoSaver.putInt("user_index", user_index);
            userInfoSaver.putString("user_nickname", user_nickname);
            userInfoSaver.putString("status_message", status_message);
            userInfoSaver.commit();

            if (login_success_code == 1) {

                Intent intent = new Intent();
                setResult(RESULT_OK, intent);

                Log.d("result_ok", "okokokokok");

                finish();

            } else
                Toast.makeText(getApplicationContext(), "등록되지 않은 아이디이거나,\n\n아이디 또는 비밀번호를 잘못 입력하셨습니다", Toast.LENGTH_LONG).show();
        }

    }

    private OAuthLoginHandler mOAuthLoginHandler = new OAuthLoginHandler() {


        @Override
        public void run(boolean b) {
            if (b) {
                accessToken = mOAuthLoginModule.getAccessToken(mContext);
                new User_Login.NaverProfileGet().execute();
            } else {
                Toast.makeText(User_Login.this, "false", Toast.LENGTH_SHORT).show();
            }
        }
    };

    //네이버 프로필 조회 API
    public class NaverProfileGet extends AsyncTask<String, Void, String> {
        //네이버 프로필 조회 API에 보낼 헤더. 그대로 쓰면 된다.
        String header = "Bearer " + accessToken;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            StringBuffer response = new StringBuffer();
            String token = accessToken;// 네이버 로그인 접근 토큰;
            String header = "Bearer " + token; // Bearer 다음에 공백 추가
            try {
                String apiURL = "https://openapi.naver.com/v1/nid/me";
                URL url = new URL(apiURL);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("Authorization", header);
                int responseCode = con.getResponseCode();
                BufferedReader br;
                if (responseCode == 200) { // 정상 호출
                    br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    Log.e("4444", "4444");
                } else {  // 에러 발생
                    br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                }
                String inputLine;

                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
                br.close();
                System.out.println(response.toString());
                System.out.println("여기냐");
            } catch (Exception e) {
                System.out.println(e);
            }

            return response.toString();
        }

        //네이버 프로필 조회 API에서 받은 jSON에서 원하는 데이터를 뽑아내는 부분
        //여기서는 닉네임, 프로필사진 주소, 이메일을 얻어오지만, 다른 값도 얻어올 수 있다.
        //이 부분을 원하는 대로 수정하면 된다.
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Log.e("완료후 받은 문자열", result);
            try {
                Intent intent = new Intent(User_Login.this, MainActivity.class);
                JSONObject jsonObject1 = new JSONObject(result);
                JSONObject jsonObject2 = (JSONObject) jsonObject1.get("response");

                String login_success_code = jsonObject1.getString("message");
                String id_string = jsonObject2.getString("id");
                int id = Integer.parseInt(id_string);

                SharedPreferences sharedPreferences_userInfo = getSharedPreferences("user_info", 0);
                SharedPreferences.Editor userInfoSaver = sharedPreferences_userInfo.edit();
                userInfoSaver.putInt("user_index", id);
                userInfoSaver.putString("user_nickname", "의지의한국인");
                userInfoSaver.putString("status_message", "타오른다!");
                userInfoSaver.commit();

                Log.d("체크", login_success_code);
                Log.d("체크", login_success_code);

                if (login_success_code.equals("success")) {

                    setResult(RESULT_OK, intent);

                    finish();

                    SharedPreferences sp = getSharedPreferences("token", 0);
                    SharedPreferences.Editor sp_edit = sp.edit();

                    sp_edit.commit();

                    Intent intent2 = new Intent();
                    setResult(RESULT_OK, intent2);

                    Thread.sleep(1000);

                    finish();

                }
            } catch (
                    InterruptedException e)

            {
                e.printStackTrace();
            } catch (
                    JSONException e)

            {
                e.printStackTrace();
            }

        }

    }
}



