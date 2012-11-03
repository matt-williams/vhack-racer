package com.github.matt.williams.vhack.racer.network;

public interface EventReceiver {
    public void onItemCollected(String item);
    public void onHapticEvent();
}
