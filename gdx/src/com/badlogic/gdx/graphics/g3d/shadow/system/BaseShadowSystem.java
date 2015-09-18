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

package com.badlogic.gdx.graphics.g3d.shadow.system;

import java.util.EnumSet;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.Cubemap.CubemapSide;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment.EnvironmentListener;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.graphics.g3d.shadow.allocation.FixedShadowMapAllocator;
import com.badlogic.gdx.graphics.g3d.shadow.allocation.ShadowMapAllocator;
import com.badlogic.gdx.graphics.g3d.shadow.allocation.ShadowMapAllocator.ShadowMapRegion;
import com.badlogic.gdx.graphics.g3d.shadow.directional.BoundingSphereDirectionalAnalyzer;
import com.badlogic.gdx.graphics.g3d.shadow.directional.DirectionalAnalyzer;
import com.badlogic.gdx.graphics.g3d.shadow.filter.FrustumLightFilter;
import com.badlogic.gdx.graphics.g3d.shadow.filter.LightFilter;
import com.badlogic.gdx.graphics.g3d.shadow.nearfar.AABBCachedNearFarAnalyzer;
import com.badlogic.gdx.graphics.g3d.shadow.nearfar.NearFarAnalyzer;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entries;

/** BaseShadowSystem allows to easily create custom shadow system
 * @author realitix */
public abstract class BaseShadowSystem implements ShadowSystem, EnvironmentListener {
	/** This class handles the camera and texture region
	 * @author realitix */
	public static class LightProperties {
		public Camera camera;
		public TextureRegion region = new TextureRegion();

		public LightProperties (Camera camera) {
			this.camera = camera;
		}
	}

	/** This class handles a LightProperties for each side ofa PointLight
	 * @author realitix */
	public static class PointLightProperties {
		public ObjectMap<CubemapSide, LightProperties> properties = new ObjectMap<CubemapSide, LightProperties>(6);
	}

	/** Main camera */
	protected Camera camera;
	/** Cameras linked with spot lights */
	protected ObjectMap<SpotLight, LightProperties> spotCameras = new ObjectMap<SpotLight, LightProperties>();
	/** Cameras linked with directional lights */
	protected ObjectMap<DirectionalLight, LightProperties> dirCameras = new ObjectMap<DirectionalLight, LightProperties>();
	/** Cameras linked with point lights */
	protected ObjectMap<PointLight, PointLightProperties> pointCameras = new ObjectMap<PointLight, PointLightProperties>();
	/** Analyzer of near and far for spot and point lights */
	protected NearFarAnalyzer nearFarAnalyzer;
	/** Allocator which choose where to render shadow map in texture */
	protected ShadowMapAllocator allocator;
	/** Analyzer which compute how to create the camera for directional light */
	protected DirectionalAnalyzer directionalAnalyzer;
	/** Filter that choose if light must be rendered */
	protected LightFilter lightFilter;
	/** Framebuffer used to render all the depth maps */
	protected FrameBuffer[] frameBuffers;
	/** Current pass in the depth process */
	protected int currentPass = -1;
	/** Iterators for cameras */
	protected Entries<SpotLight, LightProperties> spotCameraIterator;
	protected Entries<DirectionalLight, LightProperties> dirCameraIterator;
	protected Entries<PointLight, PointLightProperties> pointCameraIterator;
	/** Current side in the point light cubemap */
	protected int currentPointSide;
	protected PointLightProperties currentPointProperties;
	/** Tmp variables */
	protected Vector3 tmpV3 = new Vector3();
	protected Vector2 tmpV2 = new Vector2();
	/** Shader providers used by this system */
	protected ShaderProvider[] passShaderProviders;
	protected ShaderProvider mainShaderProvider;
	/** Current Camera used in the Pass2 shader */
	protected LightProperties currentLightProperties;

	/** Construct the system with the needed params.
	 * @param camera Camera used in the rendering process
	 * @param nearFarAnalyzer Analyzer of near and far
	 * @param allocator Allocator of shadow maps
	 * @param directionalAnalyzer Analyze directional light to create orthographic camera
	 * @param lightFilter Filter light to render */
	public BaseShadowSystem (Camera camera, NearFarAnalyzer nearFarAnalyzer, ShadowMapAllocator allocator,
		DirectionalAnalyzer directionalAnalyzer, LightFilter lightFilter) {
		this.camera = camera;
		this.nearFarAnalyzer = nearFarAnalyzer;
		this.allocator = allocator;
		this.directionalAnalyzer = directionalAnalyzer;
		this.lightFilter = lightFilter;
		frameBuffers = new FrameBuffer[getPassQuantity()];
		passShaderProviders = new ShaderProvider[getPassQuantity()];
		init();
	}

