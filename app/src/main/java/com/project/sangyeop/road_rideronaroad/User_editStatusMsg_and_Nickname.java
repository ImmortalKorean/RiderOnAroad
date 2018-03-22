package com.project.sangyeop.road_rideronaroad;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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

public class User_editStatusMsg_and_Nickname extends AppCompatActivity {

    SharedPreferences userInfo_sharedPreferences;
    SharedPreferences.Editor userInfo_editor;

    ImageButton finish_editStatusMsg_and_Ninkname, clear_all_character;
    TextView nickname_blank_alert;
    TextView current_selection_function_title;
    TextView modification_completed;
    TextView current_selection_function_name;
    TextView count_character_limit;
    TextView character_limit;
    EditText current_selection_function_content;

    int current_function_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_edit_status_msg_and__nickname);

        userInfo_sharedPreferences = getSharedPreferences("user_info", 0);
        userInfo_editor = userInfo_sharedPreferences.edit();

        finish_editStatusMsg_and_Ninkname = (ImageButton) findViewById(R.id.finish_editStatusMsg_and_Ninkname);
        clear_all_character = (ImageButton) findViewById(R.id.clear_all_character);
        modification_completed = (TextView) findViewById(R.id.modification_completed);
        current_selection_function_title = (TextView) findViewById(R.id.current_selection_function_title);
        current_selection_function_name = (TextView) findViewById(R.id.current_selection_function_name);
        count_character_limit = (TextView) findViewById(R.id.count_character_limit);
        character_limit = (TextView) findViewById(R.id.character_limit);
        nickname_blank_alert = (TextView) findViewById(R.id.nickname_blank_alert);
        current_selection_function_content = (EditText) findViewById(R.id.current_selection_function_content);

        finish_editStatusMsg_and_Ninkname.setOnClickListener(view_onClickListener);
        modification_completed.setOnClickListener(view_onClickListener);
        clear_all_character.setOnClickListener(view_onClickListener);

        Intent current_selection_function_info = getIntent();
        current_selection_function_title.setText(current_selection_function_info.getStringExtra("current_selection_function_title"));
        current_selection_function_name.setText(current_selection_function_info.getStringExtra("current_selection_function_name"));
        current_selection_function_content.setText(current_selection_function_info.getStringExtra("current_selection_function_content"));

        /**받아온 데이터의 글자제한수와 현재글자수를 세팅한다.*/
        if (!current_selection_function_content.equals(null)) {/**수정할 대화명이 null이 아니면, 현재 대화명의 글자수 표시한다.*/
            int current_character_count = current_selection_function_content.getText().toString().length();
            count_character_limit.setText(String.valueOf(current_character_count));
        } else {/**null이나 공란일 경우, 현재 글자수를  0으로 표시한다.*/
            count_character_limit.setText("0");
        }

        String get_character_limit = current_selection_function_info.getStringExtra("current_selection_function_character_limit");
        current_function_code = Integer.parseInt(get_character_limit); /**글자수 제한이 20이면 user_nickname, 60이면 user_statusMesaage을 의미한다.*/
        get_character_limit = "/" + get_character_limit; /** 분모표시를 위해서 "/"를 추가한다.  */
        character_limit.setText(get_character_limit);

        /**아래는 EditText 밑줄 색상변경 코드다.
         * 하지만 어떠한 이유로 작동되지 않는다..[일시보류] */
        int color = Color.parseColor("#c3870e");
        current_selection_function_content.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        /**글자제한수를 걸카아ㅣㅁㄴㅇ럼;닝러 집중력 한계다          */
        current_selection_function_content.addTextChangedListener(new TextWatcher() {
            String strCur; //???

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() > 60) {
                    current_selection_function_content.setText(strCur);
                    current_selection_function_content.setSelection(start);
                } else {
                    count_character_limit.setText(String.valueOf(s.length()));
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                strCur = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    View.OnClickListener view_onClickListener = new View.OnClickListener() { // 클릭 리스너들
        @Override
        public void onClick(View v) {

            switch (v.getId()) {

                case R.id.clear_all_character:

                    current_selection_function_content.setText("");

                    break;

                case R.id.finish_editStatusMsg_and_Ninkname:

                    /**대화명 및 닉네임 수정을 취소하고, 프로필 수정화면으로 돌아간다.*/

                    setResult(RESULT_CANCELED);
                    finish();

                    break;

                case R.id.modification_completed:

                    /**대화명 및 닉네임 수정을 완료하고, 프로필 수정화면으로 돌아간다.
                     *
                     * 수정된 내용은 ROAD_DB의, user_info 테이블의, user_nickname 또는 status_message에 저장된다. */


                    /** 대화명은 공란일 수 있으나, 닉네임은 공란일 수 없다. 재입력 요구*/
                    if (current_function_code == 20 && current_selection_function_content.getText().toString().length() < 1) {

                        nickname_blank_alert.setVisibility(View.VISIBLE);

                        break;

                    }

                    setResult(RESULT_OK);
                    UpdateUserTable updateUserTable = new UpdateUserTable();
                    updateUserTable.execute();

                    break;

            }
        }
    };

    class UpdateUserTable extends AsyncTask<Void, Integer, Void> {

        // 어느 유저의 친구목록을 불러올지 알기위해, 고유값인 user_index 값을 전달
        int user_index = userInfo_sharedPreferences.getInt("user_index", -1);

        String data = "";

        @Override
        protected Void doInBackground(Void... voids) {

            /**현재 유저의 인덱스값, 현재 기능 코드값, 그리고 수정된 데이터를 전송한다
             * 현재기능 코드값은 20이면 user_nickname, 60이면 user_statusMessage를 뜻한다.
             * 이는 user_info테이블의 각 column의 varchar의 크기와 같기 때문이다.
             * */
            String param = "user_index=" + user_index + "&function_code=" + current_function_code + "&modified_data=" + current_selection_function_content.getText().toString() + "";
            Log.e("POST", param); // 서버로 보내지는 데이터 확인

            try {// 서버연결

                URL url = new URL("http://yeop0715.cafe24.com/update_userTable.php");
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

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(Void v) {

            /**변경된 프로필내용을, 내부DB에 저장한다.
             * 자신의 프로필내용은, 최초로그인을 제외하고 내부DB에서 불러오게 된다. */

            if (current_function_code == 20) { /**글자수 제한이 20자인 column은 user_nickname이다*/
                userInfo_editor.putString("user_nickname", current_selection_function_content.getText().toString());
                userInfo_editor.commit();
                Log.d("유저닉네임 갱신 : ", current_selection_function_content.getText().toString());
            } else {/**글자수 제한이 60자인 colunmn은 user_statusMessage이다.*/
                userInfo_editor.putString("status_message", current_selection_function_content.getText().toString());
                userInfo_editor.commit();
                Log.d("유저대화명 갱신 : ", current_selection_function_content.getText().toString());
            }

            finish();
        }
    }
}
