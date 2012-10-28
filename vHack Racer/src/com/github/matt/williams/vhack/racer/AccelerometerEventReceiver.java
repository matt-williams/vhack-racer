package com.github.matt.williams.vhack.racer;

import java.io.IOException;
import java.net.ServerSocket;

public class AccelerometerEventReceiver {
    private ControllerCallback mControllerCallback;
    private EventSender mEventSender;
    private ReceiverThread mReceiverThread;

    public AccelerometerEventReceiver(ControllerCallback controllerCallback, EventSender eventSender) {
        mControllerCallback = controllerCallback;
        mEventSender = eventSender;
		mReceiverThread = new ReceiverThread();
    }

	private boolean listening;

	public void start() {
		listening = true;
		mReceiverThread.start();
	}
	
	public void stop() {
		listening = false;
	}

	private class ReceiverThread extends Thread {

		public void run() {

			ServerSocket serverSocket = null;

			try {
				serverSocket = new ServerSocket(10569);

				while (listening)
					new AccelerometerEventThread(serverSocket.accept(), mControllerCallback, mEventSender).start();
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Could not listen on port: 10569.");
				System.exit(-1);
			} finally {
				try {
					if (serverSocket != null) {
						serverSocket.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
}
