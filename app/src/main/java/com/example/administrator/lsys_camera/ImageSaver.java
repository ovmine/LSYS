package com.example.administrator.lsys_camera;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public  class ImageSaver implements Runnable {

    // The JPEG image
    private  Bitmap imageBitmap;

    private  File mFile;
    private  String mPath;
    private  TextureView textureView;

    // private  Bitmap bitmap;
    private static final String FileName = "LSYS";
    private Context context;
    private String type;
    private byte[] gif;
    private ArrayList<Bitmap> bitmapList;

    private ByteArrayOutputStream bos;
    private AnimatedGifEncoder encoder;
    private int gifGoalCount;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Object saveObject;
    private ImageView btChange, btFlash, btTimer, btCapture, collageButton;
    private Animation animation;
    private int gifTextId;


    // 일반촬영 Saver
    public ImageSaver(Context context, TextureView textureView,Object saveObject) {
        this.context = context;
        this.textureView = textureView;
        this.imageBitmap = textureView.getBitmap();
        this.type = "jpg";
        this.saveObject =saveObject;
    }

    // GIF Saver
    public ImageSaver(Context context, ArrayList<Bitmap> bitmapList, int gifGoalCount, Object saveObject, Animation animation, int gifTextId) {
        this.context = context;
        this.bitmapList = bitmapList;
        encoder = new AnimatedGifEncoder();
        this.gifGoalCount = gifGoalCount;
        this.type = "gif";
        this.saveObject =saveObject;
        this.animation = animation;
        this.gifTextId = gifTextId;
    }

    // Collage Saver
    public ImageSaver(Context context , TextureView textureView, Bitmap bitmap ,Object saveObject) {
        this.context = context;
        this.textureView = textureView;
        this.imageBitmap = bitmap;
        this.type = "jpg";
        this.saveObject =saveObject;
    }

    // 저장하여 갤러리에 반영시키기
    public void addImageToGallery(final String filePath, final Context context) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/"+type);
        values.put(MediaStore.MediaColumns.DATA, filePath);
        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    @Override
    public void run()
    {
        synchronized (saveObject) {
            //  if(type.equals("jpg"))
            //      imageBitmap = textureView.getBitmap();

            Date date = new Date();
            // miliSecond까지 해야 사진이 겹치지않는다
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd_hh_mm_ss_SSS");
            String timeStr = dateFormat.format(date);
            String dirStr = Environment.getExternalStorageDirectory().toString() + "/" + FileName;

            File dir = new File(dirStr);
            //해당 디렉토리의 존재여부를 확인
            if (!dir.exists()) {
                //없다면 생성
                dir.mkdirs();
            }

            if (type.equals("jpg"))
                mPath = dirStr + "/" + timeStr + ".jpg";
            else if (type.equals("gif"))
                mPath = dirStr + "/" + timeStr + ".gif";
            OutputStream fileOutputStream = null;

            try {
                mFile = new File(mPath);
                fileOutputStream = new FileOutputStream(mFile);

                // 비트맵을 파일스트림을이용하여 JPEG 형태로 보냄
                if (type.equals("jpg") && imageBitmap != null) {
                    // synchronized (drawObject) {
                    //imageBitmap = textureView.getBitmap();
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 99, fileOutputStream);
                    Log.e("jpgjpg", "yes");
                    //  }
                } else if (type.equals("gif")) {

                    encoder.start(fileOutputStream);
                    encoder.setDelay(100); //변경가능, 나중에 설정만 생성자 인자로 받아서 바꾸면 될듯?
                    encoder.setRepeat(0);

                    //encoder.setSize(60,60); //이러면 원래 사진에서 잘림. 미리 사이즈를 줄여야할듯
                    //encoder.setFrameRate(20);
                    for (int i = 0; i < gifGoalCount; i++) {
                        // mBitmap = bitmapList[i];
                        encoder.addFrame(bitmapList.get(i));
                    }
                    encoder.finish();

                    for (int i = 0; i < gifGoalCount; i++) {
                        bitmapList.get(i).recycle();
                    }
                    bitmapList.clear();
                    //fileOutputStream.write(gif);
                    //MainActivity.gifSaving =false;
                    Log.e("gifgif", "yes");
                }
                fileOutputStream.flush();
                fileOutputStream.close();


                addImageToGallery(mFile.toString(), context);

                // 저장이 되어야 다음촬영실행
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mHandler.post(new Runnable() {
                    // 찍힌게 완료 됐을 때
                    @Override
                    public void run() {
                        AppCompatActivity activity = (AppCompatActivity) context;

                        //Toast.makeText(context.getApplicationContext(), "complete", Toast.LENGTH_SHORT).show();
                        if(type.equals("jpg")) {
                            AlphaAnimation anim = new AlphaAnimation(1, 0.5f);
                            anim.setDuration(100);        // 에니메이션 동작 주기
                            anim.setRepeatCount(0);    // 에니메이션 반복 회수
                            //anim.setRepeatMode(Animation.REVERSE);// 반복하는 방법
                            textureView.startAnimation(anim);
                        }
                        else // gif일때
                        {
                            animation.cancel();

                          //  gifText.setBackgroundDrawable(null);
                            ImageView gifText = (ImageView) activity.findViewById(gifTextId);
                            gifText.setAnimation(null);
                            gifText.setVisibility(View.GONE);
                        }

                        btCapture = (ImageView) activity.findViewById(R.id.id_icon_circle);
                        btCapture.setEnabled(true);
                        btChange = (ImageView) activity.findViewById(R.id.id_icon_change);
                        btChange.setEnabled(true);
                        btFlash = (ImageView) activity.findViewById(R.id.id_icon_flash);
                        btFlash.setEnabled(true);
                        btTimer = (ImageView) activity.findViewById(R.id.id_icon_timer);
                        btTimer.setEnabled(true);
                        collageButton = (ImageView) activity.findViewById(R.id.id_icon_collage);
                        collageButton.setEnabled(true);

                    }
                });


                if (null != fileOutputStream) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
