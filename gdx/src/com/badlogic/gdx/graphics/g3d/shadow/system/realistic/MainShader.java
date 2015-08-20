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

package com.badlogic.gdx.graphics.g3d.shadow.system.realistic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.DirectionalLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.PointLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.SpotLightsAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.shadow.system.realistic.RealisticShadowSystem.LightProperties;
import com.badlogic.gdx.graphics.g3d.shadow.system.realistic.RealisticShadowSystem.PointLightProperties;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

/** This shader is used by the realistic shadow system Be careful about varying. Point light consumes six varying adn can crash the
 * shader This shader supports normal mapping and specular mapping
 * @author realitix */
public class MainShader extends DefaultShader {
	public static class Config extends DefaultShader.Config {
		public RealisticShadowSystem shadowSystem;

		public Config (RealisticShadowSystem shadowSystem) {
			this();
			this.shadowSystem = shadowSystem;
		}

		public Config () {
			super();
			numBones = 0;
			numPointLights = 0;
			numSpotLights = 3;
			numDirectionalLights = 2;
		}
	}

	public static class SpotShadow {
		public Matrix4 uvTransform = new Matrix4();
	}

	public static class PointShadow {
		public Matrix4 uvTransform = new Matrix4();
		public Vector3 direction = new Vector3();
	}

	/** **** Directional shadow **** */
	protected final int u_dirShadows0uvTransform = register(new Uniform("u_dirShadows[0].uvTransform"));
	protected final int u_dirShadows1uvTransform = register(new Uniform("u_dirShadows[1].uvTransform"));

	protected int dirShadowsLoc;
	protected int dirShadowsUvTransformOffset;
	protected int dirShadowsSize;

	// protected final SpotShadow spotShadows[];

	// Shadow projViewTrans
	protected int u_dirShadowMapProjViewTrans0 = register(new Uniform("u_dirShadowMapProjViewTrans[0]"));
	protected int u_dirShadowMapProjViewTrans1 = register(new Uniform("u_dirShadowMapProjViewTrans[1]"));
	protected int dirShadowMapProjViewTransLoc;
	protected int dirShadowMapProjViewTransSize;

	// Shadow UVTransform
	protected int u_dirShadowMapUVTransform0 = register(new Uniform("u_dirShadowMapUVTransform[0]"));
	protected int u_dirShadowMapUVTransform1 = register(new Uniform("u_dirShadowMapUVTransform[1]"));
	protected int dirShadowMapUVTransformLoc;
	protected int dirShadowMapUVTransformSize;

	/** **** Spot shadow **** */
	protected final int u_spotShadows0uvTransform = register(new Uniform("u_spotShadows[0].uvTransform"));
	protected final int u_spotShadows1uvTransform = register(new Uniform("u_spotShadows[1].uvTransform"));

	protected int spotShadowsLoc;
	protected int spotShadowsUvTransformOffset;
	protected int spotShadowsSize;

	protected final SpotShadow spotShadows[];

	// Shadow projViewTrans
	protected int u_spotShadowMapProjViewTrans0 = register(new Uniform("u_spotShadowMapProjViewTrans[0]"));
	protected int u_spotShadowMapProjViewTrans1 = register(new Uniform("u_spotShadowMapProjViewTrans[1]"));
	protected int spotShadowMapProjViewTransLoc;
	protected int spotShadowMapProjViewTransSize;

	// Shadow UVTransform
	protected int u_spotShadowMapUVTransform0 = register(new Uniform("u_spotShadowMapUVTransform[0]"));
	protected int u_spotShadowMapUVTransform1 = register(new Uniform("u_spotShadowMapUVTransform[1]"));
	protected int spotShadowMapUVTransformLoc;
	protected int spotShadowMapUVTransformSize;

	/** **** Point shadow **** */
	protected final int u_pointShadows0uvTransform = register(new Uniform("u_pointShadows[0].uvTransform"));
	protected final int u_pointShadows0direction = register(new Uniform("u_pointShadows[0].direction"));
	protected final int u_pointShadows1uvTransform = register(new Uniform("u_pointShadows[1].uvTransform"));

	protected int pointShadowsLoc;
	protected int pointShadowsUvTransformOffset;
	protected int pointShadowsDirectionOffset;
	protected int pointShadowsSize;

	protected final PointShadow pointShadows[];

	// Shadow projViewTrans
	protected int u_pointShadowMapProjViewTrans0 = register(new Uniform("u_pointShadowMapProjViewTrans[0]"));
	protected int u_pointShadowMapProjViewTrans1 = register(new Uniform("u_pointShadowMapProjViewTrans[1]"));
	protected int pointShadowMapProjViewTransLoc;
	protected int pointShadowMapProjViewTransSize;

	// Shadow UVTransform
	protected int u_pointShadowMapUVTransform0 = register(new Uniform("u_pointShadowMapUVTransform[0]"));
	protected int u_pointShadowMapUVTransform1 = register(new Uniform("u_pointShadowMapUVTransform[1]"));
	protected int pointShadowMapUVTransformLoc;
	protected int pointShadowMapUVTransformSize;

