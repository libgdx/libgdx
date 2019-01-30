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

package com.badlogic.gdx.graphics.g3d.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.CubemapAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DirectionalLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.PointLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.SpotLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.AmbientCubemap;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class DefaultShader extends BaseShader {
	public static class Config {
		/** The uber vertex shader to use, null to use the default vertex shader. */
		public String vertexShader = null;
		/** The uber fragment shader to use, null to use the default fragment shader. */
		public String fragmentShader = null;
		/** The number of directional lights to use */
		public int numDirectionalLights = 2;
		/** The number of point lights to use */
		public int numPointLights = 5;
		/** The number of spot lights to use */
		public int numSpotLights = 0;
		/** The number of bones to use */
		public int numBones = 12;
		/** */
		public boolean ignoreUnimplemented = true;
		/** Set to 0 to disable culling, -1 to inherit from {@link DefaultShader#defaultCullFace} */
		public int defaultCullFace = -1;
		/** Set to 0 to disable depth test, -1 to inherit from {@link DefaultShader#defaultDepthFunc} */
		public int defaultDepthFunc = -1;

		public Config () {
		}

		public Config (final String vertexShader, final String fragmentShader) {
			this.vertexShader = vertexShader;
			this.fragmentShader = fragmentShader;
		}
	}

	public static class Inputs {
		public final static Uniform projTrans = new Uniform("u_projTrans");
		public final static Uniform viewTrans = new Uniform("u_viewTrans");
		public final static Uniform projViewTrans = new Uniform("u_projViewTrans");
		public final static Uniform cameraPosition = new Uniform("u_cameraPosition");
		public final static Uniform cameraDirection = new Uniform("u_cameraDirection");
		public final static Uniform cameraUp = new Uniform("u_cameraUp");
		public final static Uniform cameraNearFar = new Uniform("u_cameraNearFar");

		public final static Uniform worldTrans = new Uniform("u_worldTrans");
		public final static Uniform viewWorldTrans = new Uniform("u_viewWorldTrans");
		public final static Uniform projViewWorldTrans = new Uniform("u_projViewWorldTrans");
		public final static Uniform normalMatrix = new Uniform("u_normalMatrix");
		public final static Uniform bones = new Uniform("u_bones");

		public final static Uniform shininess = new Uniform("u_shininess", FloatAttribute.Shininess);
		public final static Uniform opacity = new Uniform("u_opacity", BlendingAttribute.Type);
		public final static Uniform diffuseColor = new Uniform("u_diffuseColor", ColorAttribute.Diffuse);
		public final static Uniform diffuseTexture = new Uniform("u_diffuseTexture", TextureAttribute.Diffuse);
		public final static Uniform diffuseUVTransform = new Uniform("u_diffuseUVTransform", TextureAttribute.Diffuse);
		public final static Uniform specularColor = new Uniform("u_specularColor", ColorAttribute.Specular);
		public final static Uniform specularTexture = new Uniform("u_specularTexture", TextureAttribute.Specular);
		public final static Uniform specularUVTransform = new Uniform("u_specularUVTransform", TextureAttribute.Specular);
		public final static Uniform emissiveColor = new Uniform("u_emissiveColor", ColorAttribute.Emissive);
		public final static Uniform emissiveTexture = new Uniform("u_emissiveTexture", TextureAttribute.Emissive);
		public final static Uniform emissiveUVTransform = new Uniform("u_emissiveUVTransform", TextureAttribute.Emissive);
		public final static Uniform reflectionColor = new Uniform("u_reflectionColor", ColorAttribute.Reflection);
		public final static Uniform reflectionTexture = new Uniform("u_reflectionTexture", TextureAttribute.Reflection);
		public final static Uniform reflectionUVTransform = new Uniform("u_reflectionUVTransform", TextureAttribute.Reflection);
		public final static Uniform normalTexture = new Uniform("u_normalTexture", TextureAttribute.Normal);
		public final static Uniform normalUVTransform = new Uniform("u_normalUVTransform", TextureAttribute.Normal);
		public final static Uniform ambientTexture = new Uniform("u_ambientTexture", TextureAttribute.Ambient);
		public final static Uniform ambientUVTransform = new Uniform("u_ambientUVTransform", TextureAttribute.Ambient);
		public final static Uniform alphaTest = new Uniform("u_alphaTest");

		public final static Uniform ambientCube = new Uniform("u_ambientCubemap");
		public final static Uniform dirLights = new Uniform("u_dirLights");
		public final static Uniform pointLights = new Uniform("u_pointLights");
		public final static Uniform spotLights = new Uniform("u_spotLights");
		public final static Uniform environmentCubemap = new Uniform("u_environmentCubemap");
	}

	public static class Setters {
		public final static Setter projTrans = new GlobalSetter() {
			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID, shader.camera.projection);
			}
		};
		public final static Setter viewTrans = new GlobalSetter() {
			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID, shader.camera.view);
			}
		};
		public final static Setter projViewTrans = new GlobalSetter() {
			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID, shader.camera.combined);
			}
		};
		public final static Setter cameraPosition = new GlobalSetter() {
			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID, shader.camera.position.x, shader.camera.position.y, shader.camera.position.z,
					1.1881f / (shader.camera.far * shader.camera.far));
			}
		};
		public final static Setter cameraDirection = new GlobalSetter() {
			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID, shader.camera.direction);
			}
		};
		public final static Setter cameraUp = new GlobalSetter() {
			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID, shader.camera.up);
			}
		};
		public final static Setter cameraNearFar = new GlobalSetter() {
			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID, shader.camera.near, shader.camera.far);
			}
		};
		public final static Setter worldTrans = new LocalSetter() {
			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID, renderable.worldTransform);
			}
		};
		public final static Setter viewWorldTrans = new LocalSetter() {
			final Matrix4 temp = new Matrix4();

			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID, temp.set(shader.camera.view).mul(renderable.worldTransform));
			}
		};
		public final static Setter projViewWorldTrans = new LocalSetter() {
			final Matrix4 temp = new Matrix4();

			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID, temp.set(shader.camera.combined).mul(renderable.worldTransform));
			}
		};
		public final static Setter normalMatrix = new LocalSetter() {
			private final Matrix3 tmpM = new Matrix3();

			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID, tmpM.set(renderable.worldTransform).inv().transpose());
			}
		};

		public static class Bones extends LocalSetter {
			private final static Matrix4 idtMatrix = new Matrix4();
			public final float bones[];

			public Bones (final int numBones) {
				this.bones = new float[numBones * 16];
			}

			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				for (int i = 0; i < bones.length; i++) {
					final int idx = i / 16;
					bones[i] = (renderable.bones == null || idx >= renderable.bones.length || renderable.bones[idx] == null) ? idtMatrix.val[i % 16]
						: renderable.bones[idx].val[i % 16];
				}
				shader.program.setUniformMatrix4fv(shader.loc(inputID), bones, 0, bones.length);
			}
		}

		public final static Setter shininess = new LocalSetter() {
			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID, ((FloatAttribute)(combinedAttributes.get(FloatAttribute.Shininess))).value);
			}
		};
		public final static Setter diffuseColor = new LocalSetter() {
			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID, ((ColorAttribute)(combinedAttributes.get(ColorAttribute.Diffuse))).color);
			}
		};
		public final static Setter diffuseTexture = new LocalSetter() {
			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				final int unit = shader.context.textureBinder.bind(((TextureAttribute)(combinedAttributes
					.get(TextureAttribute.Diffuse))).textureDescription);
				shader.set(inputID, unit);
			}
		};
		public final static Setter diffuseUVTransform = new LocalSetter() {
			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				final TextureAttribute ta = (TextureAttribute)(combinedAttributes.get(TextureAttribute.Diffuse));
				shader.set(inputID, ta.offsetU, ta.offsetV, ta.scaleU, ta.scaleV);
			}
		};
		public final static Setter specularColor = new LocalSetter() {
			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID, ((ColorAttribute)(combinedAttributes.get(ColorAttribute.Specular))).color);
			}
		};
		public final static Setter specularTexture = new LocalSetter() {
			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				final int unit = shader.context.textureBinder.bind(((TextureAttribute)(combinedAttributes
					.get(TextureAttribute.Specular))).textureDescription);
				shader.set(inputID, unit);
			}
		};
		public final static Setter specularUVTransform = new LocalSetter() {
			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				final TextureAttribute ta = (TextureAttribute)(combinedAttributes.get(TextureAttribute.Specular));
				shader.set(inputID, ta.offsetU, ta.offsetV, ta.scaleU, ta.scaleV);
			}
		};
		public final static Setter emissiveColor = new LocalSetter() {
			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID, ((ColorAttribute)(combinedAttributes.get(ColorAttribute.Emissive))).color);
			}
		};
		public final static Setter emissiveTexture = new LocalSetter() {
			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				final int unit = shader.context.textureBinder.bind(((TextureAttribute)(combinedAttributes
					.get(TextureAttribute.Emissive))).textureDescription);
				shader.set(inputID, unit);
			}
		};
		public final static Setter emissiveUVTransform = new LocalSetter() {
			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				final TextureAttribute ta = (TextureAttribute)(combinedAttributes.get(TextureAttribute.Emissive));
				shader.set(inputID, ta.offsetU, ta.offsetV, ta.scaleU, ta.scaleV);
			}
		};
		public final static Setter reflectionColor = new LocalSetter() {
			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID, ((ColorAttribute)(combinedAttributes.get(ColorAttribute.Reflection))).color);
			}
		};
		public final static Setter reflectionTexture = new LocalSetter() {
			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				final int unit = shader.context.textureBinder.bind(((TextureAttribute)(combinedAttributes
					.get(TextureAttribute.Reflection))).textureDescription);
				shader.set(inputID, unit);
			}
		};
		public final static Setter reflectionUVTransform = new LocalSetter() {
			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				final TextureAttribute ta = (TextureAttribute)(combinedAttributes.get(TextureAttribute.Reflection));
				shader.set(inputID, ta.offsetU, ta.offsetV, ta.scaleU, ta.scaleV);
			}
		};
		public final static Setter normalTexture = new LocalSetter() {
			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				final int unit = shader.context.textureBinder.bind(((TextureAttribute)(combinedAttributes
					.get(TextureAttribute.Normal))).textureDescription);
				shader.set(inputID, unit);
			}
		};
		public final static Setter normalUVTransform = new LocalSetter() {
			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				final TextureAttribute ta = (TextureAttribute)(combinedAttributes.get(TextureAttribute.Normal));
				shader.set(inputID, ta.offsetU, ta.offsetV, ta.scaleU, ta.scaleV);
			}
		};
		public final static Setter ambientTexture = new LocalSetter() {
			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				final int unit = shader.context.textureBinder.bind(((TextureAttribute)(combinedAttributes
					.get(TextureAttribute.Ambient))).textureDescription);
				shader.set(inputID, unit);
			}
		};
		public final static Setter ambientUVTransform = new LocalSetter() {
			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				final TextureAttribute ta = (TextureAttribute)(combinedAttributes.get(TextureAttribute.Ambient));
				shader.set(inputID, ta.offsetU, ta.offsetV, ta.scaleU, ta.scaleV);
			}
		};

		public static class ACubemap extends LocalSetter {
			private final static float ones[] = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
			private final AmbientCubemap cacheAmbientCubemap = new AmbientCubemap();
			private final static Vector3 tmpV1 = new Vector3();
			public final int dirLightsOffset;
			public final int pointLightsOffset;

			public ACubemap (final int dirLightsOffset, final int pointLightsOffset) {
				this.dirLightsOffset = dirLightsOffset;
				this.pointLightsOffset = pointLightsOffset;
			}

			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				if (renderable.environment == null)
					shader.program.setUniform3fv(shader.loc(inputID), ones, 0, ones.length);
				else {
					renderable.worldTransform.getTranslation(tmpV1);
					if (combinedAttributes.has(ColorAttribute.AmbientLight))
						cacheAmbientCubemap.set(((ColorAttribute)combinedAttributes.get(ColorAttribute.AmbientLight)).color);

					if (combinedAttributes.has(DirectionalLightsAttribute.Type)) {
						Array<DirectionalLight> lights = ((DirectionalLightsAttribute)combinedAttributes
							.get(DirectionalLightsAttribute.Type)).lights;
						for (int i = dirLightsOffset; i < lights.size; i++)
							cacheAmbientCubemap.add(lights.get(i).color, lights.get(i).direction);
					}

					if (combinedAttributes.has(PointLightsAttribute.Type)) {
						Array<PointLight> lights = ((PointLightsAttribute)combinedAttributes.get(PointLightsAttribute.Type)).lights;
						for (int i = pointLightsOffset; i < lights.size; i++)
							cacheAmbientCubemap.add(lights.get(i).color, lights.get(i).position, tmpV1, lights.get(i).intensity);
					}

					cacheAmbientCubemap.clamp();
					shader.program.setUniform3fv(shader.loc(inputID), cacheAmbientCubemap.data, 0, cacheAmbientCubemap.data.length);
				}
			}
		}

		public final static Setter environmentCubemap = new LocalSetter() {
			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				if (combinedAttributes.has(CubemapAttribute.EnvironmentMap)) {
					shader.set(inputID, shader.context.textureBinder.bind(((CubemapAttribute)combinedAttributes
						.get(CubemapAttribute.EnvironmentMap)).textureDescription));
				}
			}
		};
	}

	private static String defaultVertexShader = null;

	public static String getDefaultVertexShader () {
		if (defaultVertexShader == null)
			defaultVertexShader = Gdx.files.classpath("com/badlogic/gdx/graphics/g3d/shaders/default.vertex.glsl").readString();
		return defaultVertexShader;
	}

	private static String defaultFragmentShader = null;

	public static String getDefaultFragmentShader () {
		if (defaultFragmentShader == null)
			defaultFragmentShader = Gdx.files.classpath("com/badlogic/gdx/graphics/g3d/shaders/default.fragment.glsl").readString();
		return defaultFragmentShader;
	}

	protected static long implementedFlags = BlendingAttribute.Type | TextureAttribute.Diffuse | ColorAttribute.Diffuse
		| ColorAttribute.Specular | FloatAttribute.Shininess;

	/** @deprecated Replaced by {@link Config#defaultCullFace} Set to 0 to disable culling */
	@Deprecated public static int defaultCullFace = GL20.GL_BACK;
	/** @deprecated Replaced by {@link Config#defaultDepthFunc} Set to 0 to disable depth test */
	@Deprecated public static int defaultDepthFunc = GL20.GL_LEQUAL;

	// Global uniforms
	public final int u_projTrans;
	public final int u_viewTrans;
	public final int u_projViewTrans;
	public final int u_cameraPosition;
	public final int u_cameraDirection;
	public final int u_cameraUp;
	public final int u_cameraNearFar;
	public final int u_time;
	// Object uniforms
	public final int u_worldTrans;
	public final int u_viewWorldTrans;
	public final int u_projViewWorldTrans;
	public final int u_normalMatrix;
	public final int u_bones;
	// Material uniforms
	public final int u_shininess;
	public final int u_opacity;
	public final int u_diffuseColor;
	public final int u_diffuseTexture;
	public final int u_diffuseUVTransform;
	public final int u_specularColor;
	public final int u_specularTexture;
	public final int u_specularUVTransform;
	public final int u_emissiveColor;
	public final int u_emissiveTexture;
	public final int u_emissiveUVTransform;
	public final int u_reflectionColor;
	public final int u_reflectionTexture;
	public final int u_reflectionUVTransform;
	public final int u_normalTexture;
	public final int u_normalUVTransform;
	public final int u_ambientTexture;
	public final int u_ambientUVTransform;
	public final int u_alphaTest;
	// Lighting uniforms
	protected final int u_ambientCubemap;
	protected final int u_environmentCubemap;
	protected final int u_dirLights0color = register(new Uniform("u_dirLights[0].color"));
	protected final int u_dirLights0direction = register(new Uniform("u_dirLights[0].direction"));
	protected final int u_dirLights1color = register(new Uniform("u_dirLights[1].color"));
	protected final int u_pointLights0color = register(new Uniform("u_pointLights[0].color"));
	protected final int u_pointLights0position = register(new Uniform("u_pointLights[0].position"));
	protected final int u_pointLights0intensity = register(new Uniform("u_pointLights[0].intensity"));
	protected final int u_pointLights1color = register(new Uniform("u_pointLights[1].color"));
	protected final int u_spotLights0color = register(new Uniform("u_spotLights[0].color"));
	protected final int u_spotLights0position = register(new Uniform("u_spotLights[0].position"));
	protected final int u_spotLights0intensity = register(new Uniform("u_spotLights[0].intensity"));
	protected final int u_spotLights0direction = register(new Uniform("u_spotLights[0].direction"));
	protected final int u_spotLights0cutoffAngle = register(new Uniform("u_spotLights[0].cutoffAngle"));
	protected final int u_spotLights0exponent = register(new Uniform("u_spotLights[0].exponent"));
	protected final int u_spotLights1color = register(new Uniform("u_spotLights[1].color"));
	protected final int u_fogColor = register(new Uniform("u_fogColor"));
	protected final int u_shadowMapProjViewTrans = register(new Uniform("u_shadowMapProjViewTrans"));
	protected final int u_shadowTexture = register(new Uniform("u_shadowTexture"));
	protected final int u_shadowPCFOffset = register(new Uniform("u_shadowPCFOffset"));
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
	protected int spotLightsLoc;
	protected int spotLightsColorOffset;
	protected int spotLightsPositionOffset;
	protected int spotLightsDirectionOffset;
	protected int spotLightsIntensityOffset;
	protected int spotLightsCutoffAngleOffset;
	protected int spotLightsExponentOffset;
	protected int spotLightsSize;

	protected final boolean lighting;
	protected final boolean environmentCubemap;
	protected final boolean shadowMap;
	protected final AmbientCubemap ambientCubemap = new AmbientCubemap();
	protected final DirectionalLight directionalLights[];
	protected final PointLight pointLights[];
	protected final SpotLight spotLights[];

	/** The renderable used to create this shader, invalid after the call to init */
	private Renderable renderable;
	/** The attributes that this shader supports */
	protected final long attributesMask;
	private final long vertexMask;
	protected final Config config;
	/** Attributes which are not required but always supported. */
	private final static long optionalAttributes = IntAttribute.CullFace | DepthTestAttribute.Type;

	public DefaultShader (final Renderable renderable) {
		this(renderable, new Config());
	}

	public DefaultShader (final Renderable renderable, final Config config) {
		this(renderable, config, createPrefix(renderable, config));
	}

	public DefaultShader (final Renderable renderable, final Config config, final String prefix) {
		this(renderable, config, prefix, config.vertexShader != null ? config.vertexShader : getDefaultVertexShader(),
			config.fragmentShader != null ? config.fragmentShader : getDefaultFragmentShader());
	}

	public DefaultShader (final Renderable renderable, final Config config, final String prefix, final String vertexShader,
		final String fragmentShader) {
		this(renderable, config, new ShaderProgram(prefix + vertexShader, prefix + fragmentShader));
	}

	public DefaultShader (final Renderable renderable, final Config config, final ShaderProgram shaderProgram) {
		final Attributes attributes = combineAttributes(renderable);
		this.config = config;
		this.program = shaderProgram;
		this.lighting = renderable.environment != null;
		this.environmentCubemap = attributes.has(CubemapAttribute.EnvironmentMap)
			|| (lighting && attributes.has(CubemapAttribute.EnvironmentMap));
		this.shadowMap = lighting && renderable.environment.shadowMap != null;
		this.renderable = renderable;
		attributesMask = attributes.getMask() | optionalAttributes;
		vertexMask = renderable.meshPart.mesh.getVertexAttributes().getMaskWithSizePacked();

		this.directionalLights = new DirectionalLight[lighting && config.numDirectionalLights > 0 ? config.numDirectionalLights : 0];
		for (int i = 0; i < directionalLights.length; i++)
			directionalLights[i] = new DirectionalLight();
		this.pointLights = new PointLight[lighting && config.numPointLights > 0 ? config.numPointLights : 0];
		for (int i = 0; i < pointLights.length; i++)
			pointLights[i] = new PointLight();
		this.spotLights = new SpotLight[lighting && config.numSpotLights > 0 ? config.numSpotLights : 0];
		for (int i = 0; i < spotLights.length; i++)
			spotLights[i] = new SpotLight();

		if (!config.ignoreUnimplemented && (implementedFlags & attributesMask) != attributesMask)
			throw new GdxRuntimeException("Some attributes not implemented yet (" + attributesMask + ")");

		// Global uniforms
		u_projTrans = register(Inputs.projTrans, Setters.projTrans);
		u_viewTrans = register(Inputs.viewTrans, Setters.viewTrans);
		u_projViewTrans = register(Inputs.projViewTrans, Setters.projViewTrans);
		u_cameraPosition = register(Inputs.cameraPosition, Setters.cameraPosition);
		u_cameraDirection = register(Inputs.cameraDirection, Setters.cameraDirection);
		u_cameraUp = register(Inputs.cameraUp, Setters.cameraUp);
		u_cameraNearFar = register(Inputs.cameraNearFar, Setters.cameraNearFar);
		u_time = register(new Uniform("u_time"));
		// Object uniforms
		u_worldTrans = register(Inputs.worldTrans, Setters.worldTrans);
		u_viewWorldTrans = register(Inputs.viewWorldTrans, Setters.viewWorldTrans);
		u_projViewWorldTrans = register(Inputs.projViewWorldTrans, Setters.projViewWorldTrans);
		u_normalMatrix = register(Inputs.normalMatrix, Setters.normalMatrix);
		u_bones = (renderable.bones != null && config.numBones > 0) ? register(Inputs.bones, new Setters.Bones(config.numBones))
			: -1;

		u_shininess = register(Inputs.shininess, Setters.shininess);
		u_opacity = register(Inputs.opacity);
		u_diffuseColor = register(Inputs.diffuseColor, Setters.diffuseColor);
		u_diffuseTexture = register(Inputs.diffuseTexture, Setters.diffuseTexture);
		u_diffuseUVTransform = register(Inputs.diffuseUVTransform, Setters.diffuseUVTransform);
		u_specularColor = register(Inputs.specularColor, Setters.specularColor);
		u_specularTexture = register(Inputs.specularTexture, Setters.specularTexture);
		u_specularUVTransform = register(Inputs.specularUVTransform, Setters.specularUVTransform);
		u_emissiveColor = register(Inputs.emissiveColor, Setters.emissiveColor);
		u_emissiveTexture = register(Inputs.emissiveTexture, Setters.emissiveTexture);
		u_emissiveUVTransform = register(Inputs.emissiveUVTransform, Setters.emissiveUVTransform);
		u_reflectionColor = register(Inputs.reflectionColor, Setters.reflectionColor);
		u_reflectionTexture = register(Inputs.reflectionTexture, Setters.reflectionTexture);
		u_reflectionUVTransform = register(Inputs.reflectionUVTransform, Setters.reflectionUVTransform);
		u_normalTexture = register(Inputs.normalTexture, Setters.normalTexture);
		u_normalUVTransform = register(Inputs.normalUVTransform, Setters.normalUVTransform);
		u_ambientTexture = register(Inputs.ambientTexture, Setters.ambientTexture);
		u_ambientUVTransform = register(Inputs.ambientUVTransform, Setters.ambientUVTransform);
		u_alphaTest = register(Inputs.alphaTest);

		u_ambientCubemap = lighting ? register(Inputs.ambientCube, new Setters.ACubemap(config.numDirectionalLights,
			config.numPointLights)) : -1;
		u_environmentCubemap = environmentCubemap ? register(Inputs.environmentCubemap, Setters.environmentCubemap) : -1;
	}

	@Override
	public void init () {
		final ShaderProgram program = this.program;
		this.program = null;
		init(program, renderable);
		renderable = null;

		dirLightsLoc = loc(u_dirLights0color);
		dirLightsColorOffset = loc(u_dirLights0color) - dirLightsLoc;
		dirLightsDirectionOffset = loc(u_dirLights0direction) - dirLightsLoc;
		dirLightsSize = loc(u_dirLights1color) - dirLightsLoc;
		if (dirLightsSize < 0) dirLightsSize = 0;

		pointLightsLoc = loc(u_pointLights0color);
		pointLightsColorOffset = loc(u_pointLights0color) - pointLightsLoc;
		pointLightsPositionOffset = loc(u_pointLights0position) - pointLightsLoc;
		pointLightsIntensityOffset = has(u_pointLights0intensity) ? loc(u_pointLights0intensity) - pointLightsLoc : -1;
		pointLightsSize = loc(u_pointLights1color) - pointLightsLoc;
		if (pointLightsSize < 0) pointLightsSize = 0;

		spotLightsLoc = loc(u_spotLights0color);
		spotLightsColorOffset = loc(u_spotLights0color) - spotLightsLoc;
		spotLightsPositionOffset = loc(u_spotLights0position) - spotLightsLoc;
		spotLightsDirectionOffset = loc(u_spotLights0direction) - spotLightsLoc;
		spotLightsIntensityOffset = has(u_spotLights0intensity) ? loc(u_spotLights0intensity) - spotLightsLoc : -1;
		spotLightsCutoffAngleOffset = loc(u_spotLights0cutoffAngle) - spotLightsLoc;
		spotLightsExponentOffset = loc(u_spotLights0exponent) - spotLightsLoc;
		spotLightsSize = loc(u_spotLights1color) - spotLightsLoc;
		if (spotLightsSize < 0) spotLightsSize = 0;
	}

	private static final boolean and (final long mask, final long flag) {
		return (mask & flag) == flag;
	}

	private static final boolean or (final long mask, final long flag) {
		return (mask & flag) != 0;
	}

	private final static Attributes tmpAttributes = new Attributes();

	// TODO: Perhaps move responsibility for combining attributes to RenderableProvider?
	private static final Attributes combineAttributes (final Renderable renderable) {
		tmpAttributes.clear();
		if (renderable.environment != null) tmpAttributes.set(renderable.environment);
		if (renderable.material != null) tmpAttributes.set(renderable.material);
		return tmpAttributes;
	}

	private static final long combineAttributeMasks (final Renderable renderable) {
		long mask = 0;
		if (renderable.environment != null) mask |= renderable.environment.getMask();
		if (renderable.material != null) mask |= renderable.material.getMask();
		return mask;
	}

	public static String createPrefix (final Renderable renderable, final Config config) {
		final Attributes attributes = combineAttributes(renderable);
		String prefix = "";
		final long attributesMask = attributes.getMask();
		final long vertexMask = renderable.meshPart.mesh.getVertexAttributes().getMask();
		if (and(vertexMask, Usage.Position)) prefix += "#define positionFlag\n";
		if (or(vertexMask, Usage.ColorUnpacked | Usage.ColorPacked)) prefix += "#define colorFlag\n";
		if (and(vertexMask, Usage.BiNormal)) prefix += "#define binormalFlag\n";
		if (and(vertexMask, Usage.Tangent)) prefix += "#define tangentFlag\n";
		if (and(vertexMask, Usage.Normal)) prefix += "#define normalFlag\n";
		if (and(vertexMask, Usage.Normal) || and(vertexMask, Usage.Tangent | Usage.BiNormal)) {
			if (renderable.environment != null) {
				prefix += "#define lightingFlag\n";
				prefix += "#define ambientCubemapFlag\n";
				prefix += "#define numDirectionalLights " + config.numDirectionalLights + "\n";
				prefix += "#define numPointLights " + config.numPointLights + "\n";
				prefix += "#define numSpotLights " + config.numSpotLights + "\n";
				if (attributes.has(ColorAttribute.Fog)) {
					prefix += "#define fogFlag\n";
				}
				if (renderable.environment.shadowMap != null) prefix += "#define shadowMapFlag\n";
				if (attributes.has(CubemapAttribute.EnvironmentMap)) prefix += "#define environmentCubemapFlag\n";
			}
		}
		final int n = renderable.meshPart.mesh.getVertexAttributes().size();
		for (int i = 0; i < n; i++) {
			final VertexAttribute attr = renderable.meshPart.mesh.getVertexAttributes().get(i);
			if (attr.usage == Usage.BoneWeight)
				prefix += "#define boneWeight" + attr.unit + "Flag\n";
			else if (attr.usage == Usage.TextureCoordinates) prefix += "#define texCoord" + attr.unit + "Flag\n";
		}
		if ((attributesMask & BlendingAttribute.Type) == BlendingAttribute.Type)
			prefix += "#define " + BlendingAttribute.Alias + "Flag\n";
		if ((attributesMask & TextureAttribute.Diffuse) == TextureAttribute.Diffuse) {
			prefix += "#define " + TextureAttribute.DiffuseAlias + "Flag\n";
			prefix += "#define " + TextureAttribute.DiffuseAlias + "Coord texCoord0\n"; // FIXME implement UV mapping
		}
		if ((attributesMask & TextureAttribute.Specular) == TextureAttribute.Specular) {
			prefix += "#define " + TextureAttribute.SpecularAlias + "Flag\n";
			prefix += "#define " + TextureAttribute.SpecularAlias + "Coord texCoord0\n"; // FIXME implement UV mapping
		}
		if ((attributesMask & TextureAttribute.Normal) == TextureAttribute.Normal) {
			prefix += "#define " + TextureAttribute.NormalAlias + "Flag\n";
			prefix += "#define " + TextureAttribute.NormalAlias + "Coord texCoord0\n"; // FIXME implement UV mapping
		}
		if ((attributesMask & TextureAttribute.Emissive) == TextureAttribute.Emissive) {
			prefix += "#define " + TextureAttribute.EmissiveAlias + "Flag\n";
			prefix += "#define " + TextureAttribute.EmissiveAlias + "Coord texCoord0\n"; // FIXME implement UV mapping
		}
		if ((attributesMask & TextureAttribute.Reflection) == TextureAttribute.Reflection) {
			prefix += "#define " + TextureAttribute.ReflectionAlias + "Flag\n";
			prefix += "#define " + TextureAttribute.ReflectionAlias + "Coord texCoord0\n"; // FIXME implement UV mapping
		}
		if ((attributesMask & TextureAttribute.Ambient) == TextureAttribute.Ambient) {
			prefix += "#define " + TextureAttribute.AmbientAlias + "Flag\n";
			prefix += "#define " + TextureAttribute.AmbientAlias + "Coord texCoord0\n"; // FIXME implement UV mapping
		}
		if ((attributesMask & ColorAttribute.Diffuse) == ColorAttribute.Diffuse)
			prefix += "#define " + ColorAttribute.DiffuseAlias + "Flag\n";
		if ((attributesMask & ColorAttribute.Specular) == ColorAttribute.Specular)
			prefix += "#define " + ColorAttribute.SpecularAlias + "Flag\n";
		if ((attributesMask & ColorAttribute.Emissive) == ColorAttribute.Emissive)
			prefix += "#define " + ColorAttribute.EmissiveAlias + "Flag\n";
		if ((attributesMask & ColorAttribute.Reflection) == ColorAttribute.Reflection)
			prefix += "#define " + ColorAttribute.ReflectionAlias + "Flag\n";
		if ((attributesMask & FloatAttribute.Shininess) == FloatAttribute.Shininess)
			prefix += "#define " + FloatAttribute.ShininessAlias + "Flag\n";
		if ((attributesMask & FloatAttribute.AlphaTest) == FloatAttribute.AlphaTest)
			prefix += "#define " + FloatAttribute.AlphaTestAlias + "Flag\n";
		if (renderable.bones != null && config.numBones > 0) prefix += "#define numBones " + config.numBones + "\n";
		return prefix;
	}

	@Override
	public boolean canRender (final Renderable renderable) {
		final long renderableMask = combineAttributeMasks(renderable);
		return (attributesMask == (renderableMask | optionalAttributes))
			&& (vertexMask == renderable.meshPart.mesh.getVertexAttributes().getMaskWithSizePacked()) && (renderable.environment != null) == lighting;
	}

	@Override
	public int compareTo (Shader other) {
		if (other == null) return -1;
		if (other == this) return 0;
		return 0; // FIXME compare shaders on their impact on performance
	}

	@Override
	public boolean equals (Object obj) {
		return (obj instanceof DefaultShader) && equals((DefaultShader) obj);
	}

	public boolean equals (DefaultShader obj) {
		return (obj == this);
	}

	private final Matrix3 normalMatrix = new Matrix3();
	private float time;
	private boolean lightsSet;

	@Override
	public void begin (final Camera camera, final RenderContext context) {
		super.begin(camera, context);

		for (final DirectionalLight dirLight : directionalLights)
			dirLight.set(0, 0, 0, 0, -1, 0);
		for (final PointLight pointLight : pointLights)
			pointLight.set(0, 0, 0, 0, 0, 0, 0);
		for (final SpotLight spotLight : spotLights)
			spotLight.set(0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 1, 0);
		lightsSet = false;

		if (has(u_time)) set(u_time, time += Gdx.graphics.getDeltaTime());
	}

	@Override
	public void render (Renderable renderable, Attributes combinedAttributes) {
		if (!combinedAttributes.has(BlendingAttribute.Type))
			context.setBlending(false, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		bindMaterial(combinedAttributes);
		if (lighting) bindLights(renderable, combinedAttributes);
		super.render(renderable, combinedAttributes);
	}

	@Override
	public void end () {
		super.end();
	}

	protected void bindMaterial (final Attributes attributes) {
		int cullFace = config.defaultCullFace == -1 ? defaultCullFace : config.defaultCullFace;
		int depthFunc = config.defaultDepthFunc == -1 ? defaultDepthFunc : config.defaultDepthFunc;
		float depthRangeNear = 0f;
		float depthRangeFar = 1f;
		boolean depthMask = true;

		for (final Attribute attr : attributes) {
			final long t = attr.type;
			if (BlendingAttribute.is(t)) {
				context.setBlending(true, ((BlendingAttribute)attr).sourceFunction, ((BlendingAttribute)attr).destFunction);
				set(u_opacity, ((BlendingAttribute)attr).opacity);
			} else if ((t & IntAttribute.CullFace) == IntAttribute.CullFace)
				cullFace = ((IntAttribute)attr).value;
			else if ((t & FloatAttribute.AlphaTest) == FloatAttribute.AlphaTest)
				set(u_alphaTest, ((FloatAttribute)attr).value);
			else if ((t & DepthTestAttribute.Type) == DepthTestAttribute.Type) {
				DepthTestAttribute dta = (DepthTestAttribute)attr;
				depthFunc = dta.depthFunc;
				depthRangeNear = dta.depthRangeNear;
				depthRangeFar = dta.depthRangeFar;
				depthMask = dta.depthMask;
			} else if (!config.ignoreUnimplemented) throw new GdxRuntimeException("Unknown material attribute: " + attr.toString());
		}

		context.setCullFace(cullFace);
		context.setDepthTest(depthFunc, depthRangeNear, depthRangeFar);
		context.setDepthMask(depthMask);
	}

	private final Vector3 tmpV1 = new Vector3();

	protected void bindLights (final Renderable renderable, final Attributes attributes) {
		final Environment lights = renderable.environment;
		final DirectionalLightsAttribute dla = attributes.get(DirectionalLightsAttribute.class, DirectionalLightsAttribute.Type);
		final Array<DirectionalLight> dirs = dla == null ? null : dla.lights;
		final PointLightsAttribute pla = attributes.get(PointLightsAttribute.class, PointLightsAttribute.Type);
		final Array<PointLight> points = pla == null ? null : pla.lights;
		final SpotLightsAttribute sla = attributes.get(SpotLightsAttribute.class, SpotLightsAttribute.Type);
		final Array<SpotLight> spots = sla == null ? null : sla.lights;

		if (dirLightsLoc >= 0) {
			for (int i = 0; i < directionalLights.length; i++) {
				if (dirs == null || i >= dirs.size) {
					if (lightsSet && directionalLights[i].color.r == 0f && directionalLights[i].color.g == 0f
						&& directionalLights[i].color.b == 0f) continue;
					directionalLights[i].color.set(0, 0, 0, 1);
				} else if (lightsSet && directionalLights[i].equals(dirs.get(i)))
					continue;
				else
					directionalLights[i].set(dirs.get(i));

				int idx = dirLightsLoc + i * dirLightsSize;
				program.setUniformf(idx + dirLightsColorOffset, directionalLights[i].color.r, directionalLights[i].color.g,
					directionalLights[i].color.b);
				program.setUniformf(idx + dirLightsDirectionOffset, directionalLights[i].direction.x,
					directionalLights[i].direction.y, directionalLights[i].direction.z);
				if (dirLightsSize <= 0) break;
			}
		}

		if (pointLightsLoc >= 0) {
			for (int i = 0; i < pointLights.length; i++) {
				if (points == null || i >= points.size) {
					if (lightsSet && pointLights[i].intensity == 0f) continue;
					pointLights[i].intensity = 0f;
				} else if (lightsSet && pointLights[i].equals(points.get(i)))
					continue;
				else
					pointLights[i].set(points.get(i));

				int idx = pointLightsLoc + i * pointLightsSize;
				program.setUniformf(idx + pointLightsColorOffset, pointLights[i].color.r * pointLights[i].intensity,
					pointLights[i].color.g * pointLights[i].intensity, pointLights[i].color.b * pointLights[i].intensity);
				program.setUniformf(idx + pointLightsPositionOffset, pointLights[i].position.x, pointLights[i].position.y,
					pointLights[i].position.z);
				if (pointLightsIntensityOffset >= 0) program.setUniformf(idx + pointLightsIntensityOffset, pointLights[i].intensity);
				if (pointLightsSize <= 0) break;
			}
		}

		if (spotLightsLoc >= 0) {
			for (int i = 0; i < spotLights.length; i++) {
				if (spots == null || i >= spots.size) {
					if (lightsSet && spotLights[i].intensity == 0f) continue;
					spotLights[i].intensity = 0f;
				} else if (lightsSet && spotLights[i].equals(spots.get(i)))
					continue;
				else
					spotLights[i].set(spots.get(i));

				int idx = spotLightsLoc + i * spotLightsSize;
				program.setUniformf(idx + spotLightsColorOffset, spotLights[i].color.r * spotLights[i].intensity,
					spotLights[i].color.g * spotLights[i].intensity, spotLights[i].color.b * spotLights[i].intensity);
				program.setUniformf(idx + spotLightsPositionOffset, spotLights[i].position);
				program.setUniformf(idx + spotLightsDirectionOffset, spotLights[i].direction);
				program.setUniformf(idx + spotLightsCutoffAngleOffset, spotLights[i].cutoffAngle);
				program.setUniformf(idx + spotLightsExponentOffset, spotLights[i].exponent);
				if (spotLightsIntensityOffset >= 0)
					program.setUniformf(idx + spotLightsIntensityOffset, spotLights[i].intensity);
				if (spotLightsSize <= 0) break;
			}
		}

		if (attributes.has(ColorAttribute.Fog)) {
			set(u_fogColor, ((ColorAttribute)attributes.get(ColorAttribute.Fog)).color);
		}

		if (lights != null && lights.shadowMap != null) {
			set(u_shadowMapProjViewTrans, lights.shadowMap.getProjViewTrans());
			set(u_shadowTexture, lights.shadowMap.getDepthMap());
			set(u_shadowPCFOffset, 1.f / (2f * lights.shadowMap.getDepthMap().texture.getWidth()));
		}

		lightsSet = true;
	}

	@Override
	public void dispose () {
		program.dispose();
		super.dispose();
	}

	public int getDefaultCullFace () {
		return config.defaultCullFace == -1 ? defaultCullFace : config.defaultCullFace;
	}

	public void setDefaultCullFace (int cullFace) {
		config.defaultCullFace = cullFace;
	}

	public int getDefaultDepthFunc () {
		return config.defaultDepthFunc == -1 ? defaultDepthFunc : config.defaultDepthFunc;
	}

	public void setDefaultDepthFunc (int depthFunc) {
		config.defaultDepthFunc = depthFunc;
	}
}
