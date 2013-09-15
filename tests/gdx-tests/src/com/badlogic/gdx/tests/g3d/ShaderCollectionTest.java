package com.badlogic.gdx.tests.g3d;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.Cubemap.CubemapSide;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.lights.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.lights.Lights;
import com.badlogic.gdx.graphics.g3d.lights.PointLight;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.tests.g3d.BaseG3dHudTest.CollapsableWindow;
import com.badlogic.gdx.tests.g3d.shaders.MultiPassShader;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.StringBuilder;

public class ShaderCollectionTest extends BaseG3dHudTest {
	/** Desktop only: Set this to an absolute path to load the shader files from an alternative location. */
	final static String hotLoadFolder = null;
	/** Desktop only: Set this to an absolute path to save the generated shader files. */
	final static String tempFolder = null;//"D:\\temp\\shaders"; 
	
	protected  String shaders[] = new String[] {
		"<default>", "depth", "gouraud", "phong", "normal", "fur", "cubemap", "reflect", "test"
	};
	
	protected String environments[] = new String[] {
		"<none>", "debug", "environment_01", "environment_02" 
	};
	
	protected String materials[] = new String[] {
		"diffuse_green", "badlogic_normal", "brick01", "brick02", "brick03", "chesterfield", 
		"cloth01", "cloth02", "elephant01", "elephant02", "fur01", "grass01", "metal01", "metal02",
		"mirror01", "mirror02", "moon01", "plastic01", "stone01", "stone02", "wood01", "wood02"
	};
	
	public static class TestShaderProvider extends DefaultShaderProvider {
		public boolean error = false;
		public String name = "default";
		
		public void clear() {
			for (final Shader shader : shaders)
				shader.dispose();
			shaders.clear();
		}
		
		public boolean revert() {
			final String defaultVert = DefaultShader.getDefaultVertexShader();
			final String defaultFrag = DefaultShader.getDefaultFragmentShader();
			if (vertexShader.equals(defaultVert) && fragmentShader.equals(defaultFrag))
				return false;
			vertexShader = defaultVert;
			fragmentShader = defaultFrag;
			clear();
			return true;
		}
		
		@Override
		public Shader getShader (Renderable renderable) {
			try {
				return super.getShader(renderable);
			} catch(Throwable e) {
				if (tempFolder != null && Gdx.app.getType() == ApplicationType.Desktop)
					Gdx.files.absolute(tempFolder).child(name+".log.txt").writeString(e.getMessage(), false);
				if (!revert())
					throw new GdxRuntimeException("Error creating shader, cannot revert to default shader", e);
				error = true;
				Gdx.app.error("ShaderTest", "Could not create shader, reverted to default shader.", e);
				return super.getShader(renderable);
			}
		}
		
		@Override
		protected Shader createShader (Renderable renderable) {
         return createShader(vertexShader, fragmentShader, renderable, renderable.lights != null,
         	renderable.lights != null && renderable.lights.environmentCubemap != null, 
         	renderable.lights != null && renderable.lights.shadowMap != null, 
         	renderable.lights != null && renderable.lights.fog != null, 2, 5, 3, renderable.bones == null ? 0 : 12);
		}
		
		public Shader createShader(final String vertexShader, final String fragmentShader, final Renderable renderable, boolean lighting, boolean environmentCubemap, boolean shadowMap, boolean fog, int numDirectional, int numPoint, int numSpot, int numBones) {
			if (tempFolder != null && Gdx.app.getType() == ApplicationType.Desktop) {
				String prefix = DefaultShader.createPrefix(renderable, lighting, environmentCubemap, shadowMap, fog, numDirectional, numPoint, numSpot, numBones);
				Gdx.files.absolute(tempFolder).child(name+".vertex.glsl").writeString(prefix + vertexShader, false);
				Gdx.files.absolute(tempFolder).child(name+".fragment.glsl").writeString(prefix + fragmentShader, false);
			}
			BaseShader result = new MultiPassShader(vertexShader, fragmentShader, renderable, lighting, environmentCubemap, shadowMap, fog, numDirectional, numPoint, numSpot, numBones);
			if (tempFolder != null && Gdx.app.getType() == ApplicationType.Desktop)
				Gdx.files.absolute(tempFolder).child(name+".log.txt").writeString(result.program.getLog(), false);
			return result;
		}
	}
	
