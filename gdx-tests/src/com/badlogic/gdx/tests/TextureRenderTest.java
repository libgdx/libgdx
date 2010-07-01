package com.badlogic.gdx.tests;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.graphics.*;

import java.util.ArrayList;

public class TextureRenderTest implements RenderListener {

    private OrthographicCamera camera;
    private Mesh mesh;
    private Texture texture;

    private ArrayList<SimpleRect> rects = new ArrayList<SimpleRect>();
    Color color = new Color(Color.GREEN);


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

        this.mesh = new Mesh(app.getGraphics(), true, false, false, 6 * 4 * rects.size(), 0,
                new VertexAttribute(VertexAttributes.Usage.Position, 2, "a_position"),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord"));


        final float[] vertices = new float[rects.size() * 6 * 4];
        int idx = 0;

        for (int i = 0; i < rects.size(); i++) {
            SimpleRect rect = rects.get(i);

            float u = rect.x * invTexWidth;
            float v = rect.y * invTexHeight;
            float u2 = (rect.x + rect.width) * invTexWidth;
            float v2 = (rect.y + rect.height) * invTexHeight;
            float fx = rect.x;
            float fy = rect.y;
            float fx2 = (rect.x + rect.width);
            float fy2 = (rect.y -rect.height);

            vertices[idx++] = fx;
            vertices[idx++] = fy;
            vertices[idx++] = u;
            vertices[idx++] = v;

            vertices[idx++] = fx;
            vertices[idx++] = fy2;
            vertices[idx++] = u;
            vertices[idx++] = v2;

            vertices[idx++] = fx2;
            vertices[idx++] = fy2;
            vertices[idx++] = u2;
            vertices[idx++] = v2;

            vertices[idx++] = fx2;
            vertices[idx++] = fy2;
            vertices[idx++] = u2;
            vertices[idx++] = v2;

            vertices[idx++] = fx2;
            vertices[idx++] = fy;
            vertices[idx++] = u2;
            vertices[idx++] = v;

            vertices[idx++] = fx;
            vertices[idx++] = fy;
            vertices[idx++] = u;
            vertices[idx++] = v;


        }
        this.mesh.setVertices(vertices);

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
        gl.glLoadIdentity();

        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);


         gl.glColor4f(color.r, color.g, color.b, color.a);
        
         gl.glColor4f(Color.WHITE.r, Color.WHITE.g, Color.WHITE.b, 0.5F);

        texture.bind();


        for (int i = 0; i < rects.size(); i++) {
            SimpleRect rect = rects.get(i);
            gl.glPushMatrix();

//            float x = (rect.index + 1) * 60F;
            gl.glTranslatef(100, 100F, 0F);

            mesh.render(GL10.GL_TRIANGLES, rect.index * 24, 24);

            gl.glPopMatrix();
        }


    }

    @Override
    public void dispose(Application app) {

    }

    private ArrayList<SimpleRect> createRects() {
        ArrayList<SimpleRect> l = new ArrayList<SimpleRect>();
        l.add(new SimpleRect(0, 10, 0, 50, 50));
        l.add(new SimpleRect(1, 60, 0, 50, 50));
        l.add(new SimpleRect(2, 110, 0, 50, 50));
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


}
