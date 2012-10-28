package com.github.matt.williams.vhack.racer;

import android.util.Log;

public class Kart implements ControllerCallback {
    private static final String TAG = "Kart";
    private static final int JUMPING_PERIOD = 50;
    private static final int COLLISION_PERIOD = 50;
    private float mOrientation;
    private float[] mPosition = new float[2];
    private float mSteering;
    private float mTargetSpeed;
    private boolean mIsOnRough;
    private int mJumping;
    private float[] mVelocity = new float[2];
    private int mCollision;
    private int mLapCount;
    private int mLapCooldown;
    private String mName;
    
    public Kart(String name, float startX, float startY, float startOrientation) {
        mName = name;
        mPosition[0] = startX;
        mPosition[1] = startY;
        mOrientation = startOrientation;
    }
    
    public synchronized void control(float steering, float targetSpeed) {
        mSteering = steering;
        mTargetSpeed = targetSpeed;
    }
    
    public synchronized void update(Map map) {
        mIsOnRough = false;
        if (mCollision > 0) {
            mCollision--;
        }
        if (mLapCooldown > 0) {
            mLapCooldown--;
        }

        float effectiveSteering = mSteering;
        float effectiveTargetSpeed = mTargetSpeed;
        if (mJumping == 0)
        {
            byte tileProperty = getTileProperty(map, mPosition);
            switch (tileProperty) {
            case Map.VOID:
                Log.e(TAG, "Player has reached the void at (" + mPosition[0] + ", " + mPosition[1] + ")");
            case Map.BLOCKING:
                effectiveTargetSpeed *= 0.7;
                break;

            case Map.ROUGH:
                mIsOnRough = true;
                effectiveSteering += (Math.random() - 0.5) * effectiveTargetSpeed / 4;
                effectiveTargetSpeed *= 0.7;
                break;

            case Map.JUMP:
                mJumping = JUMPING_PERIOD;
                break;

            case Map.FINISH:
                if (mLapCooldown == 0) {
                    mLapCount++;
                    mLapCooldown = 1000;
                }
                break;

            case Map.DRIVABLE:
            default:
            }
            mOrientation += effectiveSteering;
            mOrientation = (mOrientation < 0) ? (mOrientation + (float)(2 * Math.PI)) : (mOrientation > (float)(2 * Math.PI)) ? (mOrientation - (float)(2 * Math.PI)): mOrientation;
        } else {
            mJumping--;
        }
        
        mVelocity[0] = (float)(mVelocity[0] * 0.5 + Math.sin(mOrientation) * effectiveTargetSpeed);
        mVelocity[1] = (float)(mVelocity[1] * 0.5 - Math.cos(mOrientation) * effectiveTargetSpeed);
        if ((!driveToIfPossible(map, mVelocity[0], mVelocity[1])) &&
            (!driveToIfPossible(map, mVelocity[0] * 0.5f, mVelocity[1] * 0.5f)) &&
            (!driveToIfPossible(map, mVelocity[0] * 0.5f + mVelocity[1] * 0.5f, mVelocity[1] * 0.5f - mVelocity[0] * 0.5f)) &&
            (!driveToIfPossible(map, mVelocity[0] * 0.5f - mVelocity[1] * 0.5f, mVelocity[1] * 0.5f + mVelocity[0] * 0.5f))) {
            mCollision = COLLISION_PERIOD;
        }
    }

    private boolean driveToIfPossible(Map map, float offsetX, float offsetY) {
        float[] nextPosition = new float[2];
        nextPosition[0] = mPosition[0] + offsetX;
        nextPosition[1] = mPosition[1] + offsetY;
        if (canDriveTo(map, nextPosition)) {
            mPosition[0] = nextPosition[0];
            mPosition[1] = nextPosition[1];
            return true;
        }
        return false;
    }
    
    private boolean canDriveTo(Map map, float[] position) {
        byte nextTile = getTileProperty(map, position);
        return ((nextTile != Map.BLOCKING) && (nextTile != Map.VOID));
    }
    
    private byte getTileProperty(Map map, float position[]) {
        return map.getTileProperty(1.0f - (position[0] + 33.33f) / 66.67f, 1.0f - (position[1] + 33.33f) / 66.67f);
    }

    public float getOrientation() {
        return mOrientation;
    }

    public float[] getPosition() {
        float position[] = new float[3];
        position[0] = mPosition[0];
        position[1] = mPosition[1];
        position[2] = (float)Math.sin(mJumping * Math.PI / JUMPING_PERIOD);
        return position;
    }
    
    public boolean isOnRough() {
        return mIsOnRough;
    }
    
    public boolean hasCollided() {
        return (mCollision > 0);
    }
    
    public float getSpeed() {
    	// pythagoras
    	return (float) Math.sqrt((mVelocity[0] * mVelocity[0]) + (mVelocity[1] * mVelocity[1]));
    }
    
    public String getName() {
        return mName;
    }
}
