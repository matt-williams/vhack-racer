package com.github.matt.williams.vhack.racer.graphics;

import android.util.FloatMath;

import com.github.matt.williams.vhack.racer.game.Kart;

public class ChaseCamera extends Camera implements Kart.PositionListener {

    @Override
    public void onPositionUpdate(float x, float z, float th) {
        float targetX = x + 2 * FloatMath.sin(th);
        float targetZ = z - 2 * FloatMath.cos(th);
        this.setPositionRotation(targetX, targetZ, th);
    }
}
