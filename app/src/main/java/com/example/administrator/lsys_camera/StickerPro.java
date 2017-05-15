package com.example.administrator.lsys_camera;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.opengl.GLES20;
import android.opengl.GLUtils;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;

public class StickerPro extends AbstractSticker {

    public boolean moveOn=false;
    public boolean rotateOn=false;
    public boolean sizeOn=false;

    private int stickerNum;
    public boolean stickerSwitch;

    public boolean thisBoundChecked;

    public float perc=1;

    StickerBound stickerBound;

    float size =0.2f;//이미지 회전용

    float disatance =0.2f;//이미지 크기 조절용

    //중심점
    float posX=0.0f;
    float posY=0.0f;

    float cos=1.0f;
    float sin=0.0f;

    float SQUARE_COORDS[] = {
            0.2f, -0.2f,
            -0.2f, -0.2f,
            0.2f, 0.2f,
            -0.2f, 0.2f,
    };

    float BEFORE_MOVE_SQUARE_COORDS[]= {
            0.2f, -0.2f,
            -0.2f, -0.2f,
            0.2f, 0.2f,
            -0.2f, 0.2f,
    };//돌리기 전의 코드
    public FloatBuffer VERTEX_BUF;
    int ORIGIN_PROGRAM = 0;

    public FloatBuffer ROATED_TEXTURE_COORD_BUF;
    int[] textTexId;
    int[] stickerTexId;

    public StickerPro(Context context,int Num,float percent)
    {
        textTexId = new int[1];
        stickerTexId = new int[1];
        stickerNum=Num;
        perc=percent;

        for(int i=0;i<4;i++)
        {
            SQUARE_COORDS[i*2+1]=(BEFORE_MOVE_SQUARE_COORDS[i*2+1])*perc;
        }

        // Setup default Buffers
        if (VERTEX_BUF == null) {
            VERTEX_BUF = ByteBuffer.allocateDirect(SQUARE_COORDS.length * 4)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            VERTEX_BUF.put(SQUARE_COORDS);
            VERTEX_BUF.position(0);
        }

        if (ROATED_TEXTURE_COORD_BUF == null) {
            ROATED_TEXTURE_COORD_BUF = ByteBuffer.allocateDirect(ROATED_TEXTURE_COORDS.length * 4)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            ROATED_TEXTURE_COORD_BUF.put(ROATED_TEXTURE_COORDS);
            ROATED_TEXTURE_COORD_BUF.position(0);
        }


        ORIGIN_PROGRAM = LSYSUtility.buildProgram(context, R.raw.vertext, R.raw.original);

        this.context = context;
        stickerBound= new StickerBound(context, VERTEX_BUF, ROATED_TEXTURE_COORD_BUF,thisBoundChecked);

        stickerSwitch=false;


    }


    void draw(int canvasWidth, int canvasHeight)
    {
        Bitmap textBitmap;


        GLES20.glUseProgram(ORIGIN_PROGRAM);

        if(MainActivity.TextureviewClicked) {
            moveOn=false;
            sizeOn=false;
            rotateOn=false;
            switch (stickerBound.justcheck(SQUARE_COORDS, this)) {
                case 1: {
                    moveOn = true;
                    MainActivity.TextureviewClicked=false;
                    break;
                }
                case 2://size and Rotate
                {
                    rotateOn = true;
                    MainActivity.TextureviewClicked=false;
                    break;
                }
                case 3: {
                    textureLR();
                    MainActivity.TextureviewClicked=false;
                    break;
                }
                case 0: {
                    MainActivity.TextureviewClicked=true;
                    break;
                }
                case 5: {
                    sizeOn=true;
                    MainActivity.TextureviewClicked=false;
                    break;
                }
                case 4: {
                    MainActivity.TextureviewClicked=false;
                    break;
                }
            }
        }

        if(MainActivity.textureviewTouch&&!MainActivity.TextureviewClicked) {


            if(moveOn)
            {
                move();
            }
            if(rotateOn)
            {
                setRotate();
            }
            if(sizeOn)
            {
                setSize();
            }
        }


        if(thisBoundChecked) {
            stickerBound.draw(canvasWidth, canvasHeight,SQUARE_COORDS);

        }

        if(stickerNum==1)
            stickerTexId[0] = LSYSUtility.loadStickerTexture(context,R.drawable.line1);
        else if(stickerNum==2)
            stickerTexId[0] = LSYSUtility.loadStickerTexture(context,R.drawable.line2);
        else if(stickerNum==3)
            stickerTexId[0] = LSYSUtility.loadStickerTexture(context,R.drawable.line3);
        else
            stickerTexId[0] = LSYSUtility.loadStickerTexture(context,R.drawable.line4);

        int vPositionLocation = GLES20.glGetAttribLocation(ORIGIN_PROGRAM, "vPosition");
        GLES20.glEnableVertexAttribArray(vPositionLocation);
        GLES20.glVertexAttribPointer(vPositionLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, VERTEX_BUF);


        int vTexCoordLocation = GLES20.glGetAttribLocation(ORIGIN_PROGRAM, "vTexCoord");
        GLES20.glEnableVertexAttribArray(vTexCoordLocation);
        GLES20.glVertexAttribPointer(vTexCoordLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, ROATED_TEXTURE_COORD_BUF);

        // Render to texture

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDeleteTextures(1, stickerTexId, 0);

    }

