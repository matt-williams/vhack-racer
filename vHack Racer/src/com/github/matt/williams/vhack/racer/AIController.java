package com.github.matt.williams.vhack.racer;

import android.os.Handler;
import android.util.Log;

public class AIController {
    private static final String TAG = "AIController";
    private Map mMap;
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
    
    public AIController(Map map, Kart kart, ControllerCallback controllerCallback) {
        mMap = map;
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
        int[] tile = mMap.getPosition(position[0], position[1]);
        int[] targetTile = TARGET_TILES[tile[0] + tile[1] * 64];
//        Log.e(TAG, "Kart " + mKart.getName() + " is being smart - " + tile[0] + ", " + tile[1] + " - " + targetTile);
        if (targetTile != null) {
            float[] targetPosition = mMap.getPosition(targetTile[0], targetTile[1]);
            float[] deltaPosition = new float[] {targetPosition[0] - position[0], targetPosition[1] - position[1]};
            float distance = (float)Math.sqrt((deltaPosition[0] * deltaPosition[0]) + (deltaPosition[1] * deltaPosition[1]));
            float targetOrientation = (float)Math.atan2(-deltaPosition[0], deltaPosition[1]);
            float idealSteering = (targetOrientation - orientation) * 0.1f;
  //          Log.e(TAG, "Currently at " + position[0] + ", " + position[1] + "(" + tile[0] + ", " + tile[1] + ") - looking to " + targetTile[0] + ", " + targetTile[1] + " - ideal = " + idealSteering);
            idealSteering = (idealSteering > Math.PI) ? (float)(2 * Math.PI) - idealSteering : idealSteering;
            idealSteering = (idealSteering < -Math.PI) ? (float)(2 * Math.PI) + idealSteering : idealSteering;
            float steering = Math.min(0.2f, Math.max(-0.2f, idealSteering));
//            Log.e(TAG, "Orientation: " + orientation + ", Target: " + targetOrientation + ", Ideal: " + idealSteering + ", Actual: " + steering);
            float speed = -0.1f;
            if (distance < 2.0f) {
                speed = -0.05f + -0.05f * distance / 2.0f;
            }
            mControllerCallback.control(steering, speed);
        }
        mHandler.postDelayed(mRunnable, 100);
    }
} 