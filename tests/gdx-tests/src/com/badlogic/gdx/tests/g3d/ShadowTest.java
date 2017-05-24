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

package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.graphics.g3d.shadow.system.BaseShadowSystem;
import com.badlogic.gdx.graphics.g3d.shadow.system.ShadowSystem;
import com.badlogic.gdx.graphics.g3d.shadow.system.classical.ClassicalShadowSystem;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entries;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class ShadowTest extends GdxTest {

	public static class ShadowSystemProperties {
		public Array<ModelBatch> passBatches = new Array<ModelBatch>();
		public ModelBatch mainBatch;
	}

	PerspectiveCamera cam;
	CameraInputController inputController;

	Model model;
	ModelInstance instance;
	Environment environment;

	ObjectMap<ShadowSystem, ShadowSystemProperties> systems = new ObjectMap<ShadowSystem, ShadowSystemProperties>();

	Model axesModel;
	ModelInstance axesInstance;

	SpotLight sl;
	SpotLight sl2;
	SpotLight sl3;
	DirectionalLight dl;
	float radius = 1f;
	Vector3 center = new Vector3(), transformedCenter = new Vector3(), tmpV = new Vector3();

	Stage stage;
	Label label;

	Entries<ShadowSystem, ShadowSystemProperties> systemIterator;
	ShadowSystem currSystem;
	ShadowSystemProperties currProperties;

	@Override
	public void create () {
		environment = new Environment();

		sl = new SpotLight().setPosition(0, 10, -6).setColor(0.8f, 0.3f, 0.3f, 1).setDirection(0, -0.57346237f, 0.8192319f)
			.setIntensity(20).setCutoffAngle(60).setExponent(60);

		sl2 = new SpotLight().setPosition(0, 7, 5).setColor(0.3f, 0.8f, 0.3f, 1).setDirection(new Vector3(0, -1f, -0.06f).nor())
			.setIntensity(20).setCutoffAngle(60).setExponent(60);

		sl3 = new SpotLight().setPosition(0, 9, 6).setColor(0.3f, 0.3f, 0.8f, 1).setDirection(new Vector3(0, -1f, -0.06f).nor())
			.setIntensity(20).setCutoffAngle(60).setExponent(60);

		dl = new DirectionalLight().setColor(0.5f, 0.5f, 0.5f, 1).setDirection(0, -1f, 0);

		environment.add(sl);
		environment.add(sl2);
		environment.add(sl3);
		environment.add(dl);

		// The user camera
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(0f, 7f, 10f);
		cam.lookAt(0, 0, 0);
		cam.near = 1f;
		cam.far = 25f;
		cam.up.set(0, 1, 0);
		cam.update();

		// Load texture
		Array<Texture> wood = new Array<Texture>(3);
		wood.add(new Texture(Gdx.files.internal("data/g3d/materials/wood/diffuse.png")));
		wood.add(new Texture(Gdx.files.internal("data/g3d/materials/wood/normal.png")));
		wood.add(new Texture(Gdx.files.internal("data/g3d/materials/wood/specular.png")));

		Array<Texture> earth = new Array<Texture>(3);
		earth.add(new Texture(Gdx.files.internal("data/g3d/materials/earth/diffuse.png")));
		earth.add(new Texture(Gdx.files.internal("data/g3d/materials/earth/normal.png")));
		earth.add(new Texture(Gdx.files.internal("data/g3d/materials/earth/specular.png")));
		earth.get(0).setFilter(TextureFilter.Linear, TextureFilter.Linear);
		earth.get(1).setFilter(TextureFilter.Linear, TextureFilter.Linear);
		earth.get(2).setFilter(TextureFilter.Linear, TextureFilter.Linear);

		ModelBuilder modelBuilder = new ModelBuilder();
		modelBuilder.begin();
		MeshPartBuilder mpb = modelBuilder.part(
			"ground",
			GL20.GL_TRIANGLES,
			Usage.Position | Usage.Normal | Usage.TextureCoordinates,
			new Material(TextureAttribute.createDiffuse(wood.get(0)), TextureAttribute.createNormal(wood.get(1)), TextureAttribute
				.createSpecular(wood.get(2))));
		mpb.box(0, -1.5f, 0, 10, 1, 10);

		mpb = modelBuilder.part(
			"ball",
			GL20.GL_TRIANGLES,
			Usage.Position | Usage.Normal | Usage.TextureCoordinates,
			new Material(TextureAttribute.createDiffuse(earth.get(0)), TextureAttribute.createNormal(earth.get(1)), TextureAttribute
				.createSpecular(earth.get(2))));
		mpb.sphere(2f, 2f, 2f, 20, 20);

		model = modelBuilder.end();
		instance = new ModelInstance(model);

		Array<ModelInstance> instances = new Array<ModelInstance>();
		instances.add(instance);

		// Shadow system init
		// systems.put(new RealisticShadowSystem(cam, instances), new ShadowSystemProperties());
		systems.put(new ClassicalShadowSystem(cam, instances), new ShadowSystemProperties());

		for (ObjectMap.Entry<ShadowSystem, ShadowSystemProperties> e : systems) {
			ShadowSystem system = e.key;
			ShadowSystemProperties properties = e.value;
			for (int i = 0; i < system.getPassQuantity(); i++) {
				properties.passBatches.add(new ModelBatch(system.getPassShaderProvider(i)));
			}
			properties.mainBatch = new ModelBatch(system.getShaderProvider());
			environment.addListener((BaseShadowSystem)system);
		}
		systemIterator = systems.entries();

		createAxes();
		Gdx.input.setInputProcessor(inputController = new CameraInputController(cam));

		stage = new Stage(new ScreenViewport());
		label = new Label("", new Skin(Gdx.files.internal("data/uiskin.json")));
		stage.addActor(label);
		label.setX(100);
		label.setY(Gdx.graphics.getHeight() - 30);
	}

	private void createAxes () {
		ModelBuilder modelBuilder = new ModelBuilder();
		modelBuilder.begin();
		MeshPartBuilder builder = modelBuilder.part("axes", GL20.GL_LINES, Usage.Position | Usage.ColorUnpacked, new Material());

		float v1 = 0, v2 = 0, v3 = 100;
		// RED = X
		builder.setColor(Color.RED);
		builder.line(0, 0, 0, v1, v2, v3);

		// GREEN = Y
		builder.setColor(Color.GREEN);
		builder.line(0, 0, 0, v3, v1, v2);

		// BLUE = Z
		builder.setColor(Color.BLUE);
		builder.line(0, 0, 0, v2, v3, v1);

		axesModel = modelBuilder.end();
		axesInstance = new ModelInstance(axesModel);
	}

	long lastTime;

	@Override
	public void render () {
		if (TimeUtils.timeSinceMillis(lastTime) > 3 * 1000) {
			ObjectMap.Entry<ShadowSystem, ShadowSystemProperties> e;
			try {
				e = systemIterator.next();
			} catch (java.util.NoSuchElementException exc) {
				systemIterator.reset();
				e = systemIterator.next();
			}
			currSystem = e.key;
			currProperties = e.value;
			label.setText(currSystem.toString());
			lastTime = TimeUtils.millis();
		}

		final float delta = Gdx.graphics.getDeltaTime();

		sl.position.rotate(Vector3.Y, -delta * 20f);
		sl.position.rotate(Vector3.X, -delta * 30f);
		sl.position.rotate(Vector3.Z, -delta * 10f);
		sl.direction.set(Vector3.Zero.cpy().sub(sl.position));

		sl2.position.rotate(Vector3.Y, delta * 10f);
		sl2.position.rotate(Vector3.X, delta * 20f);
		sl2.position.rotate(Vector3.Z, delta * 30f);
		sl2.direction.set(Vector3.Zero.cpy().sub(sl2.position));

		sl3.position.rotate(Vector3.Y, delta * 30f);
		sl3.position.rotate(Vector3.X, delta * 10f);
		sl3.position.rotate(Vector3.Z, delta * 20f);
		sl3.direction.set(Vector3.Zero.cpy().sub(sl3.position));

		dl.direction.rotate(Vector3.X, delta * 10f);

		// Update shadow map
		currSystem.update();
		for (int i = 0; i < currSystem.getPassQuantity(); i++) {
			currSystem.begin(i);
			Camera camera;
			while ((camera = currSystem.next()) != null) {
				currProperties.passBatches.get(i).begin(camera);
				currProperties.passBatches.get(i).render(instance, environment);
				currProperties.passBatches.get(i).end();
			}
			camera = null;
			currSystem.end(i);
		}

		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		currProperties.mainBatch.begin(cam);
		currProperties.mainBatch.render(axesInstance);
		currProperties.mainBatch.render(instance, environment);
		currProperties.mainBatch.end();

		stage.act(delta);
		stage.draw();
	}

	@Override
	public void dispose () {
		model.dispose();
	}

	public boolean needsGL20 () {
		return true;
	}

	@Override
	public void resume () {
	}

	@Override
	public void resize (int width, int height) {
	}

	@Override
	public void pause () {
	}
}
