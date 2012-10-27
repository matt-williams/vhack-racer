package com.github.matt.williams.vhack.racer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class AccelerometerEventBroadcaster implements ControllerCallback {

	private Socket mEventSocket;

	public AccelerometerEventBroadcaster() {
		try {
			mEventSocket = new Socket("192.168.1.147", 4444);
		} catch (UnknownHostException e) {
			System.err.println("Uknown host");
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection.");
		}
	}

	@Override
	public void control(float steering, float speed) {
		PrintWriter out = null;

		try {
			out = new PrintWriter(mEventSocket.getOutputStream(), true);

			out.println(Float.toString(steering) + ", " + Float.toString(speed));
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection.");
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
