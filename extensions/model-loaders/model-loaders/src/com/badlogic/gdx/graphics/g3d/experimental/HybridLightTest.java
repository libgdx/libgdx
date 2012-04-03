
package com.badlogic.gdx.graphics.g3d.experimental;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.jogl.JoglApplication;
import com.badlogic.gdx.backends.jogl.JoglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.AnimatedModelInstance;
import com.badlogic.gdx.graphics.g3d.AnimatedModelNode;
import com.badlogic.gdx.graphics.g3d.StillModelNode;
import com.badlogic.gdx.graphics.g3d.lights.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.lights.LightManager;
import com.badlogic.gdx.graphics.g3d.lights.LightManager.LightQuality;
import com.badlogic.gdx.graphics.g3d.lights.PointLight;
import com.badlogic.gdx.graphics.g3d.loaders.ModelLoaderRegistry;
import com.badlogic.gdx.graphics.g3d.loaders.obj.ObjLoader;
import com.badlogic.gdx.graphics.g3d.loaders.ogre.OgreXmlLoader;
import com.badlogic.gdx.graphics.g3d.materials.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.materials.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.materials.MaterialAttribute;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.keyframe.KeyframedAnimation;
import com.badlogic.gdx.graphics.g3d.model.keyframe.KeyframedModel;
import com.badlogic.gdx.graphics.g3d.model.skeleton.SkeletonModel;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.graphics.g3d.test.PrototypeRendererGL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

public class HybridLightTest implements ApplicationListener {

	static final int LIGHTS_NUM = 4;
	static final float LIGHT_INTESITY = 4f;

	LightManager lightManager;

	PerspectiveCamController camController;
	PerspectiveCamera cam;

	StillModel model;
	StillModel model2;
	private Texture texture;

	FPSLogger logger = new FPSLogger();
	private Matrix4 modelMatrix = new Matrix4();
	private Matrix4 modelMatrix2 = new Matrix4();
	final private Matrix3 normalMatrix = new Matrix3();
	float timer;

	private PrototypeRendererGL20 protoRenderer;
	private StillModelNode instance;
	private StillModelNode instance2;
	private Texture texture2;
	private KeyframedModel model3;
	private int currAnimIdx;
	String[] animNames;
	private AnimatedModelNode animInstance;
	private Texture texture3;

	public void render () {

		logger.log();

		final float delta = Gdx.graphics.getDeltaTime();
		camController.update(delta);

		timer += delta;
		for (int i = 0; i < lightManager.pointLights.size; i++) {
			final Vector3 v = lightManager.pointLights.get(i).position;
			v.x += MathUtils.sin(timer) * 0.01f;
			v.z += MathUtils.cos(timer) * 0.01f;
			lightManager.pointLights.get(i).color.b = MathUtils.sin(timer) * 0.25f + 0.25f;

		}

		animInstance.time += delta;
		if (Gdx.input.justTouched()) {
			currAnimIdx++;
			if (currAnimIdx == model3.getAnimations().length) currAnimIdx = 0;
			animInstance.animation = model3.getAnimations()[currAnimIdx].name;
			animInstance.time = 0;
		}
		if (animInstance.time > model3.getAnimations()[currAnimIdx].totalDuration) {
			animInstance.time = 0;
		}

		Gdx.gl.glEnable(GL10.GL_CULL_FACE);
		Gdx.gl.glFrontFace(GL10.GL_CCW);
		Gdx.gl.glCullFace(GL10.GL_FRONT);

		Gdx.gl.glEnable(GL10.GL_DEPTH_TEST);
		Gdx.gl.glDepthMask(true);

		Gdx.gl.glClearColor(0, 0.1f, 0.2f, 0);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		protoRenderer.begin();
		protoRenderer.draw(model3, animInstance);
		protoRenderer.end();
		Gdx.gl.glCullFace(GL10.GL_BACK);
		protoRenderer.begin();
		protoRenderer.draw(model, instance);
		protoRenderer.draw(model, instance2);
		protoRenderer.draw(model2, instance2);
		protoRenderer.end();
	}

