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
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.ParticleShader;
import com.badlogic.gdx.graphics.g3d.particles.batches.BillboardParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.batches.ModelInstanceParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.batches.ParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.batches.PointSpriteParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.emitters.RegularEmitter;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ColorInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.DynamicsInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.DynamicsModifier.BrownianAcceleration;
import com.badlogic.gdx.graphics.g3d.particles.influencers.Influencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ModelInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.RegionInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ScaleInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.SpawnInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.BillboardRenderer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.ModelInstanceRenderer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.PointSpriteRenderer;
import com.badlogic.gdx.graphics.g3d.particles.values.PointSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;

/** @author Inferno
 * @author jsjolund */
public class ParticleControllerTest extends BaseG3dTest {
	public static final String DEFAULT_PARTICLE = "data/pre_particle.png", DEFAULT_SKIN = "data/uiskin.json",
		DEFAULT_MODEL = "data/cube.obj";
	static final Matrix4 TMP_M1 = new Matrix4(), TMP_M2 = new Matrix4();
	static final Quaternion TMP_Q = new Quaternion();
	static final Vector3 TMP_V = new Vector3();

	/** Action which changes a {@link ParticleController#transform}. Translates, then continually rotates the transform around an
	 * axis, which creates a circular movement. This action creates a trail of particles when performed, since it affects where new
	 * particles are generated. */
	private class RotationAction extends Action {
		private ParticleController emitter;
		Vector3 axis;
		float angle;

		public RotationAction (ParticleController emitter, Vector3 translation, Vector3 axis, float angle) {
			emitter.setTranslation(translation);
			this.emitter = emitter;
			this.axis = axis;
			this.angle = angle;
		}

		@Override
		public boolean act (float delta) {
			emitter.getTransform(TMP_M1);
			TMP_Q.set(axis, angle * delta).toMatrix(TMP_M2.val);
			TMP_M2.mul(TMP_M1);
			emitter.setTransform(TMP_M2);
			return false;
		}
	}

	/** Action which continually changes a {@link ParticleController#worldTransform}. Translation is performed on the xz-plane in a
	 * circle around origin. Rotation is performed around the y-axis. Scale is increased and decreased along the y-axis and
	 * xz-plane in cycles. This action does NOT create a trail of particles when performed, since it does not affect where new
	 * particles are generated. */
	private class WorldTransformAction extends Action {
		private ParticleController emitter;

		final static float DST_FROM_ORIGIN = 10;
		final static float POS_SPEED = 25;
		final static float ROT_SPEED = 75;
		final static float SCL_SPEED = 100;

		float currentPos = 0;
		float currentRot = 0;
		float currentScl = 0;

		Vector3 pos = new Vector3();
		Quaternion rot = new Quaternion();
		Vector3 scl = new Vector3();

		public WorldTransformAction (ParticleController emitter) {
			this.emitter = emitter;
		}

		@Override
		public boolean act (float delta) {
			rot.setFromAxis(Vector3.Y, currentRot = (currentRot + delta * ROT_SPEED) % 360);

			float deltaScl = MathUtils.sinDeg(currentScl = (currentScl + delta * SCL_SPEED) % 360);
			scl.y = deltaScl * 0.5f + 1.5f;
			scl.x = scl.z = -deltaScl * 0.5f + 1.75f;

			TMP_Q.setFromAxis(Vector3.Y, currentPos = (currentPos + delta * POS_SPEED) % 360)
				.transform(pos.set(Vector3.Z).scl(DST_FROM_ORIGIN));

			if (emitter.worldTransform != null) emitter.worldTransform.set(pos, rot, scl);
			return false;
		}
	}

	// Simulation
	ParticleEffect effect;

	// Rendering
	Environment environment;
	ParticleBatch particleBatch;
	BillboardParticleBatch billboardParticleBatch;
	ModelInstanceParticleBatch modelInstanceParticleBatch;
	PointSpriteParticleBatch pointSpriteParticleBatch;

	// UI
	Stage ui;
	Label fpsLabel;
	StringBuilder builder;

