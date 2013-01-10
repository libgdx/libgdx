package com.badlogic.gdx.graphics.g3d.xoppa;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.materials.MaterialAttribute;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;

public class BatchRendererGLES11 implements BatchRenderer {
	final float[] lightColor = {1, 1, 1, 0};
	final float[] lightPosition = {2, 5, 10, 0};

	public final int textureUnitOffset = 3;
	public final int textureCount;
	final Texture[] textures;
	final int[] textureWeights;
	public int bindCount = 0;
	public int reuseCount = 0;
	
	public BatchRendererGLES11() {
		textureCount = Math.min(GL10.GL_MAX_TEXTURE_UNITS, 16) - textureUnitOffset;
		textures = new Texture[textureCount];
		textureWeights = new int[textureCount];
	}
	
	@Override
	public int compare (RenderInstance o1, RenderInstance o2) {
		return o1.distance > o2.distance ? 1 : (o1.distance < o2.distance ? -1 : 0);
	}

	@Override
	public ShaderProgram getShader (RenderInstance instance) {
		return null;
	}
	
	Material currentMaterial;
	boolean useTexture;
	int currentTexture; // for now only allow one textureattribute to be used at a time
	private final void setMaterial(final GL11 gl, final Material mat) {
		if (mat == currentMaterial) 
			return;
		
		final boolean hasTex = mat.hasTexture();
		
		if (useTexture != hasTex || currentMaterial == null) {
			useTexture = hasTex;
			if (hasTex)
				gl.glEnable(GL11.GL_TEXTURE_2D);
			else
				gl.glDisable(GL11.GL_TEXTURE_2D);
		}
		
		currentMaterial = mat;
		for (MaterialAttribute attr : currentMaterial) {
			if (attr instanceof TextureAttribute)
				bindTextureAttribute(gl, (TextureAttribute)attr);
			else
				attr.bind();
		}
	}

	private final void bindTextureAttribute(final GL11 gl, final TextureAttribute attribute) {
		currentTexture = bindTexture(gl, attribute.texture);
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, attribute.minFilter);
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, attribute.magFilter);
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, attribute.uWrap);
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, attribute.vWrap);
	}
	
	private final int bindTexture(final GL11 gl, final Texture texture) {
		int result = -1;
		int weight = textureWeights[0];
		int windex = 0;
		for (int i = 0; i < textureCount; i++) {
			if (textures[i] == texture) {
				result = i;
				textureWeights[i]++;
			} else if (--textureWeights[i] < weight || textureWeights[i] < 0) {
				weight = textureWeights[i];
				windex = i;
			}
		}
		if (result < 0) {
			textures[windex] = texture;
			textureWeights[windex] = 100;
			result = windex;
			texture.bind(textureUnitOffset + windex);
			bindCount++;
		} else
			reuseCount++;
		return textureUnitOffset + result;
	}
	
	Mesh currentMesh;
	@Override
	public void render (Camera camera, Array<RenderInstance> instances) {
		final GL11 gl = Gdx.gl11;
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
		gl.glDisable(GL10.GL_TEXTURE_2D);
		
		for (int i = 0; i < textureCount; i++) {
			textureWeights[i] = 0;
			textures[i] = null;
		}

		for (int i = 0; i < instances.size; i++) {
			final RenderInstance instance = instances.get(i);
			setMaterial(gl, instance.material);
			//if (instance.mesh != currentMesh) { // uncomment for GLES20
				if (currentMesh != null)
					currentMesh.unbind();
				currentMesh = instance.mesh;
				VertexAttribute a = currentMesh.getVertexAttribute(Usage.TextureCoordinates);
				if (a != null)
					a.alias = ShaderProgram.TEXCOORD_ATTRIBUTE + currentTexture;
				currentMesh.bind();
			//}
			gl.glPushMatrix();
			gl.glMultMatrixf(instance.transform.val, 0);
			instance.mesh.render(instance.primitiveType);
			gl.glPopMatrix();
		}
		if (currentMesh != null) {
			currentMesh.unbind();
			currentMesh = null;
		}
	}
}
