package com.github.matt.williams.vhack.racer.graphics;

import android.opengl.Matrix;

public class Camera {
    private float mR;
    private float[] mProjectionMatrix = new float[16];
    private float[] mRotationMatrix = new float[16];
    private float[] mRotationTranslationMatrix = new float[16];
    private float[] mProjectionRotationMatrix = new float[16];
    private float[] mViewMatrix = new float[16];

    public Camera() {
        Matrix.setIdentityM(mRotationMatrix, 0);
        Matrix.setIdentityM(mRotationTranslationMatrix, 0);
    }
    
    public void setProjection(float left, float right, float bottom, float top, float near, float far) {
        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);        
        multiplyMatrices();
    }

    public void setPositionRotation(float x, float z, float r) {
        mR = r;
        Matrix.setIdentityM(mRotationMatrix, 0);
        Matrix.rotateM(mRotationMatrix, 0, (float)(r * 180 / Math.PI), 0.0f, 1.0f, 0.0f);
        System.arraycopy(mRotationMatrix, 0, mRotationTranslationMatrix, 0, 16);
        Matrix.translateM(mRotationTranslationMatrix, 0, x, 0, z);
        multiplyMatrices();
    }

    public float getR() {
        // TODO: Get rid of this.
        return mR;
    }

    public float[] getProjectionMatrix() {
        return mProjectionMatrix;
    }

    public float[] getRotationMatrix() {
        return mRotationMatrix;
    }

    public float[] getRotationTranslationMatrix() {
        return mRotationTranslationMatrix;
    }
 
    public float[] getProjectionRotationMatrix() {
        return mProjectionRotationMatrix;
    }

    public float[] getViewMatrix() {
        return mViewMatrix;
    }
    
    private void multiplyMatrices() {
        Matrix.multiplyMM(mProjectionRotationMatrix, 0, mProjectionMatrix, 0, mRotationMatrix, 0);
        Matrix.multiplyMM(mViewMatrix, 0, mProjectionMatrix, 0, mRotationTranslationMatrix, 0);
    }    
}
