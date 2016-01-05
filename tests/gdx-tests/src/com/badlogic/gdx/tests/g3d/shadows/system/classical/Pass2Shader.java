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

package com.badlogic.gdx.tests.g3d.shadows.system.classical;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/** This shader accumulates shadow with blending
 * @author realitix */
public class Pass2Shader extends DefaultShader {
	public static class Config extends DefaultShader.Config {
		public ClassicalShadowSystem shadowSystem;

		public Config (ClassicalShadowSystem shadowSystem) {
			super();
			this.shadowSystem = shadowSystem;
		}
	}

	public static class Inputs extends DefaultShader.Inputs {
		public final static Uniform shadowMapProjViewTrans = new Uniform("u_shadowMapProjViewTrans");
		public final static Uniform shadowTexture = new Uniform("u_shadowTexture");
		public final static Uniform uvTransform = new Uniform("u_uvTransform");
		public final static Uniform lightColor = new Uniform("u_lightColor");
		public final static Uniform lightDirection = new Uniform("u_lightDirection");
		public final static Uniform lightIntensity = new Uniform("u_lightIntensity");
		public final static Uniform lightPosition = new Uniform("u_lightPosition");
		public final static Uniform lightCutoffAngle = new Uniform("u_lightCutoffAngle");
		public final static Uniform lightExponent = new Uniform("u_lightExponent");
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
		public final static Setter lightColor = new GlobalSetter() {
			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				BaseLight l = shadowSystem.getCurrentLight();
				float intensity = 1;
				if (l instanceof PointLight) intensity = ((PointLight)l).intensity;
				if (l instanceof SpotLight) intensity = ((SpotLight)l).intensity;
				shader.set(inputID, l.color.r * intensity, l.color.g * intensity, l.color.b * intensity);
			}
		};
		public final static Setter lightDirection = new GlobalSetter() {
			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				BaseLight l = shadowSystem.getCurrentLight();
				if (l instanceof DirectionalLight) {
					shader.set(inputID, ((DirectionalLight)l).direction);
				}
				if (l instanceof SpotLight) {
					shader.set(inputID, ((SpotLight)l).direction);
				}
				if (l instanceof PointLight) {
					shader.set(inputID, shadowSystem.getCurrentLightProperties().camera.direction);
				}
			}
		};
		public final static Setter lightIntensity = new GlobalSetter() {
			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				BaseLight l = shadowSystem.getCurrentLight();
				if (l instanceof PointLight) {
					shader.set(inputID, ((PointLight)l).intensity);
				}
				if (l instanceof SpotLight) {
					shader.set(inputID, ((SpotLight)l).intensity);
				}
			}
		};
		public final static Setter lightPosition = new GlobalSetter() {
			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				BaseLight l = shadowSystem.getCurrentLight();
				if (l instanceof PointLight) {
					shader.set(inputID, ((PointLight)l).position);
				}
				if (l instanceof SpotLight) {
					shader.set(inputID, ((SpotLight)l).position);
				}
			}
		};
		public final static Setter lightCutoffAngle = new GlobalSetter() {
			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				if (!(shadowSystem.getCurrentLight() instanceof DirectionalLight)) {
					shader.set(inputID, ((PerspectiveCamera)shadowSystem.getCurrentLightProperties().camera).fieldOfView);
				}

			}
		};
		public final static Setter lightExponent = new GlobalSetter() {
			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				BaseLight l = shadowSystem.getCurrentLight();
				if (l instanceof SpotLight) {
					shader.set(inputID, ((SpotLight)l).exponent);
				}
				if (l instanceof PointLight) {
					shader.set(inputID, (float)0);
				}
			}
		};
	}

	protected static ClassicalShadowSystem shadowSystem;
	private static String defaultVertexShader = null;

	public static String getDefaultVertexShader () {
		if (defaultVertexShader == null)
			defaultVertexShader = Gdx.files.classpath("com/badlogic/gdx/tests/g3d/shadows/system/classical/pass2.vertex.glsl")
			.readString();
		return defaultVertexShader;
	}

	private static String defaultFragmentShader = null;

	public static String getDefaultFragmentShader () {
		if (defaultFragmentShader == null)
			defaultFragmentShader = Gdx.files.classpath("com/badlogic/gdx/tests/g3d/shadows/system/classical/pass2.fragment.glsl")
			.readString();
		return defaultFragmentShader;
	}

	protected BlendingAttribute blend = new BlendingAttribute(GL20.GL_ONE, GL20.GL_ONE);
	protected DepthTestAttribute depth = new DepthTestAttribute(GL20.GL_LEQUAL);
	protected int lightType = -1;
	public static final int LIGHT_SPOT = 0;
	public static final int LIGHT_DIR = 1;

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
		register(Inputs.lightColor, Setters.lightColor);
		register(Inputs.lightDirection, Setters.lightDirection);
		register(Inputs.lightPosition, Setters.lightPosition);
		register(Inputs.lightIntensity, Setters.lightIntensity);
		register(Inputs.lightCutoffAngle, Setters.lightCutoffAngle);
		register(Inputs.lightExponent, Setters.lightExponent);
	}

	public static String createPrefix (final Renderable renderable, final Config config) {
		String prefix = DefaultShader.createPrefix(renderable, config);
		boolean dir = (config.shadowSystem.getCurrentLight() instanceof DirectionalLight);
		if (dir)
			prefix += "#define directionalLight\n";
		else
			prefix += "#define spotLight\n";
		return prefix;
	}

	@Override
	public void render (Renderable renderable, Attributes combinedAttributes) {
		if (shadowSystem.isFirstCallPass2())
			combinedAttributes.remove(BlendingAttribute.Type);
		else
			combinedAttributes.set(blend);

		combinedAttributes.set(depth);

		super.render(renderable, combinedAttributes);
	}

	@Override
	public boolean canRender (Renderable renderable) {
		boolean ok = super.canRender(renderable);
		boolean dir = (shadowSystem.getCurrentLight() instanceof DirectionalLight);

		if (lightType == -1) {
			lightType = LIGHT_SPOT;
			if (dir) lightType = LIGHT_DIR;
		}

		if (dir && lightType != LIGHT_DIR) ok = false;
		if (!dir && lightType != LIGHT_SPOT) ok = false;
		return ok;
	}

}
