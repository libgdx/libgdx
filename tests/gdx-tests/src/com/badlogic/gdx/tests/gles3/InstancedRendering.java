/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.tests.gles3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.InstanceBufferObject
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.VertexBufferObject;
import com.badlogic.gdx.tests.utils.GdxTest;

public class InstancedRendering extends GdxTest {
    ShaderProgram shader;
    VertexBufferObject vbo;
    InstanceBufferObject ibo;

    @Override
    public void create () {
        if (Gdx.gl30 == null) {
            throw new GdxRuntimeException("GLES 3.0 profile required for this test");
        }
        String vertexShader = "#version 330" +
                "layout(location = 0) in vec4 a_position;\n" +
                "layout(location = 1) in vec2 i_offset;\n" +
                "layout(location = 2) in vec4 i_color;\n" +
                "out vec4 v_color;\n" +
                "void main() {\n" +
                "gl_Position = a_position + vec4(i_offset, 0.0, 0.0);\n" +
                "v_color = i_color;\n" +
                "}";
        String fragmentShader = "#version 330" +
                "#ifdef GL_ES\n" +
                "precision mediump float;\n" +
                "#endif\n" +
                "in vec4 v_color;\n" +
                "out vec4 f_color;\n" +
                "void main() {\n" +
                "f_color = v_color;\n" +
                "}";
        shader = new ShaderProgram(vertexShader, fragmentShader);

        vbo = new VertexBufferObject(true, 6, new VertexAttribute(Usage.Position, 2, "a_position"));
        float[] vertices = new vertices[]{
                0.0f, 0.0f, 0.1f, 0.0f, 0.0f, 0.1f,
                0.1f, 0.0f, 0.1f, 0.1f, 0.0f, 0.1f
        };
        vbo.setVertices(vertices, 0, vertices.length);
        vbo.bind(shader);

        ibo = new InstanceBufferObject(true, 10_000, new VertexAttribute(Usage.Position, 2, "i_offset"),
                                                     new VertexAttribute(Usage.ColorUnpacked, 4, "i_color"));
        FloatBuffer offsets = BufferUtils.newFloatBuffer(10000 * 6)
        for (int x = 1; x <= 100; x++) {
            for (int y = 1; y <= 100; y++) {
                offsets.put(new float[]{
                        0.1f * (x - 1), 0.1f * (y - 1),
                        Math.random().toFloat(), Math.random().toFloat(), Math.random().toFloat(), 1f
                })
            }
        }
        ibo.setInstanceData(offsets, offsets.limit());
        ibo.bind(shader);
    }

    @Override
    public void render () {
        Gdx.gl20.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
        shader.begin();
        Gdx.gl30.glDrawArraysInstanced(GL30.GL_TRIANGLES, 0, 6, 10_000);
        shader.end();
    }
}
