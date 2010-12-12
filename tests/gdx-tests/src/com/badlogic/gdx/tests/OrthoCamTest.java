package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.tests.utils.GdxTest;

public class OrthoCamTest extends GdxTest {

	@Override public boolean needsGL20 () {
		return false;
	}

	static final int WIDTH = 320;
	static final int HEIGHT = 480;	
	OrthographicCamera cam;
	Rectangle viewport;
	Mesh mesh;
	
	public void create() {
		mesh = new Mesh(true, 4, 6, new VertexAttribute(Usage.Position, 2, "a_pos"),
											 new VertexAttribute(Usage.Color, 4, "a_col"));
		mesh.setVertices( new float[] {0, 0, 1, 0, 0, 1,
								WIDTH, 0, 0, 1, 0, 1,
								WIDTH, HEIGHT, 0, 0, 1, 1,
								0, HEIGHT, 1, 0, 1, 1 });
		mesh.setIndices( new short[] { 0, 1, 2, 2, 3, 0 } );
		
		cam = new OrthographicCamera();
		cam.setViewport(WIDTH, HEIGHT);
		cam.getPosition().set(WIDTH / 2, HEIGHT / 2, 0);
		
		calculateViewport();
	}
	
	private void calculateViewport() {
		viewport = new Rectangle();
		if(Gdx.graphics.getWidth() > Gdx.graphics.getHeight()) {
			float aspect = (float)Gdx.graphics.getHeight() / HEIGHT;
			viewport.width = WIDTH * aspect;
			viewport.height = Gdx.graphics.getHeight();
			viewport.x = Gdx.graphics.getWidth() / 2 - viewport.width / 2;
			viewport.y = 0;					
		} else {
			float aspect = (float)Gdx.graphics.getWidth() / WIDTH;
			viewport.width = Gdx.graphics.getWidth();
			viewport.height = HEIGHT * aspect;
			viewport.x = 0;
			viewport.y = Gdx.graphics.getHeight() / 2 - viewport.height / 2;					
		}	
	}	
	
	public void resize(int width, int height) {
		calculateViewport();
	}
	
	public void render() {
		GL10 gl = Gdx.gl10;
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		gl.glViewport((int)viewport.x, (int)viewport.y, (int)viewport.width, (int)viewport.height);
		
		cam.setMatrices();
		mesh.render(GL10.GL_TRIANGLES);
	}
}
