package com.project.sangyeop.road_rideronaroad;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class View_userProfile extends AppCompatActivity {

    String absolutePath;
//    String imgfolderName;

    final String uploadFilePath = "storage/emulated/0/";//경로를 모르겠으면, 갤러리 어플리케이션 가서 메뉴->상세 정보
    final String uploadFileName = "profileImg_444.jpg"; //전송하고자하는 파일 이름

    int serverResponseCode = 0;
    String upLoadServerUri = null;
    ProgressDialog dialog = null;

    private static final int PICK_FROM_CAMERA = 111;
    private static final int PICK_FROM_ALBUM = 222;
    private static final int CROP_FROM_CAMERA = 333;

    private Uri mImageCaptureUri;

    String journal_img_s = null;

    ImageView logged_user_profileImg, logged_user_backgroundImg, edit_userProfile;
    ImageButton backtoMainmenu;

    SharedPreferences userInfo_sharedPreferences;
    int logged_user_index;
    String logged_user_profileImg_path;
    String logged_user_backgroundImg_path;

    TextView logged_user_nickName_on_viewProfile, logged_user_statusMsg_on_viewProfile;

    int imageLocation;
    String logged_userNickname;
    String logged_user_statusMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_userprofile);

        userInfo_sharedPreferences = getSharedPreferences("user_info", 0);
        logged_user_index = userInfo_sharedPreferences.getInt("user_index", -1);
//        logged_userNickname = userInfo_sharedPreferences.getString("user_nickname", "null");
//        logged_user_statusMessage = userInfo_sharedPreferences.getString("status_message", "null");
        /**
         *  유저인덱스는 프로필이미지 파일이름의 뒤에 붙게된다. 파일 덮어쓰기를 방지하기 위함
         *
         *  ex) 파일이름 : profileImg  /  유저인덱스 : 1  -> profileImg1
         *
         *  안그러면 다른유저의 이미지파일과 겹칠 수가 있음
         */


        logged_user_profileImg_path = "http://sangyeop0715.cafe24.com/img/profileImg_" + logged_user_index + ".jpg";
        logged_user_backgroundImg_path = "http://sangyeop0715.cafe24.com/img/backgroundImg_" + logged_user_index + ".jpg";

        backtoMainmenu = (ImageButton) findViewById(R.id.finish_EditProfile);
        logged_user_backgroundImg = (ImageView) findViewById(R.id.logged_user_backgroundImg);
        logged_user_profileImg = (ImageView) findViewById(R.id.logged_user_profileImg);
        edit_userProfile = (ImageView) findViewById(R.id.edit_userProfile);
        logged_user_nickName_on_viewProfile = (TextView) findViewById(R.id.logged_user_nickName_on_viewProfile);
        logged_user_statusMsg_on_viewProfile = (TextView) findViewById(R.id.logged_user_statusMsg_on_viewProfile);

//        logged_user_nickName_on_viewProfile.setText(logged_userNickname);
//        logged_user_statusMsg_on_viewProfile.setText(logged_user_statusMessage);

        Glide.with(View_userProfile.this)
                .load(logged_user_backgroundImg_path)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .error(R.drawable.profile)
                .into(logged_user_backgroundImg);

//        Glide.with(View_userProfile.this)
//                .load(logged_user_profileImg_path)
//                .diskCacheStrategy(DiskCacheStrategy.NONE)
//                .skipMemoryCache(true)
//                .error(R.drawable.profile)
//                .into(logged_user_profileImg);
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", 0);
        String encodedString = sharedPreferences.getString("user_img", null);

        Log.d("체크 이미지 스트링", encodedString);

        byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        logged_user_profileImg.setImageBitmap(bitmap);

        /** 일단 계속 서버에서 불러오지만 결국엔
         *
         *  자기 프사는 내부DB에서 불러오자
         */
        backtoMainmenu.setOnClickListener(new View.OnClickListener() { // X 버튼을 누르면 메인화면으로 돌아간다.
            @Override
            public void onClick(View v) {

                finish();

            }
        });