	public void create () {

		lightManager = new LightManager(LIGHTS_NUM, LightQuality.FRAGMENT);
		for (int i = 0; i < LIGHTS_NUM; i++) {
			PointLight l = new PointLight();
			l.position.set(MathUtils.random(8) - 4, MathUtils.random(6), MathUtils.random(8) - 4);
			l.color.r = 0.5f + 0.5f * MathUtils.random();
			l.color.b = 0.5f + 0.5f * MathUtils.random();
			l.color.g = 0.5f + 0.5f * MathUtils.random();
			l.intensity = 1 + MathUtils.random() * LIGHT_INTESITY;
			lightManager.addLigth(l);
		}
		lightManager.dirLight = new DirectionalLight();
		lightManager.dirLight.color.set(0.15f, 0.15f, 0.15f, 1);
		lightManager.dirLight.direction.set(-.4f, -1, 0.03f).nor();

		lightManager.ambientLight.set(.1f, 0.09f, 0.09f, 0f);

		cam = new PerspectiveCamera(45, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.near = 0.1f;
		cam.far = 64f;
		cam.position.set(0, 0.5f, -2f);
		cam.update();

		camController = new PerspectiveCamController(cam);
		Gdx.input.setInputProcessor(camController);

		texture = new Texture(Gdx.files.internal("data/multipleuvs_1.png"), Format.RGB565, true);
		texture.setFilter(TextureFilter.MipMapLinearNearest, TextureFilter.Linear);
		texture2 = new Texture(Gdx.files.internal("data/texture2UV1S.png"), Format.RGB565, true);
		texture2.setFilter(TextureFilter.MipMapLinearNearest, TextureFilter.Linear);

		model = ModelLoaderRegistry.loadStillModel(Gdx.files.internal("data/models/sphere.obj"));
		model2 = ModelLoaderRegistry.loadStillModel(Gdx.files.internal("data/models/basicscene.obj"));

		instance = new StillModelNode();
		instance.getTransform().translate(2, 0, -5);
		instance2 = new StillModelNode();

		BoundingBox box = new BoundingBox();
		model.getBoundingBox(box);
		instance.radius = box.getDimensions().len() / 2;

		model2.getBoundingBox(box);
		instance2.radius = box.getDimensions().len() / 2;

		protoRenderer = new PrototypeRendererGL20(lightManager);
		protoRenderer.cam = cam;

		MaterialAttribute c1 = new ColorAttribute(new Color(0.75f, 0.75f, 0.75f, 0.6f), ColorAttribute.diffuse);
		MaterialAttribute c2 = new ColorAttribute(new Color(0.35f, 0.35f, 0.35f, 0.35f), ColorAttribute.specular);
		MaterialAttribute t1 = new TextureAttribute(texture, 0, TextureAttribute.diffuseTexture);
		MaterialAttribute t2 = new TextureAttribute(texture2, 1, TextureAttribute.specularTexture);

		// MaterialAttribute b = new BlendingAttribute(BlendingAttribute.translucent);

		Material material2 = new Material("basic", c2, t1, t2);
		model2.setMaterial(material2);

		Material material = new Material("shiningBall", c1, c2);
		model.setMaterial(material);

		model3 = ModelLoaderRegistry.loadKeyframedModel(Gdx.files.internal("data/models/knight.md2"));
		animInstance = new AnimatedModelNode();
		animInstance.animation = model3.getAnimations()[0].name;
		animInstance.looping = true;
		model3.getBoundingBox(box);
		animInstance.radius = box.getDimensions().len() / 2;
		animInstance.getTransform().trn(-1.75f, 0f, -5.5f);
		animInstance.getTransform().scale(0.05f, 0.05f, 0.05f);
		texture3 = new Texture(Gdx.files.internal("data/models/knight.jpg"), Format.RGB565, true);
		texture3.setFilter(TextureFilter.MipMapLinearNearest, TextureFilter.Linear);

		MaterialAttribute t3 = new TextureAttribute(texture3, 0, TextureAttribute.diffuseTexture);
		Material material3 = new Material("", c2, t3, t2);
		model3.setMaterial(material3);

	}

	public void resize (int width, int height) {
	}

	public void pause () {
	}

	public void dispose () {
		model.dispose();
		model2.dispose();
		texture.dispose();

	}

	public void resume () {
	}

	public static void main (String[] argv) {
		JoglApplicationConfiguration config = new JoglApplicationConfiguration();
		config.title = "Hybrid Light";
		config.width = 800;
		config.height = 480;
		config.samples = 8;
		config.vSyncEnabled = true;
		config.useGL20 = true;
		new JoglApplication(new HybridLightTest(), config);
	}

}
