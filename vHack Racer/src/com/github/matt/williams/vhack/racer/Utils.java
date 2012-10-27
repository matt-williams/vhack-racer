package com.github.matt.williams.vhack.racer;

import android.opengl.GLES20;

public class Utils {
    private Utils() {}
    
    public static void checkErrors(String name) {
        int error = GLES20.glGetError();
        if (error != 0) {
            throw new IllegalStateException(name + " raised error " + error);
        }
    }
}
