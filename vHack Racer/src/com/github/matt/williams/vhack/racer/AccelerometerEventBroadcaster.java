package com.github.matt.williams.vhack.racer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class AccelerometerEventBroadcaster extends HandlerThread implements ControllerCallback {
    protected static final String TAG = AccelerometerEventBroadcaster.class.getName();

    private static final String SPEED = "speed";
    private static final String STEERING = "steering";
    private Socket mEventSocket;
    private PrintWriter mOut;
    private Handler mHandler;

    private ConnectionCallback mConnectionCallback;

    public AccelerometerEventBroadcaster(ConnectionCallback connectionCallback) {
        super("AccelerometerEventBroadcaster");
        mConnectionCallback = connectionCallback;
    }

    public void start() {
        super.start();
//        Log.e(TAG, "Started");
        mHandler = new Handler(getLooper()) {
            public void handleMessage(Message msg) {
                Bundle bundle = msg.getData();
//                Log.i(TAG, "Sending " + Float.toString(bundle.getFloat(STEERING)) + ", " + Float.toString(bundle.getFloat(SPEED)));
                if (mOut != null) {
                    mOut.println(Float.toString(bundle.getFloat(STEERING)) + ", " + Float.toString(bundle.getFloat(SPEED)));
                    mOut.flush();
                }
//                Log.i(TAG, "Sent " + Float.toString(bundle.getFloat(STEERING)) + ", " + Float.toString(bundle.getFloat(SPEED)));
            }
        };        
    }
    
    @Override
    protected void finalize() {
        try {
            if (mOut != null) {
                mOut.close();
            }
            if (mEventSocket != null) {
                mEventSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void onLooperPrepared() {
        boolean connected = false;
        try {
            mEventSocket = new Socket("192.168.1.68", 10569);
            mOut = new PrintWriter(mEventSocket.getOutputStream(), true);
            connected = true;
        } catch (UnknownHostException e) {
            System.err.println("Unknown host");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Couldn't get I/O for the connection.");
        }
        if (connected) {
            mConnectionCallback.onConnected();
        } else {
            mConnectionCallback.onConnectionFailed();
        }
    }
    
    boolean called;
    public void control(float steering, float speed) {
//        Log.i(TAG, "Passing on " + steering + ", " + speed);
        Message msg = mHandler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putFloat(STEERING, steering);
        bundle.putFloat(SPEED, speed);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
//        Log.i(TAG, "Passed on " + steering + ", " + speed);
    }

    public void shutdown() {
        getLooper().quit();
        mHandler = null;
    }
}
