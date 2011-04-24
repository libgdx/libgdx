package com.badlogic.gdx.graphics.g3d.test;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.jogl.JoglApplication;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.md2.MD2Loader;
import com.badlogic.gdx.graphics.g3d.model.keyframe.KeyframedModel;

public class MD2Viewer implements ApplicationListener {
	KeyframedModel model;	
	PerspectiveCamera cam;
	float angle = 0;	
	
	@Override public void create () {		
		model = new MD2Loader().load(Gdx.files.internal("data/knight.md2").read());
		Material material = new Material("knight", new TextureAttribute(new Texture("data/knight.jpg"), 0, "a_tex0"));
		model.setMaterial(material);	
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.far = 300;
		cam.position.set(0, 12, 50);
	}

	@Override public void resume () {
		
	}

	@Override public void render () {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL10.GL_DEPTH_TEST);
		Gdx.gl.glEnable(GL10.GL_TEXTURE_2D);
		
		cam.update();
		cam.apply(Gdx.gl10);
		
		angle += 45 * Gdx.graphics.getDeltaTime();
		Gdx.gl10.glRotatef(angle, 0, 1, 0);								
		model.render();
	}

	@Override public void resize (int width, int height) {
		
	}

	@Override public void pause () {
		
	}

	@Override public void dispose () {
		
	}

	public static void main(String[] argv) {
		new JoglApplication(new MD2Viewer(), "MD2 Viewer", 480, 320, false);
	}
}
