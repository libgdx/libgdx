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
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.ScreenUtils;

public class ScreenCaptureTest extends GdxTest implements InputProcessor {

	private OrthographicCamera camera;
	private SpriteBatch spriteBatch;
	private TextureRegion screenCap;
	private Mesh triangle;
	
	private float rotation = 0;
	
	public void create() {
		Gdx.input.setInputProcessor(this);
		
		camera = new OrthographicCamera(800, 480);              
		
		triangle = new Mesh(true, 3, 3, 
				new VertexAttribute(Usage.Position, 3, "a_position"));
		triangle.setVertices(new float[] {
				-0.5f, -0.5f, 0, 
				0.5f, -0.5f, 0, 
				0, 0.5f, 0
				});
		triangle.setIndices(new short[] {0, 1, 2});
		
		spriteBatch = new SpriteBatch();
	}
	
	public void render() {
		GL10 gl = Gdx.graphics.getGL10();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		gl.glViewport(0, 0, 400, 480);
		
        gl.glMatrixMode( GL10.GL_PROJECTION );
        gl.glLoadMatrixf( camera.combined.val, 0 );
        gl.glMatrixMode( GL10.GL_MODELVIEW );
        gl.glLoadIdentity();

        rotation += rotation >= 360 ? -360 + Gdx.graphics.getDeltaTime() * 45f :
        	Gdx.graphics.getDeltaTime() * 45f;
		
        gl.glPushMatrix();
        	gl.glColor4f(1, 0, 0, 1);
        	gl.glRotatef(rotation, 1, 0, 0);
        	triangle.render(GL10.GL_TRIANGLES);
        gl.glPopMatrix();
        gl.glPushMatrix();
        	gl.glColor4f(0, 1, 0, 1);
        	gl.glRotatef(rotation, 0, 1, 0);
        	triangle.render(GL10.GL_TRIANGLES);
        gl.glPopMatrix();
        gl.glPushMatrix();
        	gl.glColor4f(0, 0, 1, 1);
        	gl.glRotatef(rotation, 0, 0, 1);
        	triangle.render(GL10.GL_TRIANGLES);
        gl.glPopMatrix();
        
        gl.glViewport(400, 0, 800, 480);
        if (screenCap != null) {
        	spriteBatch.begin();
        	spriteBatch.draw(screenCap, 0, 0);
            spriteBatch.end();
        }
	}
	
	public void dispose() {
		spriteBatch.dispose();
		triangle.dispose();
		if (screenCap != null) screenCap.getTexture().dispose();
	}
	
	@Override
	public boolean needsGL20() {
		return false;
	}

	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		if (screenCap != null) screenCap.getTexture().dispose();
		screenCap = null;
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		screenCap = ScreenUtils.getFrameBufferTexture();
		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		return false;
	}

	@Override
	public boolean touchMoved(int x, int y) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}

}