	/** Construct the system with default values
	 * @param camera Camera used in the rendering process
	 * @param instances Array of model instances rendered */
	public BaseShadowSystem (Camera camera, Array<ModelInstance> instances) {
		this(camera, new AABBCachedNearFarAnalyzer(instances), new FixedShadowMapAllocator(FixedShadowMapAllocator.QUALITY_MED,
			FixedShadowMapAllocator.NB_MAP_MED), new BoundingSphereDirectionalAnalyzer(), new FrustumLightFilter(camera));
	}

	/** Initialize framebuffers and shader providers */
	protected abstract void init ();

	@Override
	public abstract int getPassQuantity ();

	@Override
	public ShaderProvider getPassShaderProvider (int n) {
		if (n >= passShaderProviders.length) {
			throw new GdxRuntimeException("ShaderProvider " + n + " doesn't exist in " + getClass().getName());
		}
		return passShaderProviders[n];
	}

	@Override
	public ShaderProvider getShaderProvider () {
		return mainShaderProvider;
	}

	@Override
	public void addLight (SpotLight spot) {
		PerspectiveCamera camera = new PerspectiveCamera(spot.cutoffAngle, 0, 0);
		camera.position.set(spot.position);
		camera.direction.set(spot.direction);
		camera.near = 0.1f;
		camera.far = 1000;
		camera.up.set(camera.direction.y, camera.direction.z, camera.direction.x);

		spotCameras.put(spot, new LightProperties(camera));
	}

	@Override
	public void addLight (DirectionalLight dir) {
		OrthographicCamera camera = new OrthographicCamera();
		camera.direction.set(dir.direction);
		camera.near = 0.1f;
		camera.far = 1000;
		dirCameras.put(dir, new LightProperties(camera));
	}

	@Override
	public void addLight (PointLight point) {
		addLight(point, EnumSet.of(CubemapSide.PositiveX, CubemapSide.NegativeX, CubemapSide.PositiveY, CubemapSide.NegativeY,
			CubemapSide.PositiveZ, CubemapSide.NegativeZ));
	}

	@Override
	public void addLight (PointLight point, Set<CubemapSide> sides) {
		PointLightProperties plProperty = new PointLightProperties();
		for (int i = 0; i < 6; i++) {
			CubemapSide cubemapSide = Cubemap.CubemapSide.values()[i];
			if (sides.contains(cubemapSide)) {
				PerspectiveCamera camera = new PerspectiveCamera(90, 0, 0);
				camera.position.set(point.position);
				camera.direction.set(cubemapSide.direction);
				camera.up.set(cubemapSide.up);
				camera.near = 0.1f;
				camera.far = 1000;

				LightProperties p = new LightProperties(camera);
				plProperty.properties.put(cubemapSide, p);
			}
		}
		pointCameras.put(point, plProperty);
	}

	@Override
	public void removeLight (SpotLight spot) {
		spotCameras.remove(spot);
	}

	@Override
	public void removeLight (DirectionalLight dir) {
		dirCameras.remove(dir);
	}

	@Override
	public void removeLight (PointLight point) {
		pointCameras.remove(point);
	}

