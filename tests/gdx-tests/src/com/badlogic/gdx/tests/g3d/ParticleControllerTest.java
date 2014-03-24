package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetErrorListener;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter.SpawnEllipseSide;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.particles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.particles.ModelInstanceParticle;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.ParticleControllerParticle;
import com.badlogic.gdx.graphics.g3d.particles.PointSpriteParticle;
import com.badlogic.gdx.graphics.g3d.particles.batches.BillboardParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.controllers.BillboardParticleController;
import com.badlogic.gdx.graphics.g3d.particles.controllers.ModelInstanceParticleController;
import com.badlogic.gdx.graphics.g3d.particles.controllers.ParticleControllerParticleController;
import com.badlogic.gdx.graphics.g3d.particles.controllers.PointSpriteParticleController;
import com.badlogic.gdx.graphics.g3d.particles.emitters.RegularEmitter;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ColorInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.RandomColorInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.RegionInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ScaleInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.SpawnShapeInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.VelocityInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ColorInfluencer.BillboardColorInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ColorInfluencer.PointSpriteColorInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.RandomColorInfluencer.ModelInstanceRandomColorInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ScaleInfluencer.*;
import com.badlogic.gdx.graphics.g3d.particles.influencers.SpawnShapeInfluencer.BillboardSpawnInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.SpawnShapeInfluencer.ModelInstanceSpawnInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.SpawnShapeInfluencer.ParticleControllerSpawnInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.SpawnShapeInfluencer.PointSpriteSpawnInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.VelocityInfluencer.BillboardVelocityInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.VelocityInfluencer.ModelInstanceVelocityInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.VelocityInfluencer.ParticleControllerVelocityInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.VelocityInfluencer.PointSpriteVelocityInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.values.AngularVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.EllipseSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.particles.values.LineSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.particles.values.MeshSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.particles.values.PointSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.particles.values.ScaledNumericValue;
import com.badlogic.gdx.graphics.g3d.particles.values.SpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.PrimitiveSpawnShapeValue.SpawnSide;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.BillboardBrownianVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.BillboardPolarVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.BillboardRotationVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.BillboardWeightVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.ModelInstanceBrownianVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.ModelInstanceCentripetalVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.ModelInstanceRotationVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.ParticleControllerCentripetalVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.ParticleControllerRotationVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.PointPolarVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.PointRotationVelocityValue;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;

/** @author Inferno */
public class ParticleControllerTest extends BaseG3dTest{
	public static final String DEFAULT_PARTICLE = "data/pre_particle.png";
	Quaternion tmpQuaternion = new Quaternion();
	Matrix4 tmpMatrix = new Matrix4(), tmpMatrix4 = new Matrix4();
	Vector3 tmpVector = new Vector3();
	
	private class RotationAction extends Action{
		private ParticleController emitter;
		Vector3 axis;
		float angle;
		
		public RotationAction (ParticleController emitter, Vector3 axis, float angle) {
			this.emitter = emitter;
			this.axis = axis;
			this.angle = angle;
		}

		@Override
		public boolean act (float delta) {
			emitter.getTransform(tmpMatrix);
			tmpQuaternion.set(axis, angle*delta).toMatrix(tmpMatrix4.val);
			tmpMatrix4.mul(tmpMatrix);
			emitter.setTransform(tmpMatrix4);
			return false;
		}
	}
	
