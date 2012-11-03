package com.github.matt.williams.vhack.racer.graphics;

import java.util.List;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;

import com.github.matt.williams.vhack.racer.R;
import com.github.matt.williams.vhack.racer.game.Item;
import com.github.matt.williams.vhack.racer.game.Kart;

public class KartManager {
    private static final float[] UV_COORDS = new float[768];
    static {
        for (int coordIndex = 0; coordIndex < UV_COORDS.length; coordIndex += 12) {
            UV_COORDS[coordIndex] = 0;
            UV_COORDS[coordIndex + 1] = 1;
            UV_COORDS[coordIndex + 2] = 1;
            UV_COORDS[coordIndex + 3] = 1;
            UV_COORDS[coordIndex + 4] = 1;
            UV_COORDS[coordIndex + 5] = 0;
            UV_COORDS[coordIndex + 6] = 1;
            UV_COORDS[coordIndex + 7] = 0;
            UV_COORDS[coordIndex + 8] = 0;
            UV_COORDS[coordIndex + 9] = 1;
            UV_COORDS[coordIndex + 10] = 0;
            UV_COORDS[coordIndex + 11] = 0;
        }
    }
    
    private Texture mTexture;
    private Program mProgram;

    public KartManager(Resources resources, int id) {
        mTexture = new Texture(BitmapFactory.decodeResource(resources,  id), GLES20.GL_REPEAT);
        mProgram = new Program(new VertexShader(resources.getString(R.string.tuxVertexShader)),
                                  new FragmentShader(resources.getString(R.string.pointFragmentShader)));
        mTexture.use(GLES20.GL_TEXTURE0);
        mProgram.setUniform("billboard", 0);
    }

    public void draw(Camera camera, List<Kart> karts) {
        mTexture.use(GLES20.GL_TEXTURE0);
        mProgram.setUniform("rotationTranslation", camera.getRotationTranslationMatrix());
        mProgram.setUniform("projection", camera.getProjectionMatrix());
        mProgram.setUniform("orientation", camera.getR());
        float[] points = new float[4 * karts.size()];
        int pointIndex = 0;
        for (Kart kart : karts) {
            points[pointIndex++] = -kart.getPosition()[0];
            points[pointIndex++] = -kart.getPosition()[1];
            points[pointIndex++] = kart.getPosition()[2];
            points[pointIndex++] = kart.getOrientation();
        }        
        float[] vertices = pointsToVertices(points, 4);
        mProgram.setVertexAttrib("xzyr", vertices, 4);
        mProgram.setVertexAttrib("uv", UV_COORDS, 2);
        mProgram.use();
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertices.length / 4);
        Utils.checkErrors("glDrawArrays");
    }
    
    private float[] pointsToVertices(float[] points, int size) {
        float[] vertices = new float[points.length * 6];
        int vertexIndex = 0;
        for (int pointIndex = 0; pointIndex < points.length / size; pointIndex++) {
            for (int index = 0; index < 6; index++) {
                for (int subIndex = 0; subIndex < size; subIndex++) {
                    vertices[vertexIndex++] = points[pointIndex * size + subIndex];
                }
            }
        }
        return vertices;
    }
}
