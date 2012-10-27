package com.github.matt.williams.vhack.racer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class AccelerometerEventBroadcaster implements ControllerCallback {

	private Socket mEventSocket;
	
	public void start() {
		(new SocketThread()).start();
	}

	@Override
	public void control(float steering, float speed) {
		(new BroadcastThread(steering, speed)).start();
	}

	private class BroadcastThread extends Thread {
		
		private float mSteering;
		private float mSpeed;
		
		public BroadcastThread(float steering, float speed) {
			mSteering = steering;
			mSpeed = speed;
		}

		public void run() {
			PrintWriter out = null;

			try {
				out = new PrintWriter(mEventSocket.getOutputStream(), true);

				out.println(Float.toString(mSteering) + ", " + Float.toString(mSpeed));
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("I/O exception.");
				System.exit(1);
			} finally {
				try {
					if (out != null) {
						out.close();
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

	private class SocketThread extends Thread {

		public void run() {
			try {
				mEventSocket = new Socket("192.168.1.68", 10569);
			} catch (UnknownHostException e) {
				System.err.println("Uknown host");
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Couldn't get I/O for the connection.");
			}
		}

	}

}
