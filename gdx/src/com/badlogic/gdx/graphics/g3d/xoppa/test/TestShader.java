package com.badlogic.gdx.graphics.g3d.xoppa.test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.xoppa.RenderContext;
import com.badlogic.gdx.graphics.g3d.xoppa.RenderInstance;
import com.badlogic.gdx.graphics.g3d.xoppa.Renderable;
import com.badlogic.gdx.graphics.g3d.xoppa.Shader;
import com.badlogic.gdx.graphics.g3d.xoppa.materials.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.xoppa.materials.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.xoppa.materials.NewMaterial;
import com.badlogic.gdx.graphics.g3d.xoppa.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.xoppa.utils.ExclusiveTextures;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class TestShader implements Shader {
	public final static String PROJECTION_TRANSFORM = "u_projTrans";
	public final static String MODEL_TRANSFORM = "u_modelTrans";
	
	private static String defaultVertexShader = null;
	public final static String getDefaultVertexShader() {
		if (defaultVertexShader == null)
			defaultVertexShader = Gdx.files.classpath("com/badlogic/gdx/graphics/g3d/xoppa/test/test.vertex.glsl").readString();
		return defaultVertexShader;
	}
	
	private static String defaultFragmentShader = null;
	public final static String getDefaultFragmentShader() {
		if (defaultFragmentShader == null)
			defaultFragmentShader = Gdx.files.classpath("com/badlogic/gdx/graphics/g3d/xoppa/test/test.fragment.glsl").readString();
		return defaultFragmentShader;
	}

	protected static long implementedFlags = BlendingAttribute.Type | TextureAttribute.Diffuse | ColorAttribute.Diffuse;
	protected static boolean ignoreUnimplemented = false;
	
	protected final ShaderProgram program;
	protected int projTransLoc;
	protected int modelTransLoc;
	protected int diffuseTextureLoc;
	protected int diffuseColorLoc;
	protected int lightsCount = 5;
	protected int lightsLoc;
	protected int lightSize;
	protected int lightPositionOffset;
	protected int lightPowerOffset;
	
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
		diffuseTextureLoc = ((mask & TextureAttribute.Diffuse) != TextureAttribute.Diffuse) ? -1 : program.getUniformLocation(TextureAttribute.DiffuseAlias);
		diffuseColorLoc = ((mask & ColorAttribute.Diffuse) != ColorAttribute.Diffuse) ? -1 : program.getUniformLocation(ColorAttribute.DiffuseAlias);
		lightsLoc = lightsCount > 0 ? program.getUniformLocation("lights[0].color") : -1;
		lightSize = (lightsLoc >= 0 && lightsCount > 1) ? (program.getUniformLocation("lights[1].color") - lightsLoc) : -1;
		lightPositionOffset = lightsLoc >= 0 ? program.getUniformLocation("lights[0].position") - lightsLoc : -1;
		lightPowerOffset = lightsLoc >= 0 ? program.getUniformLocation("lights[0].power") - lightsLoc : -1;
	}
	
	@Override
	public boolean canRender(final RenderInstance instance) {
		return mask == instance.renderable.material.getMask();
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
		final boolean b1 = arg0.renderable.material.has(BlendingAttribute.Type);
		final boolean b2 = arg1.renderable.material.has(BlendingAttribute.Type);
		if (b1 != b2) return b1 ? 1 : -1;
		return arg0.distance > arg1.distance ? (b1 ? -1 : 1) : (arg0.distance < arg1.distance ? (b1 ? 1 : -1) : 0);
	}

	private Mesh currentMesh;
	private Matrix4 currentTransform;
	private Camera camera;
	
	@Override
	public void begin (final Camera camera, final RenderContext context) {
		this.context = context;
		this.camera = camera;
		program.begin();
		context.setDepthTest(true, GL10.GL_LEQUAL);
		program.setUniformMatrix(projTransLoc, camera.combined);
	}

	@Override
	public void render (NewModel model, Matrix4 transform, final Light[] lights) {
		transform.getTranslation(Vector3.tmp);
		float dist = Vector3.tmp2.set(Vector3.tmp.x - camera.position.x, Vector3.tmp.y - camera.position.y , Vector3.tmp.z - camera.position.z).len();
		if (Vector3.tmp2.div(dist).dot(camera.direction) < 0)
			dist = -dist;
		for (Renderable renderable : model.getParts(dist)) {
			final RenderInstance instance = RenderInstance.pool.obtain(renderable, transform, dist, lights, null);
			render(instance);
			RenderInstance.pool.free(instance);
		}
	}
	
	@Override
	public void render (final RenderInstance instance) {
		if (!instance.renderable.material.has(BlendingAttribute.Type))
			context.setBlending(false, GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		if (currentTransform != instance.transform)
			program.setUniformMatrix(modelTransLoc, currentTransform = instance.transform);
		bindMaterial(instance);
		if (lightsLoc >= 0)
			bindLights(instance);
		if (currentMesh != instance.renderable.mesh) {
			if (currentMesh != null)
				currentMesh.unbind(program);
			(currentMesh = instance.renderable.mesh).bind(program);
		}
		instance.renderable.mesh.render(program, instance.renderable.primitiveType, instance.renderable.meshPartOffset, instance.renderable.meshPartSize);
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
	private final void bindMaterial(final RenderInstance instance) {
		if (currentMaterial == instance.renderable.material)
			return;
		currentMaterial = instance.renderable.material;
		for (NewMaterial.Attribute attr : currentMaterial) {
			long t = attr.getType();
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
			else 
				throw new GdxRuntimeException("unknown attribute");
		}
	}

	/////// bindTextureAttribute /////////
	TextureAttribute currentTextureAttribute;
	private final void bindTextureAttribute(final int uniform, final TextureAttribute attribute) {
		final int unit = context.textures.bindTexture(attribute.textureDescription);
		program.setUniformi(uniform, unit);
		currentTextureAttribute = attribute;
	}
	
	private final void bindLights(final RenderInstance instance) {
		for (int i = 0; i < lightsCount; i++) {
			final int loc = lightsLoc + i * lightSize;
			if (instance.lights.length <= i)
				program.setUniformf(loc + lightPowerOffset, 0f);
			else {
				program.setUniformf(loc, instance.lights[i].color);
				program.setUniformf(loc, instance.lights[i].position);
				program.setUniformf(loc, instance.lights[i].power);
			}
		}
	}
}
