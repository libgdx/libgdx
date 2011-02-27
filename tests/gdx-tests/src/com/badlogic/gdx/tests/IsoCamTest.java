package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.tests.utils.GdxTest;

public class IsoCamTest extends GdxTest {

	@Override public boolean needsGL20 () {
		return false;
	}

	Texture texture;
	Mesh mesh;
	OrthographicCamera cam;
//	Matrix4 projection;
//	Matrix4 view;
	
	@Override public void create() {
		mesh = new Mesh(true, 4, 6, new VertexAttribute(Usage.Position, 3, "a_pos"));
		mesh.setVertices(new float[] {
			-1, 0,  1,
			 1, 0,  1,
			 1, 0, -1,
			-1, 0, -1
		});
		mesh.setIndices(new short[] { 0, 1, 2, 2, 3, 0 });		
		cam = new OrthographicCamera(10, 10);		
		cam.position.set(3, 3, 3);
		cam.lookAt(0, 0, 0);			
	}
	
	@Override public void render() {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		cam.update();		
		Gdx.gl10.glMatrixMode(GL10.GL_PROJECTION);
		Gdx.gl10.glLoadMatrixf(cam.projection.val, 0);
		Gdx.gl10.glMatrixMode(GL10.GL_MODELVIEW);
		Gdx.gl10.glLoadMatrixf(cam.view.val, 0);
		
		mesh.render(GL10.GL_TRIANGLES);
	}
}
