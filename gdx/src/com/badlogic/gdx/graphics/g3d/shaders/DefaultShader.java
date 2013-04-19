package com.badlogic.gdx.graphics.g3d.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Light;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.lights.AmbientCubemap;
import com.badlogic.gdx.graphics.g3d.lights.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.lights.Lights;
import com.badlogic.gdx.graphics.g3d.lights.PointLight;
import com.badlogic.gdx.graphics.g3d.materials.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.materials.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.materials.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class DefaultShader implements Shader {
	// General uniform names
	public final static String PROJECTION_TRANSFORM = "u_projTrans";
	public final static String MODEL_TRANSFORM 		= "u_modelTrans";
	public final static String NORMAL_TRANSFORM 		= "u_normalMatrix";
	public final static String CAMERA_POSITION 		= "u_cameraPosition";
	public final static String CAMERA_DIRECTION 		= "u_cameraDirection";
	public final static String CAMERA_UP 				= "u_cameraUp";
	
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
	
	protected final ShaderProgram program;
	// General uniform locations, a shader doesn't have to implement all of them
	protected int projTransLoc;
	protected int modelTransLoc;
	protected int normalTransLoc;
	protected int cameraPosLoc;
	protected int cameraDirLoc;
	protected int cameraUpLoc;
	// Material uniform locations
	protected int diffuseTextureLoc;
	protected int diffuseColorLoc;
	protected int specularColorLoc;
	protected int shininessLoc;
	// Lighting uniform locations
	protected int ambientCubemapLoc;
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
	protected final AmbientCubemap ambientCubemap = new AmbientCubemap();
	protected final DirectionalLight directionalLights[];
	protected final PointLight pointLights[];
	
	protected RenderContext context;
	protected long mask;
	protected long attributes;
	
	public DefaultShader(final Material material, final VertexAttributes attributes, boolean lighting, int numDirectional, int numPoint, int numSpot) {
		this(getDefaultVertexShader(), getDefaultFragmentShader(), material, attributes, lighting, numDirectional, numPoint, numSpot);
	}
	
	public DefaultShader(final long mask, final long attributes, boolean lighting, int numDirectional, int numPoint, int numSpot) {
		this(getDefaultVertexShader(), getDefaultFragmentShader(), mask, attributes, lighting, numDirectional, numPoint, numSpot);
	}

	public DefaultShader(final String vertexShader, final String fragmentShader, final Material material, final VertexAttributes attributes, boolean lighting, int numDirectional, int numPoint, int numSpot) {
		this(vertexShader, fragmentShader, material.getMask(), getAttributesMask(attributes), lighting, numDirectional, numPoint, numSpot);
	}
	
	public DefaultShader(final String vertexShader, final String fragmentShader, final long mask, final long attributes, boolean lighting, int numDirectional, int numPoint, int numSpot) {
		if (!Gdx.graphics.isGL20Available())
			throw new GdxRuntimeException("This shader requires OpenGL ES 2.0");
		
		ShaderProgram.pedantic = false; // FIXME
		
		this.lighting = lighting;
		this.directionalLights = new DirectionalLight[lighting && numDirectional > 0 ? numDirectional : 0];
		for (int i = 0; i < directionalLights.length; i++)
			directionalLights[i] = new DirectionalLight();
		this.pointLights = new PointLight[lighting && numPoint > 0 ? numPoint : 0];
		for (int i = 0; i < pointLights.length; i++)
			pointLights[i] = new PointLight();
		
		String prefix = "";
		this.mask = mask;
		this.attributes = attributes;
				
		if (!ignoreUnimplemented && (implementedFlags & mask) != mask)
			throw new GdxRuntimeException("Some attributes not implemented yet ("+mask+")");
		
		if ((attributes & Usage.Color) == Usage.Color)
			prefix += "#define colorFlag\n";
		if ((attributes & Usage.Normal) == Usage.Normal) {
			prefix += "#define normalFlag\n";
			if (lighting) {
				prefix += "#define lightingFlag\n";
				prefix += "#define ambientCubemapFlag\n";
				prefix += "#define numDirectionalLights "+numDirectional+"\n";
				prefix += "#define numPointLights "+numPoint+"\n";
			}
		}
		if (can(BlendingAttribute.Type))
			prefix += "#define "+BlendingAttribute.Alias+"Flag\n";
		if (can(TextureAttribute.Diffuse))
			prefix += "#define "+TextureAttribute.DiffuseAlias+"Flag\n";
		if (can(ColorAttribute.Diffuse))
			prefix += "#define "+ColorAttribute.DiffuseAlias+"Flag\n";
		if (can(ColorAttribute.Specular))
			prefix += "#define "+ColorAttribute.SpecularAlias+"Flag\n";
		if (can(FloatAttribute.Shininess))
			prefix += "#define "+FloatAttribute.ShininessAlias+"Flag\n";

		program = new ShaderProgram(prefix + vertexShader, prefix + fragmentShader);
		if (!program.isCompiled())
			throw new GdxRuntimeException(program.getLog());
		
		// General uniforms
		projTransLoc = program.getUniformLocation(PROJECTION_TRANSFORM);
		modelTransLoc = program.getUniformLocation(MODEL_TRANSFORM);
		normalTransLoc = program.getUniformLocation(NORMAL_TRANSFORM);
		cameraPosLoc = program.getUniformLocation(CAMERA_POSITION);
		cameraDirLoc = program.getUniformLocation(CAMERA_DIRECTION);
		cameraUpLoc = program.getUniformLocation(CAMERA_UP);
		
		// Material uniforms
		diffuseTextureLoc = !can(TextureAttribute.Diffuse) ? -1 : program.getUniformLocation(TextureAttribute.DiffuseAlias);
		diffuseColorLoc = !can(ColorAttribute.Diffuse) ? -1 : program.getUniformLocation(ColorAttribute.DiffuseAlias);
		specularColorLoc = !can(ColorAttribute.Specular) ? -1 : program.getUniformLocation(ColorAttribute.SpecularAlias);
		shininessLoc = !can(FloatAttribute.Shininess) ? -1 : program.getUniformLocation(FloatAttribute.ShininessAlias);
		
		// Lighting uniforms
		ambientCubemapLoc = lighting ? program.fetchUniformLocation("ambientCubemap") : -1;
		
		dirLightsLoc = lighting ? program.fetchUniformLocation("directionalLights[0].color") : -1;
		dirLightsColorOffset = dirLightsLoc >= 0 ? 0 : -1; //program.fetchUniformLocation("directionalLights[0].color") - directionalLightsLoc : -1;
		dirLightsDirectionOffset = dirLightsLoc >= 0 ? program.fetchUniformLocation("directionalLights[0].direction") - dirLightsLoc : -1;
		dirLightsSize = (numDirectional > 1 && dirLightsLoc >= 0) ? program.fetchUniformLocation("directionalLights[1].color") - dirLightsLoc : -1;
		
		pointLightsLoc = lighting ? program.fetchUniformLocation("pointLights[0].color") : -1;
		pointLightsColorOffset = pointLightsLoc >= 0 ? 0 : -1; //program.fetchUniformLocation("pointLights[0].color") - pointLightsLoc : -1;
		pointLightsPositionOffset = pointLightsLoc >= 0 ? program.fetchUniformLocation("pointLights[0].position") - pointLightsLoc : -1;
		pointLightsIntensityOffset = pointLightsLoc >= 0 ? program.fetchUniformLocation("pointLights[0].intensity") - pointLightsLoc : -1;
		pointLightsSize = (numPoint > 1 && pointLightsLoc >= 0) ? program.fetchUniformLocation("pointLights[1].color") - pointLightsLoc : -1;
		
		// FIXME Cache vertex attribute locations...
	}
	
	private static long getAttributesMask(final VertexAttributes attributes) {
		long result = 0;
		final int n = attributes.size();
		for (int i = 0; i < n; i++)
			result |= (long)attributes.get(i).usage;
		return result;
	}
	
	@Override
	public boolean canRender(final Renderable renderable) {
		return mask == renderable.material.getMask() && 
			attributes == getAttributesMask(renderable.mesh.getVertexAttributes()) && 
			(renderable.lights != null) == lighting;
	}
	
	private final boolean can(final long flag) {
		return (mask & flag) == flag;
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
		
		if (projTransLoc >= 0)
			program.setUniformMatrix(projTransLoc, camera.combined);
		if (cameraPosLoc >= 0)
			program.setUniformf(cameraPosLoc, camera.position);
		if (cameraDirLoc >= 0)
			program.setUniformf(cameraDirLoc, camera.direction);
		if (cameraUpLoc >= 0)
			program.setUniformf(cameraUpLoc, camera.up);
		
		for (final DirectionalLight dirLight : directionalLights)
			dirLight.set(0,0,0,0,-1,0);
		for (final PointLight pointLight : pointLights)
			pointLight.set(0,0,0,0,0,0,0);
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
		if (lighting)
			bindLights(renderable);
		if (currentMesh != renderable.mesh) {
			if (currentMesh != null)
				currentMesh.unbind(program);
			renderable.mesh.setAutoBind(false); // FIXME this doesn't belong here
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
	
	Material currentMaterial;
	private final void bindMaterial(final Renderable renderable) {
		if (currentMaterial == renderable.material)
			return;
		currentMaterial = renderable.material;
		for (Material.Attribute attr : currentMaterial) {
			final long t = attr.type;
			if (BlendingAttribute.is(t))
				context.setBlending(true, ((BlendingAttribute)attr).sourceFunction, ((BlendingAttribute)attr).destFunction);
			else if (ColorAttribute.is(t)) {
				ColorAttribute col = (ColorAttribute)attr;
				if ((t & ColorAttribute.Diffuse) == ColorAttribute.Diffuse)
					program.setUniformf(diffuseColorLoc, col.color);
				else if ((t & ColorAttribute.Specular) == ColorAttribute.Specular)
					program.setUniformf(specularColorLoc, col.color);
			}
			else if (TextureAttribute.is(t)) {
				TextureAttribute tex = (TextureAttribute)attr;
				if ((t & TextureAttribute.Diffuse) == TextureAttribute.Diffuse)
					bindTextureAttribute(diffuseTextureLoc, tex);
				// TODO else if (..)
			}
			else if ((t & FloatAttribute.Shininess) == FloatAttribute.Shininess)
				program.setUniformf(shininessLoc, ((FloatAttribute)attr).value);
			else if(!ignoreUnimplemented)
					throw new GdxRuntimeException("Unknown material attribute: "+attr.toString());
		}
	}

	TextureAttribute currentTextureAttribute;
	private final void bindTextureAttribute(final int uniform, final TextureAttribute attribute) {
		final int unit = context.textureBinder.bind(attribute.textureDescription);
		program.setUniformi(uniform, unit);
		currentTextureAttribute = attribute;
	}
	
	private final Vector3 tmpV1 = new Vector3();
	private final void bindLights(final Renderable renderable) {
		final Lights lights = renderable.lights;
		final Array<DirectionalLight> dirs = lights.directionalLights; 
		final Array<PointLight> points = lights.pointLights;
		
		if (ambientCubemapLoc >= 0) {
			renderable.transform.getTranslation(tmpV1);
			ambientCubemap.set(lights.ambientLight);
			
			for (int i = directionalLights.length; i < dirs.size; i++)
				ambientCubemap.add(dirs.get(i).color, dirs.get(i).direction);
			
			for (int i = pointLights.length; i < points.size; i++)
				ambientCubemap.add(points.get(i).color, points.get(i).position, tmpV1, points.get(i).intensity);
			
			ambientCubemap.clamp();
			
			program.setUniform3fv(ambientCubemapLoc, ambientCubemap.data, 0, ambientCubemap.data.length);
		}
		
		if (dirLightsLoc >= 0) {
			for (int i = 0; i < directionalLights.length; i++) {
				if (dirs == null || i >= dirs.size) {
					if (directionalLights[i].color.r == 0f && directionalLights[i].color.g == 0f && directionalLights[i].color.b == 0f)
						continue;
					directionalLights[i].color.set(0,0,0,1);
				} else if (directionalLights[i].equals(dirs.get(i)))
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
					if (pointLights[i].intensity == 0f)
						continue;
					pointLights[i].intensity = 0f;
				} else if (pointLights[i].equals(points.get(i)))
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
	}

	@Override
	public void dispose () {
		program.dispose();
	}
}