//        logged_user_backgroundImg.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                imageLocation = 1;
//                Dialog_getImage_on_background();
//
//            }
//        });
//
//        logged_user_profileImg.setOnClickListener(new View.OnClickListener() { // 유저의 프로필사진을 클릭하면, 수정기능이 실행된다. 사진 or 앨범으로 가능
//            @Override
//            public void onClick(View view) { // 유저 프사를 클릭하면, 프사 수정이 실행된다.
//
//                imageLocation = 0;
//                Dialog_getImage(); // 다이얼로그가 뜨면서, 사진or앨범 중 원하는 메뉴로 프사를 수정할 수 있다.
//
//            }
//        });

        edit_userProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent edit_userProfile = new Intent(View_userProfile.this, User_editProfile.class);

                startActivity(edit_userProfile);

            }
        });
    }

    private void Dialog_getImage() {
        AlertDialog.Builder fromImage = new AlertDialog.Builder(this);
        fromImage.setTitle("프로필사진 수정");

        fromImage.setPositiveButton("앨범선택", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, PICK_FROM_ALBUM);

            }

        });

        fromImage.setNegativeButton("사진촬영", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                // 임시로 사용할 파일의 경로를 생성
                String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
                mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));

                intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
                // 특정기기에서 사진을 저장못하는 문제가 있어 다음을 주석처리 합니다.
//                intent.putExtra("return-data", true);

                startActivityForResult(intent, PICK_FROM_CAMERA);
            }

        });

        fromImage.setNeutralButton("취소", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Log.d("프로필 수정 취소됨", "프로필 수정 취소됨");

            }

        });

        fromImage.show();
    }

    private void Dialog_getImage_on_background() {
        AlertDialog.Builder fromImage = new AlertDialog.Builder(this);
        fromImage.setTitle("배경화면 수정");

        fromImage.setPositiveButton("앨범선택", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, PICK_FROM_ALBUM);

            }

        });

        fromImage.setNegativeButton("사진촬영", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                // 임시로 사용할 파일의 경로를 생성
                String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
                mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));

                intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
                // 특정기기에서 사진을 저장못하는 문제가 있어 다음을 주석처리 합니다.