	protected RealisticShadowSystem shadowSystem;

	private static String defaultVertexShader = null;

	public static String getDefaultVertexShader () {
		if (defaultVertexShader == null)
			defaultVertexShader = Gdx.files.classpath("com/badlogic/gdx/graphics/g3d/shadow/system/realistic/main.vertex.glsl")
				.readString();
		return defaultVertexShader;
	}

	private static String defaultFragmentShader = null;

	public static String getDefaultFragmentShader () {
		if (defaultFragmentShader == null)
			defaultFragmentShader = Gdx.files.classpath("com/badlogic/gdx/graphics/g3d/shadow/system/realistic/main.fragment.glsl")
				.readString();
		return defaultFragmentShader;
	}

	public static String createPrefix (final Renderable renderable, final Config config) {
		String prefix = DefaultShader.createPrefix(renderable, config);
		return prefix;
	}

	public MainShader (final Renderable renderable) {
		this(renderable, new Config());
	}

	public MainShader (final Renderable renderable, final Config config) {
		this(renderable, config, createPrefix(renderable, config));
	}

	public MainShader (final Renderable renderable, final Config config, final String prefix) {
		this(renderable, config, prefix, getDefaultVertexShader(), getDefaultFragmentShader());
	}

	public MainShader (final Renderable renderable, final Config config, final String prefix, final String vertexShader,
		final String fragmentShader) {
		this(renderable, config, new ShaderProgram(prefix + vertexShader, prefix + fragmentShader));
	}

	public MainShader (final Renderable renderable, final Config config, final ShaderProgram shaderProgram) {
		super(renderable, config, shaderProgram);

		this.shadowSystem = config.shadowSystem;

		this.spotShadows = new SpotShadow[lighting && config.numSpotLights > 0 ? config.numSpotLights : 0];
		for (int i = 0; i < spotLights.length; i++)
			spotShadows[i] = new SpotShadow();

		this.pointShadows = new PointShadow[lighting && config.numPointLights > 0 ? config.numPointLights : 0];
		for (int i = 0; i < pointLights.length; i++)
			pointShadows[i] = new PointShadow();
	}

	@Override
	public void init () {
		super.init();

		// Directional Shadow
		dirShadowsLoc = loc(u_dirShadows0uvTransform);
		dirShadowsUvTransformOffset = loc(u_dirShadows0uvTransform) - dirShadowsLoc;
		dirShadowsSize = loc(u_dirShadows1uvTransform) - dirShadowsLoc;
		if (dirShadowsSize < 0) dirShadowsSize = 0;

		dirShadowMapProjViewTransLoc = loc(u_dirShadowMapProjViewTrans0);
		dirShadowMapProjViewTransSize = loc(u_dirShadowMapProjViewTrans1) - dirShadowMapProjViewTransLoc;

		dirShadowMapUVTransformLoc = loc(u_dirShadowMapUVTransform0);
		dirShadowMapUVTransformSize = loc(u_dirShadowMapUVTransform1) - dirShadowMapUVTransformLoc;

		// Spot Shadow
		spotShadowsLoc = loc(u_spotShadows0uvTransform);
		spotShadowsUvTransformOffset = loc(u_spotShadows0uvTransform) - spotShadowsLoc;
		spotShadowsSize = loc(u_spotShadows1uvTransform) - spotShadowsLoc;
		if (spotShadowsSize < 0) spotShadowsSize = 0;

		spotShadowMapProjViewTransLoc = loc(u_spotShadowMapProjViewTrans0);
		spotShadowMapProjViewTransSize = loc(u_spotShadowMapProjViewTrans1) - spotShadowMapProjViewTransLoc;

		spotShadowMapUVTransformLoc = loc(u_spotShadowMapUVTransform0);
		spotShadowMapUVTransformSize = loc(u_spotShadowMapUVTransform1) - spotShadowMapUVTransformLoc;

		// Point Shadow
		pointShadowsLoc = loc(u_pointShadows0uvTransform);
		pointShadowsUvTransformOffset = loc(u_pointShadows0uvTransform) - pointShadowsLoc;
		pointShadowsDirectionOffset = loc(u_pointShadows0direction) - pointShadowsLoc;
		pointShadowsSize = loc(u_pointShadows1uvTransform) - pointShadowsLoc;
		if (pointShadowsSize < 0) pointShadowsSize = 0;

		pointShadowMapProjViewTransLoc = loc(u_pointShadowMapProjViewTrans0);
		pointShadowMapProjViewTransSize = loc(u_pointShadowMapProjViewTrans1) - pointShadowMapProjViewTransLoc;

		pointShadowMapUVTransformLoc = loc(u_pointShadowMapUVTransform0);
		pointShadowMapUVTransformSize = loc(u_pointShadowMapUVTransform1) - pointShadowMapUVTransformLoc;
	}

