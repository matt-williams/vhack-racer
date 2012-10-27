package com.github.matt.williams.vhack.racer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;

public class GameActivity extends Activity implements GLSurfaceView.Renderer, ControllerCallback {

    private GLSurfaceView mGLSurfaceView;
    private int mWidth;
    private int mHeight;
    private Texture mTerrainTexture;
    private Texture mMapTexture;
    private Program mProgram;
    private float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mPVMatrix = new float[16];
    private float mOrientation;
    private float[] mPosition = new float[2];
    private float mSteering;
    private double mSpeed;
    private AccelerometerController mAccelerometerController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        mGLSurfaceView = (GLSurfaceView)findViewById(R.id.glsurfaceview);
        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setRenderer(this);
        mAccelerometerController = new AccelerometerController((SensorManager)getSystemService(Context.SENSOR_SERVICE), this);
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
        mTerrainTexture = new Texture(BitmapFactory.decodeResource(resources, R.drawable.terrain));
        mMapTexture = new Texture(BitmapFactory.decodeResource(resources, R.drawable.map));
        mProgram = new Program(new VertexShader(resources.getString(R.string.vertexShader)),
                               new FragmentShader(resources.getString(R.string.fragmentShader)));
        mProgram.setVertexAttrib("xz", new float[] {-100, -100, 100, -100, -100, 100, 100, 100}, 2);
        mProgram.setVertexAttrib("uv", new float[] {-1, -1, 2, -1, -1, 2, 2, 2}, 2);
        mProgram.setUniform("terrain", 0);
        mProgram.setUniform("terrainSize", 1.0f/40, 1.0f/40);
        mProgram.setUniform("map", 1);
        mProgram.setUniform("mapSize", 1.0f/64, 1.0f/64);        
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
        mProgram.setUniform("matrix", mProjectionMatrix);
    }

    public void onDrawFrame(GL10 gl) {
        Matrix.setIdentityM(mViewMatrix, 0);
        mOrientation += mSteering;
        Matrix.rotateM(mViewMatrix, 0, (float)(mOrientation * 180 / Math.PI), 0.0f, 1.0f, 0.0f);
        Matrix.translateM(mViewMatrix, 0, mPosition[0], 0, mPosition[1]);
        mPosition[0] += Math.sin(mOrientation) * mSpeed;
        mPosition[1] -= Math.cos(mOrientation) * mSpeed;
        Matrix.multiplyMM(mPVMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        mProgram.setUniform("matrix", mPVMatrix);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glViewport(0, 0, mWidth, mHeight);
        mTerrainTexture.use(GLES20.GL_TEXTURE0);
        mMapTexture.use(GLES20.GL_TEXTURE1);
        mProgram.use();
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        Utils.checkErrors("glDrawArrays");
    }
    
    public void control(float steering, float speed) {
        mSteering = steering;
        mSpeed = speed;
    }
}
