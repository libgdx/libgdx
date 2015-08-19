package com.badlogic.gdx.graphics.g3d.shadow.system.realistic;

import java.util.EnumSet;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.Cubemap.CubemapSide;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment.EnvironmentListener;
import com.badlogic.gdx.graphics.g3d.Scene;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.graphics.g3d.shadow.allocation.AllocatorResult;
import com.badlogic.gdx.graphics.g3d.shadow.allocation.FixedShadowMapAllocator;
import com.badlogic.gdx.graphics.g3d.shadow.allocation.ShadowMapAllocator;
import com.badlogic.gdx.graphics.g3d.shadow.directional.BoundingSphereDirectionalAnalyzer;
import com.badlogic.gdx.graphics.g3d.shadow.directional.DirectionalAnalyzer;
import com.badlogic.gdx.graphics.g3d.shadow.filter.FrustumLightFilter;
import com.badlogic.gdx.graphics.g3d.shadow.filter.LightFilter;
import com.badlogic.gdx.graphics.g3d.shadow.nearfar.AABBCachedNearFarAnalyzer;
import com.badlogic.gdx.graphics.g3d.shadow.nearfar.NearFarAnalyzer;
import com.badlogic.gdx.graphics.g3d.shadow.system.ShadowSystem;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entries;


/**
 * The Realistic shadow system creates real shadow.
 * Indeed, with this sytem, a shadow is the absence of light.
 * This system render only one time for each light and then render the scene.
 * Be careful with this system, the PointLight consumes 6 varying in the shader
 * so it can reach very fast the max varying and not compile.
 * This system implements EnvironmentListener so the lights are added when you add them
 * in the environment.
 * @author realitix
 */
public class RealisticShadowSystem implements ShadowSystem, EnvironmentListener {
	/**
	 * This class handles the camera and texture region
	 * @author realitix
	 */
	public static class LightProperties {
		public Camera camera;
		public TextureRegion region = new TextureRegion();

		public LightProperties(Camera camera) { this.camera = camera; }
	}

	/**
	 * This class handles a LightProperties for each side ofa PointLight
	 * @author realitix
	 */
	public static class PointLightProperties {
		public ObjectMap<CubemapSide, LightProperties> properties = new ObjectMap<CubemapSide, LightProperties>(6);
	}

	/** Quantity of pass before render the scene */
	public static final int PASS_QUANTITY = 1;
	/** Main scene */
	protected Scene scene;
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
	protected final FrameBuffer frameBuffer;
	/** Current pass in the depth process */
	protected int currentPass = 0;
	/** Iterators for cameras */
	protected Entries<SpotLight, LightProperties> spotCameraIterator;
	protected Entries<DirectionalLight, LightProperties> dirCameraIterator;
	protected Entries<PointLight, PointLightProperties> pointCameraIterator;
	/** Texture which contains all the depth maps */
	protected Texture texture;
	/** Current side in the point light cubemap */
	protected int currentPointSide;
	protected PointLightProperties currentPointProperties;
	/** Tmp variables */
	protected Vector3 tmpV3 = new Vector3();
	protected Vector2 tmpV2 = new Vector2();
	/** Shader providers used by this system */
	protected final ShaderProvider pass1ShaderProvider;
	protected final ShaderProvider mainShaderProvider;

