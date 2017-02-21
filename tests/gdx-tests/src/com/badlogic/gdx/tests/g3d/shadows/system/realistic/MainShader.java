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

package com.badlogic.gdx.tests.g3d.shadows.system.realistic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.DirectionalLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.SpotLightsAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.tests.g3d.shadows.system.BaseShadowSystem.LightProperties;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

/** This shader is used by the realistic shadow system. This shader supports normal mapping and specular mapping
 * @author realitix */
public class MainShader extends DefaultShader {
	public static class Config extends DefaultShader.Config {
		public RealisticShadowSystem shadowSystem;

		public Config (RealisticShadowSystem shadowSystem) {
			super();
			numBones = 12;
			numPointLights = 2;
			numSpotLights = 5;
			numDirectionalLights = 2;
			this.shadowSystem = shadowSystem;
		}
	}

	/** **** Directional shadow **** */
	protected final int u_dirShadows0uvTransform = register(new Uniform("u_dirShadows[0].uvTransform"));
	protected final int u_dirShadows1uvTransform = register(new Uniform("u_dirShadows[1].uvTransform"));

	protected int dirShadowsLoc;
	protected int dirShadowsUvTransformOffset;
	protected int dirShadowsSize;

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

	protected RealisticShadowSystem shadowSystem;

	private static String defaultVertexShader = null;

	public static String getDefaultVertexShader () {
		if (defaultVertexShader == null)
			defaultVertexShader = Gdx.files.classpath("com/badlogic/gdx/tests/g3d/shadows/system/realistic/main.vertex.glsl")
				.readString();
		return defaultVertexShader;
	}

	private static String defaultFragmentShader = null;

	public static String getDefaultFragmentShader () {
		if (defaultFragmentShader == null)
			defaultFragmentShader = Gdx.files.classpath("com/badlogic/gdx/tests/g3d/shadows/system/realistic/main.fragment.glsl")
				.readString();
		return defaultFragmentShader;
	}

	public static String createPrefix (final Renderable renderable, final Config config) {
		return DefaultShader.createPrefix(renderable, config);
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
	}

	@Override
	protected void bindLights (final Renderable renderable, final Attributes attributes) {
		super.bindLights(renderable, attributes);
		final Environment environment = renderable.environment;

		bindDirectionalShadows(attributes);
		bindSpotShadows(attributes);

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
}
