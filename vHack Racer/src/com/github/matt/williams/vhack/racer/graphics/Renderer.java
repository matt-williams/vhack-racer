package com.github.matt.williams.vhack.racer.graphics;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.res.Resources;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.github.matt.williams.vhack.racer.R;
import com.github.matt.williams.vhack.racer.game.RaceState;

public class Renderer implements GLSurfaceView.Renderer {

    private Camera mCamera;
    private RaceState mRaceState;
    private Resources mResources;
    private Skybox mSkybox;
    private Terrain mTerrain;
    private ItemManager mBananaManager;
    private ItemManager mTreasureManager;
    private KartManager mKartManager;
    private int mWidth;
    private int mHeight;
    
    public Renderer(Camera camera, RaceState raceState, Resources resources) {
        mCamera = camera;
        mRaceState = raceState;
        mResources = resources;
    }
    
    public void onSurfaceCreated(GL10 gl, EGLConfig cfg) {
        mSkybox = new Skybox(mResources, R.drawable.skybox);
        mTerrain = new Terrain(mResources);
        mBananaManager = new ItemManager(mResources, R.drawable.banana);
        mTreasureManager = new ItemManager(mResources, R.drawable.chest);
        mKartManager = new KartManager(mResources, R.drawable.tux);

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
        mCamera.setProjection(-ratio, ratio, -1.0f, 1.0f, 1.0f, 100.0f);
    }

    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glViewport(0, 0, mWidth, mHeight);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthMask(false);

        mSkybox.draw(mCamera.getProjectionRotationMatrix());
        mTerrain.draw(mCamera.getViewMatrix());
        
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthMask(true);
        
        mBananaManager.draw(mCamera, mRaceState.getBananas());
        mTreasureManager.draw(mCamera, mRaceState.getTreasure());
        mKartManager.draw(mCamera, mRaceState.getKarts());
    }
}