	Array<ParticleController> emitters;
	Array<Action> actions;
	Environment environment;
	BillboardParticleBatch billboardParticleBatch;
	@Override
	public void create () {
		super.create();
		emitters = new Array<ParticleController>();
		actions = new Array<Action>();
		assets.load(DEFAULT_PARTICLE, Texture.class);
		loading = true;
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0f, 0f, 0.1f, 1f));
		environment.add(new DirectionalLight().set(1f, 1f, 1f,  0, -0.5f, -1 ));
		billboardParticleBatch = new BillboardParticleBatch();
		billboardParticleBatch.setCamera(cam);
	}
	
	@Override
	protected void onLoaded () {
		Texture particleTexture = assets.get(DEFAULT_PARTICLE);
		billboardParticleBatch.setTexture(assets.get(DEFAULT_PARTICLE, Texture.class));
		
		//X
		ParticleController controller = createBillboardController(new float[] {1, 0.12156863f, 0.047058824f}, particleTexture);
		controller.init();
		controller.start();
		emitters.add(controller);
		controller.translate(Vector3.tmp.set(5,5,0));
		controller.rotate(Vector3.X, -90);
		actions.add(new RotationAction(controller, Vector3.X, 360));

		//Y
		controller = createBillboardController(new float[] { 0.12156863f, 1, 0.047058824f}, particleTexture);
		controller.init();
		controller.start();
		controller.translate(Vector3.tmp.set(0,5,-5));
		controller.rotate(Vector3.Z, -90);
		actions.add(new RotationAction(controller, Vector3.Y, -360));
		emitters.add(controller);
		
		//Z
		controller = createBillboardController(new float[] {0.12156863f, 0.047058824f, 1}, particleTexture);
		controller.init();
		controller.start();
		controller.translate(Vector3.tmp.set(0,5,5));
		controller.rotate(Vector3.Z, -90);
		actions.add(new RotationAction(controller, Vector3.Z, -360));		
		emitters.add(controller);
	}

	private ParticleController createBillboardController (float[] colors, Texture particleTexture) {
		//Emission
		RegularEmitter emitter = new RegularEmitter();
		emitter.getDuration().setLow(3000);
		emitter.getEmission().setHigh(900);
		emitter.getLife().setHigh(1000);
		emitter.setMaxParticleCount(1000);

		//Spawn
		PointSpawnShapeValue pointSpawnShapeValue = new PointSpawnShapeValue();		
		pointSpawnShapeValue.xOffsetValue.setLow(0, 1f);
		pointSpawnShapeValue.xOffsetValue.setActive(true);
		pointSpawnShapeValue.yOffsetValue.setLow(0, 1f);
		pointSpawnShapeValue.yOffsetValue.setActive(true);
		pointSpawnShapeValue.zOffsetValue.setLow(0, 1f);
		pointSpawnShapeValue.zOffsetValue.setActive(true);
		BillboardSpawnInfluencer spawnSource = new BillboardSpawnInfluencer(pointSpawnShapeValue);

		BillboardScaleInfluencer scaleInfluencer = new BillboardScaleInfluencer();
		scaleInfluencer.scaleValue.setHigh(1f);

		//Color
		BillboardColorInfluencer colorInfluencer = new BillboardColorInfluencer();
		colorInfluencer.colorValue.setColors(new float[] {colors[0], colors[1], colors[2], 0,0,0});
		colorInfluencer.colorValue.setTimeline(new float[] {0, 1});
		colorInfluencer.alphaValue.setHigh(1);
		colorInfluencer.alphaValue.setTimeline(new float[] {0, 0.5f, 0.8f, 1});
		colorInfluencer.alphaValue.setScaling(new float[] {0, 0.15f, 0.5f, 0});

		//Velocity
		BillboardVelocityInfluencer velocityInfluencer = new BillboardVelocityInfluencer();
		BillboardBrownianVelocityValue velocityValue = new BillboardBrownianVelocityValue();
		velocityValue.strengthValue.setHigh(5, 10);
		velocityInfluencer.velocities.add(velocityValue);
		
		return new BillboardParticleController("Billboard Controller", emitter, billboardParticleBatch, 
			new RegionInfluencer.BillboardSingleRegionInfluencer(particleTexture),
			spawnSource,
			velocityInfluencer,
			scaleInfluencer,
			colorInfluencer
			);
	}

	@Override
	protected void render (ModelBatch batch, Array<ModelInstance> instances) {
		if(emitters.size > 0){
			//Update
			float delta = Gdx.graphics.getDeltaTime();
			for(Action action : actions)
				action.act(delta);
			billboardParticleBatch.begin();
			for (ParticleController controller : emitters){
				controller.update(delta);
				controller.draw();
			}
			billboardParticleBatch.end();
			batch.render(billboardParticleBatch, environment);
		}
		batch.render(instances, environment);
	}
}
