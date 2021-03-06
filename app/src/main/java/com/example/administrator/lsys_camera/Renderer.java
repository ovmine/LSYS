package com.example.administrator.lsys_camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.TextureView;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.administrator.lsys_camera.filter.AsciiArtFilter;
import com.example.administrator.lsys_camera.filter.BasicDeformFilter;
import com.example.administrator.lsys_camera.filter.BlueorangeFilter;
import com.example.administrator.lsys_camera.filter.ChromaticAberrationFilter;
import com.example.administrator.lsys_camera.filter.ContrastFilter;
import com.example.administrator.lsys_camera.filter.CrackedFilter;
import com.example.administrator.lsys_camera.filter.CrosshatchFilter;
import com.example.administrator.lsys_camera.filter.EMInterferenceFilter;
import com.example.administrator.lsys_camera.filter.EdgeDetectionFilter;
import com.example.administrator.lsys_camera.filter.Filter;
//import com.example.administrator.lsys_camera.filter.GausianFilter;
import com.example.administrator.lsys_camera.filter.JFAVoronoiFilter;
import com.example.administrator.lsys_camera.filter.LegofiedFilter;
import com.example.administrator.lsys_camera.filter.LichtensteinEsqueFilter;
import com.example.administrator.lsys_camera.filter.MappingFilter;
import com.example.administrator.lsys_camera.filter.MoneyFilter;
import com.example.administrator.lsys_camera.filter.NoiseWarpFilter;
import com.example.administrator.lsys_camera.filter.OriginalFilter;
import com.example.administrator.lsys_camera.filter.PixelizeFilter;
import com.example.administrator.lsys_camera.filter.PolygonizationFilter;
import com.example.administrator.lsys_camera.filter.RefractionFilter;
import com.example.administrator.lsys_camera.filter.TileMosaicFilter;
import com.example.administrator.lsys_camera.filter.TrianglesMosaicFilter;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

import static android.content.Context.SENSOR_SERVICE;
import static javax.microedition.khronos.egl.EGL10.EGL_NONE;
import static javax.microedition.khronos.egl.EGL10.EGL_NO_CONTEXT;


//TESTEST
public class Renderer  implements Runnable, TextureView.SurfaceTextureListener {

    private static final int EGL_WINDOW_BIT = 4;
    private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
    private static final int DRAW_INTERVAL = 1000 / 30;
    public static int control = 1;

    private int width, height;
    private EGLDisplay eglDisplay;
    private EGLSurface eglSurface;
    private EGLContext eglContext;
    private EGL10 egl10;

    private Camera camera;
    private SurfaceTexture cameraSurfaceTexture;
    private int cameraTextureId;
    public Thread renderThread;
    private Context context;
    private SurfaceTexture mainSurfaceTexture;
    private Filter selectedFilter;
    private int selectedFilterId = R.id.filter0;
    private SparseArray<Filter> cameraFilterMap = new SparseArray<>();

    public boolean isFrontCamera = false;
    private boolean isFlash = false;
    private Camera.Parameters params;

    public SensorManager sensorManager;
    public OrientationListener oriListener;
    public Sensor oriSensor;
    private ImageView btChange;
    private ImageView btCapture;
    private ImageView btFlash;
    private ImageView btTimer;
    private Handler mHandler = new Handler();
    //private  StickerPro sticker;

    private Processing processing;
    private Object cameraUsed;
    private Thread saveThread;

    private float percentage=1;
//    private Object drawObject;

    public ArrayList<StickerPro> stickerArry;

    // MainActivity의 요소들이 필요하므로 Context를 받아옴
    public Renderer(Context context, Object cameraUsed,Thread saveThread) {
        this.context = context;
        this.cameraUsed = cameraUsed;
        this.saveThread = saveThread;
        // this.drawObject = drawObject;
    }

    // SurfaceTuexture가 이용 가능하면 해당 함수 자동 실행,
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        // 서피스텍스쳐 이용가능하면 카메라 오픈하고, 프리뷰 렌더링 스레드 실행

        if (renderThread != null && renderThread.isAlive()) {
            // 딴거 하다  다시 사진 찍을때 새로 쓰레드 생성
            renderThread.interrupt();
        }
        this.width = width;
        this.height = height;
        percentage=(float)width/(float)height;
        renderThread = new Thread(this);

        mainSurfaceTexture = surface;