	@Override
	public boolean hasLight (SpotLight spot) {
		if (spotCameras.containsKey(spot)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean hasLight (DirectionalLight dir) {
		if (dirCameras.containsKey(dir)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean hasLight (PointLight point) {
		if (pointCameras.containsKey(point)) {
			return true;
		}
		return false;
	}

	@Override
	public void update () {
		for (ObjectMap.Entry<SpotLight, LightProperties> e : spotCameras) {
			e.value.camera.position.set(e.key.position);
			e.value.camera.direction.set(e.key.direction);
			Vector2 nearFar = nearFarAnalyzer.analyze(e.key, e.value.camera);
			e.value.camera.near = nearFar.x;
			e.value.camera.far = nearFar.y;
			e.value.camera.update();
		}

		for (ObjectMap.Entry<DirectionalLight, LightProperties> e : dirCameras) {
			e.value.camera.direction.set(e.key.direction);
			directionalAnalyzer.analyze(e.key, this.camera.frustum, e.value.camera.direction, e.value.camera).update();
			e.value.camera.update();
		}

		for (ObjectMap.Entry<PointLight, PointLightProperties> e : pointCameras) {
			for (ObjectMap.Entry<CubemapSide, LightProperties> c : e.value.properties) {
				c.value.camera.position.set(e.key.position);
				Vector2 nearFar = nearFarAnalyzer.analyze(e.key, c.value.camera);
				c.value.camera.near = nearFar.x;
				c.value.camera.far = nearFar.y;
				c.value.camera.update();
			}
		}
	}

	@Override
	public void begin (int n) {
		if (n >= passShaderProviders.length) {
			throw new GdxRuntimeException("Pass " + n + " doesn't exist in " + getClass().getName());
		}

		currentPass = n;

		spotCameraIterator = spotCameras.iterator();
		spotCameraIterator.reset();

		dirCameraIterator = dirCameras.iterator();
		dirCameraIterator.reset();

		pointCameraIterator = pointCameras.iterator();
		pointCameraIterator.reset();

		currentPointSide = 6;

		frameBuffers[n].begin();
		beginPass(n);
	}

	protected abstract void beginPass (int n);

	@Override
	public Camera next () {
		LightProperties lp = nextDirectional();
		if (lp != null) return interceptCamera(lp);

		lp = nextSpot();
		if (lp != null) return interceptCamera(lp);

		lp = nextPoint();
		if (lp != null) return interceptCamera(lp);

		return null;
	}

	protected Camera interceptCamera (LightProperties lp) {
		currentLightProperties = lp;
		return lp.camera;
	}

	protected LightProperties nextDirectional () {
		if (!dirCameraIterator.hasNext()) {
			return null;
		}

		LightProperties lp = dirCameraIterator.next().value;
		processViewport(lp);
		return lp;
	}

	protected LightProperties nextSpot () {
		if (!spotCameraIterator.hasNext()) {
			return null;
		}

		LightProperties lp = spotCameraIterator.next().value;
		if (!lightFilter.filter(currentPass, spotCameras.findKey(lp, true), lp.camera)) {
			return nextSpot();
		}
		processViewportCamera(lp.camera, processViewport(lp));
		return lp;
	}

	protected LightProperties nextPoint () {
		if (!pointCameraIterator.hasNext() && currentPointSide > 5) {
			return null;
		}

		if (currentPointSide > 5) currentPointSide = 0;

		if (currentPointSide == 0) currentPointProperties = pointCameraIterator.next().value;

		if (currentPointProperties.properties.containsKey(Cubemap.CubemapSide.values()[currentPointSide])) {
			LightProperties lp = currentPointProperties.properties.get(Cubemap.CubemapSide.values()[currentPointSide]);
			currentPointSide += 1;
			if (!lightFilter.filter(currentPass, pointCameras.findKey(currentPointProperties, true), lp.camera)) {
				return nextPoint();
			}

			processViewportCamera(lp.camera, processViewport(lp));
			return lp;
		}

		currentPointSide += 1;
		return nextPoint();
	}

	protected Vector2 processViewport (LightProperties lp) {
		Camera camera = lp.camera;
		ShadowMapRegion r = allocator.nextResult(camera);

		if (r == null) {
			return null;
		}

		TextureRegion region = lp.region;
		region.setTexture(frameBuffers[0].getColorBufferTexture());
		Gdx.gl.glViewport(r.x, r.y, r.width, r.height);
		Gdx.gl.glScissor(r.x, r.y, r.width, r.height);
		region.setRegion(r.x, r.y, r.width, r.height);

		return tmpV2.set(r.width, r.height);
	}

	protected void processViewportCamera (Camera camera, Vector2 viewport) {
		if (viewport != null) {
			camera.viewportHeight = viewport.y;
			camera.viewportWidth = viewport.x;
			camera.update();
		}
	}

	@Override
	public void end (int n) {
		if (currentPass != n) {
			throw new GdxRuntimeException("Begin " + n + " must be called before end " + n);
		}

		frameBuffers[n].end();
		endPass(n);

		if (currentPass == (getPassQuantity() - 1)) currentPass = -1;
	}

	protected abstract void endPass (int n);

	public ObjectMap<DirectionalLight, LightProperties> getDirectionalCameras () {
		return dirCameras;
	}

	public ObjectMap<SpotLight, LightProperties> getSpotCameras () {
		return spotCameras;
	}

	public ObjectMap<PointLight, PointLightProperties> getPointCameras () {
		return pointCameras;
	}

	public Texture getTexture (int n) {
		if (n >= getPassQuantity()) {
			throw new GdxRuntimeException("Can't get texture " + n);
		}
		return frameBuffers[n].getColorBufferTexture();
	}

	public LightProperties getCurrentLightProperties () {
		return currentLightProperties;
	}

	public int getCurrentPass () {
		return currentPass;
	}

	@Override
	public void onLightAdded (BaseLight light) {
		if (light instanceof DirectionalLight)
			addLight((DirectionalLight)light);
		else if (light instanceof PointLight)
			addLight((PointLight)light);
		else if (light instanceof SpotLight) addLight((SpotLight)light);
	}

	@Override
	public void onLightRemoved (BaseLight light) {
		if (light instanceof DirectionalLight)
			removeLight((DirectionalLight)light);
		else if (light instanceof PointLight)
			removeLight((PointLight)light);
		else if (light instanceof SpotLight) removeLight((SpotLight)light);
	}
}
