package com.example.administrator.lsys_camera;


import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class StickerBound extends AbstractSticker {



    public  FloatBuffer VERTEX_BUF;
    int ORIGIN_PROGRAM = 0;


    private  FloatBuffer ROATED_TEXTURE_COORD_BUF;
    int[] textTexId;
    int[] stickerTexId;
    Context context;
    MiniSticker stickerRotate;
    MiniSticker stickerX;
    MiniSticker stickerSize;
    MiniSticker stickerRL;

    public StickerBound(Context context, FloatBuffer vertexBuf, FloatBuffer roatedTextureCoordBuf,boolean thisBoundChecked)
    {
        textTexId = new int[1];
        stickerTexId = new int[1];

        thisBoundChecked=false;
        VERTEX_BUF=vertexBuf;
        ROATED_TEXTURE_COORD_BUF=roatedTextureCoordBuf;

        stickerRotate=new MiniSticker(context,vertexBuf,roatedTextureCoordBuf,2);
        stickerX=new MiniSticker(context,vertexBuf,roatedTextureCoordBuf,4);
        stickerSize=new MiniSticker(context,vertexBuf,roatedTextureCoordBuf,5);
        stickerRL=new MiniSticker(context,vertexBuf,roatedTextureCoordBuf,3);

        ORIGIN_PROGRAM = LSYSUtility.buildProgram(context, R.raw.vertext, R.raw.original);

        this.context = context;


    }


    void draw(int canvasWidth, int canvasHeight,float SQUARE_COORDS[])
    {

        Bitmap textBitmap;

        GLES20.glUseProgram(ORIGIN_PROGRAM);

        // Use shaders


        stickerTexId[0] = LSYSUtility.loadStickerTexture(context, R.drawable.stickerbound);

        int vPositionLocation = GLES20.glGetAttribLocation(ORIGIN_PROGRAM, "vPosition");
        GLES20.glEnableVertexAttribArray(vPositionLocation);
        GLES20.glVertexAttribPointer(vPositionLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, VERTEX_BUF);



        int vTexCoordLocation = GLES20.glGetAttribLocation(ORIGIN_PROGRAM, "vTexCoord");
        GLES20.glEnableVertexAttribArray(vTexCoordLocation);
        GLES20.glVertexAttribPointer(vTexCoordLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, ROATED_TEXTURE_COORD_BUF);


        // Render to texture

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDeleteTextures(1, stickerTexId, 0);
        stickerRotate.draw(canvasWidth,canvasHeight);
        stickerX.draw(canvasWidth,canvasHeight);
        stickerSize.draw(canvasWidth,canvasHeight);
        stickerRL.draw(canvasWidth,canvasHeight);

    }


    int justcheck(float[] SQUARE_COORDS,StickerPro stickerPro)
    {
        float touchposX=MainActivity.touchPosX;
        float touchposY=MainActivity.touchPosY;

        boolean checkpos=checkPos(touchposX,touchposY,SQUARE_COORDS);
        if(stickerPro.thisBoundChecked) {
            if (stickerX.check(touchposX, touchposY)) {
                MainActivity.checked = false;
                stickerPro.stickerSwitch = false;
                return stickerX.miniType;
            }
            else if (stickerSize.check(touchposX, touchposY)) {
                return stickerSize.miniType;

            }
            else if (stickerRL.check(touchposX, touchposY)) {

                return stickerRL.miniType;
            }
            else if (stickerRotate.check(touchposX, touchposY)) {

                return stickerRotate.miniType;
            }
            else if (checkpos) {
                return 1;
            }
            else
            {
                MainActivity.checked = false;
                stickerPro.thisBoundChecked=false;
                return 0;
            }
        }
        else if(!MainActivity.checked)
        {
            if (checkpos) {
                stickerPro.thisBoundChecked = true;
                MainActivity.checked = true;
                return 1;
            }
        }
        return  0;
    }
    void moveBound( float[] SQUARE_COORDS)
    {
        VERTEX_BUF = ByteBuffer.allocateDirect(SQUARE_COORDS.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        VERTEX_BUF.put(SQUARE_COORDS);

        VERTEX_BUF.position(0);
        stickerRotate.move(SQUARE_COORDS[0],SQUARE_COORDS[1]);
        stickerSize.move(SQUARE_COORDS[2],SQUARE_COORDS[3]);
        stickerX.move(SQUARE_COORDS[4],SQUARE_COORDS[5]);
        stickerRL.move(SQUARE_COORDS[6],SQUARE_COORDS[7]);

    }

    public void release() {
        stickerRotate.release();
        stickerSize.release();
        stickerX.release();
        stickerRL.release();
        ORIGIN_PROGRAM = 0;
    }

    boolean checkPos(float x, float y,float[] SQUARE_COORDS)
    {
        float slopeHor;
        float slopeVer;
        float horBot,horTop;
        float verLef,verRig;
        float horizon,vertical;

        Log.e("0","co");

        if(SQUARE_COORDS[0]!=SQUARE_COORDS[2])
        {
            slopeHor=(SQUARE_COORDS[3]-SQUARE_COORDS[1])/(SQUARE_COORDS[2]-SQUARE_COORDS[0]);

            horBot=SQUARE_COORDS[1]-slopeHor*SQUARE_COORDS[0];

            horTop=SQUARE_COORDS[5]-slopeHor*SQUARE_COORDS[4];
            horizon=y-x*slopeHor;

            if(horTop > horizon && horBot < horizon || horTop< horizon && horBot > horizon)
            {
                if(SQUARE_COORDS[4]!=SQUARE_COORDS[0]) {
                    slopeVer = (SQUARE_COORDS[5] - SQUARE_COORDS[1]) / (SQUARE_COORDS[4] - SQUARE_COORDS[0]);
                    verRig = SQUARE_COORDS[1] - slopeVer * SQUARE_COORDS[0];

                    verLef = SQUARE_COORDS[3] - slopeVer * SQUARE_COORDS[2];

                    vertical = y - x * slopeVer;

                    if (verLef > vertical && verRig < vertical || verLef < vertical && verRig > vertical)
                        return true;
                }
                else
                {
                    if (SQUARE_COORDS[0] > x && SQUARE_COORDS[2] < x || SQUARE_COORDS[0] < x && SQUARE_COORDS[2] > x)
                        return true;
                }
            }
        }
        else
        {
            if (SQUARE_COORDS[1] < y && SQUARE_COORDS[5] > y || SQUARE_COORDS[1] > y && SQUARE_COORDS[5] < y)
            {
                if(SQUARE_COORDS[4]!=SQUARE_COORDS[0]) {
                    slopeVer = (SQUARE_COORDS[5] - SQUARE_COORDS[1]) / (SQUARE_COORDS[4] - SQUARE_COORDS[0]);
                    verRig = SQUARE_COORDS[1] - slopeVer * SQUARE_COORDS[0];

                    verLef = SQUARE_COORDS[3] - slopeVer * SQUARE_COORDS[2];

                    vertical = y - x * slopeVer;
                    if (verLef > vertical && verRig < vertical || verLef < vertical && verRig > vertical)
                        return true;
                }
                else
                {
                    if(SQUARE_COORDS[0] > x && SQUARE_COORDS[2] < x || SQUARE_COORDS[0] < x && SQUARE_COORDS[2] > x)
                        return true;
                }
            }
        }
        return false;

    }



}
