package com.example.administrator.lsys_camera;


import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class MiniSticker extends AbstractSticker {



    float SQUARE_COORDS[];
    public FloatBuffer VERTEX_BUF, TEXTURE_COORD_BUF;
    int ORIGIN_PROGRAM = 0;

    private static final int BUF_ACTIVE_TEX_UNIT = GLES20.GL_TEXTURE8;
    private static FrameBuffer CAMERA_RENDER_BUF;

    private FloatBuffer ROATED_TEXTURE_COORD_BUF;
    int[] textTexId;
    int[] stickerTexId;
    int miniType;//무슨 아이콘인지? 2=회전,3=좌우 반전,0=확인, 4=삭제
    Context context;

    public MiniSticker(Context context, FloatBuffer vertexBuf, FloatBuffer roatedTextureCoordBuf, int type)
    {
        textTexId = new int[1];
        stickerTexId = new int[1];
        SQUARE_COORDS=new float[8];

        miniType=type;
        if (VERTEX_BUF == null) {
            VERTEX_BUF = ByteBuffer.allocateDirect(SQUARE_COORDS.length * 4)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            VERTEX_BUF.put(SQUARE_COORDS);
            VERTEX_BUF.position(0);
        }
        ROATED_TEXTURE_COORD_BUF=roatedTextureCoordBuf;


        ORIGIN_PROGRAM = LSYSUtility.buildProgram(context, R.raw.vertext, R.raw.original);

        this.context = context;


    }


    void draw(int canvasWidth, int canvasHeight)
    {
        Bitmap textBitmap;

        GLES20.glUseProgram(ORIGIN_PROGRAM);

        // Use shaders

        if(miniType==4)
            stickerTexId[0]= LSYSUtility.loadStickerTexture(context,R.drawable.stickerx);
        else if(miniType==3)
            stickerTexId[0]= LSYSUtility.loadStickerTexture(context,R.drawable.iconlr);
        else if (miniType==5)
            stickerTexId[0]= LSYSUtility.loadStickerTexture(context,R.drawable.stickersize);
        else
            stickerTexId[0] = LSYSUtility.loadStickerTexture(context, R.drawable.stickerrotate);


        int vPositionLocation = GLES20.glGetAttribLocation(ORIGIN_PROGRAM, "vPosition");
        GLES20.glEnableVertexAttribArray(vPositionLocation);
        GLES20.glVertexAttribPointer(vPositionLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, VERTEX_BUF);

/*
            int vTexCoordLocation = GLES20.glGetAttribLocation(ORIGIN_PROGRAM, "vTexCoord");
            GLES20.glEnableVertexAttribArray(vTexCoordLocation);
            GLES20.glVertexAttribPointer(vTexCoordLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, ROATED_TEXTURE_COORD_BUF);
*/
        // Render to texture

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDeleteTextures(1, stickerTexId, 0);

    }

    boolean check(float touchposX,float touchposY)
    {

        if(SQUARE_COORDS[0]>touchposX && SQUARE_COORDS[2]<touchposX&&SQUARE_COORDS[1]<touchposY&&SQUARE_COORDS[5]>touchposY) {
            return true;
        }
        return false;
    }
    void move(float standardX, float standardY)
    {

        SQUARE_COORDS[0]=standardX+0.1f;
        SQUARE_COORDS[1]=standardY-0.1f;
        SQUARE_COORDS[2]=standardX-0.1f;
        SQUARE_COORDS[3]=SQUARE_COORDS[1];
        SQUARE_COORDS[4]=SQUARE_COORDS[0];
        SQUARE_COORDS[5]=standardY+0.1f;
        SQUARE_COORDS[6]=SQUARE_COORDS[2];
        SQUARE_COORDS[7]=SQUARE_COORDS[5];


        VERTEX_BUF = ByteBuffer.allocateDirect(SQUARE_COORDS.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        VERTEX_BUF.put(SQUARE_COORDS);
        VERTEX_BUF.position(0);

    }




}
