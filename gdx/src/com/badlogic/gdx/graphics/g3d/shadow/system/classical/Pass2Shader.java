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

package com.badlogic.gdx.graphics.g3d.shadow.system.classical;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/** This shader accumulates shadow with blending
 * @author realitix */
public class Pass2Shader extends DefaultShader {
	public static class Config extends DefaultShader.Config {
		public ClassicalShadowSystem shadowSystem;

		public Config (ClassicalShadowSystem shadowSystem) {
			this();
			this.shadowSystem = shadowSystem;
		}

		public Config () {
			super();
		}
	}

	public static class Inputs extends DefaultShader.Inputs {
		public final static Uniform shadowMapProjViewTrans = new Uniform("u_shadowMapProjViewTrans");
		public final static Uniform shadowTexture = new Uniform("u_shadowTexture");
		public final static Uniform uvTransform = new Uniform("u_uvTransform");
		public final static Uniform lightQuantity = new Uniform("u_lightQuantity");
	}

	public static class Setters extends DefaultShader.Setters {
		public final static Setter shadowMapProjViewTrans = new GlobalSetter() {
			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID, shadowSystem.getCurrentLightProperties().camera.combined);
			}
		};
		public final static Setter shadowTexture = new GlobalSetter() {
			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID, shadowSystem.getTexture(0));
			}
		};
		public final static Setter uvTransform = new GlobalSetter() {
			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				final TextureRegion tr = shadowSystem.getCurrentLightProperties().region;
				shader.set(inputID, tr.getU(), tr.getV(), tr.getU2() - tr.getU(), tr.getV2() - tr.getV());
			}
		};
		public final static Setter lightQuantity = new GlobalSetter() {
			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID, shadowSystem.getLightQuantity());
			}
		};
	}

	protected static ClassicalShadowSystem shadowSystem;
	private static String defaultVertexShader = null;

	public static String getDefaultVertexShader () {
		if (defaultVertexShader == null)
			defaultVertexShader = Gdx.files.classpath("com/badlogic/gdx/graphics/g3d/shadow/system/classical/pass2.vertex.glsl")
			.readString();
		return defaultVertexShader;
	}

	private static String defaultFragmentShader = null;

	public static String getDefaultFragmentShader () {
		if (defaultFragmentShader == null)
			defaultFragmentShader = Gdx.files.classpath("com/badlogic/gdx/graphics/g3d/shadow/system/classical/pass2.fragment.glsl")
			.readString();
		return defaultFragmentShader;
	}

	public Pass2Shader (final Renderable renderable) {
		this(renderable, new Config());
	}

	public Pass2Shader (final Renderable renderable, final Config config) {
		this(renderable, config, createPrefix(renderable, config));
	}

	public Pass2Shader (final Renderable renderable, final Config config, final String prefix) {
		this(renderable, config, prefix, config.vertexShader != null ? config.vertexShader : getDefaultVertexShader(),
			config.fragmentShader != null ? config.fragmentShader : getDefaultFragmentShader());
	}

	public Pass2Shader (final Renderable renderable, final Config config, final String prefix, final String vertexShader,
		final String fragmentShader) {
		this(renderable, config, new ShaderProgram(prefix + vertexShader, prefix + fragmentShader));
	}

	public Pass2Shader (final Renderable renderable, final Config config, final ShaderProgram shaderProgram) {
		super(renderable, config, shaderProgram);
		shadowSystem = config.shadowSystem;
		register(Inputs.shadowMapProjViewTrans, Setters.shadowMapProjViewTrans);
		register(Inputs.shadowTexture, Setters.shadowTexture);
		register(Inputs.uvTransform, Setters.uvTransform);
		register(Inputs.lightQuantity, Setters.lightQuantity);
	}

	@Override
	public void begin (final Camera camera, final RenderContext context) {
		super.begin(camera, context);
		context.setDepthTest(GL20.GL_EQUAL);
		context.setDepthMask(true);
		context.setBlending(true, GL20.GL_ONE, GL20.GL_ONE);
	}

	@Override
	public void render (Renderable renderable, Attributes combinedAttributes) {
		if (shadowSystem.isFirstCallPass2()) {
			combinedAttributes.remove(BlendingAttribute.Type);
			combinedAttributes.set(new DepthTestAttribute());
		} else {
			combinedAttributes.set(new DepthTestAttribute(GL20.GL_EQUAL));
			combinedAttributes.set(new BlendingAttribute(GL20.GL_ONE, GL20.GL_ONE));
		}
		super.render(renderable, combinedAttributes);
	}
}
