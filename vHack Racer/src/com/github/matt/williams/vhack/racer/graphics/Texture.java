package com.github.matt.williams.vhack.racer.graphics;


import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

public class Texture {
    private static final String TAG = "Texture";
    private int mId;
    private int mWidth;
    private int mHeight;

    public Texture(Bitmap bitmap, int wrap) {
        mId = generateTextureId();
        mWidth = bitmap.getWidth();
        mHeight = bitmap.getHeight();
        int oldId = pushTexture();
        int[] maxTextureSize = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, maxTextureSize, 0);
        Log.e(TAG, "Got texture " + maxTextureSize[0]);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        Utils.checkErrors("texImage2D");
        bitmap.recycle();
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, wrap);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, wrap);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        Utils.checkErrors("glTexParameteri");
        popTexture(oldId);
    }
    
    public Texture(Bitmap bitmap) {
        this(bitmap, GLES20.GL_CLAMP_TO_EDGE);
    }

    protected void finalize() {
        Log.e(TAG, "finalizing " + mId);
        if (mId != 0) {
            GLES20.glDeleteTextures(1, new int[] {mId}, 0);
            Utils.checkErrors("glDeleteTextures");
        }
    }
    
    public void use(int channel) {
        GLES20.glActiveTexture(channel);
        Utils.checkErrors("glActiveTexture");
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mId);
        Utils.checkErrors("glBindTexture");
    }
    
    public int getId() {
        return mId;
    }
    
    private static int generateTextureId() {
        int[] ids = new int[1];
        GLES20.glGenTextures(1, ids, 0);
        Utils.checkErrors("glGenTextures");
        Log.e(TAG, "glGenTextures returned " + ids[0]);
        return ids[0];
    }
    
    private int pushTexture() {
        int[] oldIds = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_TEXTURE_BINDING_2D, oldIds, 0);
        Utils.checkErrors("glGetIntegerv");
        Log.e(TAG, "pushTexture got " + oldIds[0]);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mId);
        Utils.checkErrors("glBindTexture");
        return oldIds[0];
    }
    
    private void popTexture(int oldId) {
        Log.e(TAG, "popping texture " + oldId);
        if (oldId != 0) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, oldId);        
            Utils.checkErrors("glBindTexture");
        }
    }
    
    public int getWidth() {
        return mWidth;
    }
    
    public int getHeight() {
        return mHeight;
    }
}