package com.github.matt.williams.vhack.racer.graphics;

import com.github.matt.williams.vhack.racer.game.Kart;

public class ChaseCamera extends Camera implements Kart.PositionListener {

    @Override
    public void onPositionUpdate(float x, float z, float th) {
        float targetX = x + (float)(2 * Math.sin(th));
        float targetZ = z - (float)(2 * Math.cos(th));
        this.setPositionRotation(targetX, targetZ, th);
    }
}
