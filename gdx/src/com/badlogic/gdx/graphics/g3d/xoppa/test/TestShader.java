package com.badlogic.gdx.graphics.g3d.xoppa.test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.materials.MaterialAttribute;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.xoppa.RenderContext;
import com.badlogic.gdx.graphics.g3d.xoppa.RenderInstance;
import com.badlogic.gdx.graphics.g3d.xoppa.Shader;
import com.badlogic.gdx.graphics.g3d.xoppa.utils.ExclusiveTextures;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class TestShader implements Shader {
	// TODO This should be moved to the material class, bitwise mask to identify the signature of a material
	public final static long TextureFlag = 1 << 0;
	
	public final static String PROJECTION_TRANSFORM = "u_projTrans";
	public final static String MODEL_TRANSFORM = "u_modelTrans";
	
	public final static String DEFAULT_VERTEX_SHADER = 
			"attribute vec3 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" +
			"#ifdef texCoords0Flag\n" +
			"attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" +
			"#endif\n" +
			"uniform mat4 " + PROJECTION_TRANSFORM + ";\n" +
			"uniform mat4 " + MODEL_TRANSFORM + ";\n" +
			"#ifdef texCoords0Flag\n" +
			"varying vec2 v_texCoords0; \n" +
			"#endif\n" + 
			"void main() {\n"+
			"#ifdef texCoords0Flag\n" +
			"	v_texCoords0 = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0; \n" +
			"#endif\n" + 
			"	gl_Position = " + PROJECTION_TRANSFORM + " * (" + MODEL_TRANSFORM + " * vec4(" + ShaderProgram.POSITION_ATTRIBUTE + ", 1.0)); \n" +
			"}\n";
	
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
			"#ifdef texCoords0Flag\n" +
			"uniform sampler2D " + TextureAttribute.diffuseTexture + ";\n" +
			"varying MED vec2 v_texCoords0;\n" +
			"#endif\n" + 
			"void main() {\n" +
			"#ifdef texCoords0Flag\n" +
			"	gl_FragColor = texture2D(" + TextureAttribute.diffuseTexture + ", v_texCoords0);\n" +
			"#else\n"+
			"  gl_FragColor = vec4(1.0);\n" +
			"#endif\n" + 
			"}\n";
	
	
	protected final ShaderProgram program;
	protected int projTransLoc;
	protected int modelTransLoc;
	protected int diffuseTextureLoc;
	protected RenderContext context;
	protected long mask;
	
	protected TestShader() {
		this(DEFAULT_VERTEX_SHADER, DEFAULT_FRAGMENT_SHADER);
	}

	protected TestShader(final String vertexShader, final String fragmentShader) {
		this(new ShaderProgram(vertexShader, fragmentShader));
	}
	
	protected TestShader(ShaderProgram program) {
		if (!program.isCompiled())
			throw new GdxRuntimeException(program.getLog());
		this.program = program;
		this.projTransLoc = program.getUniformLocation(PROJECTION_TRANSFORM);
		this.modelTransLoc = program.getUniformLocation(MODEL_TRANSFORM);
		this.diffuseTextureLoc = program.getUniformLocation(TextureAttribute.diffuseTexture);
	}
	
	public static TestShader create(final Material material) {
		long mask = getMaterialSignature(material);
		String prefix = "";
		if ((mask & TextureFlag) == TextureFlag)
			prefix += "#define texCoords0Flag\n";
		final TestShader result = new TestShader(prefix + DEFAULT_VERTEX_SHADER, prefix + DEFAULT_FRAGMENT_SHADER);
		result.mask = mask;
		return result;
		// "#define texCoords0Flag\n"
	}

	// FIXME this should be done in the material itself
	public static long getMaterialSignature(final Material material) {
		int result = 0;
		for (MaterialAttribute atr : material) {
			if (atr instanceof TextureAttribute)
				result |= TextureFlag;
		}
		return result;
	}
	
	@Override
	public boolean canRender(Material material) {
		return mask == getMaterialSignature(material);
	}
	
	/*@Override
	public int compareTo (final Object other) {
		return (other instanceof RenderShader) ? compareTo((RenderShader)other) : -1;
	}*/
	
	@Override
	public int compareTo(Shader other) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public boolean equals (Object obj) {
		return (obj instanceof TestShader) ? equals((TestShader)obj) : false;
	}
	
	public boolean equals (TestShader obj) {
		return (obj == this);
	}
	
	@Override
	public int compare (final RenderInstance arg0, final RenderInstance arg1) {
		final boolean b1 = arg0.material.isNeedBlending();
		final boolean b2 = arg1.material.isNeedBlending();
		if (b1 != b2) return b1 ? 1 : -1;
		return arg0.distance > arg1.distance ? (b1 ? -1 : 1) : (arg0.distance < arg1.distance ? (b1 ? 1 : -1) : 0);
	}

	private Mesh currentMesh;
	private Matrix4 currentTransform;
	
	@Override
	public void begin (final Camera camera, final RenderContext context) {
		this.context = context;
		program.begin();
		context.setDepthTest(true, GL10.GL_LEQUAL);
		program.setUniformMatrix(projTransLoc, camera.combined);
	}

	@Override
	public void render (final RenderInstance instance) {
		// Enable/disable blending if needed
		context.setBlending(instance.material.isNeedBlending(), GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		// Set the model transform if needed
		if (currentTransform != instance.transform)
			program.setUniformMatrix(modelTransLoc, currentTransform = instance.transform);
		// Bind the material if needed
		bindMaterial(instance);
		// Bind the mesh if needed
		if (currentMesh != instance.mesh) {
			if (currentMesh != null)
				currentMesh.unbind(program);
			(currentMesh = instance.mesh).bind(program);
		}
		// Render the mesh
		instance.mesh.render(program, instance.primitiveType, instance.meshPartOffset, instance.meshPartSize);
	}

	@Override
	public void end () {
		if (currentMesh != null) {
			currentMesh.unbind(program);
			currentMesh = null;
		}
		currentTransform = null;
		currentTextureAttribute = null;
		currentMaterial = null;
		program.end();
	}
	
	/////// bindMaterial /////////
	Material currentMaterial;
	private final void bindMaterial(final RenderInstance instance) {
		if (currentMaterial == instance.material)
			return;
		currentMaterial = instance.material;
		for (MaterialAttribute attr : currentMaterial) {
			if (attr instanceof TextureAttribute)
				bindTextureAttribute((TextureAttribute)attr);
			else
				attr.bind(program);
		}
	}

	/////// bindTextureAttribute /////////
	TextureAttribute currentTextureAttribute;
	private final void bindTextureAttribute(final TextureAttribute attribute) {
		if (attribute == currentTextureAttribute)
			return;
		final int unit = context.textures.bindTexture(attribute.texture);
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, attribute.minFilter);
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, attribute.magFilter);
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, attribute.uWrap);
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, attribute.vWrap);
		program.setUniformi(diffuseTextureLoc, unit);
		currentTextureAttribute = attribute;
	}
}
