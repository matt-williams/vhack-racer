package com.github.matt.williams.vhack.racer;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;

public class GameActivity extends Activity implements GLSurfaceView.Renderer {

    private static final String TAG = "GameActivity";
    private GLSurfaceView mGLSurfaceView;
    private int mWidth;
    private int mHeight;
    private Texture mTerrainTexture;
    private Texture mMapTexture;
    private Program mMapProgram;
    private float[] mViewMatrix = new float[16];
    private float[] mRotationMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mPVMatrix = new float[16];
    private AccelerometerController mAccelerometerController;
    private AccelerometerEventReceiver mAccelerometerEventReceiver;
    private Program mSkyboxProgram;
    private Texture mSkyboxTexture;
    private Program mPointProgram;
    private Texture mBillboardTexture;
    private Kart mKart;
    private Map mMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        mGLSurfaceView = (GLSurfaceView)findViewById(R.id.glsurfaceview);
        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setRenderer(this);
        mKart = new Kart("Matt", 19.8f, -25.0f, (float)(Math.PI / 2));
        if (getPackageManager().hasSystemFeature("com.google.android.tv")) {
        	mAccelerometerEventReceiver = new AccelerometerEventReceiver(mKart);
        } else {
        	mAccelerometerController = new AccelerometerController((SensorManager)getSystemService(Context.SENSOR_SERVICE), new AccelerometerEventBroadcaster());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mAccelerometerController.start();
        mAccelerometerEventReceiver.start();
    }
    
    @Override
    public void onPause() {
        mAccelerometerController.stop();
        mAccelerometerEventReceiver.stop();
        super.onPause();
    }
    
    public void onSurfaceCreated(GL10 gl, EGLConfig cfg) {
        Resources resources = getResources();
        mSkyboxTexture = new Texture(BitmapFactory.decodeResource(resources,  R.drawable.skybox));
        mSkyboxProgram = new Program(new VertexShader(resources.getString(R.string.skyboxVertexShader)),
                                     new FragmentShader(resources.getString(R.string.skyboxFragmentShader)));
        mSkyboxProgram.setVertexAttrib("xyz", new float[] {-50, 90, -50,
                                                        -50, -90, -50,
                                                        50, 90, -50,
                                                        50, -90, -50,
                                                        50, 90, 50,
                                                        50, -90, 50,
                                                        -50, 90, 50,
                                                        -50, -90, 50,
                                                        -50, 90, -50,
                                                        -50, -90, -50}, 3);
        mSkyboxProgram.setVertexAttrib("uv", new float[] {0, 0,
                                                       0, 1,
                                                       0.25f, 0,
                                                       0.25f, 1,
                                                       0.5f, 0,
                                                       0.5f, 1,
                                                       0.75f, 0,
                                                       0.75f, 1,
                                                       1.0f, 0,
                                                       1.0f, 1}, 2);
        mSkyboxProgram.setUniform("skybox", 0);
        
        mTerrainTexture = new Texture(BitmapFactory.decodeResource(resources, R.drawable.terrain));
//        Bitmap mapBitmap = BitmapFactory.decodeStream(resources.openRawResource(R.raw.map));
        Bitmap mapBitmap = Bitmap.createBitmap(MapData.DATA, 64, 64, Bitmap.Config.ARGB_8888);
        mMap = new Map(mapBitmap);
        mMapTexture = new Texture(mapBitmap);
        mMapProgram = new Program(new VertexShader(resources.getString(R.string.mapVertexShader)),
                                  new FragmentShader(resources.getString(R.string.mapFragmentShader)));
                                  /*
        mMapProgram.setVertexAttrib("xz", new float[] {-150, -150, 150, -150, 150, 150, -150, 150}, 2);
        mMapProgram.setVertexAttrib("uv", new float[] {-1, -1, 2, -1, -1, 2, 2, 2}, 2);
        */
        mMapProgram.setUniform("terrain", 0);
        mMapProgram.setUniform("terrainSize", 1.0f/40, 1.0f/40);//Should be20.0f/mTerrainTexture.getWidth(), 20.0f/mTerrainTexture.getHeight()); but getWidth() and getHeight() return 1200!
        mMapProgram.setUniform("map", 1);
        mMapProgram.setUniform("mapSize", 1.0f/mMapTexture.getWidth(), 1.0f/mMapTexture.getHeight());
        
        mBillboardTexture = new Texture(BitmapFactory.decodeResource(resources, R.drawable.banana));
        mPointProgram = new Program(new VertexShader(resources.getString(R.string.pointVertexShader)),
                                    new FragmentShader(resources.getString(R.string.pointFragmentShader)));
        mPointProgram.setUniform("billboard", 0);
        
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }
    
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mWidth = width;
        mHeight = height;
        
