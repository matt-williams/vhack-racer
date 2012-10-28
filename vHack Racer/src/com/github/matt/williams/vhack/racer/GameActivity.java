package com.github.matt.williams.vhack.racer;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class GameActivity extends Activity implements GLSurfaceView.Renderer, ConnectionCallback {

    private static final String TAG = "GameActivity";
    public static final String EXTRA_CONNECT = "Connect";
    private GLSurfaceView mGLSurfaceView;
    private int mWidth;
    private int mHeight;
    private Texture mTerrainTexture;
    private Texture mMapTexture;
    private Program mMapProgram;
    private float[] mViewMatrix = new float[16];
    private float[] mRotationMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mPVMatrix = new float[16];
    private AccelerometerController mAccelerometerController;
    private AccelerometerEventReceiver mAccelerometerEventReceiver;
    private Program mSkyboxProgram;
    private Texture mSkyboxTexture;
    private Program mPointProgram;
    private Texture mBananaTexture;
    private Texture mTreasureTexture;
    private Kart mKart;
    private List<Kart> mKarts = new ArrayList<Kart>();
    private Map mMap;
    private Texture mTuxTexture;
    private Program mTuxProgram;
    private static final float[] UV_COORDS = new float[768];
    private TextView mLapBoard;
    private int mCurrentLap;
    private Handler mLapHandler = new Handler();
    private boolean mLapsFinished;
    private TextView mFinished;
    private int totalLapTime;
    private Handler mTimerHandler = new Handler();
    private TextView mTimer;
    private SharedPreferences mUserHighScore;
    private SharedPreferences.Editor mUserHighScoreEditor;
    private int mBestTime;
    private static final String HIGH_SCORE_PREF = "high score";
    private static final String NAME_PREF = "name";
    private AlertDialog.Builder mAlert;
    private EditText mHighScoreInput;
  	private String mTotalLapTimeFormatted;
  	private String mUserName;
  	private Handler mGameEndHandler = new Handler();
  	private boolean recordScores = true;
    static {
        for (int coordIndex = 0; coordIndex < UV_COORDS.length; coordIndex += 12) {
            UV_COORDS[coordIndex] = 0;
            UV_COORDS[coordIndex + 1] = 1;
            UV_COORDS[coordIndex + 2] = 1;
            UV_COORDS[coordIndex + 3] = 1;
            UV_COORDS[coordIndex + 4] = 1;
            UV_COORDS[coordIndex + 5] = 0;
            UV_COORDS[coordIndex + 6] = 1;
            UV_COORDS[coordIndex + 7] = 0;
            UV_COORDS[coordIndex + 8] = 0;
            UV_COORDS[coordIndex + 9] = 1;
            UV_COORDS[coordIndex + 10] = 0;
            UV_COORDS[coordIndex + 11] = 0;
        }
    }
    private AccelerometerEventBroadcaster mAccelerometerEventBroadcaster;
    private HapticsController mHapticsController;
    private SonyRemoteController mSonyRemoteController;
    private SoundController mSoundController;
    private boolean mRunLapTimer;
    private List<Item> mBananas;
    private List<Item> mTreasure;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
        boolean connect = ((intent != null) && (intent.getBooleanExtra(EXTRA_CONNECT, false)));
        if ((!getPackageManager().hasSystemFeature("com.google.android.tv")) && (connect)) {
            setContentView(R.layout.activity_game_item);
        } else {
            setContentView(R.layout.activity_game);
            mLapBoard = (TextView)findViewById(R.id.lapBoard);
            mFinished = (TextView)findViewById(R.id.finished);
            mTimer = (TextView)findViewById(R.id.timer);
    	    mAlert = new AlertDialog.Builder(this);
    	    mHighScoreInput = new EditText(this);
            mGLSurfaceView = (GLSurfaceView)findViewById(R.id.glsurfaceview);
            mGLSurfaceView.setEGLContextClientVersion(2);
            mGLSurfaceView.setRenderer(this);
            Bitmap mapBitmap = Bitmap.createBitmap(MapData.DATA, 64, 64, Bitmap.Config.ARGB_8888);
            mMap = new Map(mapBitmap);
            Kart kart = new Kart("Alice", 19.8f, -23.9f, (float)(Math.PI / 2));
            new AIController(mMap, kart, kart).start(); // TODO: Do this properly, and stop it.
            mKarts.add(kart);
            kart = new Kart("Bob", 21.875f, -28.1f, (float)(Math.PI / 2));
            new AIController(mMap, kart, kart).start(); // TODO: Do this properly, and stop it.
            mKarts.add(kart);
            kart = new Kart("Charlie", 23.96f, -23.9f, (float)(Math.PI / 2));
            new AIController(mMap, kart, kart).start(); // TODO: Do this properly, and stop it.
            mKarts.add(kart);
            mKart = new Kart("Dave", 26.04f, -28.1f, (float)(Math.PI / 2));
            mKarts.add(mKart);
            
            mBananas = new ArrayList<Item>();
            mBananas.add(new Item(mMap, 58, 42));
            mBananas.add(new Item(mMap, 45, 22));
            mBananas.add(new Item(mMap, 43, 16));
            mBananas.add(new Item(mMap, 25, 38));
            mBananas.add(new Item(mMap, 29, 13));
            mBananas.add(new Item(mMap, 32, 12));
            mBananas.add(new Item(mMap, 5, 17));
            mBananas.add(new Item(mMap, 7, 25));
            mBananas.add(new Item(mMap, 5, 49));
            
            mTreasure = new ArrayList<Item>();
            mTreasure.add(new Item(mMap, 55, 55));
            mTreasure.add(new Item(mMap, 58, 58));
            mTreasure.add(new Item(mMap, 52, 5));
            mTreasure.add(new Item(mMap, 49, 7));
            mTreasure.add(new Item(mMap, 22, 22));
            mTreasure.add(new Item(mMap, 19, 19));
            mTreasure.add(new Item(mMap, 8, 8));
            mTreasure.add(new Item(mMap, 5, 5));
            
            // lap counter
            mCurrentLap = mKart.getLapCount();
            mLapHandler.postDelayed(mLapBoardHandler, 1000);            
            mRunLapTimer = true;
        }
        
        if (getPackageManager().hasSystemFeature("com.google.android.tv")) {
            if (connect) {
                mSonyRemoteController = new SonyRemoteController(this, mKart);
            } else {
            	recordScores = false;
                mAccelerometerEventReceiver = new AccelerometerEventReceiver(mKart);
            }
            mSoundController = new SoundController(this, mKart);
        } else {
            ControllerCallback controllerCallback;
            if (connect) {
                mAccelerometerEventBroadcaster = new AccelerometerEventBroadcaster(this);
                controllerCallback = mAccelerometerEventBroadcaster;
            } else {
                controllerCallback = mKart;
                mHapticsController = new HapticsController(this, mKart);
            	mSoundController = new SoundController(this, mKart);
            }
            mAccelerometerController = new AccelerometerController((SensorManager)getSystemService(Context.SENSOR_SERVICE), controllerCallback);
        }

        // setup for the user's high score
        mUserHighScore = getSharedPreferences(HIGH_SCORE_PREF, Context.MODE_PRIVATE);
    	mUserHighScoreEditor = mUserHighScore.edit();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAccelerometerEventBroadcaster != null) {
            mAccelerometerEventBroadcaster.start();
        }
        if (mAccelerometerController != null) {
        	mAccelerometerController.start();
        }
        if (mSonyRemoteController != null) {
            mSonyRemoteController.start();
        }
        if (mAccelerometerEventReceiver != null) {
        	mAccelerometerEventReceiver.start();
        }
        if (mHapticsController != null) {
            mHapticsController.start();
        }
        if (mSoundController != null) {
        	mSoundController.start();
        }
        if (mRunLapTimer) {
            mTimerHandler.post(mLapTimerHandler);
        }
        
   	mBestTime = mUserHighScore.getInt(HIGH_SCORE_PREF, 0);
    	mUserName = mUserHighScore.getString(NAME_PREF, "Matt");
    }
    
    @Override
    public void onPause() {
        if (mAccelerometerEventBroadcaster != null) {
            mAccelerometerEventBroadcaster.shutdown();
        }
        if (mAccelerometerController != null) {
        	mAccelerometerController.stop();
        }
        if (mSonyRemoteController != null) {
            mSonyRemoteController.stop();
        }
        if (mAccelerometerEventReceiver != null) {
        	mAccelerometerEventReceiver.stop();
        }
        if (mHapticsController != null) {
            mHapticsController.stop();
        }
        if (mSoundController != null) {
        	mSoundController.stop();
        }
        super.onPause();
    }
    
    public void onSurfaceCreated(GL10 gl, EGLConfig cfg) {
        Resources resources = getResources();
        mSkyboxTexture = new Texture(BitmapFactory.decodeResource(resources, R.drawable.skybox));
        mSkyboxProgram = new Program(new VertexShader(resources.getString(R.string.skyboxVertexShader)),
                                     new FragmentShader(resources.getString(R.string.skyboxFragmentShader)));
        mSkyboxProgram.setVertexAttrib("xyz", new float[] {-50, 90, -50,
                                                        -50, -90, -50,
                                                        50, 90, -50,
                                                        50, -90, -50,
                                                        50, 90, 50,
                                                        50, -90, 50,
                                                        -50, 90, 50,
                                                        -50, -90, 50,
                                                        -50, 90, -50,
                                                        -50, -90, -50}, 3);
        mSkyboxProgram.setVertexAttrib("uv", new float[] {0, 0,
                                                       0, 1,
                                                       0.25f, 0,
                                                       0.25f, 1,
                                                       0.5f, 0,
                                                       0.5f, 1,
                                                       0.75f, 0,
                                                       0.75f, 1,
                                                       1.0f, 0,
                                                       1.0f, 1}, 2);
        mSkyboxTexture.use(GLES20.GL_TEXTURE0);
        mSkyboxProgram.setUniform("skybox", 0);
        
        mTerrainTexture = new Texture(BitmapFactory.decodeResource(resources, R.drawable.terrain));
//        Bitmap mapBitmap = BitmapFactory.decodeStream(resources.openRawResource(R.raw.map));
        Bitmap mapBitmap = Bitmap.createBitmap(MapData.DATA, 64, 64, Bitmap.Config.ARGB_8888);
        mMapTexture = new Texture(mapBitmap);
        mMapProgram = new Program(new VertexShader(resources.getString(R.string.mapVertexShader)),
                                  new FragmentShader(resources.getString(R.string.mapFragmentShader)));
        mTerrainTexture.use(GLES20.GL_TEXTURE0);
        mMapProgram.setUniform("terrain", 0);
        mMapProgram.setUniform("terrainSize", 1.0f/40, 1.0f/40);//Should be20.0f/mTerrainTexture.getWidth(), 20.0f/mTerrainTexture.getHeight()); but getWidth() and getHeight() return 1200!
        mMapTexture.use(GLES20.GL_TEXTURE1);
        mMapProgram.setUniform("map", 1);
        mMapProgram.setUniform("mapSize", 1.0f/mMapTexture.getWidth(), 1.0f/mMapTexture.getHeight());
 
        mBananaTexture = new Texture(BitmapFactory.decodeResource(resources, R.drawable.banana));
        mTreasureTexture = new Texture(BitmapFactory.decodeResource(resources, R.drawable.chest));
        mPointProgram = new Program(new VertexShader(resources.getString(R.string.pointVertexShader)),
                                    new FragmentShader(resources.getString(R.string.pointFragmentShader)));
        mBananaTexture.use(GLES20.GL_TEXTURE0);
        mPointProgram.setUniform("billboard", 0);

        mTuxTexture = new Texture(BitmapFactory.decodeResource(resources,  R.drawable.tux), GLES20.GL_REPEAT);
        mTuxProgram = new Program(new VertexShader(resources.getString(R.string.tuxVertexShader)),
                                  new FragmentShader(resources.getString(R.string.pointFragmentShader)));
        mTuxTexture.use(GLES20.GL_TEXTURE0);
        mTuxProgram.setUniform("billboard", 0);
        
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glClearDepthf(100.0f);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
    }
    
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mWidth = width;
        mHeight = height;
        
        // Create a new perspective projection matrix. The height will stay the same
        // while the width will vary as per aspect ratio.
        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 100.0f;
     
        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);        
    }

    public void onDrawFrame(GL10 gl) {
        for (Kart kart : mKarts) {
            kart.update(mMap);
            Item itemToDelete = null;
            for (Item item : mBananas) {
                if (kart.hit(item.getPosition())) {
                    kart.hitBanana();
                    itemToDelete = item;
                    break;
                }
            }
            if (itemToDelete != null) {
                mBananas.remove(itemToDelete);
            }
            itemToDelete = null;
            for (Item item : mTreasure) {
                if (kart.hit(item.getPosition())) {
                    kart.gotTreasure();
                    itemToDelete = item;
                    break;
                }
            }
            if (itemToDelete != null) {
                mTreasure.remove(itemToDelete);
            }
        }
        for (int kart1Index = 0; kart1Index < mKarts.size() - 1; kart1Index++) {
            Kart kart1 = mKarts.get(kart1Index);
            for (int kart2Index = kart1Index + 1; kart2Index < mKarts.size(); kart2Index++) {
                Kart kart2 = mKarts.get(kart2Index);
                if (kart1.hit(kart2.getPosition())) {
                    float[] kart1Position = kart1.getPosition();
                    float[] kart2Position = kart2.getPosition();
                    kart1.bumped(kart1Position[0] - kart2Position[0], kart1Position[1] - kart2Position[1]);
                    kart2.bumped(kart2Position[0] - kart1Position[0], kart2Position[1] - kart1Position[1]);
                }
            }
        }
        
        // check if the user has completed a lap
        int currentLap = mKart.getLapCount();
        if (currentLap > mCurrentLap  && currentLap <= Map.TOTAL_LAPS) {
        	mCurrentLap = currentLap;
        	
        	if (currentLap == Map.TOTAL_LAPS) {
        		mLapsFinished = true;
        	}
        	
            mLapHandler.post(mLapBoardHandler);
        }
        // if we are finished the course we don't need to update the lap counter
        if (currentLap > Map.TOTAL_LAPS) {
        	mLapHandler.removeCallbacks(mLapBoardHandler);
        	mTimerHandler.removeCallbacks(mLapTimerHandler);
        }
        
        float orientation = mKart.getOrientation();
        float position[] = mKart.getPosition();
        
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glViewport(0, 0, mWidth, mHeight);

        Matrix.setIdentityM(mViewMatrix, 0);
        Matrix.rotateM(mViewMatrix, 0, (float)(orientation * 180 / Math.PI), 0.0f, 1.0f, 0.0f);
        Matrix.multiplyMM(mPVMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
                GLES20.glDisable(GLES20.GL_DEPTH_TEST);
                GLES20.glDepthMask(false);
        
        mSkyboxProgram.setUniform("matrix", mPVMatrix);
        mSkyboxTexture.use(GLES20.GL_TEXTURE0);
        
        mSkyboxProgram.setVertexAttrib("xyz", new float[] {-50, 90, -50,
                -50, -90, -50,
                50, 90, -50,
                50, -90, -50,
                50, 90, 50,
                50, -90, 50,
                -50, 90, 50,
                -50, -90, 50,
                -50, 90, -50,
                -50, -90, -50}, 3);
        mSkyboxProgram.setVertexAttrib("uv", new float[] {0, 0,
                0, 1,
                0.25f, 0,
                0.25f, 1,
                0.5f, 0,
                0.5f, 1,
                0.75f, 0,
                0.75f, 1,
                1.0f, 0,
                1.0f, 1}, 2);
        mSkyboxProgram.setUniform("skybox", 0);
        mSkyboxProgram.use();
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 10);
        Utils.checkErrors("glDrawArrays");

        Matrix.setIdentityM(mViewMatrix, 0);
        Matrix.translateM(mViewMatrix, 0, 0, 0, -2);
        Matrix.rotateM(mViewMatrix, 0, (float)(orientation * 180 / Math.PI), 0.0f, 1.0f, 0.0f);
        Matrix.translateM(mViewMatrix, 0, position[0], 0, position[1]);
        Matrix.multiplyMM(mPVMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        mMapProgram.setUniform("matrix", mPVMatrix);

        mTerrainTexture.use(GLES20.GL_TEXTURE0);
        mMapTexture.use(GLES20.GL_TEXTURE1);
        mMapProgram.use();
        mMapProgram.setVertexAttrib("xz", new float[] {-100, -100, 100, -100, -100, 100, 100, 100}, 2);
        mMapProgram.setVertexAttrib("uv", new float[] {-1, -1, 2, -1, -1, 2, 2, 2}, 2);
        mMapProgram.setUniform("terrain", 0);
        mMapProgram.setUniform("terrainSize", 1.0f/40, 1.0f/40);//Should be20.0f/mTerrainTexture.getWidth(), 20.0f/mTerrainTexture.getHeight()); but getWidth() and getHeight() return 1200!
        mMapProgram.setUniform("map", 1);
        mMapProgram.setUniform("mapSize", 1.0f/mMapTexture.getWidth(), 1.0f/mMapTexture.getHeight());
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        Utils.checkErrors("glDrawArrays");
        
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthMask(true);
        mBananaTexture.use(GLES20.GL_TEXTURE0);
        Matrix.setIdentityM(mRotationMatrix, 0);
        Matrix.rotateM(mRotationMatrix, 0, (float)(-orientation * 180 / Math.PI), 0.0f, 1.0f, 0.0f);
        mPointProgram.setUniform("matrix", mPVMatrix);
        mPointProgram.setUniform("rotation", mRotationMatrix);
        mPointProgram.setVertexAttrib("uv",  UV_COORDS,  2);
        float[] points = new float[mBananas.size() * 2];
        int pointIndex = 0;
        for (Item banana : mBananas) {
            points[pointIndex++] = -banana.getPosition()[0];
            points[pointIndex++] = -banana.getPosition()[1];
        }
        float[] vertices = pointsToVertices(points, 2);
        mPointProgram.setVertexAttrib("xz", vertices, 2);
        mBananaTexture.use(GLES20.GL_TEXTURE0);
        mPointProgram.use();
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertices.length / 2);
        Utils.checkErrors("glDrawArrays");

        points = new float[mTreasure.size() * 2];
        pointIndex = 0;
        for (Item treasure : mTreasure) {
            points[pointIndex++] = -treasure.getPosition()[0];
            points[pointIndex++] = -treasure.getPosition()[1];
        }
        vertices = pointsToVertices(points, 2);
        mPointProgram.setVertexAttrib("xz", vertices, 2);
        mTreasureTexture.use(GLES20.GL_TEXTURE0);
        mPointProgram.use();
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertices.length / 2);
        Utils.checkErrors("glDrawArrays");        
        
        mTuxTexture.use(GLES20.GL_TEXTURE0);
        mTuxProgram.setUniform("matrix", mPVMatrix);
        mTuxProgram.setUniform("rotation", mRotationMatrix);
        mTuxProgram.setUniform("orientation", mKart.getOrientation());
        points = new float[4 * mKarts.size()];
        pointIndex = 0;
        for (Kart kart : mKarts) {
            points[pointIndex++] = -kart.getPosition()[0];
            points[pointIndex++] = -kart.getPosition()[1];
            points[pointIndex++] = kart.getPosition()[2];
            points[pointIndex++] = kart.getOrientation();
        }        
        vertices = pointsToVertices(points, 4);
        mTuxProgram.setVertexAttrib("xzyr", vertices, 4);
        mTuxProgram.setVertexAttrib("uv", UV_COORDS, 2);
        mTuxProgram.use();
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertices.length / 4);
        Utils.checkErrors("glDrawArrays");
    }

    private float[] pointsToVertices(float[] points, int size) {
        float[] vertices = new float[points.length * 6];
        int vertexIndex = 0;
        for (int pointIndex = 0; pointIndex < points.length / size; pointIndex++) {
            for (int index = 0; index < 6; index++) {
                for (int subIndex = 0; subIndex < size; subIndex++) {
                    vertices[vertexIndex++] = points[pointIndex * size + subIndex];
                }
            }
        }
        return vertices;
    }

    public void onConnected() {
        //@@TODO: Implement me.
    }

    public void onConnectionFailed() {
        //@@TODO: Implement me.
    }

    Runnable mLapBoardHandler = new Runnable() {
       public void run() {
         mLapBoard.setText(mCurrentLap + "/" + Map.TOTAL_LAPS);
         if (mLapsFinished) {
           mFinished.setVisibility(View.VISIBLE);

           // if the user has beaten their previous best time notify them
           // or if the user doesn't have a best score yet then ask them to
           // record their score
           if ((mBestTime > totalLapTime || mBestTime == 0) && recordScores) {

             mAlert.setTitle("High Score");
             mAlert.setMessage("Congratulations you have a new best time of "
                 + mTotalLapTimeFormatted
                 + ". Please eneter your name:");

             // Set an EditText view to get user input
             mAlert.setView(mHighScoreInput);
             mHighScoreInput.append(mUserName);

             mAlert.setPositiveButton("OK",
                 new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog,
                       int whichButton) {
                     String value = mHighScoreInput.getText()
                         .toString();

                     mUserHighScoreEditor.putString(NAME_PREF,
                         value);
                     mUserHighScoreEditor.putInt(
                         HIGH_SCORE_PREF, totalLapTime);
                     mUserHighScoreEditor.commit();

                     // Take the user back to the main menu
                     mGameEndHandler.postDelayed(mEndOfGameHandler, 3000);
                   }
                 });
             mAlert.show();
           } else {
             // The user didn't beat their best time so just take them back to the main menu
             mGameEndHandler.postDelayed(mEndOfGameHandler, 3000);
           }
         }	
       }
     };

     Runnable mLapTimerHandler = new Runnable() {
       public void run() {
         // if we haven't finished the course then record how long the user is taking
         if (mCurrentLap < Map.TOTAL_LAPS) {
           // update the time passed
           totalLapTime++;

           int minutes = (int) Math.floor(totalLapTime / 60);
           int seconds = totalLapTime % 60;
           // format 0-9 seconds to 0:09 etc
           String padding = "";
           if (seconds < 10) {
             padding = "0";
           }
	
           mTotalLapTimeFormatted = minutes + ":" + padding + seconds;

           mTimer.setText(mTotalLapTimeFormatted);

           // update the timer every second
           mTimerHandler.postDelayed(mLapTimerHandler, 1000);
         }	
       }	
     };
	
     Runnable mEndOfGameHandler = new Runnable() {
    		 
       public void run() {
         finish();
       }	
     };
}
