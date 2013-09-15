package com.badlogic.gdx.graphics.g3d.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.lights.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.lights.Lights;
import com.badlogic.gdx.graphics.g3d.lights.PointLight;
import com.badlogic.gdx.graphics.g3d.materials.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.materials.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.materials.IntAttribute;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.g3d.utils.TextureDescriptor;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class GLES10Shader implements Shader{
	private Camera camera;
	private RenderContext context;
	private Matrix4 currentTransform;
	private Material currentMaterial;
	private Texture currentTexture0;
	private Mesh currentMesh;
	
	/** Set to 0 to disable culling */
	public static int defaultCullFace = GL10.GL_BACK;

	public GLES10Shader() {
		if (Gdx.gl10 == null)
			throw new GdxRuntimeException("This shader requires OpenGL ES 1.x");
	}
	
	@Override
	public void init () {
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
		context.setDepthTest(GL10.GL_LEQUAL, 0, 1);
		context.setDepthMask(true);
		Gdx.gl10.glMatrixMode(GL10.GL_PROJECTION);
		Gdx.gl10.glLoadMatrixf(camera.combined.val, 0);
		Gdx.gl10.glMatrixMode(GL10.GL_MODELVIEW);
	}

	private final float[] lightVal = {0,0,0,0};
	private final float[] zeroVal4 = {0,0,0,0};
	private final float[] oneVal4  = {1,1,1,1};
	private void bindLights(Lights lights) {
		if (lights == null) {
			Gdx.gl10.glDisable(GL10.GL_LIGHTING);
			return;
		}
		Gdx.gl10.glEnable(GL10.GL_LIGHTING);
		Gdx.gl10.glLightModelfv(GL10.GL_LIGHT_MODEL_AMBIENT, getValues(lightVal, lights.ambientLight), 0);
		Gdx.gl10.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, zeroVal4, 0);
		int idx=0;
		Gdx.gl10.glPushMatrix();
		Gdx.gl10.glLoadIdentity();
		for (int i = 0; i < lights.directionalLights.size && idx < 8; i++) {
			final DirectionalLight light = lights.directionalLights.get(i);
			Gdx.gl10.glEnable(GL10.GL_LIGHT0+idx);
			Gdx.gl10.glLightfv(GL10.GL_LIGHT0+idx, GL10.GL_DIFFUSE, getValues(lightVal, light.color), 0);
			Gdx.gl10.glLightfv(GL10.GL_LIGHT0+idx, GL10.GL_POSITION, getValues(lightVal, -light.direction.x, -light.direction.y, -light.direction.z, 0f), 0);
			Gdx.gl10.glLightf(GL10.GL_LIGHT0+idx, GL10.GL_SPOT_CUTOFF, 180f);
			Gdx.gl10.glLightf(GL10.GL_LIGHT0+idx, GL10.GL_CONSTANT_ATTENUATION, 1f);
			Gdx.gl10.glLightf(GL10.GL_LIGHT0+idx, GL10.GL_LINEAR_ATTENUATION, 0f);
			Gdx.gl10.glLightf(GL10.GL_LIGHT0+idx, GL10.GL_QUADRATIC_ATTENUATION, 0f);
			idx++;
		}
		for (int i = 0; i < lights.pointLights.size && idx < 8; i++) {
			Gdx.gl10.glEnable(GL10.GL_LIGHT0+idx);
			final PointLight light = lights.pointLights.get(i);
			Gdx.gl10.glLightfv(GL10.GL_LIGHT0+idx, GL10.GL_DIFFUSE, getValues(lightVal, light.color), 0);
			Gdx.gl10.glLightfv(GL10.GL_LIGHT0+idx, GL10.GL_POSITION, getValues(lightVal, light.position.x, light.position.y, light.position.z, 1f), 0);
			Gdx.gl10.glLightf(GL10.GL_LIGHT0+idx, GL10.GL_SPOT_CUTOFF, 180f);
			Gdx.gl10.glLightf(GL10.GL_LIGHT0+idx, GL10.GL_CONSTANT_ATTENUATION, 0f);
			Gdx.gl10.glLightf(GL10.GL_LIGHT0+idx, GL10.GL_LINEAR_ATTENUATION, 0f);
			Gdx.gl10.glLightf(GL10.GL_LIGHT0+idx, GL10.GL_QUADRATIC_ATTENUATION, 1f/light.intensity);
			idx++;
		}
		while(idx < 8)
			Gdx.gl10.glDisable(GL10.GL_LIGHT0+(idx++));
		Gdx.gl10.glPopMatrix();
	}
	
	private final static float[] getValues(final float out[], final float v0, final float v1, final float v2, final float v3) {
		out[0] = v0;
		out[1] = v1;
		out[2] = v2;
		out[3] = v3;
		return out;
	}
	
	private final static float[] getValues(final float out[], final Color color) {
		return getValues(out, color.r, color.g, color.b, color.a);
	}
	
	@Override
	public void render (final Renderable renderable) {
		if (currentMaterial != renderable.material) {
			currentMaterial = renderable.material;
			if (!currentMaterial.has(BlendingAttribute.Type))
				context.setBlending(false, GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			if (!currentMaterial.has(ColorAttribute.Diffuse)) {
				Gdx.gl10.glColor4f(1,1,1,1);
				if (renderable.lights != null)
					Gdx.gl10.glDisable(GL10.GL_COLOR_MATERIAL);
			} if (!currentMaterial.has(TextureAttribute.Diffuse))
				Gdx.gl10.glDisable(GL10.GL_TEXTURE_2D);
			int cullFace = defaultCullFace;
			for (final Material.Attribute attribute : currentMaterial) {
				if (attribute.type == BlendingAttribute.Type)
					context.setBlending(true, ((BlendingAttribute)attribute).sourceFunction, ((BlendingAttribute)attribute).destFunction);
				else if (attribute.type == ColorAttribute.Diffuse) {
					Gdx.gl10.glColor4f(((ColorAttribute)attribute).color.r, ((ColorAttribute)attribute).color.g, ((ColorAttribute)attribute).color.b, ((ColorAttribute)attribute).color.a);
					if (renderable.lights != null) {
						Gdx.gl10.glEnable(GL10.GL_COLOR_MATERIAL);
						Gdx.gl10.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, getValues(lightVal, ((ColorAttribute)attribute).color), 0);
						Gdx.gl10.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, getValues(lightVal, ((ColorAttribute)attribute).color), 0);
					}
				} else if (attribute.type == TextureAttribute.Diffuse) {
					TextureDescriptor textureDesc = ((TextureAttribute)attribute).textureDescription;
					if (currentTexture0 != textureDesc.texture)
						(currentTexture0 = (Texture)textureDesc.texture).bind(0);
					currentTexture0.unsafeSetFilter(textureDesc.minFilter, textureDesc.magFilter);
					currentTexture0.unsafeSetWrap(textureDesc.uWrap, textureDesc.vWrap);
					Gdx.gl10.glEnable(GL10.GL_TEXTURE_2D);
				}
				else if ((attribute.type & IntAttribute.CullFace) == IntAttribute.CullFace)
					cullFace = ((IntAttribute)attribute).value;
			}
			context.setCullFace(cullFace);
		}
		if (currentTransform != renderable.worldTransform) { // FIXME mul localtransform
			if (currentTransform != null)
				Gdx.gl10.glPopMatrix();
			currentTransform = renderable.worldTransform;
			Gdx.gl10.glPushMatrix();
			Gdx.gl10.glLoadMatrixf(currentTransform.val, 0);
		}
		bindLights(renderable.lights);
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
