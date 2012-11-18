package com.github.matt.williams.vhack.racer.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;

public class VerbatimBitmapOptions extends Options {
    public static final VerbatimBitmapOptions INSTANCE = new VerbatimBitmapOptions();
    
    public VerbatimBitmapOptions() {
        inScaled = false;
        inPreferredConfig = Bitmap.Config.ARGB_8888;
    }
}