	/**
	 * Construct the system with the needed params.
	 * @param scene Scene used in the rendering process
	 * @param nearFarAnalyzer Analyzer of near and far
	 * @param allocator Allocator of shadow maps
	 * @param directionalAnalyzer Analyze directional light to create orthographic camera
	 * @param lightFilter Filter light to render
	 */
	public RealisticShadowSystem(Scene scene, NearFarAnalyzer nearFarAnalyzer, ShadowMapAllocator allocator,
		DirectionalAnalyzer directionalAnalyzer, LightFilter lightFilter) {
		this.scene = scene;
		this.nearFarAnalyzer = nearFarAnalyzer;
		this.allocator = allocator;
		this.directionalAnalyzer = directionalAnalyzer;
		this.lightFilter = lightFilter;
		this.frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, allocator.getSize(), allocator.getSize(), true);
		pass1ShaderProvider = new Pass1ShaderProvider();
		mainShaderProvider = new MainShaderProvider(new MainShader.Config(this));
	}

	/**
	 * Construct the system with default values
	 * @param scene Scene used in the rendering process
	 */
	public RealisticShadowSystem(Scene scene) {
		this(scene,
			new AABBCachedNearFarAnalyzer(scene),
			new FixedShadowMapAllocator(
				FixedShadowMapAllocator.QUALITY_MED,
				FixedShadowMapAllocator.NB_MAP_MED,
				scene),
				new BoundingSphereDirectionalAnalyzer(),
				new FrustumLightFilter(scene));
	}

	@Override
	public int getPassQuantity () {
		return PASS_QUANTITY;
	}

	@Override
	public ShaderProvider getPassShaderProvider (int n) {
		return pass1ShaderProvider;
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
		addLight(point, EnumSet.of(
			CubemapSide.PositiveX,
			CubemapSide.NegativeX,
			CubemapSide.PositiveY,
			CubemapSide.NegativeY,
			CubemapSide.PositiveZ,
			CubemapSide.NegativeZ));
	}

	@Override
	public void addLight (PointLight point, Set<CubemapSide> sides) {
		PointLightProperties plProperty = new PointLightProperties();
		for( int i = 0; i < 6; i++ ) {
			CubemapSide cubemapSide = Cubemap.CubemapSide.values()[i];
			if( sides.contains(cubemapSide) ) {
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
		if( spotCameras.containsKey(spot) ) {
			return true;
		}
		return false;
	}

	@Override
	public boolean hasLight (DirectionalLight dir) {
		if( dirCameras.containsKey(dir) ) {
			return true;
		}
		return false;
	}

	@Override
	public boolean hasLight (PointLight point) {
		if( pointCameras.containsKey(point) ) {
			return true;
		}
		return false;
	}

	@Override
	public void update() {
		for(ObjectMap.Entry<SpotLight, LightProperties> e : spotCameras) {
			e.value.camera.position.set(e.key.position);
			e.value.camera.direction.set(e.key.direction);
			Vector2 nearFar = nearFarAnalyzer.analyze(e.key, e.value.camera);
			e.value.camera.near = nearFar.x;
			e.value.camera.far = nearFar.y;
			e.value.camera.update();
		}

		for(ObjectMap.Entry<DirectionalLight, LightProperties> e : dirCameras) {
			e.value.camera.direction.set(e.key.direction);
			directionalAnalyzer
			.analyze(
				e.key,
				scene.getCamera().frustum,
				e.value.camera.direction)
				.set(e.value.camera);
			e.value.camera.update();
		}

		for(ObjectMap.Entry<PointLight, PointLightProperties> e : pointCameras) {
			for(ObjectMap.Entry<CubemapSide, LightProperties> c : e.value.properties) {
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
		spotCameraIterator = spotCameras.iterator();
		spotCameraIterator.reset();

		dirCameraIterator = dirCameras.iterator();
		dirCameraIterator.reset();

		pointCameraIterator = pointCameras.iterator();
		pointCameraIterator.reset();

		currentPointSide = 6;
		currentPass = n;

		allocator.begin();
		frameBuffer.begin();
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
	}

	@Override
	public Camera next () {
		Camera camera = nextDirectional();
		if( camera != null )
			return camera;

		camera = nextSpot();
		if( camera != null )
			return camera;

		camera = nextPoint();
		if( camera != null )
			return camera;

		return null;
	}

	protected Camera nextDirectional() {
		if( !dirCameraIterator.hasNext() ) {
			return null;
		}

		LightProperties lp = dirCameraIterator.next().value;
		processViewport(lp);
		return lp.camera;
	}

	protected Camera nextSpot() {
		if( !spotCameraIterator.hasNext() ) {
			return null;
		}

		LightProperties lp = spotCameraIterator.next().value;
		if( !lightFilter.filter(currentPass, spotCameras.findKey(lp, true), lp.camera) ) {
			return nextSpot();
		}
		processViewportCamera(lp.camera, processViewport(lp));
		return lp.camera;
	}

	protected Camera nextPoint() {
		if( !pointCameraIterator.hasNext() && currentPointSide > 5 ) {
			return null;
		}

		if( currentPointSide > 5 )
			currentPointSide = 0;

		if( currentPointSide == 0 )
			currentPointProperties = pointCameraIterator.next().value;

		if( currentPointProperties.properties.containsKey(Cubemap.CubemapSide.values()[currentPointSide]) ) {
			LightProperties lp = currentPointProperties.properties.get(Cubemap.CubemapSide.values()[currentPointSide]);
			currentPointSide += 1;
			if( !lightFilter.filter(currentPass, pointCameras.findKey(currentPointProperties, true), lp.camera) ) {
				return nextPoint();
			}

			processViewportCamera(lp.camera, processViewport(lp));
			return lp.camera;
		}

		currentPointSide += 1;
		return nextPoint();
	}

	protected Vector2 processViewport(LightProperties lp) {
		Camera camera = lp.camera;
		AllocatorResult r = allocator.nextResult(camera);

		if( r == null ) {
			return null;
		}

		TextureRegion region = lp.region;
		region.setTexture(frameBuffer.getColorBufferTexture());
		Gdx.gl.glViewport(r.x, r.y, r.width, r.height);
		Gdx.gl.glScissor(r.x, r.y, r.width, r.height);
		region.setRegion(r.x, r.y, r.width, r.height);

		return tmpV2.set(r.width, r.height);
	}

	protected void processViewportCamera(Camera camera, Vector2 viewport) {
		camera.viewportHeight = viewport.y;
		camera.viewportWidth = viewport.x;
		camera.update();
	}

	@Override
	public void end (int n) {
		frameBuffer.end();
		texture = frameBuffer.getColorBufferTexture();
		allocator.end();
		Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
	}

	public ObjectMap<DirectionalLight, LightProperties> getDirectionalCameras() {
		return dirCameras;
	}

	public ObjectMap<SpotLight, LightProperties> getSpotCameras() {
		return spotCameras;
	}

	public ObjectMap<PointLight, PointLightProperties> getPointCameras() {
		return pointCameras;
	}

	public Texture getTexture() {
		return texture;
	}

	@Override
	public void onLightAdded (PointLight pointLight) {
		addLight(pointLight);
	}

	@Override
	public void onLightAdded (DirectionalLight directionalLight) {
		addLight(directionalLight);
	}

	@Override
	public void onLightAdded (SpotLight spotLight) {
		addLight(spotLight);
	}

	@Override
	public void onLightRemoved (PointLight pointLight) {
		removeLight(pointLight);
	}

	@Override
	public void onLightRemoved (DirectionalLight directionalLight) {
		removeLight(directionalLight);
	}

	@Override
	public void onLightRemoved (SpotLight spotLight) {
		removeLight(spotLight);
	}
}
