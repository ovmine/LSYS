package com.example.administrator.lsys_camera;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

/**
 * Created by shin on 2017-04-23.
 */

public class showSelected extends AppCompatActivity {

    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //ImageView showView = ;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selceted_image);

        imageView=(ImageView)findViewById(R.id.showImage);

        Intent receiveIntent=getIntent();
        String fullPath=receiveIntent.getStringExtra("ImageFullPath");
        Glide.with(this).load(fullPath).into(imageView);

        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);


    }


}
