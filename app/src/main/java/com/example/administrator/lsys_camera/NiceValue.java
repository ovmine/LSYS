package com.example.administrator.lsys_camera;


public class NiceValue {

    // 카메라 촬영 모드 변수
    public static final int CAPTURE_NORMAL = 1;
    public static final int CAPTURE_GIF = 2;
    public static final int CAPTURE_COLLAGE = 3;

    // 콜라주 관련 변수
    public static int numberOfCollageCapture;
    public static final int NUMBER_OF_CAPTURE_COLLAGE_01 = 2;
    public static final int NUMBER_OF_CAPTURE_COLLAGE_02 = 2;

    public static final int COLLAGE01 = 1;
    public static final int COLLAGE02 = 2;
    public static boolean isCollageEnd = false;
    public static int collageStep = 0;
}
