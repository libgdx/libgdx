package com.badlogic.gdx.graphics.g3d.test;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.jogl.JoglApplication;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.loaders.g3d.G3dtLoader;
import com.badlogic.gdx.graphics.g3d.loaders.g3d.chunks.G3dLoader;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.keyframe.KeyframedAnimation;
import com.badlogic.gdx.graphics.g3d.model.keyframe.KeyframedModel;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.graphics.g3d.test.utils.PerspectiveCamController;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

public class QbobViewer implements ApplicationListener {
	PerspectiveCamera cam;
	KeyframedModel animModel;
	KeyframedAnimation anim;
	float animTime = 0;	
	
	StillModel model[] = new StillModel[4];
	Texture diffuse;
	Texture[] lightMaps = new Texture[4];
	FPSLogger fps = new FPSLogger();	
	PerspectiveCamController controller;
	SpriteBatch batch;
	BitmapFont font;	
	
	@Override public void create () {
		animModel = G3dtLoader.loadKeyframedModel(Gdx.files.internal("data/boy.g3dt"), true);
		anim = animModel.getAnimations()[0];
		Material material = new Material("default", new TextureAttribute(new Texture(Gdx.files.internal("data/boy.png")), 0, "tex0"));
		animModel.setMaterial(material);
		
		model[0] = G3dLoader.loadStillModel(Gdx.files.internal("data/qbob/test_section_01.dae.g3d"));
		lightMaps[0] = new Texture(Gdx.files.internal("data/qbob/world_blobbie_lm_01.jpg"), true);
		model[1] = G3dLoader.loadStillModel(Gdx.files.internal("data/qbob/test_section_02.dae.g3d"));
		lightMaps[1] = new Texture(Gdx.files.internal("data/qbob/world_blobbie_lm_02.jpg"), true);
		model[2] = G3dLoader.loadStillModel(Gdx.files.internal("data/qbob/test_section_03.dae.g3d"));
		lightMaps[2] = new Texture(Gdx.files.internal("data/qbob/world_blobbie_lm_03.jpg"), true);
		model[3] = G3dLoader.loadStillModel(Gdx.files.internal("data/qbob/test_section_04.dae.g3d"));
		lightMaps[3] = new Texture(Gdx.files.internal("data/qbob/world_blobbie_lm_04.jpg"), true);
		
		diffuse = new Texture(Gdx.files.internal("data/qbob/world_blobbie_blocks.png"), true);
																		
		cam = new PerspectiveCamera(60, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(30, 10, 85f);
		cam.direction.set(0,0,-1);
		cam.up.set(0,1,0);
		cam.near = 10f;
		cam.far = 1000;			
		
		controller = new PerspectiveCamController(cam);
		Gdx.input.setInputProcessor(controller);
		
		batch = new SpriteBatch();
		font = new BitmapFont();
	}	

	@Override public void resume () {
		
	}
	
	@Override public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);			
						
		cam.update();
		cam.apply(Gdx.gl10);						
		
		Gdx.gl.glEnable(GL10.GL_CULL_FACE);
		Gdx.gl.glEnable(GL10.GL_DEPTH_TEST);
				
		Gdx.gl.glActiveTexture(GL10.GL_TEXTURE0);
		Gdx.gl.glEnable(GL10.GL_TEXTURE_2D);
		diffuse.bind();
		diffuse.setFilter(TextureFilter.MipMapNearestNearest, TextureFilter.Linear);				
		
		Gdx.gl.glActiveTexture(GL10.GL_TEXTURE1);
		Gdx.gl.glEnable(GL10.GL_TEXTURE_2D);
		
		lightMaps[0].bind();
		lightMaps[0].setFilter(TextureFilter.MipMapNearestNearest, TextureFilter.Linear);				
		setCombiners();
		
		model[0].render();
		lightMaps[1].bind();
		lightMaps[1].setFilter(TextureFilter.MipMapNearestNearest, TextureFilter.Linear);
		setCombiners();
		
		model[1].render();
		lightMaps[2].bind();
		lightMaps[2].setFilter(TextureFilter.MipMapNearestNearest, TextureFilter.Linear);		
		setCombiners();
				
		model[2].render();
		lightMaps[3].bind();
		lightMaps[3].setFilter(TextureFilter.MipMapNearestNearest, TextureFilter.Linear);		
		setCombiners();
		model[3].render();
		
		Gdx.gl.glActiveTexture(GL10.GL_TEXTURE1);
		Gdx.gl.glDisable(GL10.GL_TEXTURE_2D);
		Gdx.gl.glActiveTexture(GL10.GL_TEXTURE0);
		Gdx.gl.glDisable(GL10.GL_CULL_FACE);
		Gdx.gl11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		
		Gdx.gl.glDisable(GL10.GL_BLEND);
		
		
		animTime += Gdx.graphics.getDeltaTime();
		if(animTime > anim.totalDuration - anim.frameDuration) animTime = 0;		
		animModel.setAnimation(anim.name, animTime, true);
				
		Gdx.gl10.glPushMatrix();
		Gdx.gl10.glTranslatef(cam.position.x, cam.position.y, 6);
		animModel.render();		
		Gdx.gl10.glPopMatrix();			
						
		Gdx.gl.glDisable(GL10.GL_DEPTH_TEST);
		batch.begin();
		font.draw(batch, "fps: " + Gdx.graphics.getFramesPerSecond(), 10, 20);
		batch.end();			
		
		fps.log();	
	}
		
	private void setCombiners() {
		Gdx.gl11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_COMBINE);
		Gdx.gl11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_COMBINE_RGB, GL11.GL_ADD_SIGNED);
		Gdx.gl11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_SRC0_RGB, GL11.GL_PREVIOUS);
		Gdx.gl11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_SRC1_RGB, GL11.GL_TEXTURE);	
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
