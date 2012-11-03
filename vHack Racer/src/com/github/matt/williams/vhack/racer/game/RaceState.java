package com.github.matt.williams.vhack.racer.game;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;

public class RaceState {

    private Map mMap;
    private List<Kart> mKarts = new ArrayList<Kart>();
    private List<Item> mBananas;
    private List<Item> mTreasure;

    public RaceState() {
        Bitmap mapBitmap = Bitmap.createBitmap(MapData.DATA, 64, 64, Bitmap.Config.ARGB_8888);
        mMap = new Map(mapBitmap);
        
        mBananas = new ArrayList<Item>();
        mBananas.add(new Item(mMap, 58, 42));
        mBananas.add(new Item(mMap, 45, 22));
        mBananas.add(new Item(mMap, 43, 16));
        mBananas.add(new Item(mMap, 25, 38));
        mBananas.add(new Item(mMap, 29, 13));
        mBananas.add(new Item(mMap, 32, 12));
        mBananas.add(new Item(mMap, 5, 17));
        mBananas.add(new Item(mMap, 7, 25));
        mBananas.add(new Item(mMap, 5, 49));
        
        mTreasure = new ArrayList<Item>();
        mTreasure.add(new Item(mMap, 55, 55));
        mTreasure.add(new Item(mMap, 58, 58));
        mTreasure.add(new Item(mMap, 52, 5));
        mTreasure.add(new Item(mMap, 49, 7));
        mTreasure.add(new Item(mMap, 22, 22));
        mTreasure.add(new Item(mMap, 19, 19));
        mTreasure.add(new Item(mMap, 8, 8));
        mTreasure.add(new Item(mMap, 5, 5));
    }
    
    public Kart addKart(String name, float x, float y) {
        Kart kart = new Kart(name, x, y, (float)(Math.PI / 2));
        mKarts.add(kart);
        return kart;
    }

    public Map getMap() {
        return mMap;
    }
    
    public List<Kart> getKarts() {
        return mKarts;
    }
    
    public List<Item> getBananas() {
        return mBananas;
    }
    
    public List<Item> getTreasure() {
        return mTreasure;
    }
    
    public void update() {
        for (Kart kart : mKarts) {
            kart.update(mMap);
            Item itemToDelete = null;
            for (Item item : mBananas) {
                if (kart.hit(item.getPosition())) {
                    kart.hitBanana();
                    itemToDelete = item;
                    break;
                }
            }
            if (itemToDelete != null) {
                mBananas.remove(itemToDelete);
            }
            itemToDelete = null;
            for (Item item : mTreasure) {
                if (kart.hit(item.getPosition())) {
                    kart.gotTreasure();
                    itemToDelete = item;
                    break;
                }
            }
            if (itemToDelete != null) {
                mTreasure.remove(itemToDelete);
            }
        }
        for (int kart1Index = 0; kart1Index < mKarts.size() - 1; kart1Index++) {
            Kart kart1 = mKarts.get(kart1Index);
            for (int kart2Index = kart1Index + 1; kart2Index < mKarts.size(); kart2Index++) {
                Kart kart2 = mKarts.get(kart2Index);
                if (kart1.hit(kart2.getPosition())) {
                    float[] kart1Position = kart1.getPosition();
                    float[] kart2Position = kart2.getPosition();
                    kart1.bumped(kart1Position[0] - kart2Position[0], kart1Position[1] - kart2Position[1]);
                    kart2.bumped(kart2Position[0] - kart1Position[0], kart2Position[1] - kart1Position[1]);
                }
            }
        }
    }

}
