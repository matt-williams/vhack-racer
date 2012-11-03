package com.github.matt.williams.vhack.racer.graphics;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;

import com.github.matt.williams.vhack.racer.R;

public class Skybox {    
    private Program mProgram;
    private Texture mTexture;
    
    public Skybox(Resources resources, int id) {
        mTexture = new Texture(BitmapFactory.decodeResource(resources, id));
        mProgram = new Program(new VertexShader(resources.getString(R.string.skyboxVertexShader)),
                               new FragmentShader(resources.getString(R.string.skyboxFragmentShader)));
        mProgram.setUniform("skybox", 0);
    }

    public void draw(float[] matrix) {
        mProgram.use();
        mProgram.setUniform("matrix", matrix);
        // TODO: Don't setVertexAttriib every frame
        mProgram.setVertexAttrib("xyz", new float[] {-50, 90, -50,
                                                      -50, -90, -50,
                                                      50, 90, -50,
                                                      50, -90, -50,
                                                      50, 90, 50,
                                                      50, -90, 50,
                                                      -50, 90, 50,
                                                      -50, -90, 50,
                                                      -50, 90, -50,
                                                      -50, -90, -50}, 3);
        mProgram.setVertexAttrib("uv", new float[] {0, 0,
                                                     0, 1,
                                                     0.25f, 0,
                                                     0.25f, 1,
                                                     0.5f, 0,
                                                     0.5f, 1,
                                                     0.75f, 0,
                                                     0.75f, 1,
                                                     1.0f, 0,
                                                     1.0f, 1}, 2);
        mTexture.use(GLES20.GL_TEXTURE0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 10);
        Utils.checkErrors("glDrawArrays");
    }
}
