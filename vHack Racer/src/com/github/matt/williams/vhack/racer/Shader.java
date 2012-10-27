package com.github.matt.williams.vhack.racer;

import android.opengl.GLES20;

public class Shader {
    private int mId;
    
    protected Shader(int type, String source) {
        mId = GLES20.glCreateShader(type);
        GLES20.glShaderSource(mId, source);
        GLES20.glCompileShader(mId);
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(mId, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == 0) {
            String shaderInfoLog = GLES20.glGetShaderInfoLog(mId);
            GLES20.glDeleteShader(mId);
            mId = 0;
            throw new IllegalArgumentException(shaderInfoLog);
        }
    }
    
    protected void finalize() {
        if (mId != 0) {
            GLES20.glDeleteShader(mId);
        }
    }
    
    public int getId() {
        return mId;
    }
}