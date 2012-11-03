package com.github.matt.williams.vhack.racer.haptics;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.github.matt.williams.vhack.racer.game.Kart;
import com.immersion.uhl.Launcher;

public class HapticsController {
    private static final String TAG = "HapticsController";
    private Kart mKart;
    private Launcher mLauncher;
    private Handler mHandler = new Handler();

    Runnable mHapticsHandler = new Runnable() {
        public void run() {
            if (mKart.isOnRough()) {
            	mLauncher.play(Launcher.LONG_BUZZ_66);
            } else if (mKart.hasCollided()) {
                try {
                    mLauncher.play(Launcher.IMPACT_RUBBER_100);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to play built-in effect, index " + Launcher.IMPACT_RUBBER_100 + ": " + e);
                }
            } else {
                float speed = mKart.getSpeed();
                if (speed <= 0.1) {
                	mLauncher.play(Launcher.ENGINE1_33);
                } else if (speed <= 0.2) {
                	mLauncher.play(Launcher.ENGINE1_66);
                } else if (speed > 0.2) {
                	mLauncher.play(Launcher.ENGINE1_100);
                }
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
        mHandler.post(mHapticsHandler);
    }
    
    public void stop() {
        mHandler.removeCallbacks(mHapticsHandler);
        // stop haptics when quiting
        mLauncher.stop();
    }
}
