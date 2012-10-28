package com.github.matt.williams.vhack.racer;


public class ItemController implements EventReceiver {

    private EventReceiver mEventReceiver;

    public ItemController(EventReceiver eventReceiver) {
        mEventReceiver = eventReceiver;
    }

	@Override
	public void onItemCollected(String item) {
		// Show item in game screen on phone

	}

	@Override
	public void onHapticEvent() {
		// TODO Auto-generated method stub

	}

}
