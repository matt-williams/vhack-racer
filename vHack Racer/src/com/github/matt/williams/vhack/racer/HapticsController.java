package com.github.matt.williams.vhack.racer;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.immersion.uhl.Launcher;

public class HapticsController {
    private static final String TAG = "HapticsController";
    private Kart mKart;
    private Launcher mLauncher;
    private Handler mHandler = new Handler();

    Runnable mHapticsHandler = new Runnable() {
        public void run() {
            Log.e(TAG, "Running HapticsHandler");
            if (mKart.isOnRough()) {
                // ...
            } else if (mKart.hasCollided()) {
                try {
                    mLauncher.play(Launcher.IMPACT_RUBBER_100);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to play built-in effect, index " + Launcher.IMPACT_RUBBER_100 + ": " + e);
                }
            } else {
                // mKart.getSpeed();
                // ...
            }
            mHandler.postDelayed(mHapticsHandler, 100);
        }
    };
        
    public HapticsController(Context context, Kart kart) {
        mKart = kart;
        try {
            mLauncher = new Launcher(context);
        } catch (Exception e) {
            Log.e(TAG, "Failed to create Launcher: " + e);
        }        
    }
    
    public void start() {
        Log.e(TAG, "Starting HapticsHandler");
        mHandler.post(mHapticsHandler);
    }
    
    public void stop() {
        Log.e(TAG, "Stopping HapticsHandler");
        mHandler.removeCallbacks(mHapticsHandler);
    }
}