        Processing.SetRotateScreen(0);
        StartCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
        stickerArry= new ArrayList<StickerPro>();
//
//        // 카메라정보. 카메라 데이터를 어떤식으로 받아드릴지 결정
//        processing = new Processing(context);
//
//        // 스티커 객체 생성
//        sticker = new StickerPro(context);
//
//        // 카메라 프리뷰 Start
//        try {
//            //   camera.setPreviewTexture(cameraSurfaceTexture);
//            //   camera.startPreview();
//
//            // For 오토포커싱
//            // 센서 이벤트가  camera객체를 필요하므로 camera객체 생성뒤에  초기화
//            // 센서매니저에서 객체를얻어와서 방향센서설정 및 방향센서 리스너 생성
//            sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);    // SensorManager 인스턴스를 가져옴
//            oriSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);    // 방향 센서
//            oriListener = new OrientationListener(camera);        // 방향 센서 리스너 인스턴스
//            sensorManager.registerListener(oriListener, oriSensor, SensorManager.SENSOR_DELAY_NORMAL);    // 방향 센서 리스너 오브젝트를 등록
//
//
//        } catch (Exception e) {
//            // Something bad happened
//            Log.e("before Thread"," error happend!!!");
//        }

        // Start rendering
        renderThread.start();
    }

    // SurfaceTexture가 사용불가능하면 해당함수 실행. destroy
    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {

        synchronized (cameraUsed) {
            if (camera != null) {
                camera.stopPreview();
                camera.release();
                camera = null;
                Log.e("camera", " release");
            }
            sensorManager.unregisterListener(oriListener);    // unregister acceleration listener

            if(saveThread != null && saveThread.isAlive()){
                saveThread.interrupt();
            }

            if (renderThread != null && renderThread.isAlive()) {
                renderThread.interrupt();
            }

            processing.release();

            while(stickerArry.size()!=0)
            {
                stickerArry.get(0).release();
                stickerArry.remove(0);
            }
            MainActivity.checked=false;
            MainActivity.newStickeron=false;
            MainActivity.textureviewTouch=false;
            MainActivity.TextureviewClicked=false;

            Log.e("SurfaceTextureDestroyed", " happen!");
        }
        return true;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    private void setMaxTextureSize() {

        //egl10 = (EGL10) EGLContext.getEGL();
        EGLContext ctx = egl10.eglGetCurrentContext();
        GL10 gl = (GL10) ctx.getGL();
        IntBuffer val = IntBuffer.allocate(1);
        gl.glGetIntegerv(GL10.GL_MAX_TEXTURE_SIZE, val);
        gl.glGetIntegerv(GL10.GL_MAX_TEXTURE_SIZE, val);

        int size = val.get(0); // 최대 크기 구함
        MainActivity.TEX_MAX_SIZE =size;
        Log.e("GL_MAX_TEXURE_SIZE: ", "  "+size);
        //Constants.setMaxTextureSize(size); // Constants는 글로벌 변수 저장용
    }

    // 받은 id를 위치한 필터의 객체를 얻는다.
    public void setSelectedFilter(int id) {
        selectedFilterId = id;
        selectedFilter = cameraFilterMap.get(id);
        if (selectedFilter != null)
            selectedFilter.onAttach();
    }

    // 카메라 객체 얻어오기
    public Camera getCamera()
    {
        if(camera != null)
            return camera;
        else
            return null;
    }

    // 카메라 열기
    private Camera OpenCamera(int facing) {
        int cameraCount;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int cameraId = 0; cameraId < cameraCount; cameraId++) {
            Camera.getCameraInfo(cameraId, cameraInfo);
            if (cameraInfo.facing == facing) {
                // 전면카메라일때
                try {
                    cam = Camera.open(cameraId);
                    break;
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }
        return cam;
    }

    private void StartCamera(int facing) {
        camera = OpenCamera(facing);
        if (camera == null) {
            camera = Camera.open();
            isFrontCamera = false;
        }
        // 새롭게 SurfaceTexture 생성

        // Texture 바인딩
        cameraTextureId = LSYSUtility.genTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);

        // 바인딩으로 얻은 Texture를 이용하여 SurfaceTexture 생성
        cameraSurfaceTexture = new SurfaceTexture(cameraTextureId);

        try {
            // 프리뷰 설정
            camera.setPreviewTexture(cameraSurfaceTexture);
            camera.startPreview();
        } catch (IOException e) {
            // Something bad happened
        }
    }

    // 카메라 전후방 변환
    private void ChangeCamera()
    {
        synchronized (cameraUsed) {
            CloseCamera();
            Log.e("close"," camera Func");
            isFrontCamera = !isFrontCamera;

            if (isFrontCamera) {
                StartCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
                Processing.SetRotateScreen(2);
            } else {
                StartCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
                Processing.SetRotateScreen(0);
            }
            oriListener.SetCamera(camera);

            OrientationListener.mAutoFocus = true; // mAutoFocus 초기화
            MainActivity.pushChangeBtn = false; // 케메라 전환 완료
            MainActivity.pushFlashBtn = false; // 플래쉬 버튼 초기화
            Log.e("change","camera End");


            mHandler.post(new Runnable() {
                //            @Override
                public void run() {
                    AppCompatActivity activity = (AppCompatActivity)context;
                    btCapture = (ImageView)activity.findViewById(R.id.id_icon_circle);
                    btCapture.setEnabled(true);
                    btChange = (ImageView)activity.findViewById(R.id.id_icon_change);
                    btChange.setEnabled(true);
                    btFlash = (ImageView)activity.findViewById(R.id.id_icon_flash);
                    btFlash.setEnabled(true);
                    btTimer = (ImageView)activity.findViewById(R.id.id_icon_timer);
                    btTimer.setEnabled(true);
                }
            });
        }
    }

    public void FlashOn()
    {
        if(camera != null) {
            params = camera.getParameters(); // open된 카메라의 설정값을 받아옴
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH); //플래쉬 모드에서 플래쉬를 켠다
            camera.setParameters(params); // 설정한(open)한 카메라에 설정값을 저장
        }
    }

    public void FlashOff()
    {
        if(camera != null) {
            params = camera.getParameters(); // open된 카메라의 설정값을 받아옴
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF); //플래쉬 모드에서 플래쉬를 끈다
            camera.setParameters(params); // 설정한(open)한 카메라에 설정값을 저장
        }
    }

    private void CloseCamera()
    {
        if(camera!=null){
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    public void run() {
        // initEGL();
        initializeEGL(mainSurfaceTexture);
        setMaxTextureSize();

        cameraFilterMap.append(R.id.filter0, new OriginalFilter(context));
        cameraFilterMap.append(R.id.filter1, new EdgeDetectionFilter(context));
        cameraFilterMap.append(R.id.filter2, new PixelizeFilter(context));
        cameraFilterMap.append(R.id.filter3, new EMInterferenceFilter(context));
        cameraFilterMap.append(R.id.filter4, new TrianglesMosaicFilter(context));
        cameraFilterMap.append(R.id.filter5, new LegofiedFilter(context));
        cameraFilterMap.append(R.id.filter6, new TileMosaicFilter(context));
        cameraFilterMap.append(R.id.filter7, new BlueorangeFilter(context));
        cameraFilterMap.append(R.id.filter8, new ChromaticAberrationFilter(context));
        cameraFilterMap.append(R.id.filter9, new BasicDeformFilter(context));
        cameraFilterMap.append(R.id.filter10, new ContrastFilter(context));
        cameraFilterMap.append(R.id.filter11, new NoiseWarpFilter(context));
        cameraFilterMap.append(R.id.filter12, new RefractionFilter(context));
        cameraFilterMap.append(R.id.filter13, new MappingFilter(context));
        cameraFilterMap.append(R.id.filter14, new CrosshatchFilter(context));
        cameraFilterMap.append(R.id.filter15, new LichtensteinEsqueFilter(context));
        cameraFilterMap.append(R.id.filter16, new AsciiArtFilter(context));
        cameraFilterMap.append(R.id.filter17, new MoneyFilter(context));
        cameraFilterMap.append(R.id.filter18, new CrackedFilter(context));
        cameraFilterMap.append(R.id.filter19, new PolygonizationFilter(context));
        cameraFilterMap.append(R.id.filter20, new JFAVoronoiFilter(context));
        //cameraFilterMap.append(R.id.filter21, new GausianFilter(context));

        // 받은 id를 textureView에 연결
        setSelectedFilter(selectedFilterId);

        // 카메라정보. 카메라 데이터를 어떤식으로 받아드릴지 결정
        processing = new Processing(context);

        // 스티커 객체 생성

        // 카메라 프리뷰 Start
        try {
            //   camera.setPreviewTexture(cameraSurfaceTexture);
            //   camera.startPreview();

            // For 오토포커싱
            // 센서 이벤트가  camera객체를 필요하므로 camera객체 생성뒤에  초기화
            // 센서매니저에서 객체를얻어와서 방향센서설정 및 방향센서 리스너 생성
            sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);    // SensorManager 인스턴스를 가져옴
            oriSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);    // 방향 센서
            oriListener = new OrientationListener(camera);        // 방향 센서 리스너 인스턴스
            sensorManager.registerListener(oriListener, oriSensor, SensorManager.SENSOR_DELAY_NORMAL);    // 방향 센서 리스너 오브젝트를 등록


        } catch (Exception e) {
            // Something bad happened
            Log.e("in Start"," error happend!!!");
        }


        // Render loop
        // 현재스레드가 중지중이지 않은 상태
        // 종료시키고싶을때 interrupt 시켜주면 된다
        while (!Thread.currentThread().isInterrupted()) {
            //  synchronized (drawObject)
            //  {
            try {
                // 카메라 전후방 변환시
                if (MainActivity.pushChangeBtn) {
                    ChangeCamera();
                }

                // Buffer Bit Color 비워주기
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

                // Update the camera preview texture
                // onSurfaceTextureUpdate 를 실행시켜줌
                synchronized (this) {
                    cameraSurfaceTexture.updateTexImage();
                }

                if(MainActivity.newStickeron)
                {
                    stickerArry.add(new StickerPro(context,MainActivity.stickerNum,percentage));
                    StickerPro temp = stickerArry.get(stickerArry.size()-1);
                    temp.stickerSwitch=true;
                    /*
                    if(!sticker.stickerSwitch)
                        sticker.stickerSwitch=true;
                    else if(!sticker2.stickerSwitch)
                        sticker2.stickerSwitch=true;
                    else if(!sticker3.stickerSwitch)
                        sticker3.stickerSwitch=true;
                    else if(!sticker4.stickerSwitch)
                        sticker4.stickerSwitch=true;
                    else if(!sticker5.stickerSwitch)
                        sticker5.stickerSwitch=true;
                        */
                    MainActivity.newStickeron=false;
                }

                // Draw camera preview
                // 카메라 프리뷰 그려주기
                processing.draw(cameraTextureId, width, height, selectedFilter, context); //////////////////////////////////////
                //    if (MainActivity.stickerOn || MainActivity.textOn)
                //        sticker.draw(width, height, MainActivity.textureviewTouch);
                int releaseNum=-1;
                for(int i=0;i<stickerArry.size();i++)
                {
                    StickerPro  temp=stickerArry.get(i);
                    temp.draw(width,height);
                    if(!temp.stickerSwitch)
                    {
                        temp.release();
                        releaseNum=i;
                    }
                }

                if(releaseNum!=-1)
                {
                    stickerArry.remove(releaseNum);
                }

                // Flush
                GLES20.glFlush();
                egl10.eglSwapBuffers(eglDisplay, eglSurface);

                //
                Thread.sleep(DRAW_INTERVAL);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            //} // syn
        }

        cameraSurfaceTexture.release();
        GLES20.glDeleteTextures(1, new int[]{cameraTextureId}, 0);
    }


    private void initEGL() {
        egl10 = (EGL10) EGLContext.getEGL();

        eglDisplay = egl10.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        if (eglDisplay == EGL10.EGL_NO_DISPLAY) {
            throw new RuntimeException("eglGetDisplay failed " +
                    android.opengl.GLUtils.getEGLErrorString(egl10.eglGetError()));
        }

        int[] version = new int[2];
        if (!egl10.eglInitialize(eglDisplay, version)) {
            throw new RuntimeException("eglInitialize failed " +
                    android.opengl.GLUtils.getEGLErrorString(egl10.eglGetError()));
        }

        int[] numOfConfigs = new int[1];
        EGLConfig[] configs = new EGLConfig[1];
        EGLConfig eglConfig = null;
        int[] configAttrib = {
                EGL10.EGL_RENDERABLE_TYPE, EGL_WINDOW_BIT,
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_ALPHA_SIZE, 8,
                EGL10.EGL_DEPTH_SIZE, 0,
                EGL10.EGL_STENCIL_SIZE, 0,
                EGL10.EGL_NONE
        };


        if (egl10.eglChooseConfig(eglDisplay, configAttrib, configs, 1, numOfConfigs)) //numOfConfigs로 제대로 선택되었는지 판별. numOfConfigs>0이면 제대로 선택된것
        {                                                                                 //eglChooseConfig는 configAttrib 속성과 일치하는 EGLConfig를 선택합니다.
            if (numOfConfigs[0] > 0)
                eglConfig = configs[0];
        } else {
            throw new IllegalArgumentException("eglChooseConfig failed " + android.opengl.GLUtils.getEGLErrorString(egl10.eglGetError()));
        }

        if (eglConfig == null) {
            throw new RuntimeException("eglConfig not initialized");
        }

        int[] contextAttrib = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE};
        eglContext = egl10.eglCreateContext(eglDisplay, eglConfig, EGL10.EGL_NO_CONTEXT, contextAttrib);
        eglSurface = egl10.eglCreateWindowSurface(eglDisplay, eglConfig, mainSurfaceTexture, null); // null 위치에 전후방 버퍼 선택옵션?

        if (eglSurface == null || eglSurface == EGL10.EGL_NO_SURFACE) {
            int error = egl10.eglGetError();
            if (error == EGL10.EGL_BAD_NATIVE_WINDOW) {
                Log.e("Renderer", "eglCreateWindowSurface returned EGL10.EGL_BAD_NATIVE_WINDOW");
                return;
            }
            throw new RuntimeException("eglCreateWindowSurface failed " +
                    android.opengl.GLUtils.getEGLErrorString(error));
        }

        if (!egl10.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
            throw new RuntimeException("eglMakeCurrent failed " +
                    android.opengl.GLUtils.getEGLErrorString(egl10.eglGetError()));
        }
    }

    // opgles 3.0 프로그래밍 가이드 참조
    private void initializeEGL(Object nativeWindow) {

        egl10 = (EGL10) EGLContext.getEGL();

        int configAttribs[] =
                {
                        EGL10.EGL_RENDERABLE_TYPE, EGL_WINDOW_BIT,
                        EGL10.EGL_RED_SIZE, 8,
                        EGL10.EGL_GREEN_SIZE, 8,
                        EGL10.EGL_BLUE_SIZE, 8,
                        EGL_NONE
                };
        int contextAttribs[] =
                {
                        EGL_CONTEXT_CLIENT_VERSION, 3,
                        EGL_NONE
                };

        //EGLDisplay display = egl10.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        eglDisplay = egl10.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        if(eglDisplay == egl10.EGL_NO_DISPLAY)
        {
            throw new RuntimeException("eglGetDisplay failed " +
                    android.opengl.GLUtils.getEGLErrorString(egl10.eglGetError()));
        }

        int[] majorMinor = new int[2];
        if(!egl10.eglInitialize(eglDisplay,majorMinor))
        {
            throw new RuntimeException("eglInitialize failed " +
                    android.opengl.GLUtils.getEGLErrorString(egl10.eglGetError()));
        }
        EGLConfig[] config = new EGLConfig[1];
        int[] numConfigs = new int[1];
        if( !egl10.eglChooseConfig(eglDisplay,configAttribs,config,1,numConfigs))
        {
            throw new RuntimeException("eglChooseConfig failed " +
                    android.opengl.GLUtils.getEGLErrorString(egl10.eglGetError()));
        }
        //EGLSurface window = egl10.eglCreateWindowSurface(eglDisplay,config[0],nativeWindow,null);
        eglSurface = egl10.eglCreateWindowSurface(eglDisplay,config[0],nativeWindow,null);

        if(eglSurface == egl10.EGL_NO_SURFACE)
        {
            int error = egl10.eglGetError();
            if (error == EGL10.EGL_BAD_NATIVE_WINDOW) {
                Log.e("eglCreateWindowSurface", " EGL_BAD_NATIVE_WINDOW");
                return;
            }
            throw new RuntimeException("eglCreateWindowSurface failed " +
                    android.opengl.GLUtils.getEGLErrorString(error));
        }

        EGLContext context = egl10.eglCreateContext(eglDisplay,config[0],EGL_NO_CONTEXT,contextAttribs);

        if(context == EGL_NO_CONTEXT)
        {
            throw new RuntimeException("eglCreateContext failed " +
                    android.opengl.GLUtils.getEGLErrorString(egl10.eglGetError()));
        }
        if(!egl10.eglMakeCurrent(eglDisplay,eglSurface,eglSurface,context))
        {
            throw new RuntimeException("eglMakeCurrent failed " +
                    android.opengl.GLUtils.getEGLErrorString(egl10.eglGetError()));
        }

    }

}