	protected Lights lights = new Lights(0.1f, 0.1f, 0.1f).add(
		new DirectionalLight().set(0.8f, 0.8f, 0.8f, -0.5f, -1.0f, -0.8f)
		//new PointLight().set(Color.RED, 5f, -2f, -2f, 20f),
		//new PointLight().set(Color.GREEN, -2f, 5f, -2f, 20f),
		//new PointLight().set(Color.BLUE, -2f, -2f, 5f, 20f)
	);
	protected TestShaderProvider shaderProvider;
	protected FileHandle shaderRoot;
	protected ModelBatch shaderBatch;
	protected CollapsableWindow shadersWindow, materialsWindow, environmentsWindow;
	protected ObjectMap<ModelInstance, AnimationController> animationControllers = new ObjectMap<ModelInstance, AnimationController>();
	protected String currentModel = null;
	protected String currentMaterial = null;
	protected boolean loadingMaterial = false;
	Cubemap cubemap;

	@Override
	public void create () {
		super.create();
		
		shaderProvider = new TestShaderProvider();
		shaderBatch = new ModelBatch(shaderProvider);
		
		cam.position.set(1,1,1);
		cam.lookAt(0,0,0);
		cam.update();
		showAxes = true;
		
		onModelClicked("g3d/shapes/teapot.g3dj");
		
		shaderRoot = (hotLoadFolder != null && Gdx.app.getType() == ApplicationType.Desktop) ? 
			Gdx.files.absolute(hotLoadFolder) : Gdx.files.internal("data/g3d/shaders");
	}
	
	@Override
	public void dispose () {
		shaderBatch.dispose();
		shaderBatch = null;
		shaderProvider = null;
		if (cubemap != null)
			cubemap.dispose();
		cubemap = null;
		super.dispose();
	}
	
	public void setEnvironment(String name) {
		if (name == null)
			return;
		if (cubemap != null) {
			cubemap.dispose();
			cubemap = null;
		}
		if (name.equals("<none>")) {
			if (lights.environmentCubemap != null) {
				lights.environmentCubemap = null;
				shaderProvider.clear();
			}
		}
		else {
			FileHandle root = Gdx.files.internal("data/g3d/environment");
			cubemap = new Cubemap(root.child(name+"_PX.png"), null,//root.child(name+"_NX.png"),
				root.child(name+"_PY.png"), root.child(name+"_NY.png"),
				root.child(name+"_PZ.png"), root.child(name+"_NZ.png"), 
				false); // FIXME mipmapping on desktop
			cubemap.load(CubemapSide.NegativeX, root.child(name+"_NX.png"));
			if (lights.environmentCubemap == null)
				shaderProvider.clear();
			lights.environmentCubemap = cubemap;
		}
	}
	
	public void setMaterial(String name) {
		if (name == null)
			return;
		if (currentlyLoading != null) {
			Gdx.app.error("ModelTest", "Wait for the current model/material to be loaded.");
			return;
		}
		
		currentlyLoading = "data/g3d/materials/"+name+".g3dj";
		loadingMaterial = true;
		if (!name.equals(currentMaterial))
			assets.load(currentlyLoading, Model.class);
		loading = true;
	}
	
	public void setShader(String name) {
		shaderProvider.error = false;
		if (name.equals("<default>")) {
			shaderProvider.vertexShader = DefaultShader.getDefaultVertexShader();
			shaderProvider.fragmentShader = DefaultShader.getDefaultFragmentShader();
			shaderProvider.name = "default";
		} else {
			ShaderLoader loader = new ShaderLoader(shaderRoot);
			shaderProvider.vertexShader = loader.load(name+".glsl:VS");
			shaderProvider.fragmentShader = loader.load(name+".glsl:FS");
			shaderProvider.name = name;
		}
		Gdx.app.log("VS", "\n"+shaderProvider.vertexShader);
		Gdx.app.log("FS", "\n"+shaderProvider.fragmentShader);
		shaderProvider.clear();
	}

	private final Vector3 tmpV = new Vector3();
	private final Quaternion tmpQ = new Quaternion();
	private final BoundingBox bounds = new BoundingBox();
	@Override
	protected void render (ModelBatch batch, Array<ModelInstance> instances) {}
	
	final Vector3 dirLightRotAxis = new Vector3(-1,-1,-1).nor();
	@Override
	public void render (Array<ModelInstance> instances) {
		lights.directionalLights.get(0).direction.rotate(dirLightRotAxis, Gdx.graphics.getDeltaTime() * 45f);

		super.render(instances);
		for (ObjectMap.Entry<ModelInstance, AnimationController> e : animationControllers.entries())
			e.value.update(Gdx.graphics.getDeltaTime());
		shaderBatch.begin(cam);
		shaderBatch.render(instances, lights);
		shaderBatch.end();
	}
	
