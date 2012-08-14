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

package com.badlogic.gdx.graphics.g3d.experimental;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g3d.AnimatedModelNode;
import com.badlogic.gdx.graphics.g3d.lights.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.lights.LightManager;
import com.badlogic.gdx.graphics.g3d.lights.LightManager.LightQuality;
import com.badlogic.gdx.graphics.g3d.lights.PointLight;
import com.badlogic.gdx.graphics.g3d.loaders.g3d.G3dLoader;
import com.badlogic.gdx.graphics.g3d.loaders.g3d.chunks.G3dExporter;
import com.badlogic.gdx.graphics.g3d.loaders.ogre.OgreXmlLoader;
import com.badlogic.gdx.graphics.g3d.materials.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.materials.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.materials.GpuSkinningAttribute;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.materials.MaterialAttribute;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.skeleton.SkeletonAnimation;
import com.badlogic.gdx.graphics.g3d.model.skeleton.SkeletonModel;
import com.badlogic.gdx.graphics.g3d.model.skeleton.SkeletonModelGpuSkinned;
import com.badlogic.gdx.graphics.g3d.test.PrototypeRendererGL20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

public class SkeletonModelGpuSkinningTest implements ApplicationListener {

	static final int LIGHTS_NUM = 4;
	static final float LIGHT_INTESITY = 3f;
	static final boolean useGpuSkinning = true;

	LightManager lightManager;

	PerspectiveCamController camController;
	PerspectiveCamera cam;
	private Texture texture;
	FPSLogger logger = new FPSLogger();
	private Matrix4 modelMatrix = new Matrix4();
	final private Matrix3 normalMatrix = new Matrix3();
	float timer;
	private PrototypeRendererGL20 protoRenderer;
	private SkeletonModel model;
	private int currAnimIdx;
	final static int gridSize = 4;
	private AnimatedModelNode[][] animInstance = new AnimatedModelNode[gridSize][gridSize];

	public void render () {

		logger.log();

		final float delta = Gdx.graphics.getDeltaTime();
		camController.update(delta);

		timer += delta;
		for (int i = 0; i < 1 && i < lightManager.pointLights.size; i++) {
			final Vector3 v = lightManager.pointLights.get(i).position;
			v.set(animInstance[0][0].getSortCenter());
			v.x += MathUtils.sin(timer);
			v.z += MathUtils.cos(timer);
		}

		Gdx.gl.glEnable(GL10.GL_CULL_FACE);
		Gdx.gl.glFrontFace(GL10.GL_CCW);
		Gdx.gl.glCullFace(GL10.GL_FRONT);

		Gdx.gl.glEnable(GL10.GL_DEPTH_TEST);
		Gdx.gl.glDepthMask(true);

		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT
			| (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));

		protoRenderer.begin();
		