	@Override
	protected void bindLights (final Renderable renderable, final Attributes attributes) {
		super.bindLights(renderable, attributes);
		final Environment environment = renderable.environment;

		bindDirectionalShadows(attributes);
		bindSpotShadows(attributes);
		bindPointShadows(attributes);

		if (shadowSystem.getTexture() != null) {
			set(u_shadowTexture, shadowSystem.getTexture());
		}
	}

	public void bindDirectionalShadows (final Attributes attributes) {
		final DirectionalLightsAttribute dla = attributes.get(DirectionalLightsAttribute.class, DirectionalLightsAttribute.Type);
		final Array<DirectionalLight> dirs = dla == null ? null : dla.lights;

		if (dirLightsLoc >= 0) {
			for (int i = 0; i < directionalLights.length; i++) {
				if (dirs == null || dirs.size <= i) {
					continue;
				}

				int idx = dirShadowsLoc + i * dirShadowsSize;

				// Shadow
				ObjectMap<DirectionalLight, LightProperties> dirCameras = shadowSystem.getDirectionalCameras();

				DirectionalLight dl = dirs.get(i);
				if (shadowSystem.hasLight(dl)) {
					// UVTransform
					final TextureRegion tr = dirCameras.get(dl).region;
					Camera cam = dirCameras.get(dl).camera;

					if (cam != null) {
						program.setUniformf(idx + dirShadowsUvTransformOffset, tr.getU(), tr.getV(), tr.getU2() - tr.getU(), tr.getV2()
							- tr.getV());

						// ProjViewTrans
						idx = dirShadowMapProjViewTransLoc + i * dirShadowMapProjViewTransSize;
						program.setUniformMatrix(idx, dirCameras.get(dl).camera.combined);
					}
				}

				if (dirLightsSize <= 0) break;
			}
		}
	}

	public void bindSpotShadows (final Attributes attributes) {
		final SpotLightsAttribute sla = attributes.get(SpotLightsAttribute.class, SpotLightsAttribute.Type);
		final Array<SpotLight> spots = sla == null ? null : sla.lights;

		if (spotLightsLoc >= 0) {
			for (int i = 0; i < spotLights.length; i++) {
				if (spots == null || spots.size <= i) {
					continue;
				}

				int idx = spotShadowsLoc + i * spotShadowsSize;

				// Shadow
				ObjectMap<SpotLight, LightProperties> spotCameras = shadowSystem.getSpotCameras();

				SpotLight sl = spots.get(i);
				if (shadowSystem.hasLight(sl)) {
					// UVTransform
					final TextureRegion tr = spotCameras.get(sl).region;
					Camera cam = spotCameras.get(sl).camera;

					if (cam != null) {
						program.setUniformf(idx + spotShadowsUvTransformOffset, tr.getU(), tr.getV(), tr.getU2() - tr.getU(),
							tr.getV2() - tr.getV());

						// ProjViewTrans
						idx = spotShadowMapProjViewTransLoc + i * spotShadowMapProjViewTransSize;
						program.setUniformMatrix(idx, spotCameras.get(sl).camera.combined);
					}
				}

				if (spotLightsSize <= 0) break;
			}
		}
	}

	public void bindPointShadows (final Attributes attributes) {
		final PointLightsAttribute pla = attributes.get(PointLightsAttribute.class, PointLightsAttribute.Type);
		final Array<PointLight> points = pla == null ? null : pla.lights;

		if (pointLightsLoc >= 0) {
			for (int i = 0; i < pointLights.length; i++) {
				if (points == null || points.size <= i) {
					continue;
				}

				// Shadow
				ObjectMap<PointLight, PointLightProperties> pointCameras = shadowSystem.getPointCameras();

				PointLight pl = points.get(i);
				if (shadowSystem.hasLight(pl)) {
					for (int j = 0; j < 6; j++) {
						if (pointCameras.get(pl).properties.containsKey(Cubemap.CubemapSide.values()[j])) {
							LightProperties property = pointCameras.get(pl).properties.get(Cubemap.CubemapSide.values()[j]);
							final TextureRegion tr = property.region;
							final Camera cam = property.camera;

							int idx = pointShadowsLoc + (6 * i) * pointShadowsSize + j * pointShadowsSize;

							if (cam != null) {
								program.setUniformf(idx + pointShadowsUvTransformOffset, tr.getU(), tr.getV(), tr.getU2() - tr.getU(),
									tr.getV2() - tr.getV());
								program.setUniformf(idx + pointShadowsDirectionOffset, cam.direction);

								// ProjViewTrans
								idx = pointShadowMapProjViewTransLoc + (i * 6 + j) * pointShadowMapProjViewTransSize;
								program.setUniformMatrix(idx, cam.combined);
							}
						}
					}
				}

				if (pointLightsSize <= 0) break;
			}
		}
	}
}
