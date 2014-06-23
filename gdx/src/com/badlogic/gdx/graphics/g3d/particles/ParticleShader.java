/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.graphics.g3d.particles;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** This is a custom shader to render the particles.
 * Usually is not required, because the {@link DefaultShader} will be used instead.
 * This shader will be used when dealing with billboards using GPU mode or point sprites. 
 * @author inferno */
public class ParticleShader extends BaseShader {
	public enum ParticleType{
		Billboard,
		Point
	}
	
	public static enum AlignMode{
		Screen, ViewPoint//, ParticleDirection
	}

	public static class Config {
		/** The uber vertex shader to use, null to use the default vertex shader. */
		public String vertexShader = null;
		/** The uber fragment shader to use, null to use the default fragment shader. */
		public String fragmentShader = null;
		public boolean ignoreUnimplemented = true;
		/** Set to 0 to disable culling*/
		public int defaultCullFace = -1;
		/** Set to 0 to disable depth test */
		public int defaultDepthFunc = -1;
		public AlignMode align = AlignMode.Screen;
		public ParticleType type = ParticleType.Billboard;
		public Config() { }
		public Config(AlignMode align, ParticleType type) {
			this.align = align;
			this.type = type;
		}
		
		public Config(AlignMode align) {
			this.align = align;
		}
		
		public Config(ParticleType type) {
			this.type = type;
		}
		
		public Config(final String vertexShader, final String fragmentShader) {
			this.vertexShader = vertexShader;
			this.fragmentShader = fragmentShader;
		}
	}

	private static String defaultVertexShader = null;
	public static String getDefaultVertexShader() {
		if (defaultVertexShader == null)
			defaultVertexShader = Gdx.files.classpath("com/badlogic/gdx/graphics/g3d/particles/particles.vertex.glsl").readString();
		return defaultVertexShader;
	}
	
	private static String defaultFragmentShader = null;
	public static String getDefaultFragmentShader() {
		if (defaultFragmentShader == null)
			defaultFragmentShader = Gdx.files.classpath("com/badlogic/gdx/graphics/g3d/particles/particles.fragment.glsl").readString();
		return defaultFragmentShader;
	}
	
	protected static long implementedFlags = BlendingAttribute.Type | TextureAttribute.Diffuse ;
	
