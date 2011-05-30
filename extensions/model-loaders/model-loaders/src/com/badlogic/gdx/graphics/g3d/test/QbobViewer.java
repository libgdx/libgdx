package com.badlogic.gdx.graphics.g3d.test;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.jogl.JoglApplication;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.loaders.g3d.chunks.G3dLoader;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;

public class QbobViewer implements ApplicationListener {
	PerspectiveCamera cam;
	StillModel model[] = new StillModel[4];
	Texture diffuse;
	Texture[] lightMaps = new Texture[4];
	FPSLogger fps = new FPSLogger();	
	PerspectiveCamController controller;
	SpriteBatch batch;
	BitmapFont font;
	
	@Override public void create () {		
		for(int i = 0; i < 4; i++) {
			model[i] = G3dLoader.loadStillModel(Gdx.files.internal("data/blobbie_world_test.dae.g3d"));
			lightMaps[i] = new Texture(Gdx.files.internal("data/blobbie_world_test_lightmap_256.jpg"), true);
		}
		
		diffuse = new Texture(Gdx.files.internal("data/world_blobbie_blocks.png"), true);
							
									
		cam = new PerspectiveCamera(60, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(30, 10, 85f);
		cam.direction.set(0,0,-1);
		cam.up.set(0,1,0);
		cam.near = 0.1f;
		cam.far = 1000;			
		
		controller = new PerspectiveCamController(cam);
		Gdx.input.setInputProcessor(controller);
		
		batch = new SpriteBatch();
		font = new BitmapFont();
	}	

	@Override public void resume () {
		
	}

	float[] lightColor = {1, 1, 1, 0};
	float[] lightPosition = {2, 5, 10, 0};
	@Override public void render () {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL10.GL_DEPTH_TEST);		
						
		cam.update();
		cam.apply(Gdx.gl10);						
		
		Gdx.gl.glEnable(GL10.GL_CULL_FACE);
				
		Gdx.gl.glActiveTexture(GL10.GL_TEXTURE0);
		Gdx.gl.glEnable(GL10.GL_TEXTURE_2D);
		diffuse.bind();		
		
		Gdx.gl.glActiveTexture(GL10.GL_TEXTURE1);
		Gdx.gl.glEnable(GL10.GL_TEXTURE_2D);
		for(int i = 0; i < 4; i++) {
			Gdx.gl10.glPushMatrix();
			if(i == 0 || i == 1) {
				Gdx.gl10.glTranslatef(i * 14 * 12f, 0, 0);
			} else {
				Gdx.gl10.glTranslatef((i-2) * 14 * 12f, 11 * 12f, 0);
			}
			lightMaps[i].bind();			
			model[i].render();
			Gdx.gl10.glPopMatrix();
		}		
		
		Gdx.gl.glActiveTexture(GL10.GL_TEXTURE1);
		Gdx.gl.glDisable(GL10.GL_TEXTURE_2D);
		Gdx.gl.glActiveTexture(GL10.GL_TEXTURE0);
		Gdx.gl.glDisable(GL10.GL_CULL_FACE);
						
		batch.begin();
		font.draw(batch, "fps: " + Gdx.graphics.getFramesPerSecond(), 10, 20);
		batch.end();
		
		fps.log();	
	}
		
	@Override public void resize (int width, int height) {
		
	}

	@Override public void pause () {
		
	}

	@Override public void dispose () {		
	}	
	
	public static void main(String[] argv) {
		new JoglApplication(new QbobViewer(), "Qbob Viewer", 800, 480, false);
	}
}
