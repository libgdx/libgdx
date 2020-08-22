
package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.batches.BillboardParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.emitters.RegularEmitter;
import com.badlogic.gdx.graphics.g3d.particles.influencers.DynamicsInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.DynamicsModifier.PolarAcceleration;
import com.badlogic.gdx.graphics.g3d.particles.influencers.RegionInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.SpawnInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.BillboardRenderer;
import com.badlogic.gdx.graphics.g3d.particles.values.CylinderSpawnShapeValue;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;

/** @author ryanastout */
public class PolarAccelerationTest extends BaseG3dTest {
	public static final String DEFAULT_PARTICLE = "data/pre_particle.png", DEFAULT_SKIN = "data/uiskin.json";
	Quaternion tmpQuaternion = new Quaternion();
	Matrix4 tmpMatrix = new Matrix4(), tmpMatrix4 = new Matrix4();
	Vector3 tmpVector = new Vector3();

	// Simulation
	Array<ParticleController> emitters;

	// Rendering
	Environment environment;
	BillboardParticleBatch billboardParticleBatch;

	// UI
	Stage ui;
	Label fpsLabel;
	StringBuilder builder;

	@Override
	public void create () {
		super.create();
		emitters = new Array<ParticleController>();
		assets.load(DEFAULT_PARTICLE, Texture.class);
		assets.load(DEFAULT_SKIN, Skin.class);
		loading = true;
		environment = new Environment();
		billboardParticleBatch = new BillboardParticleBatch();
		billboardParticleBatch.setCamera(cam);
		ui = new Stage();
		builder = new StringBuilder();
	}

	@Override
	public void resize (int width, int height) {
		super.resize(width, height);
		ui.getViewport().setWorldSize(width, height);
		ui.getViewport().update(width, height, true);
	}

	@Override
	protected void onLoaded () {
		Texture particleTexture = assets.get(DEFAULT_PARTICLE);
		billboardParticleBatch.setTexture(assets.get(DEFAULT_PARTICLE, Texture.class));
		addEmitter(particleTexture);
		setupUI();
	}

	private void addEmitter (Texture particleTexture) {
		ParticleController controller = createBillboardController(particleTexture);
		controller.init();
		controller.start();
		emitters.add(controller);

		controller.translate(new Vector3(5, 0, 5));
		controller.rotate(new Vector3(.707f, .707f, 0), 135);
	}

	private void setupUI () {
		Skin skin = assets.get(DEFAULT_SKIN);
		Table table = new Table();
		table.setFillParent(true);
		table.top().left().add(new Label("FPS ", skin)).left();
		table.add(fpsLabel = new Label("", skin)).left().expandX().row();
		ui.addActor(table);
	}

	private ParticleController createBillboardController (Texture particleTexture) {
		// Emission
		RegularEmitter emitter = new RegularEmitter();
		emitter.getDuration().setLow(3000);
		emitter.getEmission().setHigh(300);
		emitter.getLife().setHigh(4000);
		emitter.setMaxParticleCount(3000);

		// Spawn

		CylinderSpawnShapeValue cylinderSpawnShapeValue = new CylinderSpawnShapeValue();
		cylinderSpawnShapeValue.spawnWidthValue.setHigh(5);// x
		cylinderSpawnShapeValue.spawnHeightValue.setHigh(10);// y
		cylinderSpawnShapeValue.spawnDepthValue.setHigh(5);// z
		cylinderSpawnShapeValue.setEdges(true);

		SpawnInfluencer spawnSource = new SpawnInfluencer(cylinderSpawnShapeValue);
		// Dynamics
		DynamicsInfluencer dynamicsInfluencer = new DynamicsInfluencer();

		PolarAcceleration polarAcceleration = new PolarAcceleration();
		polarAcceleration.thetaValue.setActive(true);
		polarAcceleration.thetaValue.setTimeline(new float[] {0});
		polarAcceleration.thetaValue.setScaling(new float[] {1});
		polarAcceleration.thetaValue.setHigh(90);
		polarAcceleration.phiValue.setActive(true);
		polarAcceleration.phiValue.setTimeline(new float[] {0});
		polarAcceleration.phiValue.setScaling(new float[] {1});
		polarAcceleration.phiValue.setHigh(0);
		polarAcceleration.strengthValue.setActive(true);
		polarAcceleration.strengthValue.setHigh(10);
		polarAcceleration.strengthValue.setTimeline(new float[] {0});
		polarAcceleration.strengthValue.setScaling(new float[] {1});
		polarAcceleration.isGlobal = false;
		dynamicsInfluencer.velocities.add(polarAcceleration);

		ParticleController ret = new ParticleController("Billboard Controller", emitter, new BillboardRenderer(
			billboardParticleBatch), new RegionInfluencer.Single(particleTexture), spawnSource, dynamicsInfluencer);

		return ret;
	}

	@Override
	protected void render (ModelBatch batch, Array<ModelInstance> instances) {
		if (emitters.size > 0) {
			// Update
			float delta = Gdx.graphics.getDeltaTime();
			builder.delete(0, builder.length());
			builder.append(Gdx.graphics.getFramesPerSecond());
			fpsLabel.setText(builder);
			ui.act(delta);

			billboardParticleBatch.begin();
			for (ParticleController controller : emitters) {
				controller.update();
				controller.draw();
			}
			billboardParticleBatch.end();
			batch.render(billboardParticleBatch, environment);
		}
		batch.render(instances, environment);
		ui.draw();
	}
}
