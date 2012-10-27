package com.github.matt.williams.vhack.racer;

import java.io.IOException;
import java.net.ServerSocket;

public class AccelerometerEventReceiver {
    private ControllerCallback mControllerCallback;

    public AccelerometerEventReceiver(ControllerCallback controllerCallback) {
        mControllerCallback = controllerCallback;
    }

	private boolean listening;

	public void start() {
		listening = true;

		ServerSocket serverSocket = null;

		try {
			serverSocket = new ServerSocket(4444);

			while (listening)
				new AccelerometerEventThread(serverSocket.accept(), mControllerCallback).start();
		} catch (IOException e) {
			System.err.println("Could not listen on port: 4444.");
			System.exit(-1);
		} finally {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void stop() {
		listening = false;
	}
}
