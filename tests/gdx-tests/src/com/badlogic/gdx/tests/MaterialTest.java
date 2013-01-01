package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.loaders.ModelLoaderRegistry;
import com.badlogic.gdx.graphics.g3d.materials.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.materials.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.tests.utils.GdxTest;

public class MaterialTest extends GdxTest {
	
	float angleY = 0;
	
	StillModel model;
	
	TextureAttribute textureAttribute;
	ColorAttribute colorAttribute;
	BlendingAttribute blendingAttribute;

	Material material;
	
	Texture texture;
	
	Camera camera;

	@Override
	public void create () {
		model =  ModelLoaderRegistry.loadStillModel(Gdx.files.internal("data/cube.obj"));
		
		texture = new Texture(Gdx.files.internal("data/badlogic.jpg"), true);
		
		// Create material attributes. Each material can contain x-number of attributes.
		textureAttribute = new TextureAttribute(texture, 0, "Badlogic");
		colorAttribute = new ColorAttribute(Color.ORANGE, "Orange");
		blendingAttribute = new BlendingAttribute("Additive", GL10.GL_ONE, GL10.GL_ONE);

		// Assign material to model. If you pass an Material[] into setMaterials() it'll be assigned to 
		// SubMeshes accordingly.
		material = new Material();
		model.setMaterial(material);
		
		camera = new PerspectiveCamera(45, 4, 4);
		camera.position.set(3, 3, 3);
		camera.direction.set(-1, -1, -1);
		
		Gdx.input.setInputProcessor(this);
	}

	@Override
	public void render () {
		GL10 gl = Gdx.graphics.getGL10();

		gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		
		if(material.getNumberOfAttributes() == 1)
			gl.glEnable(GL10.GL_TEXTURE_2D);
		
		if(material.getNumberOfAttributes() == 3){
			gl.glDisable(GL10.GL_DEPTH_TEST);
			gl.glEnable(GL10.GL_BLEND);
		}
		else
			gl.glEnable(GL10.GL_DEPTH_TEST);
		
		camera.update();
		camera.apply(gl);
		
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		angleY += 30 * Gdx.graphics.getDeltaTime();
		gl.glRotatef(angleY, 0, 1, 0);
		
		// That's it. Materials are bound automatically on render
		model.render();
	}

	@Override
	public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		
		if(material.getNumberOfAttributes() == 0)
			material.addAttribute(textureAttribute);
		else if(material.getNumberOfAttributes() == 1)
			material.addAttribute(colorAttribute);
		else if(material.getNumberOfAttributes() == 2)
			material.addAttribute(blendingAttribute);
		else {
			GL10 gl = Gdx.gl10;
			
			// Reset state
			gl.glColor4f(1f, 1f, 1f, 1f);
			gl.glDisable(GL10.GL_TEXTURE_2D);
			gl.glDisable(GL10.GL_BLEND);
			
			material.clearAttributes();
		}
		
		return super.touchUp(screenX, screenY, pointer, button);
	}

	@Override
	public void dispose () {
		texture.dispose();
	}

	@Override
	public boolean needsGL20 () {
		return false;
	}
}
