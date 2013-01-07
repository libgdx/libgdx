package com.badlogic.gdx.graphics.g3d.xoppa;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;

public class BatchRendererGLES10 implements BatchRenderer {
	final float[] lightColor = {1, 1, 1, 0};
	final float[] lightPosition = {2, 5, 10, 0};
	
	@Override
	public int compare (RenderInstance o1, RenderInstance o2) {
		return o1.distance > o2.distance ? 1 : (o1.distance < o2.distance ? -1 : 0);
	}

	@Override
	public ShaderProgram getShader (RenderInstance instance) {
		return null;
	}

	Material currentMaterial;
	@Override
	public void render (Camera camera, Array<RenderInstance> instances) {
		final GL10 gl = Gdx.gl10;
		gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glEnable(GL10.GL_LIGHTING);
		gl.glEnable(GL10.GL_COLOR_MATERIAL);
		gl.glEnable(GL10.GL_TEXTURE_2D);

		gl.glEnable(GL10.GL_LIGHT0);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightColor, 0);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPosition, 0);
		
		camera.apply(gl);
		currentMaterial = null;
		
		for (int i = 0; i < instances.size; i++) {
			final RenderInstance instance = instances.get(i);
			if (instance.material != null && instance.material != currentMaterial) {
				currentMaterial = instance.material;
				currentMaterial.bind();
			}
			gl.glPushMatrix();
			gl.glMultMatrixf(instance.transform.val, 0);
			instance.mesh.render(instance.primitiveType);
			gl.glPopMatrix();
		}
	}
}
