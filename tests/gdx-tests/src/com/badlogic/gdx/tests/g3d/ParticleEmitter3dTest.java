package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetErrorListener;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEmitter;
import com.badlogic.gdx.graphics.g3d.particles.Emitter.ScaledNumericValue;
import com.badlogic.gdx.graphics.g3d.particles.Emitter.VelocityValue;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;

public class ParticleEmitter3dTest extends BaseG3dTest{
	public static final String DEFAULT_PARTICLE = "data/particle.png"; 
	Quaternion tmpQuaternion = new Quaternion();
	Matrix4 tmpMatrix = new Matrix4(), tmpMatrix4 = new Matrix4();
	
	private class EmitterAction extends Action{
		private ParticleEmitter emitter;
		Vector3 axis;
		float angle;
		
		public EmitterAction (ParticleEmitter emitter, Vector3 axis, float angle) {
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
	Array<ParticleEmitter> emitters;
	Array<Action> actions;
	@Override
	public void create () {
		super.create();
		emitters = new Array<ParticleEmitter>();
		actions = new Array<Action>();
		assets.load(DEFAULT_PARTICLE, Texture.class);
		loading = true;
	}
	
	@Override
	protected void onLoaded () {
		Texture particleTexture = assets.get(DEFAULT_PARTICLE);
		
		//X
		ParticleEmitter emitter = createEmitter(new float[] {1, 0.12156863f, 0.047058824f}, particleTexture);
		emitter.translate(Vector3.tmp.set(5,5,0));
		emitter.rotate(Vector3.X, -90);
		actions.add(new EmitterAction(emitter, Vector3.X, 360));
		emitters.add(emitter);
		
		//Y
		emitter = createEmitter(new float[] { 0.12156863f, 1, 0.047058824f}, particleTexture);
		emitter.translate(Vector3.tmp.set(5,5,0));
		emitter.rotate(Vector3.Y, -90);
		actions.add(new EmitterAction(emitter, Vector3.Y, -360));
		emitters.add(emitter);
		
		//Z
		emitter = createEmitter(new float[] {0.12156863f, 0.047058824f, 1}, particleTexture);
		emitter.translate(Vector3.tmp.set(0,5,5));
		emitter.rotate(Vector3.Z, -90);
		actions.add(new EmitterAction(emitter, Vector3.Z, -360));		
		emitters.add(emitter);
	}
	
	private ParticleEmitter createEmitter (float[] color, Texture texture) {
		ParticleEmitter emitter = new ParticleEmitter();

		emitter.getDuration().setLow(3000);

		emitter.getEmission().setHigh(250);

		emitter.getLife().setHigh(500, 1000);
		emitter.getLife().setTimeline(new float[] {0, 0.66f, 1});
		emitter.getLife().setScaling(new float[] {1, 1, 0.3f});

		emitter.getScaleValue().setHigh(1);

		emitter.getRotation().setLow(1, 360);
		emitter.getRotation().setHigh(180, 180);
		emitter.getRotation().setTimeline(new float[] {0, 1});
		emitter.getRotation().setScaling(new float[] {0, 1});
		emitter.getRotation().setRelative(true);

		//Velocity
		VelocityValue velocityValue = emitter.getVelocityValue(0); 
		ScaledNumericValue thetaValue = velocityValue.getTheta();
		thetaValue.setHigh(0, 359);
		thetaValue.setActive(true);
		
		ScaledNumericValue phiValue = velocityValue.getPhi();
		phiValue.setHigh(45, 135);
		phiValue.setLow(90);
		phiValue.setTimeline(new float[] {0, 0.5f, 1});
		phiValue.setScaling(new float[] {1, 0, 0});
		phiValue.setActive(true);

		velocityValue.getStrength().setHigh(5, 10);
		velocityValue.getStrength().setActive(true);
		velocityValue.setActive(true);

		//Spawn
		emitter.getSpawnDepth().setHigh(5);
		emitter.getSpawnHeight().setHigh(5);
		emitter.getSpawnWidth().setHigh(5);
		
		//Color
		emitter.getTint().setColors(color);
		emitter.getTransparency().setHigh(1, 1);
		emitter.getTransparency().setTimeline(new float[] {0, 0.2f, 0.8f, 1});
		emitter.getTransparency().setScaling(new float[] {0, 1, 0.75f, 0});

		emitter.setMaxParticleCount(200);
		emitter.setContinuous(true);
		emitter.setRegionFromTexture(texture);
		emitter.setCamera(cam);
		return emitter;
	}

	@Override
	protected void render (ModelBatch batch, Array<ModelInstance> instances) {
		if(emitters.size > 0){
			//Update
			float delta = Gdx.graphics.getDeltaTime();
			for(Action action : actions)
				action.act(delta);
			for (ParticleEmitter emitter : emitters){
				boolean complete = true;
				emitter.update(delta);
				if (!emitter.isComplete()) complete = false;
				if (complete) emitter.start();

				//Render
				batch.render(emitter);
			}
		}
	}

	@Override
	public boolean needsGL20 () {
		return true;
	}
}
