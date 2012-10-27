package com.github.matt.williams.vhack.racer;

import java.util.HashMap;
import java.util.Map;

import android.opengl.GLES20;

public class Utils {
    private Utils() {}
    
    private final static Map<Integer,String> GL_ERROR_MAP = new HashMap<Integer,String>();
    static {
        GL_ERROR_MAP.put(GLES20.GL_INVALID_ENUM, "GL_INVALID_ENUM");
        GL_ERROR_MAP.put(GLES20.GL_INVALID_FRAMEBUFFER_OPERATION, "GL_INVALID_FRAMEBUFFER_OPERATION");
        GL_ERROR_MAP.put(GLES20.GL_INVALID_OPERATION, "GL_INVALID_OPERATION");
        GL_ERROR_MAP.put(GLES20.GL_INVALID_VALUE, "GL_INVALID_VALUE");
    }
    
    public static void checkErrors(String name) {
        int error = GLES20.glGetError();
        if (error != 0) {
            String errorString = GL_ERROR_MAP.get(error);
            throw new IllegalStateException(name + " raised " + ((errorString != null) ? errorString : ("error " + error)));
        }
    }
}
