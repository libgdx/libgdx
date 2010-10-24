
package com.badlogic.gdx.tests;

import java.util.Random;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputListener;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Font;
import com.badlogic.gdx.graphics.Font.FontStyle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.tests.utils.GdxTest;

public class WaterRipples implements GdxTest, InputListener {
	static final short WIDTH = 50;
	static final short HEIGHT = 50;
	static final float INV_WIDTH = 1.0f / WIDTH;
	static final float INV_HEIGHT = 1.0f / HEIGHT;
	static final float DAMPING = 0.9f;
	static final float DISPLACEMENT = -10;
	static final float TICK = 0.033f;
	static final int RADIUS = 3;

	float accum;
	boolean initialized = false;
	PerspectiveCamera camera;
	SpriteBatch batch;
	Font font;
	Mesh mesh;
	Texture texture;
	Plane plane = new Plane(new Vector3(), new Vector3(1, 0, 0), new Vector3(0, 1, 0));
	Vector3 point = new Vector3();
	float[][] last;
	float[][] curr;
	float[][] intp;
	float[] vertices;

	@Override public void surfaceCreated () {

		if (!initialized) {
			camera = new PerspectiveCamera();
			camera.getPosition().set((WIDTH) / 2.0f, (HEIGHT) / 2.0f, WIDTH / 2.0f);
			camera.setViewport(Gdx.graphics.getWidth(), Gdx.graphics.getWidth());
			camera.setFov(90);
			camera.setNear(0.1f);
			camera.setFar(1000);
			last = new float[WIDTH + 1][HEIGHT + 1];
			curr = new float[WIDTH + 1][HEIGHT + 1];
			intp = new float[WIDTH + 1][HEIGHT + 1];
			vertices = new float[(WIDTH + 1) * (HEIGHT + 1) * 5];
			mesh = new Mesh(false, false, (WIDTH + 1) * (HEIGHT + 1), WIDTH * HEIGHT * 6, new VertexAttribute(
				VertexAttributes.Usage.Position, 3, "a_Position"), new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2,
				"a_texCoords"));
			texture = Gdx.graphics.newTexture(Gdx.files.getFileHandle("data/stones.jpg", FileType.Internal), TextureFilter.Linear,
				TextureFilter.Linear, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);

			createIndices();
			updateVertices(curr);
			initialized = true;

			batch = new SpriteBatch();
			font = Gdx.graphics.newFont("Arial", 12, FontStyle.Plain);

			Gdx.input.addInputListener(this);
		}
	}

	private void createIndices () {
		short[] indices = new short[WIDTH * HEIGHT * 6];
		int idx = 0;
		short vidx = 0;
		for (int y = 0; y < HEIGHT; y++) {
			vidx = (short)(y * (WIDTH + 1));

			for (int x = 0; x < WIDTH; x++) {
				indices[idx++] = vidx;
				indices[idx++] = (short)(vidx + 1);
				indices[idx++] = (short)(vidx + WIDTH + 1);

				indices[idx++] = (short)(vidx + 1);
				indices[idx++] = (short)(vidx + WIDTH + 2);
				indices[idx++] = (short)(vidx + WIDTH + 1);

				vidx++;
			}
		}

		mesh.setIndices(indices);
	}

	private void updateVertices (float[][] curr) {
		int idx = 0;
		for (int y = 0; y <= HEIGHT; y++) {
			for (int x = 0; x <= WIDTH; x++) {
				float xOffset = 0;
				float yOffset = 0;

				if (x > 0 && x < WIDTH && y > 0 && y < HEIGHT) {
					xOffset = (curr[x - 1][y] - curr[x + 1][y]);
					yOffset = (curr[x][y - 1] - curr[x][y + 1]);
				}

				vertices[idx++] = x;
				vertices[idx++] = y;
				vertices[idx++] = 0;
				vertices[idx++] = (x + xOffset) * INV_WIDTH;
				vertices[idx++] = (y + yOffset) * INV_HEIGHT;
			}
		}
		mesh.setVertices(vertices);
	}

	private void updateWater () {
		for (int y = 0; y < HEIGHT + 1; y++) {
			for (int x = 0; x < WIDTH + 1; x++) {
				if (x > 0 && x < WIDTH && y > 0 && y < HEIGHT) {
					curr[x][y] = (last[x - 1][y] + last[x + 1][y] + last[x][y + 1] + last[x][y - 1]) / 4 - curr[x][y];
				}
				curr[x][y] *= DAMPING;
			}
		}
	}

	private void interpolateWater (float alpha) {
		for (int y = 0; y < HEIGHT; y++) {
			for (int x = 0; x < WIDTH; x++) {
				intp[x][y] = (alpha * last[x][y] + (1 - alpha) * curr[x][y]);
			}
		}
	}

	private void touchWater (Vector3 point) {
		for (int y = Math.max(0, (int)point.y - RADIUS); y < Math.min(HEIGHT, (int)point.y + RADIUS); y++) {
			for (int x = Math.max(0, (int)point.x - RADIUS); x < Math.min(WIDTH, (int)point.x + RADIUS); x++) {
				float val = curr[x][y] + DISPLACEMENT
					* Math.max(0, (float)Math.cos(Math.PI / 2 * Math.sqrt(point.dst2(x, y, 0)) / RADIUS));
				if (val < DISPLACEMENT)
					val = DISPLACEMENT;
				else if (val > -DISPLACEMENT) val = -DISPLACEMENT;
				curr[x][y] = val;
			}
		}
	}

	long lastTick = System.nanoTime();
	Random rand = new Random();

	@Override public void render () {
		GL10 gl = Gdx.graphics.getGL10();
		gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		camera.update();
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadMatrixf(camera.getCombinedMatrix().val, 0);
		gl.glMatrixMode(GL10.GL_MODELVIEW);

		accum += Gdx.graphics.getDeltaTime();
		while (accum > TICK) {
			for (int i = 0; i < 5; i++) {
				if (Gdx.input.isTouched(i)) {
					Ray ray = camera.getPickRay(Gdx.input.getX(i),
						(int)(Gdx.input.getY(i) / (float)Gdx.graphics.getHeight() * Gdx.graphics.getWidth()));
					Intersector.intersectRayPlane(ray, plane, point);
					touchWater(point);
				}
			}

			updateWater();
			float[][] tmp = curr;
			curr = last;
			last = tmp;
			accum -= TICK;
		}

		float alpha = accum / TICK;
		interpolateWater(alpha);

		updateVertices(intp);

		gl.glEnable(GL10.GL_TEXTURE_2D);
		texture.bind();
		mesh.render(GL10.GL_TRIANGLES);

		batch.begin();
		batch.drawText(font, "fps: " + Gdx.graphics.getFramesPerSecond(), 10, 20, Color.WHITE);
		batch.end();
	}

	@Override public void dispose () {

	}

	@Override public boolean keyDown (int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override public boolean keyUp (int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override public boolean keyTyped (char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override public boolean touchDown (int x, int y, int pointer) {
// Ray ray = camera.getPickRay( x, (int)(y / (float)Gdx.graphics.getHeight() * Gdx.graphics.getWidth()));
// Intersector.intersectRayPlane( ray, plane, point );
// touchWater( point );
		return false;
	}

	@Override public boolean touchUp (int x, int y, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override public boolean touchDragged (int x, int y, int pointer) {
// Ray ray = camera.getPickRay( x, (int)(y / (float)Gdx.graphics.getHeight() * Gdx.graphics.getWidth()));
// Intersector.intersectRayPlane( ray, plane, point );
// touchWater( point );
		return false;
	}

	@Override public void surfaceChanged (int width, int height) {

	}

	@Override public boolean needsGL20 () {
		// TODO Auto-generated method stub
		return false;
	}

}