	private void addController (float x, float y, float z, Vector3 actionAxis, float actionRotation,
		ParticleController controller) {
		controller.init();
		controller.start();
		effect.getControllers().add(controller);
		ui.addAction(new RotationAction(controller, new Vector3(x, y, z), actionAxis, actionRotation));
		ui.addAction(new WorldTransformAction(controller));
	}

	@Override
	public void create () {
		super.create();

		ui = new Stage();
		builder = new StringBuilder();

		effect = new ParticleEffect();

		assets.load(DEFAULT_PARTICLE, Texture.class);
		assets.load(DEFAULT_SKIN, Skin.class);
		assets.load(DEFAULT_MODEL, Model.class);
		loading = true;

		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0f, 0f, 0.1f, 1f));
		environment.add(new DirectionalLight().set(1f, 1f, 1f, 0, -0.5f, -1));

		modelInstanceParticleBatch = new ModelInstanceParticleBatch();

		pointSpriteParticleBatch = new PointSpriteParticleBatch();
		pointSpriteParticleBatch.setCamera(cam);

		billboardParticleBatch = new BillboardParticleBatch();
		billboardParticleBatch.setCamera(cam);
		billboardParticleBatch.setUseGpu(false);
		billboardParticleBatch.setAlignMode(ParticleShader.AlignMode.ViewPoint);

		particleBatch = billboardParticleBatch;

		InputMultiplexer multiplexer = new InputMultiplexer();
		multiplexer.addProcessor(ui);
		multiplexer.addProcessor(new CameraInputController(cam));
		Gdx.input.setInputProcessor(multiplexer);

		cam.position.add(TMP_V.set(cam.direction).scl(-20));
		cam.update();
	}

	private Array<Influencer> createInfluencers (float[] colors) {
		Array<Influencer> influencers = new Array<Influencer>();

		// Spawn
		PointSpawnShapeValue pointSpawnShapeValue = new PointSpawnShapeValue();
		pointSpawnShapeValue.xOffsetValue.setLow(0, 1f);
		pointSpawnShapeValue.xOffsetValue.setActive(true);
		pointSpawnShapeValue.yOffsetValue.setLow(0, 1f);
		pointSpawnShapeValue.yOffsetValue.setActive(true);
		pointSpawnShapeValue.zOffsetValue.setLow(0, 1f);
		pointSpawnShapeValue.zOffsetValue.setActive(true);
		SpawnInfluencer spawnSource = new SpawnInfluencer(pointSpawnShapeValue);
		influencers.add(spawnSource);

		// Scale
		ScaleInfluencer scaleInfluencer = new ScaleInfluencer();
		scaleInfluencer.value.setTimeline(new float[] {0, 1});
		scaleInfluencer.value.setScaling(new float[] {1, 0});
		scaleInfluencer.value.setLow(0);
		scaleInfluencer.value.setHigh(1);
		influencers.add(scaleInfluencer);

		// Color
		ColorInfluencer.Single colorInfluencer = new ColorInfluencer.Single();
		colorInfluencer.colorValue.setColors(new float[] {colors[0], colors[1], colors[2], 0, 0, 0});
		colorInfluencer.colorValue.setTimeline(new float[] {0, 1});
		colorInfluencer.alphaValue.setHigh(1);
		colorInfluencer.alphaValue.setTimeline(new float[] {0, 0.5f, 0.8f, 1});
		colorInfluencer.alphaValue.setScaling(new float[] {0, 0.15f, 0.5f, 0});
		influencers.add(colorInfluencer);

		// Dynamics
		DynamicsInfluencer dynamicsInfluencer = new DynamicsInfluencer();
		BrownianAcceleration modifier = new BrownianAcceleration();
		modifier.strengthValue.setTimeline(new float[] {0, 1});
		modifier.strengthValue.setScaling(new float[] {0, 1});
		modifier.strengthValue.setHigh(80);
		modifier.strengthValue.setLow(1, 5);
		dynamicsInfluencer.velocities.add(modifier);
		influencers.add(dynamicsInfluencer);
		return influencers;
	}

	private ParticleController createBillboardController (float[] colors, Texture particleTexture) {
		RegularEmitter emitter = new RegularEmitter();
		emitter.getDuration().setLow(3000);
		emitter.getEmission().setHigh(2900);
		emitter.getLife().setHigh(1000);
		emitter.setMaxParticleCount(3000);
		ParticleController controller = new ParticleController();
		controller.name = "Billboard Controller";
		controller.emitter = emitter;
		controller.renderer = new BillboardRenderer(billboardParticleBatch);
		controller.particleChannels = new ParticleChannels();
		controller.influencers = createInfluencers(colors);
		controller.influencers.add(new RegionInfluencer.Single(particleTexture));
		return controller;
	}

	private ParticleController createModelInstanceController (float[] colors, Model model) {
		RegularEmitter emitter = new RegularEmitter();
		emitter.getDuration().setLow(3000);
		emitter.getEmission().setHigh(900);
		emitter.getLife().setHigh(1000);
		emitter.setMaxParticleCount(3000);
		ParticleController controller = new ParticleController();
		controller.name = "ModelInstance Controller";
		controller.emitter = emitter;
		controller.renderer = new ModelInstanceRenderer(modelInstanceParticleBatch);
		controller.particleChannels = new ParticleChannels();
		controller.influencers = createInfluencers(colors);
		controller.influencers.add(new ModelInfluencer.Single(model));
		return controller;
	}

	private ParticleController createPointSpriteController (float[] colors) {
		RegularEmitter emitter = new RegularEmitter();
		emitter.getDuration().setLow(3000);
		emitter.getEmission().setHigh(2900);
		emitter.getLife().setHigh(1000);
		emitter.setMaxParticleCount(3000);
		ParticleController controller = new ParticleController();
		controller.name = "PointSprite Controller";
		controller.emitter = emitter;
		controller.renderer = new PointSpriteRenderer(pointSpriteParticleBatch);
		controller.particleChannels = new ParticleChannels();
		controller.influencers = createInfluencers(colors);
		return controller;
	}

	@Override
	public void dispose () {
		super.dispose();
		ui.dispose();
		effect.dispose();
	}

	@Override
	protected void onLoaded () {
		pointSpriteParticleBatch.setTexture(assets.get(DEFAULT_PARTICLE, Texture.class));

		final Model particleModel = assets.get(DEFAULT_MODEL, Model.class);
		for (Node node : particleModel.nodes)
			node.scale.scl(0.3f);
		particleModel.materials.first().remove(TextureAttribute.Diffuse);
		particleModel.materials.first().set(new ColorAttribute(ColorAttribute.AmbientLight, Color.WHITE));

		Texture particleTexture = assets.get(DEFAULT_PARTICLE);
		billboardParticleBatch.setTexture(assets.get(DEFAULT_PARTICLE, Texture.class));

		float[] colorRed = new float[] {1, 0.12156863f, 0.047058824f};
		float[] colorGreen = new float[] {0.12156863f, 1, 0.047058824f};
		float[] colorBlue = new float[] {0.12156863f, 0.047058824f, 1};

		addController(5, 5, 0, Vector3.X, 360, createBillboardController(colorRed, particleTexture));
		addController(0, 5, -5, Vector3.Y, -360, createBillboardController(colorGreen, particleTexture));
		addController(0, 5, 5, Vector3.Z, -360, createBillboardController(colorBlue, particleTexture));

		addController(5, 5, 0, Vector3.X, 360, createModelInstanceController(colorRed, particleModel));
		addController(0, 5, -5, Vector3.Y, -360, createModelInstanceController(colorGreen, particleModel));
		addController(0, 5, 5, Vector3.Z, -360, createModelInstanceController(colorBlue, particleModel));

		addController(5, 5, 0, Vector3.X, 360, createPointSpriteController(colorRed));
		addController(0, 5, -5, Vector3.Y, -360, createPointSpriteController(colorGreen));
		addController(0, 5, 5, Vector3.Z, -360, createPointSpriteController(colorBlue));

		setupUI();
	}

	@Override
	protected void render (ModelBatch batch, Array<ModelInstance> instances) {
		if (effect.getControllers().size > 0) {
			// Update
			float delta = Gdx.graphics.getDeltaTime();
			builder.delete(0, builder.length());
			builder.append(Gdx.graphics.getFramesPerSecond());
			fpsLabel.setText(builder);
			ui.act(delta);

			particleBatch.begin();
			for (ParticleController controller : effect.getControllers()) {
				if (controller.renderer.isCompatible(particleBatch)) {
					controller.update();
					controller.draw();
				}
			}
			particleBatch.end();
			batch.render(particleBatch, environment);
		}
		batch.render(instances, environment);
		ui.draw();
	}

	@Override
	public void resize (int width, int height) {
		super.resize(width, height);
		ui.getViewport().setWorldSize(width, height);
		ui.getViewport().update(width, height, true);
	}

	private void setupUI () {
		Skin skin = assets.get(DEFAULT_SKIN);
		Table topTable = new Table();
		topTable.setFillParent(true);
		topTable.top().left().add(new Label("FPS ", skin)).left();
		topTable.add(fpsLabel = new Label("", skin)).left().expandX().row();
		ui.addActor(topTable);

		Table bottomTable = new Table();
		bottomTable.setFillParent(true);

		final CheckBox boxUseWorldTransform = new CheckBox("Use world transform matrix", skin);
		boxUseWorldTransform.addListener(new ClickListener() {
			boolean useWorldTransform = false;

			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				useWorldTransform = !useWorldTransform;
				effect.setWorldTransform((useWorldTransform) ? new Matrix4() : null);
				return true;
			}
		});
		final CheckBox boxScaleParticles = new CheckBox("World transform scales particles", skin);
		boxScaleParticles.addListener(new ClickListener() {
			boolean scaleParticles = false;

			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				scaleParticles = !scaleParticles;
				for (ParticleController ctrl : effect.getControllers())
					ctrl.worldTransformScalesParticles = scaleParticles;
				return true;
			}
		});
		final CheckBox boxBillboardsGPU = new CheckBox("Billboards use GPU", skin);
		boxBillboardsGPU.addListener(new ClickListener() {
			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				billboardParticleBatch.setUseGpu(!billboardParticleBatch.isUseGPU());
				return true;
			}
		});
		final CheckBox boxAlignScreen = new CheckBox("Billboards align to screen", skin);
		boxAlignScreen.addListener(new ClickListener() {
			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				boolean alignScreen = (billboardParticleBatch.getAlignMode() == ParticleShader.AlignMode.Screen);
				billboardParticleBatch
					.setAlignMode(alignScreen ? ParticleShader.AlignMode.ViewPoint : ParticleShader.AlignMode.Screen);
				return true;
			}
		});
		final CheckBox boxBillboardBatch = new CheckBox("BillboardParticleBatch", skin);
		boxBillboardBatch.addListener(new ClickListener() {
			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				particleBatch = billboardParticleBatch;
				return true;
			}
		});
		final CheckBox boxModelBatch = new CheckBox("ModelInstanceParticleBatch", skin);
		boxModelBatch.addListener(new ClickListener() {
			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				particleBatch = modelInstanceParticleBatch;
				return true;
			}
		});
		final CheckBox boxPointBatch = new CheckBox("PointSpriteParticleBatch", skin);
		boxPointBatch.addListener(new ClickListener() {
			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				particleBatch = pointSpriteParticleBatch;
				return true;
			}
		});
		ButtonGroup<CheckBox> batchesBoxes = new ButtonGroup<CheckBox>(boxBillboardBatch, boxModelBatch, boxPointBatch);

		boxBillboardsGPU.setChecked(billboardParticleBatch.isUseGPU());
		boxAlignScreen.setChecked(billboardParticleBatch.getAlignMode() == ParticleShader.AlignMode.Screen);
		if (particleBatch == billboardParticleBatch)
			boxBillboardBatch.setChecked(true);
		else if (particleBatch == modelInstanceParticleBatch)
			boxModelBatch.setChecked(true);
		else if (particleBatch == pointSpriteParticleBatch) boxPointBatch.setChecked(true);

		bottomTable.bottom().left().add(boxUseWorldTransform).left().row();
		bottomTable.bottom().left().add(boxScaleParticles).left().row();
		bottomTable.bottom().left().add(boxBillboardsGPU).left().row();
		bottomTable.bottom().left().add(boxAlignScreen).left().row();
		for (CheckBox box : batchesBoxes.getButtons())
			bottomTable.bottom().left().add(box).left().row();

		ui.addActor(bottomTable);
	}

}
