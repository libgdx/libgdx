package com.badlogic.gdx.graphics.g3d.decals;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;

/**
 * <p>Single Group strategy (all Decals same) using Z-buffer to render using screen door transparency to avoid having
 * to presort the decals. Materials are still sorted.</p>
 * <p>Avoids artifacts when the Decals are sufficiently proximate
 * and orientated such that sorting fails, and also from transparent decals overlapping
 * in plane (z-collisions) or by orientation.
 * </p>
 */
public class AlphaTestGroupStrategy implements GroupStrategy, Disposable {
    private static final int GROUP_ALPHA_TEST = 0;

    final Pool<Array<Decal>>                     arrayPool      = new Pool<Array<Decal>>(16) {
        @Override
        protected Array<Decal> newObject() {
            return new Array();
        }
    };
    final Array<Array<Decal>>                    usedArrays     = new Array<Array<Decal>>();
    final ObjectMap<DecalMaterial, Array<Decal>> materialGroups = new ObjectMap<DecalMaterial, Array<Decal>>();

    Camera        camera;
    ShaderProgram shader;

    public AlphaTestGroupStrategy(final Camera camera) {
        this.camera = camera;
        createDefaultShader();
    }

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    @Override
    public int decideGroup(Decal decal) {
        return GROUP_ALPHA_TEST;
    }

    @Override
    public void beforeGroup(int group,
                            Array<Decal> contents) {
        for (int i = 0, n = contents.size; i < n; i++) {
            Decal        decal         = contents.get(i);
            Array<Decal> materialGroup = materialGroups.get(decal.getMaterial());
            if (materialGroup == null) {
                materialGroup = arrayPool.obtain();
                materialGroup.clear();
                usedArrays.add(materialGroup);
                materialGroups.put(decal.getMaterial(),
                                   materialGroup);
            }
            materialGroup.add(decal);
        }

        contents.clear();
        for (Array<Decal> materialGroup : materialGroups.values()) {
            contents.addAll(materialGroup);
        }

        materialGroups.clear();
        arrayPool.freeAll(usedArrays);
        usedArrays.clear();
    }

    @Override
    public void afterGroup(int group) {
    }

    @Override
    public void beforeGroups() {

        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        shader.begin();
        shader.setUniformMatrix("u_projectionViewMatrix",
                                camera.combined);
        shader.setUniformi("u_texture",
                           0);
    }

    @Override
    public void afterGroups() {
        shader.end();
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
    }

    private void createDefaultShader() {
        String vertexShader = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
                              + "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
                              + "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
                              + "uniform mat4 u_projectionViewMatrix;\n" //
                              + "varying vec4 v_color;\n" //
                              + "varying vec2 v_texCoords;\n" //
                              + "\n" //
                              + "void main()\n" //
                              + "{\n" //
                              + "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
                              + "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
                              + "   gl_Position =  u_projectionViewMatrix * " + ShaderProgram.POSITION_ATTRIBUTE + ";"
                              + "\n" //
                              + "}\n";
        String fragmentShader = "#ifdef GL_ES\n" //
                                + "precision mediump float;\n" //
                                + "#endif\n" //
                                + "varying vec4 v_color;\n" //
                                + "varying vec2 v_texCoords;\n" //
                                + "uniform sampler2D u_texture;\n" //
                                + "float[] bayerMatrix ={1.0f / 17.0f,  9.0f / 17.0f,  3.0f / 17.0f, 11.0f / 17.0f," //
                                + "13.0f / 17.0f,  5.0f / 17.0f, 15.0f / 17.0f,  7.0f / 17.0f,"//
                                + "4.0f / 17.0f, 12.0f / 17.0f,  2.0f / 17.0f, 10.0f / 17.0f, " //
                                + "16.0f / 17.0f,  8.0f / 17.0f, 14.0f /17.0f,  6.0f / 17" + ".0f};\n" //
                                + "\n" //
                                + "void main()\n"//
                                + "{\n" //
                                + "int xPos = int(gl_FragCoord.x) % 4; \n" //
                                + "int yPos = int(gl_FragCoord.y) % 4; \n" //
                                + "float stipple= bayerMatrix[ xPos * (yPos*4) ]; \n" //
                                + "vec4 tex2D =  texture2D(u_texture, v_texCoords); \n" //
                                + "vec4 tinted = v_color*tex2D; \n" //
                                + "if( tinted.a <= stipple ) { discard; }\n" //
                                + "else\n" //
                                + "  { gl_FragColor = tinted; }\n" //
                                + "}";

        shader = new ShaderProgram(vertexShader,
                                   fragmentShader);
        if (shader.isCompiled() == false)
            throw new IllegalArgumentException("couldn't compile shader: " + shader.getLog());
    }

    @Override
    public ShaderProgram getGroupShader(int group) {
        return shader;
    }

    @Override
    public void dispose() {
        if (shader != null) shader.dispose();
    }
}