//                intent.putExtra("return-data", true);

                startActivityForResult(intent, PICK_FROM_CAMERA);
            }

        });

        fromImage.setNeutralButton("취소", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Log.d("프로필 수정 취소됨", "프로필 수정 취소됨");

            }

        });

        fromImage.show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        String profileImg_ID = String.valueOf(logged_user_index);
        /** profileImg_ID의 용도
         *
         * 위에서 말했듯이, 프로필사진 파일중복 overwrite되는 걸 방지하기 위한 값이다.
         * 이 값은 기본파일명 뒤에 붙는다,
         *
         * profileImg_ID는 DB상 user_info 테이블의 user_index 값과 같다.
         * 따라서 이값을 이용하면, 중복되는 걸 방지할 수 가 있다.
         *
         * ex) 파일명 : profileImg  프사아이디 : 444    ->   저장되는 파일명 : profileImg444
         */

        switch (requestCode) {

            case PICK_FROM_ALBUM:

                if (resultCode == RESULT_OK) {
                    mImageCaptureUri = data.getData();
                }

                /** 일종의 요령이자 얌수
                 *
                 * 이곳에 break 이 없어서, 다음 case 가 진행된다.
                 */

            case PICK_FROM_CAMERA:

                if (resultCode == RESULT_OK) {
                    // 이미지를 가져온 이후의 리사이즈할 이미지 크기를 결정합니다.
                    // 이후에 이미지 크롭 어플리케이션을 호출하게 됩니다.

                    Intent intent = new Intent("com.android.camera.action.CROP"); // 크롭된 이미지를 가져온다.
                    intent.setDataAndType(mImageCaptureUri, "image/*");

                    intent.putExtra("outputX", 333); // 크롭이미지의 품질(?)을 결정하는 변수들이다.
                    intent.putExtra("outputY", 333);
                    intent.putExtra("aspectX", 1);
                    intent.putExtra("aspectY", 1);
                    intent.putExtra("scale", true);
                    intent.putExtra("return-data", true);

                    startActivityForResult(intent, CROP_FROM_CAMERA);

                    break;
                }

            case CROP_FROM_CAMERA:

                if (resultCode == RESULT_OK) { // 크롭이 완료되면 !!

                    final Bundle extras = data.getExtras(); // 크롭된 이미지를 전달받는다

                    absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath();
//                    imgfolderName = "/ROAD";
                    String filename;
                    if (imageLocation == 1) {
                        filename = "/backgroundImg_" + logged_user_index + ".jpg";
                    } else {
                        filename = "/profileImg_" + logged_user_index + ".jpg";
                    }
                    final String filepath = absolutePath + filename;
//                    String filepath = absolutePath + imgfolderName +filename;

//                    String filepath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg";
//profileImg_ID 흠 이걸 어디서 처리해야하나ㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏ
//                    String filepath = "storage/external_SD/DCIM/Camera/" + System.currentTimeMillis()+".jpg";

                    Log.d("내부에 저장할 파일패스(외부) : ", absolutePath); // 외부메모리가 없으면 자동으로 내부메모리 경로를 불러올까?
                    Log.d("내부에 저장할 파일명 : ", filename);
                    Log.d("내부에 저장할 파일명 : ", filepath);

//                    String savefilepath = "storage/external_SD/DCIM/Camera/"+System.currentTimeMillis()+"test.jpg";
//                    String fileName = "test.jpg";

//                    filepath = filepath + fileName;

//                    File f = new File(filepath);

                    File f = new File(mImageCaptureUri.getPath());
//                    File f2 = new File(filepath);

                    Log.d("크롭된 이미지가 있는 패스값: ", mImageCaptureUri.getPath());

                    if (extras != null) {
                        Bitmap journal_bitmap = extras.getParcelable("data");

                        if (imageLocation == 1) {
                            logged_user_backgroundImg.setImageBitmap(journal_bitmap);
                        } else {
                            SharedPreferences sharedPreferences = getSharedPreferences("user_info", 0);
                            String encodedString = sharedPreferences.getString("user_img", null);

                            Log.d("체크 이미지 스트링", encodedString);

                            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                            logged_user_profileImg.setImageBitmap(bitmap);
                        }

                        storeCropImage(journal_bitmap, filepath);     //크롭 이미지를 저장

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { //브로드 캐스팅해서, 갤러리의 상태를 갱신시킨다. 이게 없으면 핸드폰을 리붓까지해서야 저장된게 보임
                            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE); //파일단위 갱신은 스캔너스캔파일, 폴더단위 갱신은 미디어 마운트

                            Uri contentUri = Uri.fromFile(f); //풀패스를 줘야함

                            Log.d("contenUri 값: ", String.valueOf(contentUri));

//                            mediaScanIntent.setData(contentUri);
                            mediaScanIntent.setData(Uri.parse(filepath));

                            Log.d("mediaScanIntent 값: ", String.valueOf(mediaScanIntent));

                            Log.d("mediaScadata값:", String.valueOf(mediaScanIntent.setData(Uri.parse(filepath))));

                            this.sendBroadcast(mediaScanIntent);

                            Log.d("망할 킷캣 이상은 브로드가 이상해", "으,아아아아ㅏㅇ");


                        } else {
                            final Intent intent = new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory()));
                            sendBroadcast(intent);


                            Log.d("킷캣이하는 걍 되는데 ", "으,아ㅁㄴㅇㄻㄴㅇㄹ아아아ㅏㅇ");

                        }

//                         sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+Environment.getExternalStorageDirectory())));
//                        갤러리 갱신을 위한 브로드캐스트

                        String bitmap_s = getBase64String(journal_bitmap); //액티비티 전환시 문자열로 넘기거나, 파일저장후 난중에 경로로 불러오는 방법이 있다.
                        journal_img_s = bitmap_s;

                    }

                    // 임시 파일 삭제
//                    File f = new File(mImageCaptureUri.getPath());
//
//                    if (f.exists()) {
//                        f.delete();
//                    }

                    /////////////////// 서버로 보낸다

                    Log.d("uploadFilepath : ", filepath);

//                    upLoadServerUri = "http://yeop0715.cafe24.com/uploadImg_to_server.php";
                    upLoadServerUri = "http://sangyeop0715.cafe24.com/user_signup/UploadToServer.php";

                    dialog = ProgressDialog.show(View_userProfile.this, "", "Uploading file...", true);

