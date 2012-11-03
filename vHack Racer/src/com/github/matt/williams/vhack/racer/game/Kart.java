package com.github.matt.williams.vhack.racer.game;

import android.util.Log;

public class Kart implements ControllerCallback {
    public interface PositionListener {
        public void onPositionUpdate(float x, float y, float th);
    }
    
    public interface LapListener {
        public void onLap(int lapCount);
        public void onRaceComplete();       
    }
    
    private static final String TAG = "Kart";
    private static final int JUMPING_PERIOD = 50;
    private static final int COLLISION_PERIOD = 50;
    private static final int SLIPPY_DURATION = 50;
    private static final float HIT_RADIUS = 1.5f;
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
    private LapListener mLapListener;
    private int mSlippy;
    private boolean mGotTreasure;
    private PositionListener mPositionListener;
    
    public Kart(String name, float startX, float startY, float startOrientation) {
        mName = name;
        mPosition[0] = startX;
        mPosition[1] = startY;
        mOrientation = startOrientation;
    }

    public void setLapListener(LapListener lapListener) {
        mLapListener = lapListener;
    }
    
    public void setPositionListener(PositionListener positionListener) {
        mPositionListener = positionListener;
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
        if (mSlippy > 0) {
            mSlippy--;
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
                effectiveSteering += (Math.random() - 0.5) * effectiveTargetSpeed / 320;
                effectiveTargetSpeed *= 0.7;
                break;

            case Map.JUMP:
                mJumping = JUMPING_PERIOD;
                break;

            case Map.FINISH:
                if (mLapCooldown == 0) {
                    mLapCount++;
                    if (mLapListener != null) {
                        mLapListener.onLap(mLapCount);
                        if (mLapCount == Map.TOTAL_LAPS) {
                            mLapListener.onRaceComplete();                            
                        }
                    }
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

        if (mSlippy == 0) {
            mVelocity[0] = (float)(mVelocity[0] * 0.5 + Math.sin(mOrientation) * effectiveTargetSpeed);
            mVelocity[1] = (float)(mVelocity[1] * 0.5 - Math.cos(mOrientation) * effectiveTargetSpeed);
        }
        if ((!driveToIfPossible(map, mVelocity[0], mVelocity[1])) &&
            (!driveToIfPossible(map, mVelocity[0] * 0.5f, mVelocity[1] * 0.5f)) &&
            (!driveToIfPossible(map, mVelocity[0] * 0.5f + mVelocity[1] * 0.5f, mVelocity[1] * 0.5f - mVelocity[0] * 0.5f)) &&
            (!driveToIfPossible(map, mVelocity[0] * 0.5f - mVelocity[1] * 0.5f, mVelocity[1] * 0.5f + mVelocity[0] * 0.5f))) {
            mCollision = COLLISION_PERIOD;
        }
        if (mPositionListener != null) {
            mPositionListener.onPositionUpdate(mPosition[0], mPosition[1], mOrientation);
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
    
    public boolean isSlipping() {
        return (mSlippy > 0);
    }
    
    public boolean gotTreasureRecently() {
        boolean gotTreasure = mGotTreasure;
        mGotTreasure = false;
        return gotTreasure;
    }
    
    public float getSpeed() {
    	// pythagoras
    	return (float) Math.sqrt((mVelocity[0] * mVelocity[0]) + (mVelocity[1] * mVelocity[1]));
    }
    
    public String getName() {
        return mName;
    }
    
    public int getLapCount() {
    	return mLapCount;
    }

    public boolean hit(float[] position) {
        float deltaX = mPosition[0] - position[0];
        float deltaY = mPosition[1] - position[1];
        return (Math.sqrt((deltaX * deltaX) + (deltaY * deltaY)) < HIT_RADIUS);
    }

    public void hitBanana() {
        mSlippy = SLIPPY_DURATION;
    }

    public void gotTreasure() {
        mGotTreasure = true;
    }

    public void bumped(float x, float y) {
        float modulus = (float)Math.sqrt((x * x) + (y * y));
        mVelocity[0] += x / modulus * 0.2f;
        mVelocity[1] += y / modulus * 0.2f;
        mCollision = COLLISION_PERIOD;
    }
}
