package com.project.sangyeop.road_rideronaroad;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


public class faceDetect extends AppCompatActivity
        implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "opencv";
    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat matInput;
    private Mat matResult;

    //    public native void ConvertRGBtoGray(long matAddrInput, long matAddrResult);
    public static native long loadCascade(String cascadeFileName);

    public static native int detect(long cascadeClassifier_face,
                                    long cascadeClassifier_eye, long matAddrInput, long matAddrResult);

    public long cascadeClassifier_face = 0;
    public long cascadeClassifier_eye = 0;

    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("native-lib");
    }


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    ImageButton doCapture;
    Handler mHandler;
    int faceDetect = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * 얼굴이 1개 이상 검출이 되면, 촬영버튼이 활성화 되고,
         * 아니면 비활성화 됩니다.
         */
        mHandler = new Handler();

        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                // UI 작업 수행 X

                while (true) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (faceDetect > 0) {

                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                doCapture.setEnabled(true);
                                doCapture.setVisibility(View.VISIBLE);
                                // UI 작업 수행 O
                            }
                        },0 );

                    } else {

                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                doCapture.setEnabled(false);
                                doCapture.setVisibility(View.GONE);
                                // UI 작업 수행 O
                            }
                        }, 0);
                    }
                }
            }
        });
        t.start();

        /**
         * 그리고 상단에 있는 상태바를 없애기 위해
         * MainActivity의 onCreate() 메소드에 다음 코드가 필요합니다.
         *
         * 아래 한줄은 또 뭐냐
         */
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_face_detect);

        doCapture = findViewById(R.id.capture);
        doCapture.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {

                                             /**
                                              * 이미지를 캡처합니다.
                                              *
                                              * 아 근데 전혀 원리를 모르겠네. 으아아ㅏㅏㅏㅏㅏㅏㅏㅏㅏ
                                              */

                                             Bitmap bitmap = Bitmap.createBitmap(mOpenCvCameraView.getWidth() / 4, mOpenCvCameraView.getHeight() / 4, Bitmap.Config.ARGB_8888);

                                             try {
                                                 bitmap = Bitmap.createBitmap(matInput.cols(), matInput.rows(), Bitmap.Config.ARGB_8888);
                                                 Utils.matToBitmap(matInput, bitmap);

                                             } catch (Exception e) {
                                                 System.out.println(e.getMessage());
                                             }

                                             /**
                                              * 이미지를 저장합니다.
                                              */
                                             ByteArrayOutputStream baos=new  ByteArrayOutputStream();
                                             bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
                                             byte [] b=baos.toByteArray();
                                             String temp= Base64.encodeToString(b, Base64.DEFAULT);

                                             SharedPreferences sharedPreferences = getSharedPreferences("user_info",0);
                                             SharedPreferences.Editor editor = sharedPreferences.edit();

                                             editor.putString("user_img", temp);
                                             Log.d("체크 이미지 스트링",temp);
                                             editor.commit();
//                                             Intent resultIntent = new Intent();
//                                             resultIntent.putExtra("test","444444");
                                             setResult(RESULT_OK);
                                             finish();

                                         }
                                     }
        );


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //퍼미션 상태 확인
            if (!hasPermissions(PERMISSIONS)) {

                //퍼미션 허가 안되어있다면 사용자에게 요청
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            } else read_cascade_file(); //추가
        } else read_cascade_file(); //추가

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        /**
         * 활용할 카메라를,
         * 전면카메라는 1, 후면카메라는 0 으로 설정할 수 있습니다.
         */
        mOpenCvCameraView.setCameraIndex(1); // front-camera(1),  back-camera(0)
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "onResume :: Internal OpenCV library not found.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "onResum :: OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();

        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    /**
     * 카메라로부터 영상을 가져올 때마다 jni 함수 detect를 호출하도록 합니다.
     * 얼굴 검출하는 cpp 코드를 호출하는 부분입니다.
     *
     * @param inputFrame
     * @return
     */
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        matInput = inputFrame.rgba();

        if (matResult != null) matResult.release();
        matResult = new Mat(matInput.rows(), matInput.cols(), matInput.type());

        /**
         *  카메라로부터 영상을 읽어올 때
         *  전면 카메라의 경우 영상이 뒤집혀서 읽히기 때문에  180도 회전 시켜줘야 합니다.
         */
        Core.flip(matInput, matInput, 1);

        /**
         *  JAVA에서 영상이 들어오기 시작하면
         *  CascadeClassifier 객체를 인자로 해서 호출되어 얼굴 인식 결과를 영상에 표시해줍니다.
         */
        faceDetect = detect(cascadeClassifier_face, cascadeClassifier_eye, matInput.getNativeObjAddr(),
                matResult.getNativeObjAddr());

        Log.d("상판검출수", faceDetect + "");


        /**
         * 최종 결과를 안드로이드폰의 화면에 보여지도록 결과 Mat 객체를 리턴합니다
         */

        return matResult;

        /**
         * 실행결과입니다. 얼굴 위치와 눈 위치가 검출된 결과입니다.
         이상하게도 화면이 portrait 방향일 때에는 검출이 안되고  아래 화면처럼 landscape 방향일 때에만 검출이 됩니다.
         단 가로로 회전시켜야 얼굴 인식이 됩니다.
         (OpenCV에서 전체 화면을 사용하기 위해서 가로로 화면을 고정시켜 놓았기때문입니다.)


         주의 할 점은 이미지 프로세싱 혹은 컴퓨터 비전 기술 특성상 장소에 따라 제대로 인식이 안되거나 오류가 있을 수 있습니다.
         가장 큰 원인은 장소마다 조명 상태가 다르기 때문입니다.
         적절한 조명 상태에서 잘 인식이 됩니다.

         추후 시간나는대로 코드분석 결과를 포스팅해서 본 포스팅에 링크를 걸어둘 예정입니다.
         (아직 미정입니다.)
         */
    }


    /**
     * 여기서부턴 퍼미션 관련 메소드
     */

    static final int PERMISSIONS_REQUEST_CODE = 1000;

    /**
     * 외장 저장소에 파일 저장하기 위한 퍼미션을 추가합니다.
     */
    String[] PERMISSIONS = {"android.permission.CAMERA",
            "android.permission.WRITE_EXTERNAL_STORAGE"};

    private boolean hasPermissions(String[] permissions) {
        int result;

        //스트링 배열에 있는 퍼미션들의 허가 상태 여부 확인
        for (String perms : permissions) {

            result = ContextCompat.checkSelfPermission(this, perms);

            if (result == PackageManager.PERMISSION_DENIED) {
                //허가 안된 퍼미션 발견
                return false;
            }
        }

        //모든 퍼미션이 허가되었음
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean cameraPermissionAccepted = grantResults[0]
                            == PackageManager.PERMISSION_GRANTED;

                    /**
                     * WRITE_EXTERNAL_STORAGE 퍼미션 확인을 위해 필요한 코드를 추가합니다.
                     */
                    boolean writePermissionAccepted = grantResults[1]
                            == PackageManager.PERMISSION_GRANTED;

                    if (!cameraPermissionAccepted || !writePermissionAccepted) {
                        showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");
                        return;
                    } else {
                        read_cascade_file();
                    }
                }
                break;
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(faceDetect.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
            }
        });
        builder.create().show();
    }

    /**
     * xml 파일을 가져오기 위한 메소드들을 추가합니다.
     * 현재 이미 파일을 copyFile 메소드를 이용해서 가져온 경우에 대한 처리가 빠져있습니다.
     *
     * @param filename
     */
    private void copyFile(String filename) {
        String baseDir = Environment.getExternalStorageDirectory().getPath();
        String pathDir = baseDir + File.separator + filename;

        AssetManager assetManager = this.getAssets();

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            Log.d(TAG, "copyFile :: 다음 경로로 파일복사 " + pathDir);
            inputStream = assetManager.open(filename);
            outputStream = new FileOutputStream(pathDir);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();
            inputStream = null;
            outputStream.flush();
            outputStream.close();
            outputStream = null;
        } catch (Exception e) {
            Log.d(TAG, "copyFile :: 파일 복사 중 예외 발생 " + e.toString());
        }

    }

    /**
     * cpp 파일의 loadCascade 함수를 호출하도록 구현되어있는데 자바 함수를 사용하도록 변경해도 됩니다.
     */
    private void read_cascade_file() {
        //copyFile 메소드는 Assets에서 해당 파일을 가져와
        //외부 저장소 특정위치에 저장하도록 구현된 메소드입니다.
        copyFile("haarcascade_frontalface_alt.xml");
        copyFile("haarcascade_eye_tree_eyeglasses.xml");

        Log.d(TAG, "read_cascade_file:");

        //loadCascade 메소드는 외부 저장소의 특정 위치에서 해당 파일을 읽어와서
        //CascadeClassifier 객체로 로드합니다.

        /**
         *  내부 저장소로부터 XML 파일(Harr cascade 트레이닝 데이터)을 읽어와
         *  CascadeClassifier 객체를 생성후 자바로 넘겨줍니다.
         */
        cascadeClassifier_face = loadCascade("haarcascade_frontalface_alt.xml");
        Log.d(TAG, "read_cascade_file:");

        cascadeClassifier_eye = loadCascade("haarcascade_eye_tree_eyeglasses.xml");
    }

    public class RecevingMsgTEST extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            while (true) {
                Log.d("asdfasdfasdf", "asdfasdfasdfasdf");
            }
        }
    }


}