        // Create a new perspective projection matrix. The height will stay the same
        // while the width will vary as per aspect ratio.
        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 100.0f;
     
        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);        
    }

    public void onDrawFrame(GL10 gl) {
        mKart.update(mMap);
        float orientation = mKart.getOrientation();
        float position[] = mKart.getPosition();
        
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glViewport(0, 0, mWidth, mHeight);

        Matrix.setIdentityM(mViewMatrix, 0);
        Matrix.rotateM(mViewMatrix, 0, (float)(orientation * 180 / Math.PI), 0.0f, 1.0f, 0.0f);
        Matrix.multiplyMM(mPVMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        
        mSkyboxProgram.setUniform("matrix", mPVMatrix);
        mSkyboxTexture.use(GLES20.GL_TEXTURE0);
        
        mSkyboxProgram.setVertexAttrib("xyz", new float[] {-50, 90, -50,
                -50, -90, -50,
                50, 90, -50,
                50, -90, -50,
                50, 90, 50,
                50, -90, 50,
                -50, 90, 50,
                -50, -90, 50,
                -50, 90, -50,
                -50, -90, -50}, 3);
        mSkyboxProgram.setVertexAttrib("uv", new float[] {0, 0,
                0, 1,
                0.25f, 0,
                0.25f, 1,
                0.5f, 0,
                0.5f, 1,
                0.75f, 0,
                0.75f, 1,
                1.0f, 0,
                1.0f, 1}, 2);
        mSkyboxProgram.setUniform("skybox", 0);
        mSkyboxProgram.use();
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 10);
        Utils.checkErrors("glDrawArrays");

        Matrix.setIdentityM(mViewMatrix, 0);
        Matrix.translateM(mViewMatrix, 0, 0, 0, -1);
        Matrix.rotateM(mViewMatrix, 0, (float)(orientation * 180 / Math.PI), 0.0f, 1.0f, 0.0f);
        Matrix.translateM(mViewMatrix, 0, position[0], 0, position[1]);
        Matrix.multiplyMM(mPVMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        mMapProgram.setUniform("matrix", mPVMatrix);

        mTerrainTexture.use(GLES20.GL_TEXTURE0);
        mMapTexture.use(GLES20.GL_TEXTURE1);
        mMapProgram.use();
        mMapProgram.setVertexAttrib("xz", new float[] {-100, -100, 100, -100, -100, 100, 100, 100}, 2);
        mMapProgram.setVertexAttrib("uv", new float[] {-1, -1, 2, -1, -1, 2, 2, 2}, 2);
        mMapProgram.setUniform("terrain", 0);
        mMapProgram.setUniform("terrainSize", 1.0f/40, 1.0f/40);//Should be20.0f/mTerrainTexture.getWidth(), 20.0f/mTerrainTexture.getHeight()); but getWidth() and getHeight() return 1200!
        mMapProgram.setUniform("map", 1);
        mMapProgram.setUniform("mapSize", 1.0f/mMapTexture.getWidth(), 1.0f/mMapTexture.getHeight());
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        Utils.checkErrors("glDrawArrays");
        
        mBillboardTexture.use(GLES20.GL_TEXTURE0);
        Matrix.setIdentityM(mRotationMatrix, 0);
        Matrix.rotateM(mRotationMatrix, 0, (float)(-orientation * 180 / Math.PI), 0.0f, 1.0f, 0.0f);
        mPointProgram.setUniform("matrix", mPVMatrix);
        mPointProgram.setUniform("rotation", mRotationMatrix);
        float[] points = new float[] {0, 30, 0, -30, 30, 0, -30, 0};
        float[] vertices = new float[points.length * 12];
        for (int pointIndex = 0; pointIndex < points.length / 2; pointIndex++) {
            vertices[pointIndex * 24] = points[pointIndex * 2];
            vertices[pointIndex * 24 + 1] = points[pointIndex * 2 + 1];
            vertices[pointIndex * 24 + 2] = -1;
            vertices[pointIndex * 24 + 3] = 1;
            vertices[pointIndex * 24 + 4] = points[pointIndex * 2];
            vertices[pointIndex * 24 + 5] = points[pointIndex * 2 + 1];
            vertices[pointIndex * 24 + 6] = 1;
            vertices[pointIndex * 24 + 7] = 1;
            vertices[pointIndex * 24 + 8] = points[pointIndex * 2];
            vertices[pointIndex * 24 + 9] = points[pointIndex * 2 + 1];
            vertices[pointIndex * 24 + 10] = 1;
            vertices[pointIndex * 24 + 11] = -1;
            vertices[pointIndex * 24 + 12] = points[pointIndex * 2];
            vertices[pointIndex * 24 + 13] = points[pointIndex * 2 + 1];
            vertices[pointIndex * 24 + 14] = 1;
            vertices[pointIndex * 24 + 15] = 1;
            vertices[pointIndex * 24 + 16] = points[pointIndex * 2];
            vertices[pointIndex * 24 + 17] = points[pointIndex * 2 + 1];
            vertices[pointIndex * 24 + 18] = 1;
            vertices[pointIndex * 24 + 19] = -1;
            vertices[pointIndex * 24 + 20] = points[pointIndex * 2];
            vertices[pointIndex * 24 + 21] = points[pointIndex * 2 + 1];
            vertices[pointIndex * 24 + 22] = -1;
            vertices[pointIndex * 24 + 23] = -1;
        }
        mPointProgram.setVertexAttrib("xzuv", vertices, 4);
        mPointProgram.use();
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertices.length / 4);
        Utils.checkErrors("glDrawArrays");
    }
}
