package com.github.matt.williams.vhack.racer.control;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.github.matt.williams.vhack.racer.game.ControllerCallback;
import com.sony.rdis.receiver.utility.RdisUtility;
import com.sony.rdis.receiver.utility.RdisUtilityConnectionListener;
import com.sony.rdis.receiver.utility.RdisUtilityEventListener;
import com.sony.rdis.receiver.utility.RdisUtilityGamePad;

public class SonyRemoteController implements RdisUtilityConnectionListener, RdisUtilityEventListener {
    private static final String TAG = "SonyRemoteController";
    private Activity mActivity;
    private ControllerCallback mControllerCallback;
    private RdisUtility mRdisUtility;
    private RdisUtilityGamePad mGamePad;
    private float mLastSteering;
    private float mLastSpeed;

    public SonyRemoteController(Activity activity, ControllerCallback controllerCallback) {
        mActivity = activity;
        mControllerCallback = controllerCallback;
    }

    public void start() {
        mRdisUtility = new RdisUtility(mActivity, this, null);
        mRdisUtility.resume();
    }
    
    public void stop() {
        mRdisUtility.pause();
        if (mGamePad != null) {
            mRdisUtility.unregisterGamePad(mGamePad);
        }
        mRdisUtility.destroy();
    }
    
    public void onConnected(RdisUtilityGamePad gamePad) {
        if ((gamePad.isDefaultGamePad()) &&
            (mGamePad == null)) {
            int[] sensorArray = gamePad.getSensorType();
            for (int ii = 0; ii < sensorArray.length; ii++) {
                if (sensorArray[ii] == Sensor.TYPE_ACCELEROMETER) {
                    int[] sensor = new int[1];
                    sensor[0] = Sensor.TYPE_ACCELEROMETER;
                    Log.e(TAG, "Registering gamepad");
                    mRdisUtility.registerGamePad(gamePad, this, sensor, 0);
                    mGamePad = gamePad;
                    break;
                }
            }
        }
    }

    public void onDisconnected(RdisUtilityGamePad gamePad) {
        if (mGamePad == gamePad) {
            Log.e(TAG, "Unregistering gamepad");
            mRdisUtility.unregisterGamePad(gamePad);
            mGamePad = null;
        }
    }

    public void onAccuracyChanged(Sensor arg0, int arg1) {
        // TODO Auto-generated method stub
        
    }

    public boolean onKeyDown(int arg0, KeyEvent arg1) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean onKeyUp(int arg0, KeyEvent arg1) {
        // TODO Auto-generated method stub
        return false;
    }

    public void onSensorChanged(SensorEvent event) {
        Log.e(TAG, "Got event " + event.values[0] + ", " + event.values[1] + ", " + event.values[2]);
        float steering = Math.max(-0.5f, Math.min(0.5f, (float)event.values[1])) * (float)(Math.PI / 80);
        float speed = (0.2f - event.values[2]) * 0.02f;
        if ((steering != mLastSteering) ||
            (speed != mLastSpeed)) {
            mLastSteering = steering;
            mLastSpeed = speed;
            mControllerCallback.control(steering, speed);
        }
    }

    public boolean onTouchEvent(MotionEvent arg0) {
        // TODO Auto-generated method stub
        return false;
    }
}
