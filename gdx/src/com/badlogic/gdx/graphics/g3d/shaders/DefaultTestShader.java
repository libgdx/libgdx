package com.badlogic.gdx.graphics.g3d.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.lights.AmbientCubemap;
import com.badlogic.gdx.graphics.g3d.lights.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.lights.Lights;
import com.badlogic.gdx.graphics.g3d.lights.PointLight;
import com.badlogic.gdx.graphics.g3d.materials.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.materials.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.materials.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader.Input;
import com.badlogic.gdx.graphics.g3d.shaders.ShaderBuilder.Part;
import com.badlogic.gdx.graphics.g3d.shaders.inputs.AmbientCubemapInput;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class DefaultTestShader extends BaseShader {
	public final static Part position0	= Part.newInput("a_position",	"attribute vec3 a_position;");
	public final static Part color0		= Part.newInput("a_color",		"attribute vec3 a_color;");
	public final static Part normal0		= Part.newInput("a_normal",		"attribute vec3 a_normal;");
	public final static Part texCoords0	= Part.newInput("a_texCoords0",	"attribute vec2 a_texCoords0;");

	public final static Part position		= Part.newVariable("position", "vec4");
	public final static Part positionInit0	= Part.newModifier("positionInit0", position, 1, "position = vec4(a_position,1.0);", position0);

	public final static Part color			= Part.newVariable("color", "vec4");
	public final static Part colorDefault	= Part.newModifier("colorDefault", color, 1, "color = vec4(1.0);");
	public final static Part colorInit0		= Part.newModifier("colorInit0", color, 1, "color = vec4(a_color, 1.0);", color0);

	public final static Part normal			= Part.newVariable("normal", "vec3");
	public final static Part normalInit0	= Part.newModifier("normalInit0", normal, 1, "normal = a_normal;", normal0);
	
	public final static Part gl_Position	= Part.newOutput("gl_Position", ShaderBuilder.VERTEX);
	public final static Part gl_PositionDefault	= Part.newModifier("gl_PositionDefault", gl_Position, 1, "gl_Position = position;", position);
	
	public final static Part gl_FragColor	= Part.newOutput("gl_FragColor", ShaderBuilder.FRAGMENT);
	public final static Part gl_FragColorDefault	= Part.newModifier("gl_FragColorDefault", gl_FragColor, 1, "gl_FragColor = color;", color);
	
	// Sub shaders:
	public final static Part worldTrans		= Part.newInput("u_worldTrans",	"uniform mat4 u_worldTrans;");
	public final static Part positionWorld	= Part.newModifier("positionWorld", position, 10, "position = u_worldTrans * position;", worldTrans);
	public final static Part normalWorld	= Part.newModifier("normalWorld", normal, 10, "normal = normalize(u_worldTrans * vec4(normal,1.0)).xyz;", worldTrans);
	
	public final static Part projTrans	= Part.newInput("u_projTrans",	"uniform mat4 u_projTrans;");
	public final static Part positionProject	= Part.newModifier("positionProject", position, -10, "position = u_projTrans * position;", projTrans);
	
	public final static Part ambientCubemap	= Part.newInput("u_ambientCubemap",	"uniform vec3 u_ambientCubemap[6];");
	
	public final static Part getAmbientCubemap = new Part(Part.CLAZZ, "getAmbientCubemap", false, ShaderBuilder.AUTO, true, 0, 0, 0, null, false, null, 0, false,
		"vec3 getAmbientCubemap() {\n"+
		"	vec3 squaredNormal = normal * normal;\n"+
		"	vec3 isPositive  = step(0.0, normal);\n"+
		"	return squaredNormal.x * mix(u_ambientCubemap[0], u_ambientCubemap[1], isPositive.x) +\n"+
		"		squaredNormal.y * mix(u_ambientCubemap[2], u_ambientCubemap[3], isPositive.y) +\n"+
		"		squaredNormal.z * mix(u_ambientCubemap[4], u_ambientCubemap[5], isPositive.z);\n"+
		"}\n", normal, ambientCubemap);
	public final static Part applyAmbientCubemap	= Part.newModifier("applyAmbientCubemap", color, 0, "color.rgb *= getAmbientCubemap();", getAmbientCubemap);
	
	public final static Part DirectionalLight = new Part(Part.CLAZZ, "DirectionalLight", false, ShaderBuilder.AUTO, true, 0, 0, 0, null, false, null, 0, false,
		"struct DirectionalLight\n"+
		"{\n"+
		"	vec3 color;\n"+
		"	vec3 direction;\n"+
		"};");
	
	// Global uniforms
	protected final Input u_projTrans				= register(new Input(GLOBAL_UNIFORM, "u_projTrans"));
	protected final Input u_cameraPosition			= register(new Input(GLOBAL_UNIFORM, "u_cameraPosition"));
	protected final Input u_cameraDirection		= register(new Input(GLOBAL_UNIFORM, "u_cameraDirection"));
	protected final Input u_cameraUp					= register(new Input(GLOBAL_UNIFORM, "u_cameraUp"));
	// Object uniforms
	protected final Input u_worldTrans				= register(new Input(LOCAL_UNIFORM, "u_worldTrans"));
	protected final Input u_normalMatrix			= register(new Input(LOCAL_UNIFORM, "u_normalMatrix", 0, Usage.Normal));
	protected final Input u_bones						= register(new Input(LOCAL_UNIFORM, "u_bones"));
	// Material uniforms
	protected final Input u_shininess				= register(new Input(LOCAL_UNIFORM, "u_shininess", FloatAttribute.Shininess));
	protected final Input u_opacity					= register(new Input(LOCAL_UNIFORM, "u_opacity", BlendingAttribute.Type));
	protected final Input u_diffuseColor			= register(new Input(LOCAL_UNIFORM, "u_diffuseColor", ColorAttribute.Diffuse));
	protected final Input u_diffuseTexture			= register(new Input(LOCAL_UNIFORM, "u_diffuseTexture", TextureAttribute.Diffuse));
	protected final Input u_specularColor			= register(new Input(LOCAL_UNIFORM, "u_specularColor", ColorAttribute.Specular));
	protected final Input u_specularTexture		= register(new Input(LOCAL_UNIFORM, "u_specularTexture", TextureAttribute.Specular));
	protected final Input u_normalTexture			= register(new Input(LOCAL_UNIFORM, "u_normalTexture", TextureAttribute.Normal));
	protected final Input u_alphaTest				= register(new Input(LOCAL_UNIFORM, "u_alphaTest", FloatAttribute.AlphaTest));
	// Lighting uniforms
	protected final Input u_ambientLight			= register(new Input(LOCAL_UNIFORM, "u_ambientLight"));
	protected final Input u_ambientCubemap			= register(new AmbientCubemapInput(0, 0, LOCAL_UNIFORM, "u_ambientCubemap"));
	protected final Input u_fogColor				   = register(new Input(LOCAL_UNIFORM, "u_fogColor"));
	// Vertex attributes
	protected final Input a_position					= register(new Input(VERTEX_ATTRIBUTE, "a_position", 0, Usage.Position));
	protected final Input a_position1				= register(new Input(VERTEX_ATTRIBUTE, "a_position1", 0, Usage.Position));
	protected final Input a_color						= register(new Input(VERTEX_ATTRIBUTE, "a_color", 0, Usage.Color));
	protected final Input a_color1					= register(new Input(VERTEX_ATTRIBUTE, "a_color1", 0, Usage.Color));
	protected final Input a_normal					= register(new Input(VERTEX_ATTRIBUTE, "a_normal", 0, Usage.Normal));
	protected final Input a_normal1					= register(new Input(VERTEX_ATTRIBUTE, "a_normal1", 0, Usage.Normal));
	protected final Input a_texCoords0				= register(new Input(VERTEX_ATTRIBUTE, "a_texCoords0", 0, Usage.TextureCoordinates));
	protected final Input a_texCoords1				= register(new Input(VERTEX_ATTRIBUTE, "a_texCoords1", 0, Usage.TextureCoordinates));
//	public static final int BoneWeight = 64;
//	public static final int Tangent = 128;
//	public static final int BiNormal = 256;
	
	protected final long materialMask;
	protected final long vertexMask;
	protected final long userMask;
	
	public DefaultTestShader (final Renderable renderable, long userMask) {
		this.materialMask = renderable.material.getMask();
		this.vertexMask = renderable.mesh.getVertexAttributes().getMask();
		this.userMask = userMask;
	}
	
	@Override
	public void init () {
		ShaderBuilder builder = new ShaderBuilder();
		builder.materialMask = materialMask;
		builder.vertexMask = vertexMask;
		builder.userMask = userMask;
		ShaderProgram program = builder.build(this, gl_Position, gl_FragColor);
		init(program, materialMask, vertexMask, userMask);
	}

	@Override
	public int compareTo (Shader other) {
		return 0;
	}

	@Override
	public boolean canRender (Renderable renderable) {
		return materialMask == renderable.material.getMask() && 
			vertexMask == renderable.mesh.getVertexAttributes().getMask();
	}
	
	@Override
	public void begin (Camera camera, RenderContext context) {
		super.begin(camera, context);
		float fogDist  = 1.09f / camera.far;
		fogDist *= fogDist;
		
		set(u_projTrans, camera.combined);
		set(u_cameraPosition, camera.position.x, camera.position.y, camera.position.z, fogDist);
		set(u_cameraDirection, camera.direction);
		set(u_cameraUp, camera.up);
		context.setDepthTest(true, GL10.GL_LEQUAL);
		
	}
	
	protected final AmbientCubemap cacheAmbientCubemap = new AmbientCubemap();
	Vector3 tmpV1 = new Vector3();
	@Override
	public void render (Renderable renderable) {
		if (!renderable.material.has(BlendingAttribute.Type))
			context.setBlending(false, GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		set(u_worldTrans, renderable.worldTransform);
		
		super.render(renderable);
	}
}
