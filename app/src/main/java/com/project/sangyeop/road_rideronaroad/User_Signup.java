package com.project.sangyeop.road_rideronaroad;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class User_Signup extends AppCompatActivity {

    EditText et_id, et_pw, et_pwchk, et_name, et_phone, et_email;
    Button btn_join, btn_InfoPermit, btn_idChk;
    String s_id, s_pw, s_pwchk, s_name, s_phone, s_email;
    CheckBox chk_InfoPermit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_signup);

        et_id = (EditText) findViewById(R.id.et_id);
        et_pw = (EditText) findViewById(R.id.et_pw);
        et_pwchk = (EditText) findViewById(R.id.et_pwchk);
        et_name = (EditText) findViewById(R.id.et_name);
//        et_phone = (EditText) findViewById(R.id.et_phone);
//        et_email = (EditText) findViewById(R.id.et_email);

        btn_join = (Button) findViewById(R.id.btn_join);
        btn_InfoPermit = (Button) findViewById(R.id.btn_infoPermit);
        btn_idChk = (Button) findViewById(R.id.btn_idChk);


        btn_join.setOnClickListener(btn_ClickListener);
        btn_InfoPermit.setOnClickListener(btn_ClickListener);
        btn_idChk.setOnClickListener(btn_ClickListener);


        chk_InfoPermit = (CheckBox) findViewById(R.id.chk_InfoPermit);

    }


    Button.OnClickListener btn_ClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            idChkDB iddb;

            switch (v.getId()) {

                case R.id.btn_idChk:

                    iddb = new idChkDB();
                    iddb.execute();

                    break;

                case R.id.btn_infoPermit:

                    AlertDialog.Builder info = new AlertDialog.Builder(User_Signup.this);

                    info.setTitle("개인정보 취급방침");
                    info.setMessage("안드로이드 앱 개인정보 취급방침\n" +
                            "\n" +
                            "1. 개인정보의 처리 목적 <ROAD 로드>은(는) 다음의 목적을 위하여 개인정보를 처리하고 있으며, 다음의 목적 이외의 용도로는 이용하지 않습니다.\n" +
                            "① 사용자 본인확인\n" +
                            "\n" +
                            "2. 정보주체의 권리,의무 및 그 행사방법 이용자는 개인정보주체로서 다음과 같은 권리를 행사할 수 있습니다.\n" +
                            "① 정보주체는 <기관/회사명>(‘사이트URL’이하 ‘사이트명) 에 대해 언제든지 다음 각 호의 개인정보 보호 관련 권리를 행사할 수 있습니다.\n" +
                            "1) 개인정보 열람요구\n" +
                            "2) 오류 등이 있을 경우 정정 요구\n" +
                            "3) 삭제요구\n" +
                            "4) 처리정지 요구\n" +
                            "* <ROAD>는 앱 사용자의 사용정보를 수집 및 보유하지 않습니다.\n" +
                            "\n" +
                            "3. 처리하는 개인정보의 항목 작성\n" +
                            "① <ROAD 로드>은(는) 다음의 개인정보 항목을 처리하고 있습니다.\n" +
                            "\n" +
                            "4. 개인정보의 파기<ROAD 로드>은(는) 원칙적으로 개인정보 처리목적이 달성된 경우에는 지체없이 해당 개인정보를 파기합니다. 파기의 절차, 기한 및 방법은 다음과 같습니다.\n" +
                            "– 사용되는 개인정보는 구글플레이스토어 및 인앱결제에 관련된 부분이며 <ROAD 로드>은 다른 목적을 위해 어떠한 정보도 취득하거나 변조 및 사용하지 않습니다.\n" +
                            "– 앱 삭제시 더 이상 위 권한을 사용하지 않습니다.\n" +
                            "\n" +
                            "5. 개인정보의 안전성 확보 조치 <ROAD 로드>은(는) 개인정보보호법 제29조에 따라 다음과 같이 안전성 확보에 필요한 기술적/관리적 및 물리적 조치를 하고 있습니다.\n" +
                            "①내부관리계획의 수립 및 시행\n" +
                            "– 개인정보의 안전한 처리를 위하여 내부관리계획을 수립하고 시행하고 있습니다.\n" +
                            "② 개인정보에 대한 접근 제한\n" +
                            "– 개인정보를 처리하는 데이터베이스시스템에 대한 접근권한의 부여,변경,말소를 통하여 개인정보에 대한 접근통제를 위하여 필요한 조치를 하고 있으며 침입차단시스템을 이용하여 외부로부터의 무단 접근을 통제하고 있습니다.\n" +
                            "③비인가자에 대한 출입 통제\n" +
                            "– 앱 개발실의 보안조치로서 출입통제 절차를 수립, 운영하고 있습니다.\n" +
                            "\n" +
                            "6. 개인정보 보호책임자 작성\n" +
                            "①<ROAD 로드> 은(는) 개인정보 처리에 관한 업무를 총괄해서 책임지고, 개인정보 처리와 관련한 정보주체의 불만처리 및 피해구제 등을 위하여 아래와 같이 개인정보 보호책임자를 지정하고 있습니다.\n" +
                            "\n" +
                            "▶ 개인정보 보호책임자 및 담당부서\n" +
                            "성명: 이상엽\n" +
                            "직책: 제작자\n" +
                            "연락처 : lee_sangyeop0715@naver.com\n" +
                            "② 정보주체께서는 <ROAD 로드>의 서비스(또는 사업)을 이용하시면서 발생한 모든 개인정보 보호 관련 문의, 불만처리, 피해구제 등에 관한 사항을 개인정보 보호책임자 및 담당부서로 문의하실 수 있습니다. <ROAD 로드>은(는) 정보주체의 문의에 대해 지체 없이 답변 및 처리해드릴 것입니다.\n" +
                            "\n" +
                            "7. 개인정보 처리방침 변경\n" +
                            "①이 개인정보처리방침은 시행일로부터 적용되며, 법령 및 방침에 따른 변경내용의 추가, 삭제 및 정정이 있는 경우에는 변경사항의 시행 7일 전부터 공지사항을 통하여 고지할 것입니다.\n" +
                            "이 개인정보처리방침은 2017년 11월 01일부터 적용 됩니다.");
                    info.setPositiveButton("확인", null);

                    AlertDialog infogo = info.create();

                    infogo.show();

                    break;

                case R.id.btn_join:

                    try { // NULL 예외처리

                        s_id = et_id.getText().toString();
                        s_pw = et_pw.getText().toString();
                        s_pwchk = et_pwchk.getText().toString();
                        s_name = et_name.getText().toString();
//                        s_phone = et_phone.getText().toString();
//                        s_email = et_email.getText().toString();

                    } catch (NullPointerException e) {
                        Toast.makeText(getApplicationContext(), "데이터가 없습니다", Toast.LENGTH_SHORT).show();
                        Log.e("err", e.getMessage());  //
                    }

                    if (s_id.length() == 0) {
                        Toast.makeText(getApplicationContext(), "아이디를 입력해주세요", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (s_pw.length() == 0) {
                        Toast.makeText(getApplicationContext(), "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (s_name.length() == 0) {
                        Toast.makeText(getApplicationContext(), "이름을 입력해주세요", Toast.LENGTH_SHORT).show();
                        return;
                    }

//                    if (s_phone.length() == 0) {
//                        Toast.makeText(getApplicationContext(), "연락처를 입력해주세요", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//
//                    if (s_email.length() == 0) {
//                        Toast.makeText(getApplicationContext(), "이메일을 입력해주세요", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//
                    if (!chk_InfoPermit.isChecked()) {
                        Toast.makeText(getApplicationContext(), "개인정보 처리방침에 대한 동의가 필요합니다", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    iddb = new idChkDB();
                    iddb.execute();


                    if (iddb.idChkResult >= 1) {
                        Toast.makeText(getApplicationContext(), "이미 사용중인 아이디 입니다", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        if (s_pw.equals(s_pwchk)) {

                            UserSignupInDB userSignupInDB = new UserSignupInDB();
                            userSignupInDB.execute();

                        } else {

                            Toast.makeText(getApplicationContext(), "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show();

                        }
                    }


                    break;
            }

        }
    };

    public class UserSignupInDB extends AsyncTask<Void, Integer, Void> {

        String data = "";

        @Override
        protected Void doInBackground(Void... voids) {

            String param = "user_id=" + s_id + "&user_pw=" + s_pw + "&user_name=" + s_name + "";

            try {// 서버연결
                URL url = new URL("http://yeop0715.cafe24.com/user_signup.php");
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
                Log.e("RECV DATA", data);

                if (data.equals("1")) {

                    Log.e("RESULT", "성공적으로 처리되었습니다!");
                } else {

                    Log.e("RESULT", "에러 발생! ERRCODE = " + data);
                }

            } catch (MalformedURLException e) { // 망할 예외처리들은 왜케 많아 .. 뭐가 뭔지도 잘 모르겠어
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (data.equals("1")) {
                Toast.makeText(getApplicationContext(), "회원가입이 완료됐습니다", Toast.LENGTH_SHORT).show();
                finish();
            } else
                Toast.makeText(getApplicationContext(), "이미 사용중인 아이디 입니다", Toast.LENGTH_SHORT).show();
        }


    }

    public class idChkDB extends AsyncTask<Void, Integer, Void> {

        int idChkResult = -1;
        String data = "";

        @Override
        protected Void doInBackground(Void... voids) {

            // input para 생성
            s_id = et_id.getText().toString();
            String param = "u_id=" + s_id + "";
            // 마지막에 ""는 왜 넣었을까.. 문자열로 만들어 줄려고???

            try {// 서버연결
                URL url = new URL("http://yeop0715.cafe24.com/ID_duplication_check.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); // 와 이거 뭐지
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.connect();


                Log.e("RECV DATA", param);
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
                Log.e("받은 값", data);

            } catch (MalformedURLException e) { // 망할 예외처리들은 왜케 많아 .. 뭐가 뭔지도 잘 모르겠어
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            int i = Integer.parseInt(data);
            if (i >= 1) {
                Toast.makeText(getApplicationContext(), "이미 사용중인 아이디입니다", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(getApplicationContext(), "사용가능한 아이디 입니다", Toast.LENGTH_SHORT).show();

            idChkResult = i;
        }


    }

    public void onBackPressed() {

        AlertDialog.Builder join_alert = new AlertDialog.Builder(User_Signup.this);

        join_alert.setTitle("로그인 화면으로 돌아갑니다.");
        join_alert.setMessage("입력된 내용은 저장되지 않습니다\n");
        join_alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                finish();

            }
        });

        join_alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                return;
            }
        });

        AlertDialog infogo = join_alert.create();

        infogo.show();
    }
}
