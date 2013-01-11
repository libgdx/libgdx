package com.badlogic.gdx.graphics.g3d.xoppa.test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.materials.MaterialAttribute;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.xoppa.RenderInstance;
import com.badlogic.gdx.graphics.g3d.xoppa.utils.ExclusiveTextures;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class OldBatchRendererGLES20 implements OldBatchRenderer {
	public final static String PROJECTION_TRANSFORM = "u_projTrans";
	public final static String MODEL_TRANSFORM = "u_modelTrans";
	
	public final static String DEFAULT_VERTEX_SHADER = 
			"attribute vec3 " + ShaderProgram.POSITION_ATTRIBUTE + ";" +
			"attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;" +
			"uniform mat4 " + PROJECTION_TRANSFORM + ";" +
			"uniform mat4 " + MODEL_TRANSFORM + ";" +
			"varying vec2 v_texCoords;" +
			"void main() {"+
			"	v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;" +
			"	gl_Position = " + PROJECTION_TRANSFORM + " * (" + MODEL_TRANSFORM + " * vec4(" + ShaderProgram.POSITION_ATTRIBUTE + ", 1.0));" +
			"}";
	
	public final static String DEFAULT_FRAGMENT_SHADER = 
			"#ifdef GL_ES\n" + 
			"#define LOWP lowp\n" +
			"#define MED mediump\n" +
			"#define HIGH highp\n" +
			"precision mediump float;\n" +
			"#else\n" +
			"#define MED\n" +
			"#define LOWP\n" +
			"#endif\n" + 
			"uniform sampler2D " + TextureAttribute.diffuseTexture + ";" +
			"varying MED vec2 v_texCoords;" +
			"void main() {" +
			"	gl_FragColor = texture2D(" + TextureAttribute.diffuseTexture + ", v_texCoords);" +
			"}";
	
	public final ShaderProgram defaultShader;
		
	protected ExclusiveTextures textures;
	
	public OldBatchRendererGLES20(final ExclusiveTextures textures) {
		this.textures = textures;
		defaultShader = new ShaderProgram(DEFAULT_VERTEX_SHADER, DEFAULT_FRAGMENT_SHADER);
		if (!defaultShader.isCompiled())
			throw new GdxRuntimeException(defaultShader.getLog());
		
	}
	
	@Override
	public int compare (RenderInstance arg0, RenderInstance arg1) {
		// TODO
		return arg0.distance > arg1.distance ? 1 : (arg0.distance < arg1.distance ? -1 : 0);
	}

	@Override
	public ShaderProgram getShader (RenderInstance instance) {
		// TODO
		return defaultShader;
	}
	
	@Override
	public void render (final Camera camera, final Array<RenderInstance> instances) {
		// breakdown this method into smaller inline pieces for easy testing
		final GL20 gl = Gdx.gl20;
		begin(gl, camera);
		for (int i = 0; i < instances.size; i++) {
			final RenderInstance instance = instances.get(i);
			bindShader(gl, camera, instance);
			bindMaterial(gl, camera, instance);
			renderMesh(gl, camera, instance);
		}
		end(gl);
	}
	
	/////// bindShader /////////
	private ShaderProgram currentShader;
	private ShaderProgram previousShader;
	private boolean shaderChanged;
	private final void bindShader(final GL20 gl, final Camera camera, final RenderInstance instance) {
		shaderChanged = instance.shader != currentShader;
		if (!shaderChanged)
			return;
		if (currentShader != null)
			currentShader.end();
		previousShader = currentShader;
		//currentShader = instance.shader;
		currentShader.begin();
		
		currentShader.setUniformMatrix(PROJECTION_TRANSFORM, camera.combined);
	}

	/////// bindMaterial /////////
	Material currentMaterial;
	private final void bindMaterial(final GL20 gl, final Camera camera, final RenderInstance instance) {
		if (currentMaterial == instance.material)
			return;
		currentMaterial = instance.material;
		for (MaterialAttribute attr : currentMaterial) {
			if (attr instanceof TextureAttribute)
				bindTextureAttribute(gl, (TextureAttribute)attr);
			else
				attr.bind(currentShader);
		}
	}

	/////// bindTextureAttribute /////////
	TextureAttribute currentTextureAttribute;
	int unittmp = 0;
	private final void bindTextureAttribute(final GL20 gl, final TextureAttribute attribute) {
		if (attribute == currentTextureAttribute && !shaderChanged)
			return;
		final int unit = textures.bindTexture(attribute.texture);
		attribute.texture.bind(unit);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, attribute.minFilter);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, attribute.magFilter);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, attribute.uWrap);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, attribute.vWrap);
		currentShader.setUniformi(TextureAttribute.diffuseTexture, unit);
		currentTextureAttribute = attribute;
	}

	/////// renderMesh /////////
	private Mesh currentMesh;
	private Matrix4 currentTransform;
	private final void renderMesh(final GL20 gl, final Camera camera, final RenderInstance instance) {
		if (currentMesh != instance.mesh || shaderChanged) {
			if (currentMesh != null)
				currentMesh.unbind(shaderChanged ? previousShader : currentShader);
			(currentMesh = instance.mesh).bind(currentShader);
		}
		if (currentTransform != instance.transform || shaderChanged)
			currentShader.setUniformMatrix(MODEL_TRANSFORM, currentTransform = instance.transform);
		instance.mesh.render(currentShader, instance.primitiveType);
	}

	/////// begin /////////
	private final void begin(final GL20 gl, final Camera camera) {
		Gdx.gl.glEnable(GL10.GL_DEPTH_TEST);
		Gdx.gl.glDepthFunc(GL10.GL_LEQUAL);
		Gdx.gl.glEnable(GL10.GL_BLEND);
		Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
	}

	/////// end /////////
	private final void end(final GL20 gl) {
		if (currentMesh != null) {
			currentMesh.unbind(currentShader);
			currentMesh = null;
		}
		if (currentShader != null) {
			currentShader.end();
			currentShader = null;
		}
	}
}
