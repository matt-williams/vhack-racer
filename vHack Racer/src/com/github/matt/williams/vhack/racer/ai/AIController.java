package com.github.matt.williams.vhack.racer.ai;

import com.github.matt.williams.vhack.racer.game.ControllerCallback;
import com.github.matt.williams.vhack.racer.game.Kart;
import com.github.matt.williams.vhack.racer.game.RaceState;

import android.os.Handler;
import android.util.FloatMath;
import android.util.Log;

public class AIController {
    private static final String TAG = "AIController";
    private RaceState mRaceState;
    private Kart mKart;
    private ControllerCallback mControllerCallback;
    private Handler mHandler = new Handler();

    private static int[][] TARGET_TILES = new int[64 * 64][];
    static {
        setTargets(0, 0, 12, 50, 5, 51);
        setTargets(0, 51, 50, 63, 51, 58);
        setTargets(51, 13, 63, 63, 57, 12);
        setTargets(51, 0, 63, 12, 50, 6);
        setTargets(38, 0, 50, 11, 43, 12);
        setTargets(38, 12, 50, 38, 43, 39);
        setTargets(38, 39, 50, 50, 37, 43);
        setTargets(13, 27, 37, 50, 20, 26);
        setTargets(13, 13, 24, 26, 25, 17);
        setTargets(25, 13, 37, 26, 28, 12);
        setTargets(13, 0, 37, 12, 12, 5);
    }

    private static void setTargets(int left, int top, int right, int bottom, int targetX, int targetY) {
        for (int y = top; y <= bottom; y++) {
            for (int x = left; x <= right; x++) {
                TARGET_TILES[x + y * 64] = new int[] {targetX, targetY};
            }
        }
    }

    private Runnable mRunnable = new Runnable() {
        public void run() {
            beSmart();
        }
    };
    
    public AIController(RaceState raceState, Kart kart, ControllerCallback controllerCallback) {
        mRaceState = raceState;
        mKart = kart;
        mControllerCallback = controllerCallback;
    }

    public void start() {
        mHandler.post(mRunnable);
    }

    public void stop() {
        mHandler.removeCallbacks(mRunnable);
    }

    public void beSmart() {
        float[] position = mKart.getPosition();
        float orientation = mKart.getOrientation();
        int[] tile = mRaceState.getMap().getPosition(position[0], position[1]);
        int[] targetTile = TARGET_TILES[tile[0] + tile[1] * 64];
        if (targetTile != null) {
            float[] targetPosition = mRaceState.getMap().getPosition(targetTile[0], targetTile[1]);
            float[] deltaPosition = new float[] {targetPosition[0] - position[0], targetPosition[1] - position[1]};
            float distance = FloatMath.sqrt((deltaPosition[0] * deltaPosition[0]) + (deltaPosition[1] * deltaPosition[1]));
            float targetOrientation = (float)Math.atan2(-deltaPosition[0], deltaPosition[1]);
            targetOrientation = (targetOrientation < 0) ? (targetOrientation + (float)(2 * Math.PI)) : (targetOrientation > (float)(2 * Math.PI)) ? (targetOrientation - (float)(2 * Math.PI)): targetOrientation;
            float idealSteering = (targetOrientation - orientation) * 0.1f;
            idealSteering = (idealSteering > Math.PI) ? (float)(2 * Math.PI) - idealSteering : idealSteering;
            idealSteering = (idealSteering < -Math.PI) ? (float)(2 * Math.PI) + idealSteering : idealSteering;
            float steering = Math.min(0.2f, Math.max(-0.2f, idealSteering));
            float speed = -0.1f;
            if (Math.abs(steering) >= 0.2f) {
                speed = 0;
            } else if (distance < 2.0f) {
                speed = -0.05f + -0.05f * distance / 2.0f;
            }
            mControllerCallback.control(steering, speed);
        }
        mHandler.postDelayed(mRunnable, 100);
    }
}