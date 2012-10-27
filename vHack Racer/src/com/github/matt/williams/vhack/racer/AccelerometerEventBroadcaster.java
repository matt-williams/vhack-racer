package com.github.matt.williams.vhack.racer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class AccelerometerEventBroadcaster implements ControllerCallback {
    protected static final String TAG = AccelerometerEventBroadcaster.class.getName();

    private static final String SPEED = "speed";
    private static final String STEERING = "steering";
    private SocketThread socketThread;

    public void start() {
        socketThread = new SocketThread();
        socketThread.start();
    }

    public void control(float steering, float speed) {
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putFloat(STEERING, steering);
        bundle.putFloat(SPEED, speed);
        msg.setData(bundle);
        socketThread.mHandler.sendMessage(msg);
    }

    private class SocketThread extends Thread {
        private Socket mEventSocket;
        private PrintWriter mOut;
        public Handler mHandler;

        public void run() {
            Looper.prepare();

            mHandler = new Handler() {
                public void handleMessage(Message msg) {
                    Bundle bundle = msg.getData();
                    Log.i(TAG, "Sending " + Float.toString(bundle.getFloat(STEERING)) + ", " + Float.toString(bundle.getFloat(SPEED)));
                    mOut.println(Float.toString(bundle.getFloat(STEERING)) + ", " + Float.toString(bundle.getFloat(SPEED)));
                }
            };
            
            try {
                mEventSocket = new Socket("192.168.1.68", 10569);
                mOut = new PrintWriter(mEventSocket.getOutputStream(), true);
            } catch (UnknownHostException e) {
                System.err.println("Uknown host");
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Couldn't get I/O for the connection.");
            }

            Looper.loop();

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
    }

}