	static final Vector3 TMP_VECTOR3 = new Vector3();
	public static class Inputs{
		public final static Uniform cameraRight = new Uniform("u_cameraRight");
		public final static Uniform cameraInvDirection = new Uniform("u_cameraInvDirection");
		public final static Uniform screenWidth = new Uniform("u_screenWidth");
		public final static Uniform regionSize = new Uniform("u_regionSize");
	}
	public static class Setters{
		public final static Setter cameraRight = new Setter() {
			@Override public boolean isGlobal (BaseShader shader, int inputID) { return true; }
			@Override public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID,  TMP_VECTOR3.set(shader.camera.direction).crs(shader.camera.up).nor());
			}
		};

		public final static Setter cameraUp = new Setter() {
			@Override public boolean isGlobal (BaseShader shader, int inputID) { return true; }
			@Override public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID,  TMP_VECTOR3.set(shader.camera.up).nor());
			}
		};
		
		public final static Setter cameraInvDirection = new Setter() {
			@Override public boolean isGlobal (BaseShader shader, int inputID) { return true; }
			@Override public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID,  TMP_VECTOR3.set(-shader.camera.direction.x, -shader.camera.direction.y, -shader.camera.direction.z).nor());
			}
		};
		public final static Setter cameraPosition = new Setter() {
			@Override public boolean isGlobal (BaseShader shader, int inputID) { return true; }
			@Override public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID,  shader.camera.position);
			}
		};
		public final static Setter screenWidth = new Setter() {
			@Override public boolean isGlobal (BaseShader shader, int inputID) { return true; }
			@Override public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID,  (float)Gdx.graphics.getWidth());
			}
		};
		public final static Setter worldViewTrans = new Setter() {
			final Matrix4 temp = new Matrix4();
			@Override public boolean isGlobal (BaseShader shader, int inputID) { return false; }
			@Override public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID, temp.set(shader.camera.view).mul(renderable.worldTransform));
			}
		};
	}
	
	/** The renderable used to create this shader, invalid after the call to init */
	private Renderable renderable;
	private long materialMask;
	private long vertexMask;
	protected final Config config;
	/** Material attributes which are not required but always supported. */
	private final static long optionalAttributes = IntAttribute.CullFace | DepthTestAttribute.Type;
	
	public ParticleShader(final Renderable renderable) {
		this(renderable, new Config());
	}
	
	public ParticleShader(final Renderable renderable, final Config config) {
		this(renderable, config, createPrefix(renderable, config));
	}

	public ParticleShader(final Renderable renderable, final Config config, final String prefix) {
		this(renderable, config, prefix, 
				config.vertexShader != null ? config.vertexShader : getDefaultVertexShader(), 
				config.fragmentShader != null ? config.fragmentShader : getDefaultFragmentShader());
	}
	
	public ParticleShader(final Renderable renderable, final Config config, final String prefix, final String vertexShader, final String fragmentShader) {
		this(renderable, config, new ShaderProgram(prefix + vertexShader, prefix + fragmentShader));
	}
	
	public ParticleShader(final Renderable renderable, final Config config, final ShaderProgram shaderProgram) {
		this.config = config;
		this.program = shaderProgram;
		this.renderable = renderable;
		materialMask = renderable.material.getMask() | optionalAttributes;
		vertexMask = renderable.mesh.getVertexAttributes().getMask();

		if (!config.ignoreUnimplemented && (implementedFlags & materialMask) != materialMask)
			throw new GdxRuntimeException("Some attributes not implemented yet ("+materialMask+")");

		// Global uniforms
		register(DefaultShader.Inputs.viewTrans, DefaultShader.Setters.viewTrans);
		register(DefaultShader.Inputs.projViewTrans, DefaultShader.Setters.projViewTrans);
		register(DefaultShader.Inputs.projTrans, DefaultShader.Setters.projTrans);
		register(Inputs.screenWidth, Setters.screenWidth);
		register(DefaultShader.Inputs.cameraUp, Setters.cameraUp);
		register(Inputs.cameraRight, Setters.cameraRight);
		register(Inputs.cameraInvDirection, Setters.cameraInvDirection);
		register(DefaultShader.Inputs.cameraPosition, Setters.cameraPosition);
		
		// Object uniforms
		register(DefaultShader.Inputs.diffuseTexture, DefaultShader.Setters.diffuseTexture);
	}

	@Override
	public void init () {
		final ShaderProgram program = this.program;
		this.program = null;
		init(program, renderable);
		renderable = null;
	}
	
	public static String createPrefix(final Renderable renderable, final Config config) {
		String prefix = "";
		if(Gdx.app.getType() == ApplicationType.Desktop)
			prefix +="#version 120\n";
		else
			prefix +="#version 100\n";
		if(config.type == ParticleType.Billboard){
			prefix +="#define billboard\n";
			if(config.align == AlignMode.Screen) 
				prefix += "#define screenFacing\n";
			else if(config.align == AlignMode.ViewPoint) 
				prefix += "#define viewPointFacing\n";
			//else if(config.align == AlignMode.ParticleDirection) 
			//	prefix += "#define paticleDirectionFacing\n";
		}
		return prefix;
	}
	
	@Override
	public boolean canRender(final Renderable renderable) {
		return (materialMask == (renderable.material.getMask() | optionalAttributes)) && 
			(vertexMask == renderable.mesh.getVertexAttributes().getMask());
	}
	
	@Override
	public int compareTo(Shader other) {
		if (other == null) return -1;
		if (other == this) return 0;
		return 0; // FIXME compare shaders on their impact on performance
	}
	
	@Override
	public boolean equals (Object obj) {
		return (obj instanceof ParticleShader) ? equals((ParticleShader)obj) : false;
	}
	
	public boolean equals (ParticleShader obj) {
		return (obj == this);
	}

	@Override
	public void begin (final Camera camera, final RenderContext context) {
		super.begin(camera, context);
	}
	
	@Override
	public void render (final Renderable renderable) {
		if (!renderable.material.has(BlendingAttribute.Type))
			context.setBlending(false, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		bindMaterial(renderable);
		super.render(renderable);
	}

	@Override
	public void end () {
		currentMaterial = null;
		super.end();
	}
	
	Material currentMaterial;
	protected void bindMaterial(final Renderable renderable) {
		if (currentMaterial == renderable.material)
			return;
		
		int cullFace = config.defaultCullFace == -1 ? GL20.GL_BACK : config.defaultCullFace;
		int depthFunc = config.defaultDepthFunc == -1 ? GL20.GL_LEQUAL : config.defaultDepthFunc;
		float depthRangeNear = 0f;
		float depthRangeFar = 1f;
		boolean depthMask = true;
		
		currentMaterial = renderable.material;
		for (final Attribute attr : currentMaterial) {
			final long t = attr.type;
			if (BlendingAttribute.is(t)) {
				context.setBlending(true, ((BlendingAttribute)attr).sourceFunction, ((BlendingAttribute)attr).destFunction);
			}
			else if ((t & DepthTestAttribute.Type) == DepthTestAttribute.Type) {
				DepthTestAttribute dta = (DepthTestAttribute)attr;
				depthFunc = dta.depthFunc;
				depthRangeNear = dta.depthRangeNear;
				depthRangeFar = dta.depthRangeFar;
				depthMask = dta.depthMask;
			}
			else if(!config.ignoreUnimplemented)
				throw new GdxRuntimeException("Unknown material attribute: "+attr.toString());
		}
		
		context.setCullFace(cullFace);
		context.setDepthTest(depthFunc, depthRangeNear, depthRangeFar);
		context.setDepthMask(depthMask);
	}

	@Override
	public void dispose () {
		program.dispose();
		super.dispose();
	}
	
	public int getDefaultCullFace() {
		return config.defaultCullFace == -1 ? GL20.GL_BACK : config.defaultCullFace; 
	}
	
	public void setDefaultCullFace(int cullFace) {
		config.defaultCullFace = cullFace;
	}
	
	public int getDefaultDepthFunc() {
		return config.defaultDepthFunc == -1 ? GL20.GL_LEQUAL : config.defaultDepthFunc; 
	}
	
	public void setDefaultDepthFunc(int depthFunc) {
		config.defaultDepthFunc = depthFunc;
	}
}
