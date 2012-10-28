package com.github.matt.williams.vhack.racer;

public interface EventSender {
    public void sendItemCollected(String item);
    public void sendHapticEvent();
}
