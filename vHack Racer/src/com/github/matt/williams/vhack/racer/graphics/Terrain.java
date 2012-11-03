package com.github.matt.williams.vhack.racer.graphics;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;

import com.github.matt.williams.vhack.racer.R;
import com.github.matt.williams.vhack.racer.game.MapData;

public class Terrain {
    private Texture mTerrainTexture;
    private Texture mMapTexture;
    private Program mMapProgram;

    public Terrain(Resources resources) {
        mTerrainTexture = new Texture(BitmapFactory.decodeResource(resources, R.drawable.terrain));
//      Bitmap mapBitmap = BitmapFactory.decodeStream(resources.openRawResource(R.raw.map));
        Bitmap mapBitmap = Bitmap.createBitmap(MapData.DATA, 64, 64, Bitmap.Config.ARGB_8888);
        mMapTexture = new Texture(mapBitmap);
        mMapProgram = new Program(new VertexShader(resources.getString(R.string.mapVertexShader)),
                                  new FragmentShader(resources.getString(R.string.mapFragmentShader)));
        mTerrainTexture.use(GLES20.GL_TEXTURE0);
        mMapProgram.setUniform("terrain", 0);
        mMapProgram.setUniform("terrainSize", 1.0f/40, 1.0f/40);//Should be20.0f/mTerrainTexture.getWidth(), 20.0f/mTerrainTexture.getHeight()); but getWidth() and getHeight() return 1200!
        mMapTexture.use(GLES20.GL_TEXTURE1);
        mMapProgram.setUniform("map", 1);
        mMapProgram.setUniform("mapSize", 1.0f/mMapTexture.getWidth(), 1.0f/mMapTexture.getHeight());

    }

    public void draw(float[] matrix) {
        mMapProgram.setUniform("matrix", matrix);
        mTerrainTexture.use(GLES20.GL_TEXTURE0);
        mMapTexture.use(GLES20.GL_TEXTURE1);
        mMapProgram.use();
        mMapProgram.setVertexAttrib("xz", new float[] {-100, -100, 100, -100, -100, 100, 100, 100}, 2);
        mMapProgram.setVertexAttrib("uv", new float[] {-1, -1, 2, -1, -1, 2, 2, 2}, 2);
        mMapProgram.setUniform("terrain", 0);
        mMapProgram.setUniform("terrainSize", 1.0f / 40, 1.0f / 40);//Should be20.0f/mTerrainTexture.getWidth(), 20.0f/mTerrainTexture.getHeight()); but getWidth() and getHeight() return 1200!
        mMapProgram.setUniform("map", 1);
        mMapProgram.setUniform("mapSize", 1.0f / mMapTexture.getWidth(), 1.0f / mMapTexture.getHeight());
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        Utils.checkErrors("glDrawArrays");        
    }
}
