/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
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
package com.badlogic.gdx.tests.desktop;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.backends.desktop.JoglApplication;
import com.badlogic.gdx.graphics.*;

import java.util.ArrayList;

public class TextureRenderTest implements RenderListener {

    private OrthographicCamera camera;
    private Mesh mesh;
    private Texture texture;

    private ArrayList<SimpleRect> rects = new ArrayList<SimpleRect>();


    @Override
    public void surfaceCreated(final Application app) {
        camera = new OrthographicCamera(app.getGraphics());
        camera.setViewport(480, 320);
        camera.getPosition().set(app.getGraphics().getWidth() / 2, app.getGraphics().getHeight() / 2, 0);

        Pixmap pixmap = app.getGraphics().newPixmap(app.getFiles().getFileHandle("data/badlogic.jpg", Files.FileType.Internal));
        texture = app.getGraphics().newTexture(pixmap, Texture.TextureFilter.Linear, Texture.TextureFilter.Linear, Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge, true);


        float invTexWidth = 1.0f / texture.getWidth();
        float invTexHeight = 1.0f / texture.getHeight();

        rects = createRects();

        this.mesh = new Mesh(app.getGraphics(), true, true, false, 4 * rects.size(), 12,
                new VertexAttribute(VertexAttributes.Usage.Position, 2, "a_position"),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord"));


        final float[] vertices = new float[rects.size() * 16];
        int index = 0;

        for (int i = 0; i < rects.size(); i++) {
            SimpleRect rect = rects.get(i);


            float u = rect.x * invTexWidth;
            float v = rect.y * invTexHeight;
            float u2 = (rect.x + rect.width) * invTexWidth;
            float v2 = (rect.y + rect.height) * invTexHeight;

            float w = rect.width / 2;
            float h = rect.height / 2;

            vertices[index++] = -w;
            vertices[index++] = -h;
            vertices[index++] = u;
            vertices[index++] = v2;

            vertices[index++] = w;
            vertices[index++] = -h;
            vertices[index++] = u2;
            vertices[index++] = v2;

            vertices[index++] = w;
            vertices[index++] = h;
            vertices[index++] = u2;
            vertices[index++] = v;

            vertices[index++] = -w;
            vertices[index++] = h;
            vertices[index++] = u;
            vertices[index++] = v;


        }
        this.mesh.setVertices(vertices);
        this.mesh.setIndices(new short[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11});

    }

    @Override
    public void surfaceChanged(Application app, int width, int height) {

    }

    @Override
    public void render(Application app) {

        GL10 gl = app.getGraphics().getGL10();
        gl.glViewport(0, 0, app.getGraphics().getWidth(), app.getGraphics().getHeight());
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        gl.glEnable(GL10.GL_TEXTURE_2D);

        camera.update();

        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadMatrixf(camera.getCombinedMatrix().val, 0);
        gl.glMatrixMode(GL10.GL_MODELVIEW);

        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE);

        gl.glLoadIdentity();


        texture.bind();

        for (int i = 0; i < rects.size(); i++) {
            SimpleRect rect = rects.get(i);

            gl.glPushMatrix();
            gl.glTranslatef(rect.index * 100 + 50, 100, 0F);
            mesh.render(GL10.GL_TRIANGLE_FAN, rect.index * 4, 4);
            gl.glPopMatrix();

        }

    }

    @Override
    public void dispose(Application app) {

    }

    private ArrayList<SimpleRect> createRects() {
        ArrayList<SimpleRect> l = new ArrayList<SimpleRect>();
        l.add(new SimpleRect(0, 0, 0, 50, 50));
        l.add(new SimpleRect(1, 50, 0, 50, 50));
        l.add(new SimpleRect(2, 100, 0, 50, 50));
        return l;
    }


    private static class SimpleRect {
        public int index;
        public float x;
        public float y;
        public float height;
        public float width;

        private SimpleRect(int index, float x, float y, float width, float height) {
            this.index = index;
            this.x = x;
            this.y = y;
            this.height = height;
            this.width = width;
        }
    }

    public static void main( String[] argv )
    {
    	JoglApplication app = new JoglApplication("TextureRender Test", 480, 320, false );
    	app.getGraphics().setRenderListener( new TextureRenderTest() );
    }
}
