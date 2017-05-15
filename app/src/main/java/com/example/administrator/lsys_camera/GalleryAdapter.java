package com.example.administrator.lsys_camera;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;

/**
 * Created by shin on 2017-04-21.
 */

public class GalleryAdapter extends BaseAdapter{
    int CustomGalleryItemBg;
    String mBasePath;
    Context mContext;
    String[] mImgs;
    Bitmap bm;
    DataSetObservable mDataSetObservable = new DataSetObservable(); // DataSetObservable(DataSetObserver)의 생성

    public String TAG = "Gallery Adapter Example :: ";

    public GalleryAdapter(Context context, String basepath){
        this.mContext = context;
        this.mBasePath = basepath;

        File file = new File(mBasePath);
        if(!file.exists()){
            if(!file.mkdirs()){
                Log.e("","here");
            }
        }
        mImgs = file.list();

        TypedArray array = mContext.obtainStyledAttributes(R.styleable.GalleryThema);
        CustomGalleryItemBg = array.getResourceId(0, R.styleable.GalleryThema_android_galleryItemBackground);
        array.recycle();
    }

    @Override
    public int getCount() {
        File dir = new File(mBasePath);
        mImgs = dir.list();
        return mImgs.length;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    // Adapter 내 Item에서 직접 주소를 받아오도록 method 추가.
    public String getItemPath(int position){
        String path = mBasePath + File.separator + mImgs[position];
        return path;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
        } else {
            imageView = (ImageView) convertView;
        }
        String fileFullPath=mBasePath + File.separator + mImgs[position];
        imageView.setPadding(8, 8, 8, 8);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT, GridView.LayoutParams.MATCH_PARENT));
        Glide.with(mContext).load(fileFullPath).override(300,300).into(imageView);
        return imageView;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer){ // DataSetObserver의 등록(연결)
        mDataSetObservable.registerObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer){ // DataSetObserver의 해제
        mDataSetObservable.unregisterObserver(observer);
    }

    @Override
    public void notifyDataSetChanged(){ // 위에서 연결된 DataSetObserver를 통한 변경 확인
        mDataSetObservable.notifyChanged();
    }
}

