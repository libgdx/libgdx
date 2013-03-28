package com.badlogic.gdx.graphics.g3d.test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.materials.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.materials.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.materials.NewMaterial;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class TestShader implements Shader {
	public final static String PROJECTION_TRANSFORM = "u_projTrans";
	public final static String MODEL_TRANSFORM = "u_modelTrans";
	public final static String NORMAL_TRANSFORM = "u_normalMatrix";
	
	private static String defaultVertexShader = null;
	public final static String getDefaultVertexShader() {
		if (defaultVertexShader == null)
			defaultVertexShader = Gdx.files.classpath("com/badlogic/gdx/graphics/g3d/test/test.vertex.glsl").readString();
		return defaultVertexShader;
	}
	
	private static String defaultFragmentShader = null;
	public final static String getDefaultFragmentShader() {
		if (defaultFragmentShader == null)
			defaultFragmentShader = Gdx.files.classpath("com/badlogic/gdx/graphics/g3d/test/test.fragment.glsl").readString();
		return defaultFragmentShader;
	}

	protected static long implementedFlags = BlendingAttribute.Type | TextureAttribute.Diffuse | ColorAttribute.Diffuse;
	public static boolean ignoreUnimplemented = false;
	
	protected final ShaderProgram program;
	protected int projTransLoc;
	protected int modelTransLoc;
	protected int normalTransLoc;
	protected int diffuseTextureLoc;
	protected int diffuseColorLoc;
	public static int lightsCount = 5;
	protected int lightsLoc;
	protected int lightSize;
	protected int lightPositionOffset;
	protected int lightPowerOffset;
	
	private Light[] currentLights = new Light[lightsCount];
	
	protected RenderContext context;
	protected long mask;
	
	public TestShader(final NewMaterial material) {
		this(getDefaultVertexShader(), getDefaultFragmentShader(), material);
	}
	
	public TestShader(final long mask) {
		this(getDefaultVertexShader(), getDefaultFragmentShader(), mask);
	}

	public TestShader(final String vertexShader, final String fragmentShader, final NewMaterial material) {
		this(vertexShader, fragmentShader, material.getMask());
	}
	
	public TestShader(final String vertexShader, final String fragmentShader, final long mask) {
		String prefix = "";
		this.mask = mask;
		
		if (!ignoreUnimplemented && (implementedFlags & mask) != mask)
			throw new GdxRuntimeException("Some attributes not implemented yet ("+mask+")");
		
		if (lightsCount > 0)
			prefix += "#define lightsCount "+lightsCount+"\n";
		if ((mask & BlendingAttribute.Type) == BlendingAttribute.Type)
			prefix += "#define "+BlendingAttribute.Alias+"Flag\n";
		if ((mask & TextureAttribute.Diffuse) == TextureAttribute.Diffuse)
			prefix += "#define "+TextureAttribute.DiffuseAlias+"Flag\n";
		if ((mask & ColorAttribute.Diffuse) == ColorAttribute.Diffuse)
			prefix += "#define "+ColorAttribute.DiffuseAlias+"Flag\n";

		program = new ShaderProgram(prefix + vertexShader, prefix + fragmentShader);
		if (!program.isCompiled())
			throw new GdxRuntimeException(program.getLog());
		
		projTransLoc = program.getUniformLocation(PROJECTION_TRANSFORM);
		modelTransLoc = program.getUniformLocation(MODEL_TRANSFORM);
		normalTransLoc = program.getUniformLocation(NORMAL_TRANSFORM);
		diffuseTextureLoc = ((mask & TextureAttribute.Diffuse) != TextureAttribute.Diffuse) ? -1 : program.getUniformLocation(TextureAttribute.DiffuseAlias);
		diffuseColorLoc = ((mask & ColorAttribute.Diffuse) != ColorAttribute.Diffuse) ? -1 : program.getUniformLocation(ColorAttribute.DiffuseAlias);
		lightsLoc = lightsCount > 0 ? program.getUniformLocation("lights[0].color") : -1;
		lightSize = (lightsLoc >= 0 && lightsCount > 1) ? (program.getUniformLocation("lights[1].color") - lightsLoc) : -1;
		lightPositionOffset = lightsLoc >= 0 ? program.getUniformLocation("lights[0].position") - lightsLoc : -1;
		lightPowerOffset = lightsLoc >= 0 ? program.getUniformLocation("lights[0].power") - lightsLoc : -1;
	}
	
	@Override
	public boolean canRender(final Renderable renderable) {
		return mask == renderable.material.getMask();
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

	private Mesh currentMesh;
	private Matrix4 currentTransform;
	private Matrix3 normalMatrix = new Matrix3();
	private Camera camera;
	
	@Override
	public void begin (final Camera camera, final RenderContext context) {
		this.context = context;
		this.camera = camera;
		program.begin();
		context.setDepthTest(true, GL10.GL_LEQUAL);
		program.setUniformMatrix(projTransLoc, camera.combined);
		for (int i = 0; i < currentLights.length; i++)
			currentLights[i] = null;
	}

	@Override
	public void render (final Renderable renderable) {
		if (!renderable.material.has(BlendingAttribute.Type))
			context.setBlending(false, GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		if (currentTransform != renderable.transform) {
			program.setUniformMatrix(modelTransLoc, currentTransform = renderable.transform);
			program.setUniformMatrix(normalTransLoc, normalMatrix.set(currentTransform));
		}
		bindMaterial(renderable);
		if (lightsLoc >= 0)
			bindLights(renderable);
		if (currentMesh != renderable.mesh) {
			if (currentMesh != null)
				currentMesh.unbind(program);
			(currentMesh = renderable.mesh).bind(program);
		}
		renderable.mesh.render(program, renderable.primitiveType, renderable.meshPartOffset, renderable.meshPartSize);
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
	NewMaterial currentMaterial;
	private final void bindMaterial(final Renderable renderable) {
		if (currentMaterial == renderable.material)
			return;
		currentMaterial = renderable.material;
		for (NewMaterial.Attribute attr : currentMaterial) {
			final long t = attr.type;
			if (BlendingAttribute.is(t))
				context.setBlending(true, ((BlendingAttribute)attr).sourceFunction, ((BlendingAttribute)attr).destFunction);
			else if (ColorAttribute.is(t)) {
				ColorAttribute col = (ColorAttribute)attr;
				if ((t & ColorAttribute.Diffuse) == ColorAttribute.Diffuse)
					program.setUniformf(diffuseColorLoc, col.color);
				// TODO else if (..)
			}
			else if (TextureAttribute.is(t)) {
				TextureAttribute tex = (TextureAttribute)attr;
				if ((t & TextureAttribute.Diffuse) == TextureAttribute.Diffuse)
					bindTextureAttribute(diffuseTextureLoc, tex);
				// TODO else if (..)
			}  
			else {
				if(!ignoreUnimplemented) {
					throw new GdxRuntimeException("unknown attribute");
				}
			}
		}
	}

	/////// bindTextureAttribute /////////
	TextureAttribute currentTextureAttribute;
	private final void bindTextureAttribute(final int uniform, final TextureAttribute attribute) {
		final int unit = context.textureBinder.bind(attribute.textureDescription);
		program.setUniformi(uniform, unit);
		currentTextureAttribute = attribute;
	}
	 
	private final void bindLights(final Renderable renderable) {
		for (int i = 0; i < lightsCount; i++) {
			final int loc = lightsLoc + i * lightSize;
			if (renderable.lights.length <= i) {
				if (currentLights[i] != null) {
					program.setUniformf(loc + lightPowerOffset, 0f);
					currentLights[i] = null;
				}
			}
			else {
				if (currentLights[i] != renderable.lights[i]) {
					program.setUniformf(loc, renderable.lights[i].color);
					program.setUniformf(loc + lightPositionOffset, renderable.lights[i].position);
					program.setUniformf(loc + lightPowerOffset, renderable.lights[i].power);
					currentLights[i] = renderable.lights[i];
				}
			}
		}
	}

	@Override
	public void dispose () {
		program.dispose();
	}
}
