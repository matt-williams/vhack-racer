package com.github.matt.williams.vhack.racer;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class AccelerometerController implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ControllerCallback mControllerCallback;

    public AccelerometerController(SensorManager sensorManager, ControllerCallback controllerCallback) {
        mSensorManager = sensorManager;
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mControllerCallback = controllerCallback;
    }

    public void start() {
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }
    
    public void stop() {
        mSensorManager.unregisterListener(this);
    }
    
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Ignore.
    }

    public void onSensorChanged(SensorEvent event) {
        float steering = Math.max(-0.5f, Math.min(0.5f, (float)event.values[1])) * (float)(Math.PI / 80);
        float speed = (0.2f - event.values[2]) * 0.02f;
        mControllerCallback.control(steering, speed);
    }
}