//                    uploadFile(filepath);
                    new Thread(new Runnable() {
                        public void run() {

                            uploadFile(filepath);

                            runOnUiThread(new Runnable() {
                                public void run() {
//                                    messageText.setText("upl/ading started.....");


                                }
                            });

                        }
                    }).start();

                    //////////////////


                    break;
                }


        }
    }

    public String getBase64String(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

        byte[] imageBytes = byteArrayOutputStream.toByteArray();

        return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
    }

    private void storeCropImage(Bitmap bitmap, String filePath) {
        File copyFile = new File(filePath);
        BufferedOutputStream out = null;

        try {

            copyFile.createNewFile();
            out = new BufferedOutputStream(new FileOutputStream(copyFile));

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

            out.flush();
            out.close();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public int uploadFile(String sourceFileUri) {

        String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);

        if (!sourceFile.isFile()) {

            dialog.dismiss();

            Log.d("업로드 파일", "소스파일이 없시유 :" + sourceFileUri);

            runOnUiThread(new Runnable() {
                public void run() {
//                    messageText.setText("Source File not exist :"
//                            +uploadFilePath + "" + uploadFileName);
                }
            });

            return 0;

        } else {
            try {

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(upLoadServerUri);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);

                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + fileName + "\"" + lineEnd); //아 이부분 이해가 필요해
//                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + fileName + "\"" + lineEnd);


                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                if (serverResponseCode == 200) {

                    runOnUiThread(new Runnable() {
                        public void run() {

//                            String msg = "File Upload Completed.\n\n See uploaded file here : \n\n"
//                                    +uploadFileName;


//                            messageText.setText(msg);
                            Log.d("전송완료 ㅋ", "본서버에 되면 참 좋을텐 데, 암튼 File Upload Complete.");


                        }
                    });

                }
                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

                // 서버 -> 안드로이드로 para 전달
                String data = null;

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
                Log.e("받은값", data);

            } catch (MalformedURLException ex) {

                dialog.dismiss();
                ex.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
//                        messageText.setText("MalformedURLException Exception : check script url.");
                        Toast.makeText(View_userProfile.this, "MalformedURLException",
                                Toast.LENGTH_SHORT).show();
                    }
                });

                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

                dialog.dismiss();
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
//                        messageText.setText("Got Exception : see logcat ");
                    }
                });
            }
            dialog.dismiss();

            return serverResponseCode;

        } // End else block
    }

    public void onResume() {
        super.onResume();

        /** Resume 마다 갱신은 정말 아닌 것 같다.
         *
         *  내부 DB가 필요하다.
         */

        /**프로필 정보 갱신*/

        logged_userNickname = userInfo_sharedPreferences.getString("user_nickname", "null");
        logged_user_statusMessage = userInfo_sharedPreferences.getString("status_message", "null");

        logged_user_nickName_on_viewProfile.setText(logged_userNickname);
        logged_user_statusMsg_on_viewProfile.setText(logged_user_statusMessage);
        Log.d("유저닉네임 갱신 : ", logged_userNickname);
        Log.d("유저닉네임 갱신 : ", logged_user_statusMessage);

//        imgV.setBackground(new ShapeDrawable(new OvalShape())); 뭐하는 애들이엇더라
//        imgV.setClipToOutline(true);

        /** 현재 이미지가 업로드 되는 속도보다, Resume 되는 속도가 훨씬 빠르기 때문에,
         *  수정된 사진이 아니라, 이전 사진이 로딩되어 버린다.
         *
         *  이를 방지하기위해 로딩을 1초 지연처리했다.
         *
         *  더 나은 방법이 있을텐 데, 아직 잘 모르겠다.
         */

        try {
            Thread.sleep(1000);

//            Glide.with(View_userProfile.this)
//                    .load(logged_user_backgroundImg_path)
//                    .diskCacheStrategy(DiskCacheStrategy.NONE)
//                    .skipMemoryCache(true)
//                    .error(R.drawable.profile)
//                    .into(logged_user_backgroundImg);

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
}
