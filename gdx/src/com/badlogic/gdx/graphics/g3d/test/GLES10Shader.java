package com.badlogic.gdx.graphics.g3d.test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.materials.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.materials.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class GLES10Shader implements Shader{
	public static int lightsCount = 2;
	public final static float ambient[] = { 0.3f, 0.3f, 0.3f, 1.0f };
	public final static Light defaultLight = new Light(1f, 1f, 1f, 1f, 0, 10f, 10f, 50f); 
	
	private Camera camera;
	private RenderContext context;
	private Matrix4 currentTransform;
	private Material currentMaterial;
	private Texture currentTexture0;
	private Mesh currentMesh;
	private Light lights[] = new Light[lightsCount];
	
	public GLES10Shader() {
		if (Gdx.gl10 == null)
			throw new GdxRuntimeException("This shader requires OpenGL ES 1.x");
	}
	
	@Override
	public boolean canRender(final Renderable renderable) {
		return true;
	}
	
	@Override
	public int compareTo(Shader other) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public boolean equals (Object obj) {
		return (obj instanceof GLES10Shader) ? equals((GLES10Shader)obj) : false;
	}
	
	public boolean equals (GLES10Shader obj) {
		return (obj == this);
	}
	
	@Override
	public void begin (final Camera camera, final RenderContext context) {
		this.context = context;
		this.camera = camera;
		context.setDepthTest(true, GL10.GL_LEQUAL);
		Gdx.gl10.glMatrixMode(GL10.GL_PROJECTION);
		Gdx.gl10.glLoadMatrixf(camera.combined.val, 0);
		if (lightsCount < 1)
			Gdx.gl10.glDisable(GL10.GL_LIGHTING);
		else {
			Gdx.gl10.glEnable(GL10.GL_LIGHTING);
			Gdx.gl10.glEnable(GL10.GL_COLOR_MATERIAL);
			for (int i = 0; i < lightsCount; i++)
				Gdx.gl10.glEnable(GL10.GL_LIGHT0 + i);
			for (int i = lightsCount; i < 8; i++)
				Gdx.gl10.glDisable(GL10.GL_LIGHT0 + i);
			Gdx.gl10.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, ambient, 0);
		}
		Gdx.gl10.glMatrixMode(GL10.GL_MODELVIEW);
	}

	private float[] lightVal = {0,0,0,0};
	private void bindLight(int num, Light light) {
		if (light == null && num == 0)
			light = defaultLight;
		if (lights[num] == light)
			return;
		lights[num] = light;
		if (light == null) {
			lightVal[0] = lightVal[1] = lightVal[2] = lightVal[3] = 0f;
			Gdx.gl10.glLightfv(GL10.GL_LIGHT0+num, GL10.GL_DIFFUSE, lightVal, 0);
		} else {
			lightVal[0] = light.color.r;
			lightVal[1] = light.color.g;
			lightVal[2] = light.color.b;
			lightVal[3] = light.color.a;
			Gdx.gl10.glLightfv(GL10.GL_LIGHT0+num, GL10.GL_DIFFUSE, lightVal, 0);
			lightVal[0] = light.position.x;
			lightVal[1] = light.position.y;
			lightVal[2] = light.position.z;
			lightVal[3] = 1f;
			Gdx.gl10.glPushMatrix();
			Gdx.gl10.glLoadIdentity();
			Gdx.gl10.glLightfv(GL10.GL_LIGHT0+num, GL10.GL_POSITION, lightVal, 0);
			Gdx.gl10.glPopMatrix();
		}
	}
	
	@Override
	public void render (final Renderable renderable) {
		if (currentMaterial != renderable.material) {
			currentMaterial = renderable.material;
			if (!currentMaterial.has(BlendingAttribute.Type))
				context.setBlending(false, GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			if (!currentMaterial.has(ColorAttribute.Diffuse))
				Gdx.gl10.glColor4f(1,1,1,1);
			if (!currentMaterial.has(TextureAttribute.Diffuse))
				Gdx.gl10.glDisable(GL10.GL_TEXTURE_2D);
			for (final Material.Attribute attribute : currentMaterial) {
				if (attribute.type == BlendingAttribute.Type)
					context.setBlending(true, ((BlendingAttribute)attribute).sourceFunction, ((BlendingAttribute)attribute).destFunction);
				else if (attribute.type == ColorAttribute.Diffuse)
					Gdx.gl10.glColor4f(((ColorAttribute)attribute).color.r, ((ColorAttribute)attribute).color.g, ((ColorAttribute)attribute).color.b, ((ColorAttribute)attribute).color.a);
				else if (attribute.type == TextureAttribute.Diffuse) {
					if (currentTexture0 != ((TextureAttribute)attribute).textureDescription.texture)
						(currentTexture0 = ((TextureAttribute)attribute).textureDescription.texture).bind(0);
					Gdx.gl10.glEnable(GL10.GL_TEXTURE_2D);
				}
			}
		}
		if (currentTransform != renderable.transform) {
			if (currentTransform != null)
				Gdx.gl10.glPopMatrix();
			currentTransform = renderable.transform;
			Gdx.gl10.glPushMatrix();
			Gdx.gl10.glLoadMatrixf(currentTransform.val, 0);
		}
		for (int i = 0; i < lightsCount; i++) {
			final Light light = (renderable.lights == null || renderable.lights.length <= i) ? null : renderable.lights[i];
			bindLight(i, light);
		}
		if (currentMesh != renderable.mesh) {
			if (currentMesh != null)
				currentMesh.unbind();
			(currentMesh = renderable.mesh).bind();
		}
		renderable.mesh.render(renderable.primitiveType, renderable.meshPartOffset, renderable.meshPartSize);
	}

	@Override
	public void end () {
		if (currentMesh != null)
			currentMesh.unbind();
		currentMesh = null;
		if (currentTransform != null)
			Gdx.gl10.glPopMatrix();
		currentTransform = null;
		currentTexture0 = null;
		currentMaterial = null;
		Gdx.gl10.glDisable(GL10.GL_LIGHTING);
	}

	@Override
	public void dispose () {
		// TODO Auto-generated method stub
		
	}
}
