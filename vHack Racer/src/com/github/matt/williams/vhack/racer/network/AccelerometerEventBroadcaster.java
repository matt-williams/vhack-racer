package com.github.matt.williams.vhack.racer.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import com.github.matt.williams.vhack.racer.game.ControllerCallback;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

public class AccelerometerEventBroadcaster extends HandlerThread implements ControllerCallback {
    protected static final String TAG = AccelerometerEventBroadcaster.class.getName();

    private static final String SPEED = "speed";
    private static final String STEERING = "steering";
    private Socket mEventSocket;
    private PrintWriter mOut;
    private BufferedReader mIn;
    private Handler mHandler;

    private ConnectionCallback mConnectionCallback;
    private EventReceiver mEventReceiver;

    public AccelerometerEventBroadcaster(ConnectionCallback connectionCallback, EventReceiver eventReceiver) {
        super("AccelerometerEventBroadcaster");
        mConnectionCallback = connectionCallback;
        mEventReceiver = eventReceiver;
    }

    public void start() {
        super.start();
        mHandler = new Handler(getLooper()) {
            public void handleMessage(Message msg) {
                Bundle bundle = msg.getData();
                if (mOut != null) {
                    mOut.println(Float.toString(bundle.getFloat(STEERING)) + ", " + Float.toString(bundle.getFloat(SPEED)));
                    mOut.flush();
                }
                if (mIn != null) {
                	try {
						String item = mIn.readLine();
						mEventReceiver.onItemCollected(item);
					} catch (IOException e) {
						e.printStackTrace();
					}
                }
            }
        };        
    }
    
    @Override
    protected void finalize() {
        try {
            if (mOut != null) {
                mOut.close();
            }
            if (mIn != null) {
            	mIn.close();
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
        Log.i(TAG, "Connecting to TV");
        try {
            mEventSocket = new Socket("192.168.1.68", 10569);
            mOut = new PrintWriter(mEventSocket.getOutputStream(), true);
			mIn = new BufferedReader(new InputStreamReader(
					mEventSocket.getInputStream()));
            connected = true;
        } catch (UnknownHostException e) {
            System.err.println("Unknown host");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Couldn't get I/O for the connection.");
        }
        if (connected) {
            Log.i(TAG, "Connected to TV");
            mConnectionCallback.onConnected();
        } else {
            Log.i(TAG, "Connection to TV failed");
            mConnectionCallback.onConnectionFailed();
        }
    }
    
    boolean called;
    public void control(float steering, float speed) {
        Message msg = mHandler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putFloat(STEERING, steering);
        bundle.putFloat(SPEED, speed);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    public void shutdown() {
        getLooper().quit();
        mHandler = null;
    }
}
