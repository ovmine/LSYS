package com.example.administrator.lsys_camera.collage;

import android.graphics.Bitmap;

public class BitmapCrop {
    public static Bitmap cropBitmapForCollage01(Bitmap original) {
        Bitmap result = Bitmap.createBitmap(
                original
                , original.getWidth() / 4       //X 시작위치, 원본의 4/1
                , 0                             //Y 시작위치, 처음부터
                , original.getWidth() / 2       // 넓이, 원본의 2/1
                , original.getHeight());        // 높이, 원본의 크기
        if (result != original) {
            original.recycle();
        }
        return result;
    }

    public static Bitmap cropBitmapForCollage02(Bitmap original) {
        Bitmap result = Bitmap.createBitmap(
                original
                , 0                             //X 시작위치, 처음부터
                , original.getHeight() / 4      //Y 시작위치, 원본의 4/1
                , original.getWidth()           //넓이, 원본의 크기
                , original.getHeight() / 2);    //높이, 원본의 2/1
        if (result != original) {
            original.recycle();
        }
        return result;
    }
}
