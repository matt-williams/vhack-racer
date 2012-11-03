package com.github.matt.williams.vhack.racer.sound;

import com.github.matt.williams.vhack.racer.R;
import com.github.matt.williams.vhack.racer.game.Kart;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Handler;

public class SoundController {
    private Handler mHandler = new Handler();
    private SoundPool mSoundPool;
    private int mEngineSoundID;
    private int mCrashSoundID;
    private int mBananaSoundID;
    private int mTreasureSoundID;
    private boolean mCrashSoundLoaded;
    private boolean mBananaSoundLoaded;
    private boolean mTreasureSoundLoaded;
    private int mCrashCooloff;
    private int mBananaCooloff;
    private int mTreasureCooloff;
	private Context mContext;

    Runnable mSoundLoadHandler = new Runnable() {

        public void run() {
            // Load the sound
        	mEngineSoundID = mSoundPool.load(mContext, R.raw.engine_small, 1);
            mCrashSoundID = mSoundPool.load(mContext, R.raw.crash, 2);
            mBananaSoundID = mSoundPool.load(mContext, R.raw.wee, 2);
            mTreasureSoundID = mSoundPool.load(mContext, R.raw.grab_collectable, 2);
            mHandler.postDelayed(mSoundPlayHandler, 100);
        }
    };
    
    Runnable mSoundPlayHandler = new Runnable() {
        public void run() {
            mCrashCooloff = (mCrashCooloff <= 0) ? 0 : mCrashCooloff - 1;
            mBananaCooloff = (mBananaCooloff <= 0) ? 0 : mBananaCooloff - 1;
            mTreasureCooloff = (mTreasureCooloff <= 0) ? 0 : mTreasureCooloff - 1;
            if ((mKart.gotTreasureRecently()) &&
                (mTreasureSoundLoaded) &&
                (mTreasureCooloff == 0)) {
                mSoundPool.play(mTreasureSoundID, 1, 1, 1, 0, 1f);
                mTreasureCooloff = 15;
            }
            if ((mKart.hasCollided()) &&
                (mCrashSoundLoaded) &&
                (mCrashCooloff == 0)) {
                mSoundPool.play(mCrashSoundID, 1, 1, 1, 0, 1f);
                mCrashCooloff = 15;
            }
            if ((mKart.isSlipping()) &&
                (mBananaSoundLoaded) &&
                (mBananaCooloff == 0)) {
                mSoundPool.play(mBananaSoundID, 1, 1, 1, 0, 1f);
                mBananaCooloff = 15;
            }
            mHandler.postDelayed(mSoundPlayHandler, 100);            
        }
    };
    
    private Kart mKart;
    
	public SoundController(Context context, Kart kart) {
		mContext = context;
		mKart = kart;
		
    	mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
    	mSoundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {

        public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
        	  // when the sound is fully loaded play it
              if (sampleId == mEngineSoundID) {
                  mSoundPool.play(mEngineSoundID, 1, 1, 1, -1, 1f);
              } else if (sampleId == mCrashSoundID) {
                  mCrashSoundLoaded = true;
              } else if (sampleId == mBananaSoundID) {
                  mBananaSoundLoaded = true;
              } else if (sampleId == mTreasureSoundID) {
                  mTreasureSoundLoaded = true;
              }
          }
        });
	}
    
    public void start() {
        mHandler.post(mSoundLoadHandler);
    }
    
    public void stop() {
        mHandler.removeCallbacks(mSoundLoadHandler);
        mHandler.removeCallbacks(mSoundPlayHandler);
        // when we are stopping kill the sound or this will get more annoying
        mSoundPool.stop(mEngineSoundID);
        mSoundPool.stop(mCrashSoundID);
        mSoundPool.stop(mBananaSoundID);
        mSoundPool.stop(mTreasureSoundID);
        mSoundPool.release();
        mSoundPool = null;
    }
}
