
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
import com.badlogic.gdx.graphics.g3d.particles.influencers.DynamicsModifier.TangentialAcceleration;
import com.badlogic.gdx.graphics.g3d.particles.influencers.Influencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ParticleControllerFinalizerInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ParticleControllerInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.RegionInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.SpawnInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.BillboardRenderer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.ParticleControllerControllerRenderer;
import com.badlogic.gdx.graphics.g3d.particles.values.CylinderSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.particles.values.PointSpawnShapeValue;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;

/** @author ryanastout */
public class ParticleControllerInfluencerSingleTest extends BaseG3dTest {
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
		controller.rotate(Vector3.X, 90);
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
		emitter.getDuration().setLow(1000);
		emitter.getEmission().setHigh(300);
		emitter.getLife().setHigh(4000);
		emitter.setMaxParticleCount(20);

		// Spawn

		CylinderSpawnShapeValue cylinderSpawnShapeValue = new CylinderSpawnShapeValue();
		cylinderSpawnShapeValue.spawnWidthValue.setHigh(5);// x
		cylinderSpawnShapeValue.spawnHeightValue.setHigh(10);// y
		cylinderSpawnShapeValue.spawnDepthValue.setHigh(5);// z
		cylinderSpawnShapeValue.setEdges(true);

		SpawnInfluencer spawnSource = new SpawnInfluencer(cylinderSpawnShapeValue);
		// Dynamics
		DynamicsInfluencer dynamicsInfluencer = new DynamicsInfluencer();

		TangentialAcceleration tangentialAcceleration = new TangentialAcceleration();
		tangentialAcceleration.thetaValue.setActive(true);
		tangentialAcceleration.thetaValue.setTimeline(new float[] {0});
		tangentialAcceleration.thetaValue.setScaling(new float[] {1});
		tangentialAcceleration.thetaValue.setHigh(90);
		tangentialAcceleration.phiValue.setActive(true);
		tangentialAcceleration.phiValue.setTimeline(new float[] {0});
		tangentialAcceleration.phiValue.setScaling(new float[] {1});
		tangentialAcceleration.phiValue.setHigh(0);
		tangentialAcceleration.strengthValue.setActive(true);
		tangentialAcceleration.strengthValue.setHigh(10);
		tangentialAcceleration.strengthValue.setTimeline(new float[] {0});
		tangentialAcceleration.strengthValue.setScaling(new float[] {1});
		tangentialAcceleration.isGlobal = false;
		dynamicsInfluencer.velocities.add(tangentialAcceleration);

		ParticleController ret = new ParticleController("Billboard Controller", emitter, new BillboardRenderer(
			billboardParticleBatch), new RegionInfluencer.Single(particleTexture), spawnSource, dynamicsInfluencer);

		ParticleControllerInfluencer pci = new ParticleControllerInfluencer.Single(ret);
		SpawnInfluencer si = new SpawnInfluencer(new PointSpawnShapeValue());
		Influencer pcfi = new ParticleControllerFinalizerInfluencer();
		RegularEmitter emitter2 = new RegularEmitter();
		emitter2.getDuration().setLow(3000);
		emitter2.getEmission().setHigh(300);
		emitter2.getLife().setHigh(4000);
		emitter2.setMaxParticleCount(30);
		ParticleController ret2 = new ParticleController("Bigger", emitter2, new ParticleControllerControllerRenderer(), pci, si,
			pcfi);
		return ret2;
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
