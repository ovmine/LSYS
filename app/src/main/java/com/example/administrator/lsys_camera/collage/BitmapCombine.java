package com.example.administrator.lsys_camera.collage;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

public class BitmapCombine {
    public static Bitmap combineBitmapForCollage01(Bitmap first, Bitmap second)
    {
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inDither = true;
        option.inPurgeable = true;
        Bitmap bitmap = null;

        // merge 1
        bitmap = Bitmap.createScaledBitmap(
                first,
                first.getWidth()+second.getWidth(),
                first.getHeight(),
                true);

        Paint p = new Paint();
        p.setDither(true);
        p.setFlags(Paint.ANTI_ALIAS_FLAG);
        Canvas c = new Canvas(bitmap);
        c.drawBitmap(first, 0, 0, p);

        // merge 2
        c.drawBitmap(
                second,
                first.getWidth(),
                0,
                p);

        first.recycle();
        second.recycle();

        return bitmap;
    }

    public static Bitmap combineBitmapForCollage02(Bitmap first, Bitmap second)
    {
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inDither = true;
        option.inPurgeable = true;
        Bitmap bitmap = null;

        // merge 1
        bitmap = Bitmap.createScaledBitmap(first, first.getWidth(), first.getHeight()+second.getHeight(), true);

        Paint p = new Paint();
        p.setDither(true);
        p.setFlags(Paint.ANTI_ALIAS_FLAG);
        Canvas c = new Canvas(bitmap);
        c.drawBitmap(first, 0, 0, p);

        // merge 2
        c.drawBitmap(second, 0, first.getHeight(), p);

        first.recycle();
        second.recycle();

        return bitmap;
    }
}