	@Override
	protected void getStatus (StringBuilder stringBuilder) {
		super.getStatus(stringBuilder);

		if (shaderProvider.error)
			stringBuilder.append(" ERROR CREATING SHADER, REVERTED TO DEFAULT");
		else {
			for (final ModelInstance instance : instances) {
				if (instance.animations.size > 0) {
					stringBuilder.append(" press space or menu to switch animation");
					break;
				}
			}
		}
	}
	
	protected String currentlyLoading;
	@Override
	protected void onModelClicked(final String name) {
		if (name == null)
			return;
		if (currentlyLoading != null) {
			Gdx.app.error("ModelTest", "Wait for the current model/material to be loaded.");
			return;
		}
		
		currentlyLoading = "data/"+name; 
		loadingMaterial = false;
		if (!name.equals(currentModel))
			assets.load(currentlyLoading, Model.class);
		loading = true;
	}
	
	@Override
	protected void onLoaded() {
		if (currentlyLoading == null || currentlyLoading.isEmpty())
			return;
		
		if (loadingMaterial) {
			loadingMaterial = false;
			if (currentMaterial != null && !currentMaterial.equals(currentlyLoading))
					assets.unload(currentMaterial);
			currentMaterial = currentlyLoading;
			currentlyLoading = null;
			ModelInstance instance = instances.get(0);
			if (instance != null) {
				instance.materials.get(0).clear();
				instance.materials.get(0).set(assets.get(currentMaterial, Model.class).materials.get(0));
			}
		} else {
			if (currentModel != null && !currentModel.equals(currentlyLoading))
				assets.unload(currentModel);
			currentModel = currentlyLoading; 
			currentlyLoading = null;
			
			instances.clear();
			animationControllers.clear();
			final ModelInstance instance = new ModelInstance(assets.get(currentModel, Model.class), transform);
			instances.add(instance);
			if (instance.animations.size > 0)
				animationControllers.put(instance, new AnimationController(instance));
			
			instance.calculateBoundingBox(bounds);
			cam.position.set(1,1,1).nor().scl(bounds.getDimensions().len() * 0.75f).add(bounds.getCenter());
			cam.up.set(0,1,0);
			cam.lookAt(inputController.target.set(bounds.getCenter()));
			cam.far = Math.max(100f, bounds.getDimensions().len() * 2.0f);
			cam.update();
			moveRadius = bounds.getDimensions().len() * 0.25f;
		}
	}

	@Override
	protected void createHUD () {
		super.createHUD();

		final List shadersList = new List(shaders, skin);
		shadersList.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				if (!shadersWindow.isCollapsed() && getTapCount() == 2) {
					setShader(shadersList.getSelection());
					shadersWindow.collapse();
				}
			}
		});
		shadersWindow = addListWindow("Shaders", shadersList, -1, -1);
		
		final List materialsList = new List(materials, skin);
		materialsList.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				if (!materialsWindow.isCollapsed() && getTapCount() == 2) {
					setMaterial(materialsList.getSelection());
					materialsWindow.collapse();
				}
			}
		});
		materialsWindow = addListWindow("Materials", materialsList, modelsWindow.getWidth(), -1);
		
		final List environmentsList = new List(environments, skin);
		environmentsList.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				if (!environmentsWindow.isCollapsed() && getTapCount() == 2) {
					setEnvironment(environmentsList.getSelection());
					environmentsWindow.collapse();
				}
			}
		});
		environmentsWindow = addListWindow("Environments", environmentsList, materialsWindow.getRight(), -1);
	}
	
	protected void switchAnimation() {
		for (ObjectMap.Entry<ModelInstance, AnimationController> e : animationControllers.entries()) {
			int animIndex = 0;
			if (e.value.current != null) {
				for (int i = 0; i < e.key.animations.size; i++) {
					final Animation animation = e.key.animations.get(i);
					if (e.value.current.animation == animation) {
						animIndex = i;
						break;
					}
				}
			}
			animIndex = (animIndex + 1) % e.key.animations.size;
			e.value.animate(e.key.animations.get(animIndex).id, -1, 1f, null, 0.2f);
		}
	}
	
	@Override
	public boolean needsGL20 () {
		return true;
	}
	
	@Override
	public boolean keyUp (int keycode) {
		if (keycode == Keys.SPACE || keycode == Keys.MENU)
			switchAnimation();
		return super.keyUp(keycode);
	}
}