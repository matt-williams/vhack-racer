package com.github.matt.williams.vhack.racer;

public class Item {
    private float[] mPosition;
    
    public Item(float x, float y) {
        mPosition = new float[] {x, y};
    }
    
    public Item(Map map, int tileX, int tileY) {
        mPosition = map.getPosition(tileX, tileY);
    }
    
    public float[] getPosition() {
        return mPosition;
    }
}
