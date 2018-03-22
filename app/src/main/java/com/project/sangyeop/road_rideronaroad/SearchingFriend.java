package com.project.sangyeop.road_rideronaroad;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
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
import java.util.ArrayList;

public class SearchingFriend extends AppCompatActivity {

    EditText et_GetUserInfo;
    String userInfo;
    ImageButton imgBtn_CloseFinder, imgBtn_StartSearch;

    ArrayList<SearchingFriend_Array> searchingResult_arrays;
    SearchingFriend_Adapter searchingResult_adapter;
    ListView searchingREsult_listview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.searchingfriend_screen);

        imgBtn_CloseFinder = (ImageButton) findViewById(R.id.imgBtn_CloseFinder);
        imgBtn_CloseFinder.setOnClickListener(view_onClickListener);

        et_GetUserInfo = (EditText) findViewById(R.id.et_GetUserInfo);

        imgBtn_StartSearch = (ImageButton) findViewById(R.id.imgBtn_StartSearch); // 친구출력부랑 겹침 수정요망 ********
        imgBtn_StartSearch.setOnClickListener(view_onClickListener);

        searchingResult_arrays = new ArrayList<SearchingFriend_Array>();

        searchingResult_adapter = new SearchingFriend_Adapter(SearchingFriend.this, searchingResult_arrays);
        searchingREsult_listview = (ListView) findViewById(R.id.SearchingResult_listview);
        searchingREsult_listview.setAdapter(searchingResult_adapter);

    }

    View.OnClickListener view_onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {

                case R.id.imgBtn_CloseFinder: // 유저검색 액티비티 종료, 메인화면으로 돌아감

                    finish();

                    break;

                case R.id.imgBtn_StartSearch: // 유저가 입력한 내용을 기준으로, 유저검색 시작

                    /*먼저, 입력된 정보가 유효한지 검사.
                    * null 또는 공백문자는 무효
                    * */
                    boolean userInfo_is_Invalid;
                    userInfo_is_Invalid = et_GetUserInfo.getText().toString().replace(" ", "").equals("");

                    if (userInfo_is_Invalid == false) { // 정보가 유효하면, 검색시작
                        userInfo = et_GetUserInfo.getText().toString(); // 찾고싶은 유저정보(아이디 또는 닉네임)

                        SearchingFriends searchingFriends = new SearchingFriends();
                        searchingFriends.execute();

                    } else { // 무효하면 재입력 요구
                        Toast.makeText(getApplicationContext(), "닉네임 또는 아이디를 입력해주세요.", Toast.LENGTH_SHORT).show();

                        /*
                        * 카톡처럼 정보가 무효하면 검색버튼이 비활화되는 걸로 할 수도 있다.
                        * 그러기위해선 우선 입력되는 정보가 유효한지 확인하는 스레드가 필요하다.
                        * 구현하기 귀찮냐? ...좀.. 쉬고하자
                        * */
                    }

                    break;

            }
        }
    };

    class SearchingFriends extends AsyncTask<Void, Void, Void> {

        String data = "";

        @Override
        protected Void doInBackground(Void... voids) {

            String param = "user_info=" + userInfo + "";

            Log.e("POST", param); // 서버로 보내지는 데이터 확인

            try {// 서버연결

                URL url = new URL("http://yeop0715.cafe24.com/searchingFriend.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); // 컨텐트 타입?
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

                Log.e("서버로부터 받은 데이터는 ==>", data); //서버에서 받은 data변수에 데이터값 확인

                String result = "";
                JSONArray jsonArray = new JSONArray(data);

                int user_index;
                String user_id;
                String user_nickname;

                searchingResult_arrays.clear();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    user_index = jsonObject.getInt("user_index");
                    user_nickname = jsonObject.getString("user_nickname");

                    Log.d("유저 인덱스", String.valueOf(user_index));
                    Log.d("유저 이름", user_nickname);

                    searchingResult_arrays.add(new SearchingFriend_Array(user_index, user_nickname));

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

            searchingResult_adapter.notifyDataSetChanged(); // 검색결과 갱신

        }
    }


}
