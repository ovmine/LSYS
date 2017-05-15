package com.example.administrator.lsys_camera;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import java.io.File;

/**
 * Created by shin on 2017-04-21.
 */

public class photoGallery extends AppCompatActivity {

    public String basePath = null;
    public GridView mGridView;
    public GalleryAdapter mGalleryAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photogallery);

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),"LSYS");

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
            }
        }

        basePath = mediaStorageDir.getPath();

        mGridView = (GridView)findViewById(R.id.gridview); // .xml의 GridView와 연결
        mGalleryAdapter = new GalleryAdapter(this, basePath); // 앞에서 정의한 Custom Image Adapter와 연결
        mGridView.setAdapter(mGalleryAdapter); // GridView가 Custom Image Adapter에서 받은 값을 뿌릴 수 있도록 연결
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getApplicationContext(), mGalleryAdapter.getItemPath(position), Toast.LENGTH_LONG).show();
                Intent imageIntent = new Intent(getApplicationContext(),showSelected.class);
                imageIntent.putExtra("ImageFullPath",mGalleryAdapter.getItemPath(position));
                startActivity(imageIntent);
            }
        });
    }

}


