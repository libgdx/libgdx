package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.loaders.ModelLoader;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.MathUtils;

public class FramebufferToTextureTest extends GdxTest {

	@Override public boolean needsGL20 () {
		return false;
	}

	Texture fbTexture;
	Texture texture;
	Mesh mesh;
	PerspectiveCamera cam;
	SpriteBatch batch;
	BitmapFont font;
	Color clearColor = new Color(0.2f, 0.2f, 0.2f, 1);
	float angle = 0;
	
	@Override public void create() {
		mesh = ModelLoader.loadObj(Gdx.files.internal("data/cube.obj").read());		
		texture = new Texture(Gdx.files.internal("data/badlogic.jpg"), true);
		texture.setFilter(TextureFilter.MipMap, TextureFilter.Linear);

		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(3, 3, 3);
		cam.direction.set(-1, -1, -1);
		batch = new SpriteBatch();
		font = new BitmapFont();
	}	
	
	@Override public void render() {
		GL10 gl = Gdx.graphics.getGL10();

		gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		gl.glClearColor(clearColor.g, clearColor.g, clearColor.b, clearColor.a);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		
		cam.update();
		cam.apply(gl);
		
		angle += 45 * Gdx.graphics.getDeltaTime();
		gl.glPushMatrix();
		gl.glRotatef(angle, 0, 1, 0);
		texture.bind();
		mesh.render(GL10.GL_TRIANGLES);
		gl.glPopMatrix();
		
		if(Gdx.input.justTouched() || fbTexture == null) {
//			if(fbTexture != null) fbTexture.dispose();
//			fbTexture = Texture.getFrameBufferTexture();
//			clearColor.set(MathUtils.random(), MathUtils.random(), MathUtils.random(), 1);
		}								
		
		batch.begin();
		if(fbTexture != null) {			
			batch.draw(fbTexture, 0, Gdx.graphics.getHeight() - 100, 100, 100, 0, 0, fbTexture.getWidth(), fbTexture.getHeight(), false, true);
		}
		font.draw(batch, "Touch screen to take a snapshot", 10, 40);
		batch.end();
	}
	
	@Override public void pause() {
		fbTexture = null;
	}
}
