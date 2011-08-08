package com.badlogic.gdx.graphics.g3d.test;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.backends.jogl.JoglApplication;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g3d.loaders.ModelLoaderRegistry;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer10;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

public class WolfensteinTest implements ApplicationListener {
	public static void main(String[] argv) {
		new JoglApplication(new WolfensteinTest(), "Wolfenstein", 480, 320, false);
	}

	PerspectiveCamera camera;
	StillModel model;
	ImmediateModeRenderer10 renderer;
	Texture texture;
	float[] triangles;
	short[] indices;
	
	@Override
	public void create() {
		model = ModelLoaderRegistry.loadStillModel(Gdx.files.internal("data/level.g3dt"));
		texture = new Texture(Gdx.files.internal("data/wall.png"), true);
		texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		model.setMaterial(new Material("mat", new TextureAttribute(texture, 0, "a_texCoord")));
		
		triangles = new float[model.subMeshes[0].mesh.getNumVertices() * model.subMeshes[0].mesh.getVertexSize() / 4];
		indices = new short[model.subMeshes[0].mesh.getNumIndices()];
		
		model.subMeshes[0].mesh.getVertices(triangles);
		model.subMeshes[0].mesh.getIndices(indices);
		
		camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());	
		camera.position.y = 1;
		
		renderer = new ImmediateModeRenderer10();
	}
	
	Vector3 movement = new Vector3();
	Vector3 intersection = new Vector3();
	Ray ray = new Ray(new Vector3(), new Vector3());
	private void processInput() {		movement.set(0, 0, 0);
		
		if(Gdx.input.isKeyPressed(Keys.W)) {
			movement.add(camera.direction.tmp().mul(Gdx.graphics.getDeltaTime()));
		}
		
		if(Gdx.input.isKeyPressed(Keys.S)) {
			movement.add(camera.direction.tmp().mul(-Gdx.graphics.getDeltaTime()));
		}
		
		if(Gdx.input.isKeyPressed(Keys.A)) {
			camera.rotate(20 * Gdx.graphics.getDeltaTime(), 0, 1, 0);
		}
		
		if(Gdx.input.isKeyPressed(Keys.D)) {
			camera.rotate(-20 * Gdx.graphics.getDeltaTime(), 0, 1, 0);
		}
		
		movement.mul(2);
		camera.position.add(movement);
		
		ray.origin.set(camera.position);
		ray.direction.set(camera.direction);
		
		if(Intersector.intersectRayTriangles(ray, triangles, indices, model.subMeshes[0].mesh.getVertexSize() / 4, intersection)) {
			Gdx.app.log("wolf", "intersected wall: " + intersection);
		}
	}


	@Override
	public void render() {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL10.GL_DEPTH_TEST);
		Gdx.gl.glEnable(GL10.GL_TEXTURE_2D);
		
		camera.update();
		camera.apply(Gdx.gl10);
		model.render();
	
		Gdx.gl.glDisable(GL10.GL_DEPTH_TEST);
		Gdx.gl10.glPointSize(5);
		renderer.begin(GL10.GL_POINTS);
		renderer.color(1, 0, 0, 1);
		renderer.vertex(intersection);
		renderer.end();
		Gdx.gl10.glPointSize(1);
		Gdx.gl10.glColor4f(1, 1, 1, 1);
		
		processInput();
	}
	
	@Override
	public void resume() {
		
	}

	@Override
	public void resize(int width, int height) {
		
	}

	@Override
	public void pause() {
		
	}

	@Override
	public void dispose() {		
	}
}