		for (int i = 0; i < gridSize; i++) {
			for (int j = 0; j < gridSize; j++) {
				AnimatedModelNode instance = animInstance[i][j];
				instance.time += MathUtils.clamp(delta, 0, 0.1f)*0.5f;
				if (instance.time > model.getAnimations()[currAnimIdx].totalDuration) {
					instance.time = 0;
				}
				instance.matrix.val[12] = -i * 2 + 8;
				instance.matrix.val[14] = -j * 2 - 4;
				protoRenderer.draw(model, instance);
			}
		}
		protoRenderer.end();
	}

	public void create () {

		lightManager = new LightManager(LIGHTS_NUM, LightQuality.FRAGMENT);
		for (int i = 0; i < LIGHTS_NUM; i++) {
			PointLight l = new PointLight();
			l.position.set(MathUtils.random(6) - 3, 1 + MathUtils.random(6), MathUtils.random(6) - 3);
			l.color.r = MathUtils.random();
			l.color.b = MathUtils.random();
			l.color.g = MathUtils.random();
			l.intensity = LIGHT_INTESITY;
			lightManager.addLigth(l);
		}
		lightManager.dirLight = new DirectionalLight();
		lightManager.dirLight.color.set(0.1f, 0.1f, 0.1f, 1);
		lightManager.dirLight.direction.set(-.4f, -1, 0.03f).nor();

		lightManager.ambientLight.set(.01f, 0.01f, 0.03f, 0f);

		cam = new PerspectiveCamera(45, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.near = 0.1f;
		cam.far = 64f;
		cam.position.set(2, 2.75f, 1f);
		cam.update();

		camController = new PerspectiveCamController(cam);
		Gdx.input.setInputProcessor(camController);

		texture = new Texture(Gdx.files.internal("data/models/robot.jpg"), Format.RGB565, true);
		texture.setFilter(TextureFilter.MipMapLinearNearest, TextureFilter.Linear);

		//model = ModelLoaderRegistry.loadSkeletonModel(Gdx.files.internal("data/models/robot-mesh.xml"));
		
		String fileName = "data/models/robot-mesh.xml.g3d";
		
		if (!fileName.endsWith(".g3d") && Gdx.app.getType() == ApplicationType.Desktop) {
			model = new OgreXmlLoader().load(Gdx.files.internal(fileName),
				Gdx.files.internal(fileName.replace("mesh.xml", "skeleton.xml")));
		
			G3dExporter.export(model, Gdx.files.absolute(fileName + ".g3d"));
			model = G3dLoader.loadSkeletonModel(Gdx.files.absolute(fileName + ".g3d"));
		}
		else
		{
			model = G3dLoader.loadSkeletonModel(Gdx.files.internal(fileName));
		}
		
		if(useGpuSkinning){
			model = SkeletonModelGpuSkinned.CreateFromSkeletonModel(model);
		}

		protoRenderer = new PrototypeRendererGL20(lightManager);
		protoRenderer.cam = cam;

		MaterialAttribute c1 = new ColorAttribute(new Color(0.75f, 0.75f, 0.75f, 0.3f), ColorAttribute.diffuse);
		MaterialAttribute c2 = new ColorAttribute(new Color(0.35f, 0.35f, 0.35f, 0.35f), ColorAttribute.specular);
		MaterialAttribute c3 = new ColorAttribute(new Color(0.2f, 1f, 0.15f, 1.0f), ColorAttribute.rim);
		MaterialAttribute c4 = new ColorAttribute(new Color(0.0f, 0.0f, 0.0f, 0.35f), ColorAttribute.fog);

		MaterialAttribute b = new BlendingAttribute(BlendingAttribute.translucent);

		BoundingBox box = new BoundingBox();
		
		for (int i = 0; i < gridSize; i++) {
			for (int j = 0; j < gridSize; j++) {
				AnimatedModelNode instance = new AnimatedModelNode();
				SkeletonAnimation[] animations = model.getAnimations();
				SkeletonAnimation sa = animations[(i+j*gridSize)%animations.length];
				instance.animation = sa.name;
				instance.time = MathUtils.random(sa.totalDuration);
				instance.looping = true;
				
				model.getBoundingBox(box);
				
				instance.matrix.trn(-1.75f, 0f, -5.5f);
				instance.matrix.scale(0.02f, 0.02f, 0.02f);
				box.mul(instance.matrix);
				
				instance.radius = (box.getDimensions().len() / 2);
				
				animInstance[i][j] = instance;
			}
		}
		
		MaterialAttribute t1 = new TextureAttribute(texture, 0, TextureAttribute.diffuseTexture);
		GpuSkinningAttribute gpuAttribute = new GpuSkinningAttribute(model.skeleton);
		
		Material material = new Material("s", t1, gpuAttribute);
		model.setMaterial(material);

	}

	public void resize (int width, int height) {
	}

	public void pause () {
	}

	public void dispose () {
		model.dispose();
		texture.dispose();

	}

	public void resume () {
	}

	public static void main (String[] argv) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Hybrid Light";
		config.width = 800;
		config.height = 480;
		config.samples = 8;
		config.vSyncEnabled = false;
		config.useGL20 = true;
		new LwjglApplication(new SkeletonModelGpuSkinningTest(), config);
	}

}
