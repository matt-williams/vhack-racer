package com.github.matt.williams.vhack.racer;

import java.util.Set;

import android.graphics.Bitmap;
import android.util.Log;

public class Map {
    private static final String TAG = "Map";
    private int mWidth;
    private int mHeight;
    private int[] mTiles;

    private static final int NUM_TILES = 40 * 40;
    public static final byte VOID = 0;
    public static final byte DRIVABLE = 1;
    public static final byte BLOCKING = 2;
    public static final byte ROUGH = 3;
    public static final byte JUMP = 4;
    public static final byte FINISH = 5;
    private static final byte[] TILE_PROPERTIES = new byte[40 * 40];
    static {
        final byte V = VOID;
        final byte D = DRIVABLE;
        final byte B = BLOCKING;
        final byte R = ROUGH;
        final byte J = JUMP;
        final byte F = FINISH;
        byte[] tileProperties = new byte[] {
          D,D,B,B,B,V,V,V,R,R,B,B,B,B,B,R,R,R,B,B,B,B,B,B,V,V,B,B,B,V,V,R,R,R,V,V,V,V,V,V,
          B,B,B,D,B,V,V,V,F,R,B,D,B,B,B,R,R,R,B,D,B,B,D,B,B,B,B,D,B,B,B,R,R,R,R,R,V,V,V,V,
          B,B,B,B,B,V,V,V,F,R,B,B,B,J,J,R,R,R,B,B,B,B,B,B,B,B,B,B,B,B,B,R,R,R,R,R,V,V,V,V,
          B,B,B,B,B,D,D,D,F,R,J,J,J,J,J,R,R,B,B,B,B,B,B,B,B,B,B,B,B,B,B,R,R,R,R,R,V,V,V,V,
          B,B,B,D,B,D,R,D,D,D,J,D,J,J,J,R,R,B,B,V,V,B,D,B,B,B,B,D,B,B,B,R,R,R,R,R,V,V,V,V,
          D,D,B,B,B,D,D,D,D,D,J,J,J,J,J,V,V,B,V,V,V,B,B,B,V,V,B,B,B,V,V,R,R,R,V,V,V,V,V,V,
        };
        System.arraycopy(tileProperties, 0, TILE_PROPERTIES, 0, tileProperties.length);
    }
    
    public Map(Bitmap bitmap) {
        mWidth = bitmap.getWidth();
        mHeight = bitmap.getHeight();
        mTiles = new int[mWidth * mHeight];
        bitmap.getPixels(mTiles, 0, mWidth, 0, 0, mWidth, mHeight);
        for (int tileIndex = 0; tileIndex < mTiles.length; tileIndex++) {
            mTiles[tileIndex] = ((mTiles[tileIndex] & 0x00ff0000) >> 16) + (((mTiles[tileIndex] & 0x0000ff00) >> 8) * 40);
        }
    }
    
    public int getWidth() {
        return mWidth;
    }
    
    public int getHeight() {
        return mHeight;
    }    
    
    public int getTile(float x, float y) {
        int tile;
        if ((x >= 0) && (x < 1) && (y >= 0) && (y < 1))
        {
            tile = mTiles[(int)(Math.floor(x * mWidth)) + ((int)Math.floor(y * mHeight)) * mWidth];
        } else {
            tile = 39 * 40 + 39;
        }
        return tile;
    }
    
    public byte getTileProperty(float x, float y) {
        return TILE_PROPERTIES[getTile(x, y)];
    }    

    public float[] getPosition(int tileX, int tileY) {
        float[] position = new float[2];
        position[0] = (1.0f - (tileX / mWidth)) * 200 / 3 - 100.0f / 3;
        position[1] = (1.0f - (tileY / mHeight)) * 200 / 3 - 100.0f / 3;
        return position;
    }
    
    public int[] getPosition(float positionX, float positionY) {
        int[] tile = new int[2];
        tile[0] = (int)(Math.floor((1.0f - (positionX + 33.33f) / 66.67f) * mWidth));
        tile[0] = (int)(Math.floor((1.0f - (positionY + 33.33f) / 66.67f) * mHeight));
        return tile;
    }
}