    void move()
    {

        float tempX=MainActivity.touchPosX;
        float tempY=MainActivity.touchPosY;

        posX = MainActivity.touchPosX; // virtual center coord
        posY = MainActivity.touchPosY;
        upload();

    }

    void textureLR()
    {
        for(int i=0;i<5;i+=4)
        {
            float temp;
            temp=ROATED_TEXTURE_COORDS[i];
            ROATED_TEXTURE_COORDS[i]=ROATED_TEXTURE_COORDS[i+2];
            ROATED_TEXTURE_COORDS[i+2]=temp;

            ROATED_TEXTURE_COORD_BUF = ByteBuffer.allocateDirect(ROATED_TEXTURE_COORDS.length * 4)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            ROATED_TEXTURE_COORD_BUF.put(ROATED_TEXTURE_COORDS);
            ROATED_TEXTURE_COORD_BUF.position(0);
        }
    }

    void setSize()
    {
        float touchposX=MainActivity.touchPosX; //touch value
        float touchposY=MainActivity.touchPosY;
        //정사각형의 중심점 을 원점으로 옮겼을시의 가상의 touchX,Y값
        float virtualX;
        float virtualY;

        //square= 중심점부터 손가락위치까지의 거리의 제곱 값
        float square;


        virtualX=touchposX-posX; // vector a
        virtualY=touchposY-posY; // vector a
        square=virtualX*virtualX+virtualY*virtualY; // vector a size^2

        if(square!=0) {
            disatance = (float) Math.sqrt(square / 2); // vector a가 사각형의 중점으로부터 우측 꼭지점으로 가는 벡터,
            // vector b는 크기가 a이고 방향이 (1/sqrt(2),-1/sqrt(2))인 벡터
        }

        if(disatance<0.1f)
            disatance=0.1f;

        calcSize(0,cos,sin);
        calcSize(1,cos,sin);
        calcSize(2,cos,sin);
        calcSize(3,cos,sin);


        upload();
    }

    void setRotate()//일단 우측 하단을 기준으로 함.
    {
        //touchX,Y는 유저의 손가락 위치를 나타내는 변수이며 기본적으로 우측 하단을 기준으로 한다.
        float touchposX=MainActivity.touchPosX; //touch value
        float touchposY=MainActivity.touchPosY;
        //정사각형의 중심점 을 원점으로 옮겼을시의 가상의 touchX,Y값
        float virtualX;
        float virtualY;


        //square= 중심점부터 손가락위치까지의 거리의 제곱 값
        float square;


        virtualX=touchposX-posX; // vector a
        virtualY=touchposY-posY; // vector a
        square=virtualX*virtualX+virtualY*virtualY; // vector a size^2

        if(square!=0) {
            size = (float) Math.sqrt(square / 2); // vector a가 사각형의 중점으로부터 우측 꼭지점으로 가는 벡터,
            // vector b는 크기가 a이고 방향이 (1/sqrt(2),-1/sqrt(2))인 벡터

            //cos = (size * virtualX - size*virtualY) / square;
            cos = (size * virtualX - size*virtualY) / square;
            sin = (float) Math.sqrt(1 - (cos * cos));

            if((virtualX+virtualY)<0)//가상의 touchpos(X,Y)가 y=-x의 왼쪽아래일경우
                sin=-sin;//sin의 값을 음수로 설정.

            if(size<0.1f)
                size=0.1f;

            calcSize(0,cos,sin);
            calcSize(1,cos,sin);
            calcSize(2,cos,sin);
            calcSize(3,cos,sin);


            upload();

        }

    }

    void calcSize(int number,float cos, float sin)
    {
        int x=number*2;
        int y=1+x;
        float virtualX=-disatance;
        float virtualY=disatance;

        if(number==0)
        {
            virtualX=disatance;
            virtualY=-disatance;
        }
        if(number==1)
        {

            virtualY=-disatance;
        }
        if(number==2)
        {
            virtualX=disatance;
        }


        float sizeX=virtualX*cos-virtualY*sin;
        float sizeY=virtualX*sin+virtualY*cos;

        BEFORE_MOVE_SQUARE_COORDS[x]=sizeX;
        BEFORE_MOVE_SQUARE_COORDS[y]=sizeY;


    }


    void upload()
    {
        for(int i=0;i<4;i++)
        {
            SQUARE_COORDS[i*2]=BEFORE_MOVE_SQUARE_COORDS[i*2]+posX;
            SQUARE_COORDS[i*2+1]=(BEFORE_MOVE_SQUARE_COORDS[i*2+1]+posY)*perc;
        }

        VERTEX_BUF = ByteBuffer.allocateDirect(SQUARE_COORDS.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        VERTEX_BUF.put(SQUARE_COORDS);

        stickerBound.moveBound(SQUARE_COORDS);
        VERTEX_BUF.position(0);

    }
    public void release() {
        stickerBound.release();
        ORIGIN_PROGRAM = 0;
        stickerSwitch=false;
    }


}
