package com.github.matt.williams.vhack.racer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    private Program mSkyboxProgram;
    private Texture mSkyboxTexture;
    private Program mPointProgram;
    private Texture mBananaTexture;
    private Kart mKart;
    private List<Kart> mKarts = new ArrayList<Kart>();
    private Map mMap;
    private Texture mTuxTexture;
    private Program mTuxProgram;
    private static final float[] UV_COORDS = new float[768];
    static {
        for (int coordIndex = 0; coordIndex < UV_COORDS.length; coordIndex += 12) {
            UV_COORDS[coordIndex] = 0;
            UV_COORDS[coordIndex + 1] = 1;
            UV_COORDS[coordIndex + 2] = 1;
            UV_COORDS[coordIndex + 3] = 1;
            UV_COORDS[coordIndex + 4] = 1;
            UV_COORDS[coordIndex + 5] = 0;
            UV_COORDS[coordIndex + 6] = 1;
            UV_COORDS[coordIndex + 7] = 0;
            UV_COORDS[coordIndex + 8] = 0;
            UV_COORDS[coordIndex + 9] = 1;
            UV_COORDS[coordIndex + 10] = 0;
            UV_COORDS[coordIndex + 11] = 0;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        mGLSurfaceView = (GLSurfaceView)findViewById(R.id.glsurfaceview);
        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setRenderer(this);
        mKart = new Kart("Matt", 19.8f, -23.9f, (float)(Math.PI / 2));
        mKarts.add(mKart);
        mKarts.add(new Kart("Alice", 21.875f, -28.1f, (float)(Math.PI / 2)));
        mKarts.add(new Kart("Bob", 23.96f, -23.9f, (float)(Math.PI / 2)));
        mKarts.add(new Kart("Charlie", 26.04f, -28.1f, (float)(Math.PI / 2)));
        mAccelerometerController = new AccelerometerController((SensorManager)getSystemService(Context.SENSOR_SERVICE), mKart);
    }

    @Override
    public void onResume() {
        super.onResume();
        mAccelerometerController.start();
    }
    
    @Override
    public void onPause() {
        mAccelerometerController.stop();
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
        mMapProgram.setUniform("terrain", 0);
        mMapProgram.setUniform("terrainSize", 1.0f/40, 1.0f/40);//Should be20.0f/mTerrainTexture.getWidth(), 20.0f/mTerrainTexture.getHeight()); but getWidth() and getHeight() return 1200!
        mMapProgram.setUniform("map", 1);
        mMapProgram.setUniform("mapSize", 1.0f/mMapTexture.getWidth(), 1.0f/mMapTexture.getHeight());
        
        mBananaTexture = new Texture(BitmapFactory.decodeResource(resources, R.drawable.banana));
        mPointProgram = new Program(new VertexShader(resources.getString(R.string.pointVertexShader)),
                                    new FragmentShader(resources.getString(R.string.pointFragmentShader)));
        mPointProgram.setUniform("billboard", 0);

        mTuxTexture = new Texture(BitmapFactory.decodeResource(resources,  R.drawable.tux), GLES20.GL_REPEAT);
        mTuxProgram = new Program(new VertexShader(resources.getString(R.string.tuxVertexShader)),
                                  new FragmentShader(resources.getString(R.string.pointFragmentShader)));
        mTuxProgram.setUniform("billboard", 0);
        
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glClearDepthf(100.0f);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
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
        
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glViewport(0, 0, mWidth, mHeight);

        Matrix.setIdentityM(mViewMatrix, 0);
        Matrix.rotateM(mViewMatrix, 0, (float)(orientation * 180 / Math.PI), 0.0f, 1.0f, 0.0f);
        Matrix.multiplyMM(mPVMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
                GLES20.glDisable(GLES20.GL_DEPTH_TEST);
                GLES20.glDepthMask(false);
        
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
        Matrix.translateM(mViewMatrix, 0, 0, 0, -2);
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
        
                GLES20.glEnable(GLES20.GL_DEPTH_TEST);
                GLES20.glDepthMask(true);
        mBananaTexture.use(GLES20.GL_TEXTURE0);
        Matrix.setIdentityM(mRotationMatrix, 0);
        Matrix.rotateM(mRotationMatrix, 0, (float)(-orientation * 180 / Math.PI), 0.0f, 1.0f, 0.0f);
        mPointProgram.setUniform("matrix", mPVMatrix);
        mPointProgram.setUniform("rotation", mRotationMatrix);
        float[] points = new float[] {0, 30, 0, -30, 30, 0, -30, 0};
        float[] vertices = pointsToVertices(points, 2);
        mPointProgram.setVertexAttrib("xz", vertices, 2);
        mPointProgram.setVertexAttrib("uv",  UV_COORDS,  2);
        mPointProgram.use();
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertices.length / 2);
        Utils.checkErrors("glDrawArrays");

        mTuxTexture.use(GLES20.GL_TEXTURE0);
        mTuxProgram.setUniform("matrix", mPVMatrix);
        mTuxProgram.setUniform("rotation", mRotationMatrix);
        mTuxProgram.setUniform("orientation", mKart.getOrientation());
        points = new float[4 * mKarts.size()];
        Log.e(TAG, "Position (" + mKart.getPosition()[0] + ", " + mKart.getPosition()[1] + ")");
        int pointIndex = 0;
        for (Kart kart : mKarts) {
            points[pointIndex++] = -kart.getPosition()[0];
            points[pointIndex++] = -kart.getPosition()[1];
            points[pointIndex++] = kart.getPosition()[2];
            points[pointIndex++] = kart.getOrientation();
        }        
        vertices = pointsToVertices(points, 4);
        mTuxProgram.setVertexAttrib("xzyr", vertices, 4);
        mTuxProgram.setVertexAttrib("uv", UV_COORDS, 2);
        mTuxProgram.use();
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertices.length / 4);
        Utils.checkErrors("glDrawArrays");
    }

    private float[] pointsToVertices(float[] points, int size) {
        float[] vertices = new float[points.length * 6];
        int vertexIndex = 0;
        for (int pointIndex = 0; pointIndex < points.length / size; pointIndex++) {
            for (int index = 0; index < 6; index++) {
                for (int subIndex = 0; subIndex < size; subIndex++) {
                    vertices[vertexIndex++] = points[pointIndex * size + subIndex];
                }
            }
        }
        return vertices;
    }
}
