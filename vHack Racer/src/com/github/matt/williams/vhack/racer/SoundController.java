package com.github.matt.williams.vhack.racer;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Handler;

public class SoundController {
    private Handler mHandler = new Handler();
    private SoundPool mSoundPool;
    private int mSoundID;
	private Context mContext;

    Runnable mSoundHandler = new Runnable() {
        public void run() {
            // Load the sound
        	mSoundID = mSoundPool.load(mContext, R.raw.engine_small, 1);
        }
    };
    
	public SoundController(Context context) {
		mContext = context;
		
    	mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
    	mSoundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
          @Override
          public void onLoadComplete(SoundPool soundPool, int sampleId,
              int status) {
        	  // when the sound is fully loaded play it
        	  mSoundPool.play(mSoundID, 1, 1, 1, -1, 1f);
          }
        });
	}
    
    public void start() {
        mHandler.post(mSoundHandler);
    }
    
    public void stop() {
        mHandler.removeCallbacks(mSoundHandler);
        // when we are stopping kill the sound or this will get more annoying
        mSoundPool.stop(mSoundID);
        mSoundPool.release();
        mSoundPool = null;
    }
}
