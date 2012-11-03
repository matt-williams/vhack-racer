package com.github.matt.williams.vhack.racer.graphics;

import android.opengl.GLES20;
import android.util.SparseArray;

public class Utils {
    private Utils() {}
    
    private final static SparseArray<String> GL_ERRORS = new SparseArray<String>();
    static {
        GL_ERRORS.put(GLES20.GL_INVALID_ENUM, "GL_INVALID_ENUM");
        GL_ERRORS.put(GLES20.GL_INVALID_FRAMEBUFFER_OPERATION, "GL_INVALID_FRAMEBUFFER_OPERATION");
        GL_ERRORS.put(GLES20.GL_INVALID_OPERATION, "GL_INVALID_OPERATION");
        GL_ERRORS.put(GLES20.GL_INVALID_VALUE, "GL_INVALID_VALUE");
    }
    
    public static void checkErrors(String name) {
        int error = GLES20.glGetError();
        if (error != 0) {
            String errorString = GL_ERRORS.get(error);
            throw new IllegalStateException(name + " raised " + ((errorString != null) ? errorString : ("error " + error)));
        }
    }
}
