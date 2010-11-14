package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.VertexArray;
import com.badlogic.gdx.graphics.glutils.VertexBufferObject;
import com.badlogic.gdx.graphics.glutils.VertexBufferObjectSubData;
import com.badlogic.gdx.graphics.glutils.VertexData;
import com.badlogic.gdx.tests.utils.GdxTest;

public class VBOVATest extends GdxTest {

	static final int TRIANGLES = 10000;
	VertexBufferObject vbo;
	VertexBufferObjectSubData vbosd;
	VertexArray va;
	VertexData buffer;
	float[] vertices;
	int mode = 0;
	long startTime = 0;
	int frames = 0;
	boolean isStatic = false;
	
	@Override
	public void create() {
		VertexAttribute[] attributes = { new VertexAttribute(Usage.Position, 3, "a_pos")};
		vbo = new VertexBufferObject(false, TRIANGLES*3, attributes);
		vbosd = new VertexBufferObjectSubData(false, TRIANGLES*3, attributes);
		va = new VertexArray(TRIANGLES*3, attributes);
		vertices = new float[TRIANGLES*3*3];
		
		int len = vertices.length;
		float col = Color.WHITE.toFloatBits();
		for(int i = 0; i < len; i+=9) {
			float x = (float)Math.random() * 2 - 1f;
			float y = (float)Math.random() * 2 - 1f;
			vertices[i+0] = -.01f + x; vertices[i+1] = -.01f + y; vertices[i+2] = 0;
			vertices[i+3] = .01f + x; vertices[i+4] = -.01f + y; vertices[i+5] = 0;
			vertices[i+6] = 0f + x; vertices[i+7] = .01f + y; vertices[i+8] = 0;
		}
		
		startTime = System.nanoTime();
	}
	
	@Override
	public void render() {
		Gdx.gl11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		
		switch(mode) {
		case 0:
			buffer = vbo;
			Gdx.gl11.glColor4f(1, 0, 0, 1);
			break;
		case 1:
			buffer = vbosd;
			Gdx.gl11.glColor4f(0, 1, 0, 1);
			break;
		case 2:
			buffer = va;
			Gdx.gl11.glColor4f(0, 0, 1, 1);
			break;
		}
		
		buffer.bind();
		if(!isStatic)
			buffer.setVertices(vertices, 0, vertices.length);
		Gdx.gl11.glDrawArrays(GL11.GL_TRIANGLES, 0, TRIANGLES*3);
		buffer.unbind();
		
		long endTime = System.nanoTime();
		if(endTime-startTime >= 4000000000l ) {
			double secs = (endTime-startTime)/1000000000.0;
			double fps = frames / secs;
			Gdx.app.log("VBOVATest", "mode " + buffer.getClass().getSimpleName() + ", static: " + isStatic + ", fps: " + fps);
			mode++;
			if( mode > 2) {
				mode = 0;
				isStatic = !isStatic;
			}
			startTime = System.nanoTime();
			frames = 0;
		}
		
		frames++;
	}
	
	
	@Override
	public boolean needsGL20() {
		return false;
	}

}
