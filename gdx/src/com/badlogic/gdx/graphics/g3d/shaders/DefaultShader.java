package com.badlogic.gdx.graphics.g3d.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.lights.AmbientCubemap;
import com.badlogic.gdx.graphics.g3d.lights.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.lights.Lights;
import com.badlogic.gdx.graphics.g3d.lights.PointLight;
import com.badlogic.gdx.graphics.g3d.materials.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.materials.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.materials.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.materials.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.materials.IntAttribute;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class DefaultShader extends BaseShader {
	public static class Inputs {
		public final static Uniform projTrans = new Uniform("u_projTrans");
		public final static Uniform cameraPosition = new Uniform("u_cameraPosition");
		public final static Uniform cameraDirection = new Uniform("u_cameraDirection");
		public final static Uniform cameraUp = new Uniform("u_cameraUp");
		
		public final static Uniform worldTrans = new Uniform("u_worldTrans");
		public final static Uniform normalMatrix = new Uniform("u_normalMatrix");
		public final static Uniform bones = new Uniform("u_bones");
		
		public final static Uniform shininess = new Uniform("u_shininess", FloatAttribute.Shininess);
		public final static Uniform opacity = new Uniform("u_opacity", BlendingAttribute.Type);
		public final static Uniform diffuseColor = new Uniform("u_diffuseColor", ColorAttribute.Diffuse);
		public final static Uniform diffuseTexture = new Uniform("u_diffuseTexture", TextureAttribute.Diffuse);
		public final static Uniform specularColor = new Uniform("u_specularColor", ColorAttribute.Specular);
		public final static Uniform specularTexture = new Uniform("u_specularTexture", TextureAttribute.Specular);
		public final static Uniform normalTexture	= new Uniform("u_normalTexture", TextureAttribute.Normal);
		public final static Uniform alphaTest = new Uniform("u_alphaTest", FloatAttribute.AlphaTest);
		
		public final static Uniform ambientCube = new Uniform("u_ambientCubemap");
		public final static Uniform dirLights = new Uniform("u_dirLights");
		public final static Uniform pointLights = new Uniform("u_pointLights");
	}
	
	public static class Setters {
		public final static Setter projTrans = new Setter() {
			@Override public boolean isGlobal (BaseShader shader, int inputID) { return true; }
			@Override public void set (BaseShader shader, int inputID, Renderable renderable) {
				shader.set(inputID, shader.camera.combined);
			}
		};
		public final static Setter cameraPosition = new Setter() {
			@Override public boolean isGlobal (BaseShader shader, int inputID) { return true; }
			@Override public void set (BaseShader shader, int inputID, Renderable renderable) {
				shader.set(inputID, shader.camera.position.x, shader.camera.position.y, shader.camera.position.z, 1.1881f/(shader.camera.far*shader.camera.far));
			}
		};
		public final static Setter cameraDirection = new Setter() {
			@Override public boolean isGlobal (BaseShader shader, int inputID) { return true; }
			@Override public void set (BaseShader shader, int inputID, Renderable renderable) {
				shader.set(inputID, shader.camera.direction);
			}
		};
		public final static Setter cameraUp = new Setter() {
			@Override public boolean isGlobal (BaseShader shader, int inputID) { return true; }
			@Override public void set (BaseShader shader, int inputID, Renderable renderable) {
				shader.set(inputID, shader.camera.up);
			}
		};
		public final static Setter worldTrans = new Setter() {
			@Override public boolean isGlobal (BaseShader shader, int inputID) { return false; }
			@Override public void set (BaseShader shader, int inputID, Renderable renderable) {
				shader.set(inputID, renderable.worldTransform);
			}
		};
		public final static Setter normalMatrix = new Setter() {
			private final Matrix3 tmpM = new Matrix3();
			@Override public boolean isGlobal (BaseShader shader, int inputID) { return false; }
			@Override public void set (BaseShader shader, int inputID, Renderable renderable) {
				shader.set(inputID, tmpM.set(renderable.worldTransform).inv().transpose());
			}
		};
		public static class Bones implements Setter {
			private final static Matrix4 idtMatrix = new Matrix4();
			public final float bones[];
			public Bones (final int numBones) {
				this.bones = new float[numBones * 16];
			}
			@Override public boolean isGlobal (BaseShader shader, int inputID) { return false; }
			@Override public void set (BaseShader shader, int inputID, Renderable renderable) {
				for (int i = 0; i < bones.length; i++) {
					final int idx = i/16;
					bones[i] = (renderable.bones == null || idx >= renderable.bones.length || renderable.bones[idx] == null) ? 
						idtMatrix.val[i%16] : renderable.bones[idx].val[i%16];
				}
				shader.program.setUniformMatrix4fv(shader.loc(inputID), bones, 0, bones.length);
			}
		}
		public final static Setter shininess = new Setter() {
			@Override public boolean isGlobal (BaseShader shader, int inputID) { return false; }
			@Override public void set (BaseShader shader, int inputID, Renderable renderable) {
				shader.set(inputID, ((FloatAttribute)(renderable.material.get(FloatAttribute.Shininess))).value);
			}
		};
		public final static Setter diffuseColor = new Setter() {
			@Override public boolean isGlobal (BaseShader shader, int inputID) { return false; }
			@Override public void set (BaseShader shader, int inputID, Renderable renderable) {
				shader.set(inputID, ((ColorAttribute)(renderable.material.get(ColorAttribute.Diffuse))).color);
			}
		};
		public final static Setter diffuseTexture = new Setter() {
			@Override public boolean isGlobal (BaseShader shader, int inputID) { return false; }
			@Override public void set (BaseShader shader, int inputID, Renderable renderable) {
				final int unit = shader.context.textureBinder.bind(((TextureAttribute)(renderable.material.get(TextureAttribute.Diffuse))).textureDescription);
				shader.set(inputID, unit);
			}
		};
		public final static Setter specularColor = new Setter() {
			@Override public boolean isGlobal (BaseShader shader, int inputID) { return false; }
			@Override public void set (BaseShader shader, int inputID, Renderable renderable) {
				shader.set(inputID, ((ColorAttribute)(renderable.material.get(ColorAttribute.Specular))).color);
			}
		};
		public final static Setter specularTexture = new Setter() {
			@Override public boolean isGlobal (BaseShader shader, int inputID) { return false; }
			@Override public void set (BaseShader shader, int inputID, Renderable renderable) {
				final int unit = shader.context.textureBinder.bind(((TextureAttribute)(renderable.material.get(TextureAttribute.Specular))).textureDescription);
				shader.set(inputID, unit);
			}
		};
		public static class ACubemap implements Setter {
			private final static float ones[] = {1, 1, 1,   1, 1, 1,   1, 1, 1,   1, 1, 1,   1, 1, 1,   1, 1, 1};
			private final AmbientCubemap cacheAmbientCubemap = new AmbientCubemap();
			private final static Vector3 tmpV1 = new Vector3();
			public final int dirLightsOffset;
			public final int pointLightsOffset;
			public ACubemap (final int dirLightsOffset, final int pointLightsOffset) {
				this.dirLightsOffset = dirLightsOffset;
				this.pointLightsOffset = pointLightsOffset;
			}
			@Override public boolean isGlobal (BaseShader shader, int inputID) { return false; }
			@Override public void set (BaseShader shader, int inputID, Renderable renderable) {
				if (renderable.lights == null)
					shader.program.setUniform3fv(shader.loc(inputID), ones, 0, ones.length);
				else {
					renderable.worldTransform.getTranslation(tmpV1);
					cacheAmbientCubemap.set(renderable.lights.ambientLight);
						
					for (int i = dirLightsOffset; i < renderable.lights.directionalLights.size; i++)
						cacheAmbientCubemap.add(renderable.lights.directionalLights.get(i).color, renderable.lights.directionalLights.get(i).direction);
						
					for (int i = pointLightsOffset; i < renderable.lights.pointLights.size; i++)
						cacheAmbientCubemap.add(renderable.lights.pointLights.get(i).color, renderable.lights.pointLights.get(i).position, tmpV1, renderable.lights.pointLights.get(i).intensity);
						
					cacheAmbientCubemap.clamp();
					
					shader.program.setUniform3fv(shader.loc(inputID), cacheAmbientCubemap.data, 0, cacheAmbientCubemap.data.length);
				}
			}
		}
	}

	private static String defaultVertexShader = null;
	public final static String getDefaultVertexShader() {
		if (defaultVertexShader == null)
			defaultVertexShader = Gdx.files.classpath("com/badlogic/gdx/graphics/g3d/shaders/default.vertex.glsl").readString();
		return defaultVertexShader;
	}
	
	private static String defaultFragmentShader = null;
	public final static String getDefaultFragmentShader() {
		if (defaultFragmentShader == null)
			defaultFragmentShader = Gdx.files.classpath("com/badlogic/gdx/graphics/g3d/shaders/default.fragment.glsl").readString();
		return defaultFragmentShader;
	}
	
	protected static long implementedFlags = BlendingAttribute.Type | TextureAttribute.Diffuse | ColorAttribute.Diffuse | 
		ColorAttribute.Specular | FloatAttribute.Shininess;
	
	public static boolean ignoreUnimplemented = true;
	/** Set to 0 to disable culling */
	public static int defaultCullFace = GL10.GL_BACK;
	/** Set to 0 to disable depth test */
	public static int defaultDepthFunc = GL10.GL_LEQUAL;
	
	// Global uniforms
	public final int u_projTrans;
	public final int u_cameraPosition;
	public final int u_cameraDirection;
	public final int u_cameraUp;
	public final int u_time;
	// Object uniforms
	public final int u_worldTrans;
	public final int u_normalMatrix;
	public final int u_bones;
	// Material uniforms
	public final int u_shininess;
	public final int u_opacity;
	public final int u_diffuseColor;
	public final int u_diffuseTexture;
	public final int u_specularColor;
	public final int u_specularTexture;
	public final int u_normalTexture;
	public final int u_alphaTest;
	// Lighting uniforms
	protected final int u_ambientCubemap;
	protected final int u_dirLights0color		= register(new Uniform("u_dirLights[0].color"));
	protected final int u_dirLights0direction	= register(new Uniform( "u_dirLights[0].direction"));
	protected final int u_dirLights1color		= register(new Uniform("u_dirLights[1].color"));
	protected final int u_pointLights0color		= register(new Uniform("u_pointLights[0].color"));
	protected final int u_pointLights0position	= register(new Uniform("u_pointLights[0].position"));
	protected final int u_pointLights0intensity= register(new Uniform("u_pointLights[0].intensity"));
	protected final int u_pointLights1color		= register(new Uniform("u_pointLights[1].color"));
	protected final int u_fogColor				   = register(new Uniform("u_fogColor"));
	// FIXME Cache vertex attribute locations...
	
	protected int dirLightsLoc;
	protected int dirLightsColorOffset;
	protected int dirLightsDirectionOffset;
	protected int dirLightsSize;
	protected int pointLightsLoc;
	protected int pointLightsColorOffset;
	protected int pointLightsPositionOffset;
	protected int pointLightsIntensityOffset;
	protected int pointLightsSize;

	protected boolean lighting;
	protected boolean fog;
	protected final AmbientCubemap ambientCubemap = new AmbientCubemap();
	protected final DirectionalLight directionalLights[];
	protected final PointLight pointLights[];
	
	/** The renderable used to create this shader, invalid after the call to init */
	private Renderable renderable;
	private long materialMask;
	private long vertexMask;
	/** Material attributes which are not required but always supported. */
	private final static long optionalAttributes = IntAttribute.CullFace | DepthTestAttribute.Type;
	
	public DefaultShader(final Renderable renderable, boolean lighting, boolean fog, int numDirectional, int numPoint, int numSpot, int numBones) {
		this(getDefaultVertexShader(), getDefaultFragmentShader(), renderable, lighting, fog, numDirectional, numPoint, numSpot, numBones);
	}
	
	public DefaultShader(final String vertexShader, final String fragmentShader, final Renderable renderable, boolean lighting, boolean fog, int numDirectional, int numPoint, int numSpot, int numBones) {
		this(createPrefix(renderable, lighting, fog, numDirectional, numPoint, numSpot, numBones), 
			vertexShader, fragmentShader, renderable, lighting, fog, numDirectional, numPoint, numSpot, numBones);
	}

	public DefaultShader(final String prefix, final String vertexShader, final String fragmentShader, final Renderable renderable, boolean lighting, boolean fog, int numDirectional, int numPoint, int numSpot, int numBones) {
		this(new ShaderProgram(prefix + vertexShader, prefix + fragmentShader), renderable, lighting, fog, numDirectional, numPoint, numSpot, numBones);
	}
	
	public DefaultShader(final ShaderProgram shaderProgram, final Renderable renderable, boolean lighting, boolean fog, int numDirectional, int numPoint, int numSpot, int numBones) {
		this.program = shaderProgram;
		this.lighting = lighting;
		this.fog = fog;
		this.renderable = renderable;
		materialMask = renderable.material.getMask() | optionalAttributes;
		vertexMask = renderable.mesh.getVertexAttributes().getMask();

		this.directionalLights = new DirectionalLight[lighting && numDirectional > 0 ? numDirectional : 0];
		for (int i = 0; i < directionalLights.length; i++)
			directionalLights[i] = new DirectionalLight();
		this.pointLights = new PointLight[lighting && numPoint > 0 ? numPoint : 0];
		for (int i = 0; i < pointLights.length; i++)
			pointLights[i] = new PointLight();

		if (!ignoreUnimplemented && (implementedFlags & materialMask) != materialMask)
			throw new GdxRuntimeException("Some attributes not implemented yet ("+materialMask+")");
		
		// Global uniforms
		u_projTrans				= register(Inputs.projTrans, Setters.projTrans);
		u_cameraPosition		= register(Inputs.cameraPosition, Setters.cameraPosition);
		u_cameraDirection		= register(Inputs.cameraDirection, Setters.cameraDirection);
		u_cameraUp				= register(Inputs.cameraUp, Setters.cameraUp);
		u_time					= register(new Uniform("u_time"));
		// Object uniforms
		u_worldTrans			= register(Inputs.worldTrans, Setters.worldTrans);
		u_normalMatrix			= register(Inputs.normalMatrix, Setters.normalMatrix);
		u_bones 					= numBones > 0 ? register(Inputs.bones, new Setters.Bones(numBones)) : -1;
		
		u_shininess				= register(Inputs.shininess, Setters.shininess);
		u_opacity 				= register(Inputs.opacity);
		u_diffuseColor			= register(Inputs.diffuseColor, Setters.diffuseColor);
		u_diffuseTexture		= register(Inputs.diffuseTexture, Setters.diffuseTexture);
		u_specularColor		= register(Inputs.specularColor, Setters.specularColor);
		u_specularTexture		= register(Inputs.specularTexture, Setters.specularTexture);
		u_normalTexture		= register(Inputs.normalTexture);
		u_alphaTest				= register(Inputs.alphaTest);
		
		u_ambientCubemap		= lighting ? register(Inputs.ambientCube, new Setters.ACubemap(numDirectional, numPoint)) : -1;
	}

	@Override
	public void init () {
		final ShaderProgram program = this.program;
		this.program = null;
		init(program, renderable);
		renderable = null;
		
		dirLightsLoc 					= loc(u_dirLights0color);
		dirLightsColorOffset			= loc(u_dirLights0color) - dirLightsLoc;
		dirLightsDirectionOffset 	= loc(u_dirLights0direction) - dirLightsLoc;
		dirLightsSize 					= loc(u_dirLights1color) - dirLightsLoc;
		
		pointLightsLoc 				= loc(u_pointLights0color);
		pointLightsColorOffset 		= loc(u_pointLights0color) - pointLightsLoc;
		pointLightsPositionOffset 	= loc(u_pointLights0position) - pointLightsLoc;
		pointLightsIntensityOffset = loc(u_pointLights0intensity) - pointLightsLoc;
		pointLightsSize 				= loc(u_pointLights1color) - pointLightsLoc;
	}
	
	private static String createPrefix(final Renderable renderable, boolean lighting, boolean fog, int numDirectional, int numPoint, int numSpot, int numBones) {
		String prefix = "";
		final long mask = renderable.material.getMask();
		final long attributes = renderable.mesh.getVertexAttributes().getMask();
		if (((attributes & Usage.Color) == Usage.Color) || ((attributes & Usage.ColorPacked) == Usage.ColorPacked))
			prefix += "#define colorFlag\n";
		if ((attributes & Usage.Normal) == Usage.Normal) {
			prefix += "#define normalFlag\n";
			if (lighting) {
				prefix += "#define lightingFlag\n";
				prefix += "#define ambientCubemapFlag\n";
				prefix += "#define numDirectionalLights "+numDirectional+"\n";
				prefix += "#define numPointLights "+numPoint+"\n";
				if (fog) {
	 				prefix += "#define fogFlag\n";
 				}
			}
		}
		final int n = renderable.mesh.getVertexAttributes().size();
		for (int i = 0; i < n; i++) {
			final VertexAttribute attr = renderable.mesh.getVertexAttributes().get(i);
			if (attr.usage == Usage.BoneWeight)
				prefix += "#define boneWeight"+attr.unit+"Flag\n";
		}
		if ((attributes & Usage.Tangent) == Usage.Tangent)
			prefix += "#define tangentFlag\n";
		if ((attributes & Usage.BiNormal) == Usage.BiNormal)
			prefix += "#define binormalFlag\n";
		if ((mask & BlendingAttribute.Type) == BlendingAttribute.Type)
			prefix += "#define "+BlendingAttribute.Alias+"Flag\n";
		if ((mask & TextureAttribute.Diffuse) == TextureAttribute.Diffuse)
			prefix += "#define "+TextureAttribute.DiffuseAlias+"Flag\n";
		if ((mask & TextureAttribute.Normal) == TextureAttribute.Normal)
			prefix += "#define "+TextureAttribute.NormalAlias+"Flag\n";
		if ((mask & ColorAttribute.Diffuse) == ColorAttribute.Diffuse)
			prefix += "#define "+ColorAttribute.DiffuseAlias+"Flag\n";
		if ((mask & ColorAttribute.Specular) == ColorAttribute.Specular)
			prefix += "#define "+ColorAttribute.SpecularAlias+"Flag\n";
		if ((mask & FloatAttribute.Shininess) == FloatAttribute.Shininess)
			prefix += "#define "+FloatAttribute.ShininessAlias+"Flag\n";
		if ((mask & FloatAttribute.AlphaTest) == FloatAttribute.AlphaTest)
			prefix += "#define "+FloatAttribute.AlphaTestAlias+"Flag\n";
		if (numBones > 0)
			prefix += "#define numBones "+numBones+"\n";
		Gdx.app.log("Prefix","\n"+prefix);
		return prefix;
	}
	
	@Override
	public boolean canRender(final Renderable renderable) {
		return (materialMask == (renderable.material.getMask() | optionalAttributes)) && 
			(vertexMask == renderable.mesh.getVertexAttributes().getMask()) && 
			(renderable.lights != null) == lighting &&
            ((renderable.lights != null && renderable.lights.fog != null) == fog);
	}
	
	private final boolean can(final long flag) {
		return (materialMask & flag) == flag;
	}
	
	@Override
	public int compareTo(Shader other) {
		if (other == null) return -1;
		if (other == this) return 0;
		return 0; // FIXME compare shaders on their impact on performance
	}
	
	@Override
	public boolean equals (Object obj) {
		return (obj instanceof DefaultShader) ? equals((DefaultShader)obj) : false;
	}
	
	public boolean equals (DefaultShader obj) {
		return (obj == this);
	}

	private Matrix3 normalMatrix = new Matrix3();
	private Camera camera;
	private float time;
	private boolean lightsSet;
	
	@Override
	public void begin (final Camera camera, final RenderContext context) {
		super.begin(camera, context);
		
		for (final DirectionalLight dirLight : directionalLights)
			dirLight.set(0,0,0,0,-1,0);
		for (final PointLight pointLight : pointLights)
			pointLight.set(0,0,0,0,0,0,0);
		lightsSet = false;
		
		if (has(u_time))
			set(u_time, time+=Gdx.graphics.getDeltaTime());
	}
	
	@Override
	public void render (final Renderable renderable) {
		if (!renderable.material.has(BlendingAttribute.Type))
			context.setBlending(false, GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		bindMaterial(renderable);
		if (lighting)
			bindLights(renderable);
		super.render(renderable);
	}

	@Override
	public void end () {
		currentMaterial = null;
		super.end();
	}
	
	Material currentMaterial;
	private final void bindMaterial(final Renderable renderable) {
		if (currentMaterial == renderable.material)
			return;
		
		int cullFace = defaultCullFace;
		int depthFunc = defaultDepthFunc;
		float depthRangeNear = 0f;
		float depthRangeFar = 1f;
		boolean depthMask = true;
		
		currentMaterial = renderable.material;
		for (final Material.Attribute attr : currentMaterial) {
			final long t = attr.type;
			if (BlendingAttribute.is(t)) {
				context.setBlending(true, ((BlendingAttribute)attr).sourceFunction, ((BlendingAttribute)attr).destFunction);
				set(u_opacity, ((BlendingAttribute)attr).opacity);
			}
			else if ((t & IntAttribute.CullFace) == IntAttribute.CullFace)
				cullFace = ((IntAttribute)attr).value;
			else if ((t & FloatAttribute.AlphaTest) == FloatAttribute.AlphaTest)
				set(u_alphaTest, ((FloatAttribute)attr).value);
			else if ((t & DepthTestAttribute.Type) == DepthTestAttribute.Type) {
				DepthTestAttribute dta = (DepthTestAttribute)attr;
				depthFunc = dta.depthFunc;
				depthRangeNear = dta.depthRangeNear;
				depthRangeFar = dta.depthRangeFar;
				depthMask = dta.depthMask;
			}
			else if(!ignoreUnimplemented)
				throw new GdxRuntimeException("Unknown material attribute: "+attr.toString());
		}
		
		context.setCullFace(cullFace);
		context.setDepthTest(depthFunc, depthRangeNear, depthRangeFar, depthMask);
	}

	private final Vector3 tmpV1 = new Vector3();
	private final void bindLights(final Renderable renderable) {
		final Lights lights = renderable.lights;
		final Array<DirectionalLight> dirs = lights.directionalLights; 
		final Array<PointLight> points = lights.pointLights;
		
		if (dirLightsLoc >= 0) {
			for (int i = 0; i < directionalLights.length; i++) {
				if (dirs == null || i >= dirs.size) {
					if (lightsSet && directionalLights[i].color.r == 0f && directionalLights[i].color.g == 0f && directionalLights[i].color.b == 0f)
						continue;
					directionalLights[i].color.set(0,0,0,1);
				} else if (lightsSet && directionalLights[i].equals(dirs.get(i)))
					continue;
				else
					directionalLights[i].set(dirs.get(i));
				
				int idx = dirLightsLoc + i * dirLightsSize; 
				program.setUniformf(idx+dirLightsColorOffset, directionalLights[i].color.r, directionalLights[i].color.g, directionalLights[i].color.b);
				program.setUniformf(idx+dirLightsDirectionOffset, directionalLights[i].direction);
			}
		}
		
		if (pointLightsLoc >= 0) {
			for (int i = 0; i < pointLights.length; i++) {
				if (points == null || i >= points.size) {
					if (lightsSet && pointLights[i].intensity == 0f)
						continue;
					pointLights[i].intensity = 0f;
				} else if (lightsSet && pointLights[i].equals(points.get(i)))
					continue;
				else
					pointLights[i].set(points.get(i));

				int idx = pointLightsLoc + i * pointLightsSize;
				program.setUniformf(idx+pointLightsColorOffset, pointLights[i].color.r, pointLights[i].color.g, pointLights[i].color.b);
				program.setUniformf(idx+pointLightsPositionOffset, pointLights[i].position);
				if (pointLightsIntensityOffset >= 0)
					program.setUniformf(idx+pointLightsIntensityOffset, pointLights[i].intensity);
			}
		}

		if (lights.fog != null) {
			set(u_fogColor, lights.fog);
		}
		lightsSet = true;
	}

	@Override
	public void dispose () {
		program.dispose();
		super.dispose();
	}
}
