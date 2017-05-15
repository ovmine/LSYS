package com.example.administrator.lsys_camera;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.Image;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.administrator.lsys_camera.recycler.adapter.RecyclerAdapterCollage;
import com.example.administrator.lsys_camera.recycler.adapter.RecyclerAdapterFilter;
import com.example.administrator.lsys_camera.recycler.adapter.RecyclerAdapterSticker;
import com.example.administrator.lsys_camera.recycler.list.ItemListCollage;
import com.example.administrator.lsys_camera.recycler.list.ItemListFilter;
import com.example.administrator.lsys_camera.recycler.list.ItemListSticker;
import com.example.administrator.lsys_camera.collage.*;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Animation.AnimationListener {

    // Renderer 객체, EGL에 그림을 그려줌
    private Renderer renderer;

    // TextureView, 실시간 프리뷰보여줌, OpenGL에의해 그려지는 그림을 여기에 그림
    private TextureView textureView;

    // AutoFocusing 객체
    private Camera.AutoFocusCallback saveAutoFocus;

    // Renderer에서 쓰임, 예전에 테스트용으로넣은것 (추후삭제가능)
    public static int mainControl = 1;

    // tiemrImage
    private Bitmap bitmap;
    private Bitmap bitmapPre;

    // 메뉴구성 관련
    private Animation animation_slidemenu_filter;
    private Animation animation_slidemenu_collage;
    private Animation animation_slidemenu_sticker;
    private View slidemenu_filter;
    private View slidemenu_collage;
    private View slidemenu_sticker;

    // Request code for runtime permissions
    private final int REQUEST_CODE_STORAGE_PERMS = 321;

    // 동기화를위한 flag 변수들
    public static boolean pushChangeBtn = false;    // false 일때가 후면카메라
    public static boolean pushFlashBtn = false;     // false 일때가 Off

    public static boolean textOn = false;
    public static boolean stickerOn = false;

    //스티커 체크용
    //public static boolean textureviewTouch=false;
    //public static boolean checked=false;
    //public static float touchPosX;//터치 위치
    //public static float touchPosY;

    //스티커 체크용
    public static boolean newStickeron=false;
    public static boolean TextureviewClicked=false;
    public static boolean textureviewTouch=false;
    public static boolean checked=false;
    public static float touchPosX;//터치 위치
    public static float touchPosY;
    public static int stickerNum;

    public static float stickerPosX;//스티커 위치
    public static float stickerPosY;

    // 이미지버튼
    private ImageView btChange;
    private ImageView btFlash;
    private ImageView capture;
    private ImageView btTimer;
    private ImageView collageButton;

    // 타이머 관련
    private int timerTime = 0; // 타이머 시간 값
    private int timerText;
    private  Thread timerThread;
    private Handler mHandler ;
    private int mCount;
    private Context context = this;

    public static int TEX_MAX_SIZE;


    //줌 관련
    private double touchDistance; // 양손가락으로 터치할때 그 간격

    // GIF 변수
    boolean gifOn =false; // 움짤모드
    boolean gifContinuity = true; // 연속촬영 or 수동촬영
    int gifGap =100; // 촬영 간격 500이면 0.5초
    int MAXPICTURE =20; // MaxPicture
    int gifGoalCount =MAXPICTURE; // 설정으로 바꿀것
    int gifCount = 0; // 현재 몇장 저장되었는지 알려주는 변수

    ArrayList<Bitmap> bitmapListGif; // 촬영 이미지 임시저장하는 List
    ArrayList<Bitmap> bitmapListCollage; // 촬영 이미지 임시저장하는 List

    ArrayList<Bitmap> bitmapListTemp; // 콜라주를 위한 임시 비트맵
    Bitmap collageCombineImage = null;

    private Thread saveThread;

    private  Object cameraUsed = new Object(); // Synchronized Object
    private  Object drawObject = new Object(); // Synchronized Object
    private  Object timerSync = new Object(); // Synchronized Object
    private  Object saveSync = new Object(); // Synchronized Object

    // 메뉴 애니메이션 관련
    private boolean buttonFilterOn = false;
    private boolean buttonCollageOn = false;
    private boolean buttonStickerOn = false;

    // recycler View
    private RecyclerView recycler_View_Filter;
    private RecyclerView recycler_View_Collage;
    private RecyclerView recycler_View_Sticker;
    private RecyclerView.LayoutManager recycler_LayoutManager_Filter;
    private RecyclerView.LayoutManager recycler_LayoutManager_Collage;
    private RecyclerView.LayoutManager recycler_LayoutManager_Sticker;
    List<ItemListFilter> list_Filter;
    List<ItemListCollage> list_Collage;
    List<ItemListSticker> list_Sticker;

    // 스위치 (gif on/off)
    private Switch switchOfGif;

    // 촬영모드설정
    private int mode = CAPTURE_NORMAL;
    public static final int CAPTURE_NORMAL = 1;
    public static final int CAPTURE_GIF = 2;
    public static final int CAPTURE_COLLAGE = 3;

    public int collageNumber = 0;


    // 카메라 권한을 갖고있는지 확인
    // API 23 이상부터는 프로그램 실행중에 권한을 요청받음. (최초1회)
    private boolean hasPermissions() {
        int res = 0;
        // list all permissions which you want to check are granted or not.
        String[] permissions = new String[] {
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        for (String perms : permissions){
            res = checkCallingOrSelfPermission(perms);
            if (!(res == PackageManager.PERMISSION_GRANTED)){
                // it return false because your app dosen't have permissions.
                return false;
            }

        }
        // it return true, your app has permissions.
        return true;
    }

    // 필수권한요청, 권한요청 물어보기
    private void requestNecessaryPermissions() {
        // make array of permissions which you want to ask from user.
        String[] permissions = new String[] {
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // have arry for permissions to requestPermissions method.
            // and also send unique Request code.
            // 원래 권한을 요청하는 함수
            requestPermissions(permissions, REQUEST_CODE_STORAGE_PERMS);
        }
    }

    /* when user grant or deny permission then your app will check in
      onRequestPermissionsReqult about user's response. */
    // 권한요청 승인/거절 을 눌렀을때 상황
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grandResults) {
        // this boolean will tell us that user granted permission or not.
        boolean allowed = true;
        switch (requestCode) {
            case REQUEST_CODE_STORAGE_PERMS:
                for (int res : grandResults) {
                    // if user granted all required permissions then 'allowed' will return true.
                    allowed = allowed && (res == PackageManager.PERMISSION_GRANTED);
                    Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                // if user denied then 'allowed' return false
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                allowed = false;
                break;
        }

        // allowed 가 true인것은 사용자가 승인을 누른 상태
        if (allowed) {
            // if user granted permissions then do your work.
            Start();
        }
        else {
            // else give any custom waring message.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    Toast.makeText(MainActivity.this, "Camera Permissions denied", Toast.LENGTH_SHORT).show();
                }
                else if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                    Toast.makeText(MainActivity.this, "Storage Permissions denied", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            if (!hasPermissions()) {
                // your app doesn't have permissions, ask for them.
                requestNecessaryPermissions();
            } else {
                // your app already have permissions allowed.
                // do what you want.

                Start();
            }
        }
        else {
            // 예외처리, 카메라가 아예 없는 디바이스의 경우임
            Toast.makeText(MainActivity.this, "Camera not supported", Toast.LENGTH_LONG).show();
        }
    }

    private void Capture()
    {
        // 다른 버튼들 모두 사용불가 하게 함
        btChange.setEnabled(false);
        btFlash.setEnabled(false);
        btTimer.setEnabled(false);

       saveThread = LSYSUtility.Save(MainActivity.this, textureView,saveSync);
    }


    class GifTimer extends Thread
    {
        int gap;
        int goalCount;
        int i;
        public GifTimer(int goalCount, int gap){
            this.gap = gap;
            this.goalCount = goalCount;
        }
        public void run()
        {
                try {
                    for(i = 0; i<goalCount;i++)
                    {
                        bitmapListGif.add(i, textureView.getBitmap(textureView.getWidth() / 2, textureView.getHeight() / 2));
                        Thread.sleep(gap);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            if(i == goalCount)
            {

            }
        }
    }

    // 카메라 타이머
    class Timer extends Thread
    {
        int mtimerTime;

        public Timer(int time)
        {
            mtimerTime = time;
        }
        public void run()
        {
            if(mtimerTime != 0) {
                try {
                    // 5,4,3,2,1 를 나오게함. toast가 딜레이가 있으므로 다른것으로 수정이필요함 (애니메이션같은걸로)
                    for(timerText=mtimerTime; timerText>0;timerText--) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), " "+timerText, Toast.LENGTH_SHORT).show();
                            }
                        });
                        //타이머 시간동안 쉬기
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else
                timerText = 0;

            //찍으려고할때,
            if(timerText == 0) {
                    LSYSUtility.Save(MainActivity.this, textureView,saveSync); // 여기내부에 doingCapture = false를 넣으면 Thread가 종료되면서 실행 안될수도 있음?
            }
        }
    }

    void TimerExit()
    {
        // 타이머스레드 예외처리, 타이머를 끄는 함수
        if (timerThread != null && timerThread.isAlive()) { // 딴거 하다  다시 사진 찍을때 새로 쓰레드 생성
            timerThread.interrupt();
            capture.setEnabled(true); // 타이머를 중간에 종료하면 쓰레드 실행이 되지않아, 여기서 버튼들을 활성화
            btTimer.setEnabled(true);
        }
    }

    private void handleZoom(MotionEvent event, Camera camera){// Camera.Parameters params) {
        Camera.Parameters params = camera.getParameters();
        int maxZoom = params.getMaxZoom();
        int zoom = params.getZoom();
        double newTouchDistance = getFingerSpacing(event);
        if(Math.abs(newTouchDistance - touchDistance) > 5 ) { //미세한 움직임은 무시
            if (newTouchDistance > touchDistance) {
                //zoom in
                if (zoom < maxZoom)
                    zoom++;
            } else if (newTouchDistance < touchDistance) {
                //zoom out
                if (zoom > 0)
                    zoom--;
            }
        }
        touchDistance = newTouchDistance;
        params.setZoom(zoom);
        camera.setParameters(params);
    }

    public void handleFocus(MotionEvent event, Camera camera){// Camera.Parameters params) {
        Camera.Parameters params = camera.getParameters();
        int pointerId = event.getPointerId(0);
        int pointerIndex = event.findPointerIndex(pointerId);
        // Get the pointer's current position
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);

        List<String> supportedFocusModes = params.getSupportedFocusModes();
        if (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean b, Camera camera) {
                    // currently set to auto-focus on single touch
                }
            });
        }
    }

    /** Determine the space between the first two fingers */
    private double getFingerSpacing(MotionEvent event) {
        // ...
        double x = event.getX(0) - event.getX(1);
        double y = event.getY(0) - event.getY(1);

        return Math.sqrt(x * x + y * y);
    }

    private AnimationSet SetTimerAnimation(int type)
    {
        AnimationSet timerAniSet = new AnimationSet(true);
        timerAniSet.setInterpolator(new AccelerateInterpolator());

        if(type == 1) {
        /*
        timerTransAni = new TranslateAnimation
                (textureView.getWidth()/2,   // fromXDelta
                        textureView.getWidth()/2,  // toXDelta
                        textureView.getHeight()/2,    // fromYDelta
                        textureView.getHeight()/2);// toYDelta
        timerTransAni.setDuration(1000);
*/
            Animation timerAlpaAni = new AlphaAnimation(1.0f, 0.0f);
            timerAlpaAni.setDuration(1000);

            Animation ani02 = new RotateAnimation(0, 90);
            ani02.setDuration(1000);

            //timerAniSet.addAnimation(timerTransAni);
            timerAniSet.addAnimation(timerAlpaAni);
            //timerAniSet.addAnimation(ani02);
        }
        else if(type == 2) { // gif 저장용 애니메이션
            AlphaAnimation anim = new AlphaAnimation(1, 0.5f);
            anim.setDuration(1000);        // 에니메이션 동작 주기
            anim.setRepeatCount(-1);    // 에니메이션 반복 회수
            //anim.setRepeatMode(Animation.REVERSE);// 반복하는 방법
            // timerImage.setAnimation(timerAniSet);
            timerAniSet.addAnimation(anim);

        }
        return timerAniSet;
    }


    private void SetTimerImage(int number, ImageView timerImage)
    {
        timerImage.setImageBitmap(null);

        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }

        switch (number)
        {
            case 1:
                bitmap = LSYSUtility.GetBitmapImage(context,R.drawable.icon_count_01);
                timerImage.setImageBitmap(bitmap);
                break;
            case 2:
                bitmap = LSYSUtility.GetBitmapImage(context,R.drawable.icon_count_02);
                timerImage.setImageBitmap(bitmap);
                break;
            case 3:
                bitmap = LSYSUtility.GetBitmapImage(context,R.drawable.icon_count_03);
                timerImage.setImageBitmap(bitmap);
                break;
            case 4:
                bitmap = LSYSUtility.GetBitmapImage(context,R.drawable.icon_count_04);
                timerImage.setImageBitmap(bitmap);
                break;
            case 5:
                bitmap = LSYSUtility.GetBitmapImage(context,R.drawable.icon_count_05);
                timerImage.setImageBitmap(bitmap);
                break;
            case 6:
                bitmap = LSYSUtility.GetBitmapImage(context,R.drawable.icon_count_06);
                timerImage.setImageBitmap(bitmap);
                break;
            case 7:
                bitmap = LSYSUtility.GetBitmapImage(context,R.drawable.icon_count_07);
                timerImage.setImageBitmap(bitmap);
                break;
            case 8:
                bitmap = LSYSUtility.GetBitmapImage(context,R.drawable.icon_count_08);
                timerImage.setImageBitmap(bitmap);
                break;
            case 9:
                bitmap = LSYSUtility.GetBitmapImage(context,R.drawable.icon_count_09);
                timerImage.setImageBitmap(bitmap);
                break;
            case 10:
                bitmap = LSYSUtility.GetBitmapImage(context,R.drawable.icon_count_10);
                timerImage.setImageBitmap(bitmap);
                break;
            case 11:
                bitmap = LSYSUtility.GetBitmapImage(context,R.drawable.img_loading);
                timerImage.setImageBitmap(bitmap);
                break;
        }
    }

    // 카메라를 실행
    public void Start()
    {
        // 아랫줄은 상태바(배터리, 시간 표시)를 없애줌
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        bitmapListGif = new ArrayList<Bitmap>();
        bitmapListCollage = new ArrayList<Bitmap>();
        bitmapListTemp = new ArrayList<Bitmap>();

        //bos = new ByteArrayOutputStream();
        //encoder = new AnimatedGifEncoder();

        // Renderer 생성
        renderer = new Renderer(this, cameraUsed, saveThread);

        // 카메라 출력해주는 textureView
        textureView = (TextureView) findViewById(R.id.textureView);

        // 타미어 이미지
       // timerImage = (ImageView)findViewById(R.id.id_icon_timertext);

   //     for(int i=1; i<=10; i++)
     //   {
         //   SetTimerImage(i);
          //  SetTimerAnimation();
            //timerImage.startAnimation(timerAniSet);
       // }


       // timerImage.setVisibility(View.GONE);
        // 나중에 test할것 (삭제해도 ok?)
        assert textureView != null;

        // renderer를 textureView에 연결
        textureView.setSurfaceTextureListener(renderer);


        textureView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.e("","onTouch");
                textureviewTouch=true;
                return true;
            }
        });


        // textureView를 클릭하는 상황. (나중에 수정 예정)
        textureView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                 synchronized (cameraUsed) {

                     Log.e("sss","sss");
                     Camera camera = renderer.getCamera();
                     if(camera != null)
                     {
                     // Get the pointer ID
                     Camera.Parameters params = camera.getParameters();

                     int action = event.getAction();

                     if (event.getPointerCount() > 1) { // 현재 PointerCount가 1보다 클때. 즉, 손가락이 2개가 터치되어있을때

                         switch (action & MotionEvent.ACTION_MASK) {
                             case MotionEvent.ACTION_DOWN:

                                 Log.e("down", "down");
                                 break;

                             case MotionEvent.ACTION_POINTER_DOWN:
                                 touchDistance = getFingerSpacing(event);
                                 Log.e("ACTION_POINTER_DOWN", "ACTION_POINTER_DOWN");
                                 break;

                             case MotionEvent.ACTION_MOVE:
                                 if (params.isZoomSupported()) //줌을 지원하면
                                 {
                                     camera.cancelAutoFocus(); // cancelAutoFocus
                                     handleZoom(event, camera);
                                 }

                                 break;

                             case MotionEvent.ACTION_UP:

                             case MotionEvent.ACTION_CANCEL:


                                 break;
                         }

                     } else {
                         switch (event.getAction()) {
                             case MotionEvent.ACTION_DOWN:
                                 // renderer.setSelectedFilter(R.id.filter0);
                                 // renderer.AutoFocusing();
  //                               touchPosX = (event.getX() - (textureView.getWidth() / 2)) / textureView.getWidth() * 2;
  //                               touchPosY = -(event.getY() - (textureView.getHeight() / 2)) / textureView.getHeight() * 2;

                                 textureviewTouch=true;
                                 TextureviewClicked=true;
                                 touchPosX=(event.getX()-(textureView.getWidth()/2))/textureView.getWidth()*2;
                                 touchPosY=-(event.getY()-(textureView.getHeight()/2))/textureView.getHeight()*2;
                             //    Log.e("down!!","down!!");

                                 Log.e("down" + event.getX(), "down");
                                 break;
                             case MotionEvent.ACTION_MOVE:
                                 if(checked) {
                                     touchPosX = (event.getX() - (textureView.getWidth() / 2)) / textureView.getWidth() * 2;
                                     touchPosY = -(event.getY() - (textureView.getHeight() / 2)) / textureView.getHeight() * 2;
                                 }
                                 break;

                             case MotionEvent.ACTION_UP:
                                 textureviewTouch=false;
                                 Log.e("up","up");
                                 break;
                             case MotionEvent.ACTION_CANCEL:
                                 // renderer.setSelectedFilter(filterId);
                                 //   renderer.AutoFocusing();

                                 break;
                         }
                     }
                 }
                     }
                return false;
            }
        });


        // 핸드폰에 맞춰줌
        textureView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                renderer.onSurfaceTextureSizeChanged(null, v.getWidth(), v.getHeight());
            }
        });

        mHandler = new TimerHandler();
        //타이머 이미지 로드
        //for(int i =0; i<10;i++)
        //Glide.with(this).load(R.drawable.smile).into(timerImage[i]);

        // 슬라이드 메뉴 구현부
        slidemenu_filter = findViewById(R.id.id_submenu_filter);
        slidemenu_filter.setVisibility(View.GONE);
        slidemenu_collage = findViewById(R.id.id_submenu_collage);
        slidemenu_collage.setVisibility(View.GONE);
        slidemenu_sticker = findViewById(R.id.id_submenu_sticker);
        slidemenu_sticker.setVisibility(View.GONE);

        // 필터 아이콘 클릭
        ImageView btSlideUp = (ImageView) findViewById(R.id.id_icon_filter);
        Glide.with(this).load(R.drawable.icon_filter).into(btSlideUp);
        btSlideUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                // collage가 열려있다면
                if(buttonCollageOn==true){
                    animation_slidemenu_collage = AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_down_menu_collage);
                    animation_slidemenu_collage.setAnimationListener(MainActivity.this);
                    slidemenu_collage.startAnimation(animation_slidemenu_collage);
                    slidemenu_collage.setVisibility(View.GONE);
                    buttonCollageOn = false;
                }

                // filter button을 처음클릭한 상태. 필터 선택창 slide up
                if(buttonFilterOn == false){
                    animation_slidemenu_filter = AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_up_menu_filter);
                    animation_slidemenu_filter.setAnimationListener(MainActivity.this);
                    slidemenu_filter.setVisibility(View.VISIBLE);
                    slidemenu_filter.startAnimation(animation_slidemenu_filter);
                    buttonFilterOn = true;
                }
                // filter button을 클릭했던걸 닫는상태. 필터 선택창 slide down
                else {
                    animation_slidemenu_filter = AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_down_menu_filter);
                    animation_slidemenu_filter.setAnimationListener(MainActivity.this);
                    slidemenu_filter.startAnimation(animation_slidemenu_filter);
                    slidemenu_filter.setVisibility(View.GONE);
                    buttonFilterOn = false;
                }
            }
        });

        // 카메라 전/후방 전환
        btChange = (ImageView)findViewById(R.id.id_icon_change);
        Glide.with(this).load(R.drawable.icon_change).into(btChange);
        btChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(!pushChangeBtn) {
                    synchronized (saveSync) {
                        // 여기서 ChangeCamera()를 직접호출시, 변경도중 화면이 거꾸로 보이는 잔상이 나와서 일단 이 플래그만 유지.
                        synchronized (timerSync) { // view.gone 때문에
                            mHandler.removeCallbacksAndMessages(null); // 핸들러 콜백이벤트 및 메세지 제거
                            mHandler = null;
                            mHandler = new TimerHandler();
                            ImageView timerImage = (ImageView) findViewById(R.id.id_icon_timertext);
                            timerImage.setVisibility(View.GONE);

                            for (int i = 0; i < gifCount; i++) {
                                bitmapListGif.get(i).recycle();
                            }
                            bitmapListGif.clear();
                            gifCount =0;
                            // save thread interupt // saving상태가 아닌데 버튼눌리면 화면전환
                            //code
                        }
                        capture.setEnabled(false);
                        btChange.setEnabled(false);
                        btFlash.setEnabled(false);
                        btTimer.setEnabled(false);
                        pushChangeBtn = true;

                        TimerExit(); //TimerThread 예외처리
                    }
                }
            }
        });

        // 플래시 on
        btFlash = (ImageView)findViewById(R.id.id_icon_flash);
        Glide.with(this).load(R.drawable.icon_flash).into(btFlash);
        btFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(!renderer.isFrontCamera) {
                    synchronized (cameraUsed) {
                        pushFlashBtn = !pushFlashBtn;
                        if (pushFlashBtn)
                            renderer.FlashOn();
                        else
                            renderer.FlashOff();
                    }
                }
            }
        });

        // 타이머
        btTimer = (ImageView)findViewById(R.id.id_icon_timer);
        Glide.with(this).load(R.drawable.icon_timer).into(btTimer);
        btTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    // TODO Auto-generated method stub
                    switch (timerTime) {
                        case 0:
                            timerTime = 3;
                            Toast.makeText(getApplicationContext(), "3초 설정", Toast.LENGTH_SHORT).show();
                            break;
                        case 3:
                            timerTime = 5;
                            Toast.makeText(getApplicationContext(), "5초 설정", Toast.LENGTH_SHORT).show();
                            break;
                        case 5:
                            timerTime = 10;
                            Toast.makeText(getApplicationContext(), "10초 설정", Toast.LENGTH_SHORT).show();
                            break;
                        case 10:
                            timerTime = 0;
                            Toast.makeText(getApplicationContext(), "Timer off", Toast.LENGTH_SHORT).show();
                            break;
                    }
            }
        });

        // 사진촬영
        capture = (ImageView)findViewById(R.id.id_icon_circle);
        Glide.with(this).load(R.drawable.icon_circle).into(capture);
        capture.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                capture.setEnabled(false);

                Log.e("capture mode? "," "+mode);

                switch (mode){
                    case NiceValue.CAPTURE_NORMAL:

                        if(timerTime == 0)
                        {
                            Capture();
                        }
                        else
                        {
                            // Timer 버튼 사용불가 하게 함
                            btTimer.setEnabled(false);

                            //   timerThread = new Timer(timerTime);
                            //  timerThread.start();

                            //  SetTimerImage(timerTime);
                            Message message = new Message();
                            message.arg1 = timerTime;
                            message.arg2 = R.id.id_icon_timertext; //타이머 이미지뷰 id

                            mHandler.sendMessage(message);

                        }
                        break;

                    case NiceValue.CAPTURE_GIF:
                        /*
                        mBitmap = textureView.getBitmap();
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 4;
                        Bitmap resized = Bitmap.createScaledBitmap(mBitmap, 60, 60, true);
                        */
                        if(!gifContinuity) {
                            bitmapListGif.add(gifCount, textureView.getBitmap(200, 300));
                            gifCount++;

                            if (gifCount == gifGoalCount) // 안의 부분은 따로 스레드로 뺼것임
                            {
                                // 다른 버튼들 모두 사용불가 하게 함
                                btChange.setEnabled(false);
                                btFlash.setEnabled(false);
                                btTimer.setEnabled(false);

                                ImageView timerImage = (ImageView) findViewById(R.id.id_icon_timertext);

                                AnimationSet timerAniSet;
                                SetTimerImage(1, timerImage); // 시간에 맞는 이미지 설정
                                timerImage.setVisibility(View.VISIBLE); // 뷰 VISIBLE
                                timerAniSet = SetTimerAnimation(2); // 애니메이션 효과 선택
                                //timerImage.startAnimation(timerAniSet); // 애니메이션 시작
                                timerImage.setAnimation(timerAniSet); // 애니메이션 시작


                                saveThread = LSYSUtility.Save(MainActivity.this, bitmapListGif, gifGoalCount, saveSync, timerAniSet, R.id.id_icon_timertext);
                                // 팅길때 예상: 내부의 set버튼들과 변수초기화, 비우는것들 다 따로 지우기(처리)


                                // 초기화
                                gifCount = 0;
                            } else
                                capture.setEnabled(true);
                        }
                        else // 연속 촬영일때,
                        {
                            // 다른 버튼들 모두 사용불가 하게 함
                            btChange.setEnabled(false);
                            btFlash.setEnabled(false);
                            btTimer.setEnabled(false);
                            collageButton.setEnabled(false);


                            Message message = new Message();
                            message.arg1 = gifGoalCount;
                            message.arg2 = R.id.id_icon_timertext; //타이머 이미지뷰 id

                            mHandler.sendMessage(message);

                        }
                        break;

                    case NiceValue.CAPTURE_COLLAGE:

                        switch (collageNumber){
                            case NiceValue.COLLAGE01:

                                bitmapListCollage.add(textureView.getBitmap());
                                bitmapListTemp.add(BitmapCrop.cropBitmapForCollage01(bitmapListCollage.get(NiceValue.collageStep)));
                                NiceValue.collageStep++;

                                if(NiceValue.collageStep == NiceValue.NUMBER_OF_CAPTURE_COLLAGE_01){
                                    collageCombineImage = BitmapCombine.combineBitmapForCollage01(bitmapListTemp.get(0), bitmapListTemp.get(1));
                                    NiceValue.isCollageEnd = true;
                                    LSYSUtility.Save(MainActivity.this, textureView, collageCombineImage, saveSync);
                                    Toast.makeText(getApplicationContext(), "콜라주1 촬영완료", Toast.LENGTH_SHORT).show();
                                }

                                capture.setEnabled(true);

                                break;

                            case NiceValue.COLLAGE02:

                                bitmapListCollage.add(textureView.getBitmap());
                                bitmapListTemp.add(BitmapCrop.cropBitmapForCollage02(bitmapListCollage.get(NiceValue.collageStep)));
                                NiceValue.collageStep++;

                                if(NiceValue.collageStep == NiceValue.NUMBER_OF_CAPTURE_COLLAGE_02){
                                    collageCombineImage = BitmapCombine.combineBitmapForCollage02(bitmapListTemp.get(0), bitmapListTemp.get(1));
                                    NiceValue.isCollageEnd = true;
                                    LSYSUtility.Save(MainActivity.this, textureView, collageCombineImage, saveSync);
                                    Toast.makeText(getApplicationContext(), "콜라주2 촬영완료", Toast.LENGTH_SHORT).show();
                                }

                                capture.setEnabled(true);

                                break;

                            default:
                                break;
                        }

                        break;

                    default:
                        break;
                }
            }
        });

        // 갤러리
        ImageView textSticker =(ImageView)findViewById(R.id.id_icon_gallery);
        Glide.with(this).load(R.drawable.icon_gallery).into(textSticker);
        textSticker.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent galleryOpenIntent=new Intent(getApplicationContext(),photoGallery.class);//갤러리를 오픈하기 위한 인텐트
                startActivity(galleryOpenIntent);//갤러리 열어줌.
            }
        });

        // 이미지스티커
        ImageView imageSticker =(ImageView)findViewById(R.id.id_icon_image);
        Glide.with(this).load(R.drawable.icon_image).into(imageSticker);
        imageSticker.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                // sticker button을 처음클릭한 상태. 필터 선택창 slide up
                if(buttonStickerOn == false){
                    animation_slidemenu_sticker = AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_up_menu_sticker);
                    animation_slidemenu_sticker.setAnimationListener(MainActivity.this);
                    slidemenu_sticker.setVisibility(View.VISIBLE);
                    slidemenu_sticker.startAnimation(animation_slidemenu_sticker);
                    buttonStickerOn = true;
                }
                // sticker button을 클릭했던걸 닫는상태. 필터 선택창 slide down
                else {
                    animation_slidemenu_sticker = AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_down_menu_sticker);
                    animation_slidemenu_sticker.setAnimationListener(MainActivity.this);
                    slidemenu_sticker.startAnimation(animation_slidemenu_sticker);
                    slidemenu_sticker.setVisibility(View.GONE);
                    buttonStickerOn = false;
                }
            }
        });

        collageButton = (ImageView)findViewById(R.id.id_icon_collage);
        Glide.with(this).load(R.drawable.icon_collage).into(collageButton);
        collageButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                // filter menu가 열려있으면 닫아준다
                if(buttonFilterOn == true){
                    animation_slidemenu_filter = AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_down_menu_filter);
                    animation_slidemenu_filter.setAnimationListener(MainActivity.this);
                    slidemenu_filter.startAnimation(animation_slidemenu_filter);
                    slidemenu_filter.setVisibility(View.GONE);
                    buttonFilterOn = false;
                }

                // filter button을 처음클릭한 상태. 필터 선택창 slide up
                if(buttonCollageOn == false){
                    animation_slidemenu_collage = AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_up_menu_collage);
                    animation_slidemenu_collage.setAnimationListener(MainActivity.this);
                    slidemenu_collage.setVisibility(View.VISIBLE);
                    slidemenu_collage.startAnimation(animation_slidemenu_collage);
                    buttonCollageOn = true;
                }
                // filter button을 클릭했던걸 닫는상태. 필터 선택창 slide down
                else {
                    animation_slidemenu_collage = AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_down_menu_collage);
                    animation_slidemenu_collage.setAnimationListener(MainActivity.this);
                    slidemenu_collage.startAnimation(animation_slidemenu_collage);
                    slidemenu_collage.setVisibility(View.GONE);
                    buttonCollageOn = false;
                }
            }
        });

        // recycler view - 필터
        recycler_LayoutManager_Filter = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recycler_View_Filter = (RecyclerView) findViewById(R.id.id_filter_select);
        recycler_View_Filter.setHasFixedSize(true);
        recycler_View_Filter.setLayoutManager(recycler_LayoutManager_Filter);

        // recycler view - 콜라주
        recycler_LayoutManager_Collage = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recycler_View_Collage = (RecyclerView) findViewById(R.id.id_collage_select);
        recycler_View_Collage.setHasFixedSize(true);
        recycler_View_Collage.setLayoutManager(recycler_LayoutManager_Collage);

        // recycler view - 스티커
        recycler_LayoutManager_Sticker = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recycler_View_Sticker = (RecyclerView) findViewById(R.id.id_sticker_select);
        recycler_View_Sticker.setHasFixedSize(true);
        recycler_View_Sticker.setLayoutManager(recycler_LayoutManager_Sticker);

        // item 추가 - 필터
        list_Filter = new ArrayList<>();
        list_Filter.add(new ItemListFilter(R.drawable.filter00 , "0번"));
        list_Filter.add(new ItemListFilter(R.drawable.filter01 , "1번"));
        list_Filter.add(new ItemListFilter(R.drawable.filter02 , "2번"));
        list_Filter.add(new ItemListFilter(R.drawable.filter03 , "3번"));
        list_Filter.add(new ItemListFilter(R.drawable.filter04 , "4번"));
        list_Filter.add(new ItemListFilter(R.drawable.filter05 , "5번"));
        list_Filter.add(new ItemListFilter(R.drawable.filter06 , "6번"));
        list_Filter.add(new ItemListFilter(R.drawable.filter07 , "7번"));
        list_Filter.add(new ItemListFilter(R.drawable.filter08 , "8번"));
        list_Filter.add(new ItemListFilter(R.drawable.filter09 , "9번"));
        list_Filter.add(new ItemListFilter(R.drawable.filter10 , "10번"));

        // item 추가 - 콜라주
        list_Collage = new ArrayList<>();
        list_Collage.add(new ItemListCollage(R.drawable.img_collage_00));
        list_Collage.add(new ItemListCollage(R.drawable.img_collage_01));
        list_Collage.add(new ItemListCollage(R.drawable.img_collage_02));

        // item 추가 - 스티커
        list_Sticker = new ArrayList<>();
        list_Sticker.add(new ItemListSticker(R.drawable.line1));
        list_Sticker.add(new ItemListSticker(R.drawable.line2));
        list_Sticker.add(new ItemListSticker(R.drawable.line3));
        list_Sticker.add(new ItemListSticker(R.drawable.line4));


        // Adapter 적용 - 필터
        RecyclerAdapterFilter mRecyclerAdapterFilter = new RecyclerAdapterFilter(MainActivity.this , list_Filter, R.layout.item_list_filter);
        recycler_View_Filter.setAdapter(mRecyclerAdapterFilter);

        // Adapter 적용 - 콜라주
        RecyclerAdapterCollage mRecyclerAdapterCollage = new RecyclerAdapterCollage(MainActivity.this , list_Collage, R.layout.item_list_collage);
        recycler_View_Collage.setAdapter(mRecyclerAdapterCollage);

        // Adapter 적용 - 스티커
        RecyclerAdapterSticker mRecyclerAdapterSticker = new RecyclerAdapterSticker(MainActivity.this , list_Sticker, R.layout.item_list_sticker);
        recycler_View_Sticker.setAdapter(mRecyclerAdapterSticker);

        // filter recycler view 클릭에 따른 이벤트
        mRecyclerAdapterFilter.setItemClick(new RecyclerAdapterFilter.ItemClick() {
            @Override
            public void onClick(int position) {
                switch (position){
                    case 1: renderer.setSelectedFilter(R.id.filter1); break;
                    case 2: renderer.setSelectedFilter(R.id.filter2); break;
                    case 3: renderer.setSelectedFilter(R.id.filter3); break;
                    case 4: renderer.setSelectedFilter(R.id.filter4); break;
                    case 5: renderer.setSelectedFilter(R.id.filter5); break;
                    case 6: renderer.setSelectedFilter(R.id.filter6); break;
                    case 7: renderer.setSelectedFilter(R.id.filter7); break;
                    case 8: renderer.setSelectedFilter(R.id.filter8); break;
                    case 9: renderer.setSelectedFilter(R.id.filter9); break;
                    case 10: renderer.setSelectedFilter(R.id.filter10); break;
                    default: break;
                }
            }
        });

        // collage recycler view 클릭에 따른 이벤트
        mRecyclerAdapterCollage.setItemClick(new RecyclerAdapterCollage.ItemClick() {
            @Override
            public void onClick(int position) {
                switch (position){
                    case 0:
                        mode = NiceValue.CAPTURE_NORMAL;
                        collageNumber = 0;
                        NiceValue.numberOfCollageCapture = 0;
                        Toast.makeText(getApplicationContext(), "일반촬영모드", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        mode = NiceValue.CAPTURE_COLLAGE;
                        Log.e("Collage mode?"," "+mode);
                        collageNumber = 1;
                        NiceValue.numberOfCollageCapture = NiceValue.NUMBER_OF_CAPTURE_COLLAGE_01;
                        Toast.makeText(getApplicationContext(), "콜라주1 모드", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        mode = NiceValue.CAPTURE_COLLAGE;
                        Log.e("Collage mode?"," "+mode);
                        collageNumber = 2;
                        NiceValue.numberOfCollageCapture = NiceValue.NUMBER_OF_CAPTURE_COLLAGE_01;
                        Toast.makeText(getApplicationContext(), "콜라주2 모드", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        });

        // sticker recycler view 클릭에 따른 이벤트
        mRecyclerAdapterSticker.setItemClick(new RecyclerAdapterSticker.ItemClick() {
            @Override
            public void onClick(int position) {
                stickerNum=position+1;
                newStickeron=true;
            }
        });

        // 스위치 설정
        switchOfGif = (Switch)findViewById(R.id.id_switch_gif);
        switchOfGif.setChecked(false);
        switchOfGif.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    mode = CAPTURE_GIF;
                    Toast.makeText(getApplicationContext(), "움짤모드 ON", Toast.LENGTH_SHORT).show();

                    mHandler.removeCallbacksAndMessages(null); // 핸들러 콜백이벤트 및 메세지 제거
                    mHandler = null;
                    mHandler = new TimerHandler();
                    ImageView timerImage = (ImageView) findViewById(R.id.id_icon_timertext);
                    timerImage.setVisibility(View.GONE);

                    btTimer.setEnabled(true);
                    capture.setEnabled(true);

                    for (int i = 0; i < gifCount; i++) {
                        bitmapListGif.get(i).recycle();
                    }
                    bitmapListGif.clear();
                    gifCount = 0; // gifCount를 전역변수로 해서 save스레드에서 o으로 바꿔주는게 확실함 여기 말고 save호출부분에서

                }else{
                    mode = CAPTURE_NORMAL;
                    Toast.makeText(getApplicationContext(), "움짤모드 OFF", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    @Override
    public void onAnimationStart(Animation animation) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
        // TODO Auto-generated method stub
    }

    private class TimerHandler extends Handler
    {
        public void handleMessage(Message msg) {
            synchronized (timerSync) {
                int timertime = msg.arg1;
                int viewId = msg.arg2;

                final ImageView timerImage = (ImageView) findViewById(viewId);
                timerImage.setVisibility(View.GONE);
                Log.e("message arg: ", " " + msg.arg1);
                //mBtnAni.setBackgroundResource(timerImage[mCount]);

                if(mode == CAPTURE_NORMAL) {
                    // 타이머기능일때
                    if (msg.arg1 != 0) {
                        AnimationSet timerAniSet;
                        SetTimerImage(timertime, timerImage); // 시간에 맞는 이미지 설정
                        timerImage.setVisibility(View.VISIBLE); // 뷰 VISIBLE
                        timerAniSet = SetTimerAnimation(1); // 애니메이션 효과 선택
                        //timerImage.startAnimation(timerAniSet); // 애니메이션 시작
                        timerImage.setAnimation(timerAniSet); // 애니메이션 시작
                    }
                }
                else // 움짤 연속 촬영일때
                {
                    int i = gifGoalCount - timertime;
                    bitmapListGif.add(i, textureView.getBitmap(200, 300));
                    gifCount++;
                }

                int nextCount = timertime - 1;
                Message message = new Message();
                message.arg1 = nextCount;
                message.arg2 = viewId;

                //mHandler.sendEmptyMessageDelayed(0,1000);
                if(mode == CAPTURE_NORMAL) {
                    if (msg.arg1 != 0)
                        mHandler.sendMessageDelayed(message, 1000);
                    else
                        saveThread = LSYSUtility.Save(MainActivity.this, textureView, saveSync); // 여기내부에 doingCapture = false를 넣으면 Thread가 종료되면서 실행 안될수도 있음?
                }
                else // 움짤 연속 촬영일때 저장
                {
                    if (msg.arg1 != 0)
                        mHandler.sendMessageDelayed(message, gifGap);
                    else {
                        AnimationSet timerAniSet;

                        SetTimerImage(11, timerImage); // 시간에 맞는 이미지 설정

                        timerImage.setVisibility(View.VISIBLE); // 뷰 VISIBLE
                        timerAniSet = SetTimerAnimation(2); // 애니메이션 효과 선택
                        //timerImage.startAnimation(timerAniSet); // 애니메이션 시작
                        timerImage.setAnimation(timerAniSet); // 애니메이션 시작

                        saveThread = LSYSUtility.Save(MainActivity.this, bitmapListGif, gifGoalCount, saveSync, timerAniSet, viewId);
                        // 팅길때 예상: 내부의 set버튼들과 변수초기화, 비우는것들 다 따로 지우기(처리)
                        // 초기화
                        gifCount = 0; //???
                    }
                }

            }
        }
    }

}
