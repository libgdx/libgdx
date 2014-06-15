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
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.benchmark.BenchmarkModelBatch;
import com.badlogic.gdx.graphics.benchmark.GL20Benchmark;
import com.badlogic.gdx.graphics.benchmark.GL30Benchmark;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader.Config;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.StringBuilder;

/** @author Daniel Holderbaum */
public class Benchmark3DTest extends BaseG3dHudTest {

	protected Environment environment;

	protected Label trisCountLabel, textureBindsLabel, shaderSwitchesLabel, drawCallsLabel, glCallsLabel, lightsLabel;

	protected CheckBox lightingCheckBox, lightsCheckBox;

	protected boolean lighting;

	@Override
	public void create () {
		super.create();

		Gdx.gl = new GL20Benchmark(Gdx.gl);
		Gdx.gl20 = new GL20Benchmark(Gdx.gl20);
		Gdx.gl30 = new GL30Benchmark(Gdx.gl30);

		randomizeLights();

		cam.position.set(1, 1, 1);
		cam.lookAt(0, 0, 0);
		cam.update();
		showAxes = true;
		lighting = true;

		trisCountLabel = new Label("Tris: 999", skin);
		trisCountLabel.setPosition(0, fpsLabel.getTop());
		hud.addActor(trisCountLabel);

		textureBindsLabel = new Label("Texture binds: 999", skin);
		textureBindsLabel.setPosition(0, trisCountLabel.getTop());
		hud.addActor(textureBindsLabel);

		shaderSwitchesLabel = new Label("Shader switches: 999", skin);
		shaderSwitchesLabel.setPosition(0, textureBindsLabel.getTop());
		hud.addActor(shaderSwitchesLabel);

		drawCallsLabel = new Label("Draw calls: 999", skin);
		drawCallsLabel.setPosition(0, shaderSwitchesLabel.getTop());
		hud.addActor(drawCallsLabel);

		glCallsLabel = new Label("GL calls: 999", skin);
		glCallsLabel.setPosition(0, drawCallsLabel.getTop());
		hud.addActor(glCallsLabel);

		lightsLabel = new Label("Lights: 999", skin);
		lightsLabel.setPosition(0, glCallsLabel.getTop());
		hud.addActor(lightsLabel);

		lightingCheckBox = new CheckBox("Lighting", skin);
		lightingCheckBox.setChecked(lighting);
		lightingCheckBox.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				lighting = lightingCheckBox.isChecked();
			}
		});
		lightingCheckBox.setPosition(hudWidth - lightingCheckBox.getWidth(), gridCheckBox.getTop());
		hud.addActor(lightingCheckBox);

		lightsCheckBox = new CheckBox("Randomize lights", skin);
		lightsCheckBox.setChecked(false);
		lightsCheckBox.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				lightsCheckBox.setChecked(false);
				randomizeLights();
			}
		});
		lightsCheckBox.setPosition(hudWidth - lightsCheckBox.getWidth(), lightingCheckBox.getTop());
		hud.addActor(lightsCheckBox);

		moveCheckBox.remove();
		rotateCheckBox.remove();
	}

	protected void randomizeLights () {
		int pointLights = MathUtils.random(5);
		int directionalLights = MathUtils.random(5);

		DefaultShader.Config config = new Config();
		config.numDirectionalLights = directionalLights;
		config.numPointLights = pointLights;
		config.numSpotLights = 0;

		modelBatch.dispose();
		modelBatch = new BenchmarkModelBatch(new DefaultShaderProvider(config));

		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1.f));

		for (int i = 0; i < pointLights; i++) {
			environment.add(new PointLight().set(randomColor(), randomPosition(), MathUtils.random(10f)));
		}

		for (int i = 0; i < directionalLights; i++) {
			environment.add(new DirectionalLight().set(randomColor(), randomPosition()));
		}
	}

	protected Color randomColor () {
		return new Color(MathUtils.random(1.0f), MathUtils.random(1.0f), MathUtils.random(1.0f), MathUtils.random(1.0f));
	}

	protected Vector3 randomPosition () {
		return new Vector3(MathUtils.random(-10f, 10f), MathUtils.random(-10f, 10f), MathUtils.random(-10f, 10f));
	}

	private final Vector3 tmpV = new Vector3();
	private final Quaternion tmpQ = new Quaternion();
	private final BoundingBox bounds = new BoundingBox();

	protected void getStatus (final StringBuilder stringBuilder) {
		stringBuilder.setLength(0);
		stringBuilder.append("GL calls: ");
		stringBuilder.append(((GL20Benchmark)Gdx.gl).calls + ((GL20Benchmark)Gdx.gl20).calls + ((GL30Benchmark)Gdx.gl30).calls);
		glCallsLabel.setText(stringBuilder);

		stringBuilder.setLength(0);
		stringBuilder.append("Draw calls: ");
		stringBuilder.append(((GL20Benchmark)Gdx.gl).drawCalls + ((GL20Benchmark)Gdx.gl20).drawCalls
			+ ((GL30Benchmark)Gdx.gl30).drawCalls);
		drawCallsLabel.setText(stringBuilder);

		stringBuilder.setLength(0);
		stringBuilder.append("Shader switches: ");
		stringBuilder.append(((GL20Benchmark)Gdx.gl).shaderSwitches + ((GL20Benchmark)Gdx.gl20).shaderSwitches
			+ ((GL30Benchmark)Gdx.gl30).shaderSwitches);
		shaderSwitchesLabel.setText(stringBuilder);

		stringBuilder.setLength(0);
		stringBuilder.append("Texture binds: ");
		stringBuilder.append(((GL20Benchmark)Gdx.gl).textureBinds + ((GL20Benchmark)Gdx.gl20).textureBinds
			+ ((GL30Benchmark)Gdx.gl30).textureBinds);
		textureBindsLabel.setText(stringBuilder);

		stringBuilder.setLength(0);
		stringBuilder.append("Tris: ");
		stringBuilder.append(((BenchmarkModelBatch)modelBatch).tris / 3);
		trisCountLabel.setText(stringBuilder);

		stringBuilder.setLength(0);
		stringBuilder.append("Lights: ");
		stringBuilder.append(environment.directionalLights.size + environment.pointLights.size);
		stringBuilder.append(", Directional: ");
		stringBuilder.append(environment.directionalLights.size);
		stringBuilder.append(", Point: ");
		stringBuilder.append(environment.pointLights.size);
		lightsLabel.setText(stringBuilder);

		((BenchmarkModelBatch)modelBatch).reset();
		((GL20Benchmark)Gdx.gl).reset();
		((GL20Benchmark)Gdx.gl20).reset();
		((GL30Benchmark)Gdx.gl30).reset();

		stringBuilder.setLength(0);
		super.getStatus(stringBuilder);
	}

	@Override
	protected void render (ModelBatch batch, Array<ModelInstance> instances) {
		if (lighting) {
			batch.render(instances, environment);
		} else {
			batch.render(instances);
		}
	}

	protected String currentlyLoading;

	@Override
	protected void onModelClicked (final String name) {
		if (name == null) return;

		currentlyLoading = "data/" + name;
		assets.load(currentlyLoading, Model.class);
		loading = true;
	}

	@Override
	protected void onLoaded () {
		if (currentlyLoading == null || currentlyLoading.length() == 0) return;

		final ModelInstance instance = new ModelInstance(assets.get(currentlyLoading, Model.class));
		instance.transform = new Matrix4().idt();
		instance.transform.setToTranslation(MathUtils.random(-10, 10), MathUtils.random(-10, 10), MathUtils.random(-10, 10));
		instance.transform.rotate(Vector3.X, MathUtils.random(-180, 180));
		instance.transform.rotate(Vector3.Y, MathUtils.random(-180, 180));
		instance.transform.rotate(Vector3.Z, MathUtils.random(-180, 180));
		instances.add(instance);
	}

	@Override
	public boolean keyUp (int keycode) {
		if (keycode == Keys.SPACE || keycode == Keys.MENU) {
			onLoaded();
		}
		return super.keyUp(keycode);
	}

}
