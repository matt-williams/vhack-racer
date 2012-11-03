package com.github.matt.williams.vhack.racer.activity;

import com.github.matt.williams.vhack.racer.R;
import com.github.matt.williams.vhack.racer.ai.AIController;
import com.github.matt.williams.vhack.racer.control.AccelerometerController;
import com.github.matt.williams.vhack.racer.control.SonyRemoteController;
import com.github.matt.williams.vhack.racer.game.ControllerCallback;
import com.github.matt.williams.vhack.racer.game.GameLoop;
import com.github.matt.williams.vhack.racer.game.ItemController;
import com.github.matt.williams.vhack.racer.game.Kart;
import com.github.matt.williams.vhack.racer.game.Kart.LapListener;
import com.github.matt.williams.vhack.racer.game.Map;
import com.github.matt.williams.vhack.racer.game.RaceState;
import com.github.matt.williams.vhack.racer.graphics.ChaseCamera;
import com.github.matt.williams.vhack.racer.graphics.Renderer;
import com.github.matt.williams.vhack.racer.haptics.HapticsController;
import com.github.matt.williams.vhack.racer.network.AccelerometerEventBroadcaster;
import com.github.matt.williams.vhack.racer.network.AccelerometerEventReceiver;
import com.github.matt.williams.vhack.racer.network.ConnectionCallback;
import com.github.matt.williams.vhack.racer.network.EventReceiver;
import com.github.matt.williams.vhack.racer.network.EventSender;
import com.github.matt.williams.vhack.racer.sound.SoundController;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class GameActivity extends Activity implements ConnectionCallback, EventReceiver, EventSender, LapListener {

    private static final String TAG = "GameActivity";
    public static final String EXTRA_CONNECT = "Connect";
    private GLSurfaceView mGLSurfaceView;
    private AccelerometerController mAccelerometerController;
    private AccelerometerEventReceiver mAccelerometerEventReceiver;
    private EventReceiver mEventReceiver;
    private EventSender mEventSender;
    private Renderer mRenderer;
    private Kart mKart;
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
    private AccelerometerEventBroadcaster mAccelerometerEventBroadcaster;
    private HapticsController mHapticsController;
    private SonyRemoteController mSonyRemoteController;
    private SoundController mSoundController;
    private boolean mRunLapTimer;
    private RaceState mRaceState;
    private ChaseCamera mCamera;
    private GameLoop mGameLoop;

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

            mRaceState = new RaceState();
            Kart kart = mRaceState.addKart("Alice", 19.8f, -23.9f);
            new AIController(mRaceState, kart, kart).start(); // TODO: Do this properly, and stop it.
            kart = mRaceState.addKart("Bob", 21.875f, -28.1f);
            new AIController(mRaceState, kart, kart).start(); // TODO: Do this properly, and stop it.
            kart = mRaceState.addKart("Charlie", 23.96f, -23.9f);
            new AIController(mRaceState, kart, kart).start(); // TODO: Do this properly, and stop it.
            mKart = mRaceState.addKart("Dave", 26.04f, -28.1f);
            mKart.setLapListener(this);

            mGameLoop = new GameLoop(mRaceState);

            mCamera = new ChaseCamera();
            mKart.setPositionListener(mCamera);
            mRenderer = new Renderer(mCamera, mRaceState, getResources());
            mGLSurfaceView = (GLSurfaceView)findViewById(R.id.glsurfaceview);
            mGLSurfaceView.setEGLContextClientVersion(2);
            mGLSurfaceView.setRenderer(mRenderer);

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
                mAccelerometerEventReceiver = new AccelerometerEventReceiver(mKart, this);

                // playing multiplayer on TV so broadcast item and haptic events to all
                // connected users
                mEventReceiver = new ItemController(this);
            }
            mSoundController = new SoundController(this, mKart);
        } else {
            ControllerCallback controllerCallback;
            if (connect) {
                mAccelerometerEventBroadcaster = new AccelerometerEventBroadcaster(this, this);
                controllerCallback = mAccelerometerEventBroadcaster;
            } else {
                controllerCallback = mKart;
                mHapticsController = new HapticsController(this, mKart);
                mSoundController = new SoundController(this, mKart);

                // for phones and tablets handle broadcast item events from the TV in
                // multiplayer mode and show the user which item they collected or
                // what haptic event happened e.g. hit a wall
                mEventReceiver = new ItemController(this);
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
        if (mGameLoop != null) {
            mGameLoop.start();
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
        if (mGameLoop != null) {
            mGameLoop.stop();
        }
        super.onPause();
    }

    public void onConnected() {
        //@@TODO: Implement me.
    }

    public void onConnectionFailed() {
        //@@TODO: Implement me.
    }

    public void onLap(int lapCount) {
        mCurrentLap = lapCount;
        mLapHandler.post(mLapBoardHandler);
    }

    public void onRaceComplete() {
        mLapsFinished = true;
        mTimerHandler.removeCallbacks(mLapTimerHandler);
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

    @Override
    public void onItemCollected(String item) {
        // Show user the item they have collected (phone only to keep hidden from opponents)

    }

    @Override
    public void onHapticEvent() {
        // provide haptic feedback to the user (crashed into barrier, opponent etc)

    }

    @Override
    public void sendItemCollected(String item) {
        // Send the user the item they collected

    }

    @Override
    public void sendHapticEvent() {
        // Tell the user's device what event occurred so they can provide a collision vibration
        // or rough terrain vibration etc

    }
}
