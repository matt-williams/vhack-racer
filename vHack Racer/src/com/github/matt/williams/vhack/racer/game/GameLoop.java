package com.github.matt.williams.vhack.racer.game;

import android.os.Handler;

public class GameLoop implements Runnable {
    private final static long UPDATE_PERIOD = 20;
    private Handler mHandler = new Handler();
    
    RaceState mRaceState;

    public GameLoop(RaceState raceState) {
        mRaceState = raceState;
    }
    
    public void start() {
        // TODO Sort out synchronization of start processing
        mHandler.postDelayed(this, 2000);
    }
    
    public void run() {
        mRaceState.update();
        mHandler.postDelayed(this, UPDATE_PERIOD);
    }
    
    public void stop() {
        mHandler.removeCallbacks(this);
    }
}
