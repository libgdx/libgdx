package com.badlogic.gdx.graphics.g3d.particles.influencers;

import com.badlogic.gdx.graphics.g3d.particles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.particles.ModelInstanceParticle;
import com.badlogic.gdx.graphics.g3d.particles.Particle;
import com.badlogic.gdx.graphics.g3d.particles.ParticleControllerParticle;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.graphics.g3d.particles.PointParticle;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityDatas.VelocityData;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValue;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public abstract class VelocityInfluencer<T extends Particle> extends Influencer<T> {
	
	//Billboards
	
	public static class BillboardVelocityInfluencer extends VelocityInfluencer<BillboardParticle>{
		public BillboardVelocityInfluencer () {}

		public BillboardVelocityInfluencer (BillboardVelocityInfluencer billboardVelocityInfluencer) {
			super(billboardVelocityInfluencer);
		}
		
		public void update(){
			for(int i=0, activeCount = controller.emitter.activeCount; i < activeCount; ++i){
				BillboardParticle particle = controller.particles[i];
				particle.velocity.set(0, 0, 0);
				for(int k=0; k < velocities.size; ++k)
					velocities.items[k].addVelocity(controller, particle, particle.velocityData[k]);
				
				if(Particle.ROTATION_ACCUMULATOR != 0){
					Particle.ROTATION_ACCUMULATOR*= controller.deltaTime;
					float cosBeta = MathUtils.cosDeg(Particle.ROTATION_ACCUMULATOR),
							sinBeta = MathUtils.sinDeg(Particle.ROTATION_ACCUMULATOR);
					
					float cos = particle.cosRotation*cosBeta - particle.sinRotation*sinBeta,
							sin = particle.sinRotation*cosBeta + particle.cosRotation*sinBeta;
					
					particle.cosRotation = cos;
					particle.sinRotation = sin;
					
					Particle.ROTATION_ACCUMULATOR = 0;
				}
				
				particle.x += particle.velocity.x* controller.deltaTime; 
				particle.y += particle.velocity.y* controller.deltaTime; 
				particle.z += particle.velocity.z* controller.deltaTime;
			}
		}

		@Override
		public ParticleSystem<BillboardParticle> copy () {
			return new BillboardVelocityInfluencer(this);
		}
	}
	
	
	
	//Model Instances
	
	public static class ModelInstanceVelocityInfluencer extends VelocityInfluencer<ModelInstanceParticle>{
		public ModelInstanceVelocityInfluencer () {}

		public ModelInstanceVelocityInfluencer (ModelInstanceVelocityInfluencer billboardVelocityInfluencer) {
			super(billboardVelocityInfluencer);
		}
		
		@Override
		public void update(){
			for(int i=0, activeCount = controller.emitter.activeCount; i < activeCount; ++i){
				ModelInstanceParticle particle = controller.particles[i];
				particle.velocity.set(0, 0, 0);
				float[] val = particle.instance.transform.val;
				for(int k=0; k < velocities.size; ++k)
					velocities.items[k].addVelocity(controller, particle, particle.velocityData[k]);
				
				if(!Particle.ROTATION_3D_ACCUMULATOR.isIdentity()){
					float angle = Particle.ROTATION_3D_ACCUMULATOR.getAngle()*controller.deltaTime;
					Particle.ROTATION_3D_ACCUMULATOR.getAxisAngle(TMP_V1);
					Particle.ROTATION_3D_ACCUMULATOR.setFromAxis(TMP_V1, angle);
					particle.rotation.mulLeft(Particle.ROTATION_3D_ACCUMULATOR);
					Particle.ROTATION_3D_ACCUMULATOR.idt();
				}
				
				val[Matrix4.M03] += particle.velocity.x*controller.deltaTime;
				val[Matrix4.M13] += particle.velocity.y*controller.deltaTime;
				val[Matrix4.M23] += particle.velocity.z*controller.deltaTime;
			}
		}
		
		@Override
		public ParticleSystem<ModelInstanceParticle> copy () {
			return new ModelInstanceVelocityInfluencer(this);
		}
	}


	//ParticleController

	public static class ParticleControllerVelocityInfluencer extends VelocityInfluencer<ParticleControllerParticle>{
		public ParticleControllerVelocityInfluencer () {}

		public ParticleControllerVelocityInfluencer (ParticleControllerVelocityInfluencer billboardVelocityInfluencer) {
			super(billboardVelocityInfluencer);
		}
		
		@Override
		public void update(){
			for(int i=0, activeCount = controller.emitter.activeCount; i < activeCount; ++i){
				ParticleControllerParticle particle = controller.particles[i];
				particle.velocity.set(0, 0, 0);
				for(int k=0; k < velocities.size; ++k)
					velocities.items[k].addVelocity(controller, particle, particle.velocityData[k]);

				if(!Particle.ROTATION_3D_ACCUMULATOR.isIdentity()){
					float angle = Particle.ROTATION_3D_ACCUMULATOR.getAngle()*controller.deltaTime;
					Particle.ROTATION_3D_ACCUMULATOR.getAxisAngle(TMP_V1);
					Particle.ROTATION_3D_ACCUMULATOR.setFromAxis(TMP_V1, angle);
					particle.rotation.mulLeft(Particle.ROTATION_3D_ACCUMULATOR);
					Particle.ROTATION_3D_ACCUMULATOR.idt();
				}

				particle.controller.velocity.set(particle.velocity);
			}
		}

		@Override
		public ParticleSystem<ParticleControllerParticle> copy () {
			return new ParticleControllerVelocityInfluencer(this);
		}
	}


	//Points

	public static class PointSpriteVelocityInfluencer extends VelocityInfluencer<PointParticle>{
		public PointSpriteVelocityInfluencer () {}

		public PointSpriteVelocityInfluencer (PointSpriteVelocityInfluencer billboardVelocityInfluencer) {
			super(billboardVelocityInfluencer);
		}

		public void update(){
			for(int i=0, activeCount = controller.emitter.activeCount; i < activeCount; ++i){
				PointParticle particle = controller.particles[i];
				particle.velocity.set(0, 0, 0);
				for(int k=0; k < velocities.size; ++k)
					velocities.items[k].addVelocity(controller, particle, particle.velocityData[k]);
				
				if(Particle.ROTATION_ACCUMULATOR != 0){
					Particle.ROTATION_ACCUMULATOR*=controller.deltaTime;
					float cosBeta = MathUtils.cosDeg(Particle.ROTATION_ACCUMULATOR),
							sinBeta = MathUtils.sinDeg(Particle.ROTATION_ACCUMULATOR);
					
					float cos = particle.cosRotation*cosBeta - particle.sinRotation*sinBeta,
							sin = particle.sinRotation*cosBeta + particle.cosRotation*sinBeta;
					
					particle.cosRotation = cos;
					particle.sinRotation = sin;
					
					Particle.ROTATION_ACCUMULATOR = 0;
				}
				
				particle.x += particle.velocity.x*controller.deltaTime; 
				particle.y += particle.velocity.y*controller.deltaTime; 
				particle.z += particle.velocity.z*controller.deltaTime;
			}
		}

		@Override
		public ParticleSystem<PointParticle> copy () {
			return new PointSpriteVelocityInfluencer(this);
		}
	}






	public Array<VelocityValue> velocities;
	
	public VelocityInfluencer(){
		this.velocities = new Array<VelocityValue>(true, 3, VelocityValue.class);
	}

	public VelocityInfluencer(VelocityValue...velocities){
		this.velocities = new Array<VelocityValue>(true, velocities.length, VelocityValue.class);
		for(VelocityValue value : velocities){
			this.velocities.add(value.copy());
		}
	}
	
	public VelocityInfluencer (VelocityInfluencer<T> billboardVelocityInfluencer) {
		this((VelocityValue[])billboardVelocityInfluencer.velocities.toArray(VelocityValue.class));
	}
	
	@Override
	public void init () {
		Particle.ROTATION_ACCUMULATOR = 0;
		Particle.ROTATION_3D_ACCUMULATOR.idt();
		for(int i=0, activeCount = controller.emitter.maxParticleCount; i < activeCount; ++i){
			Particle particle = controller.particles[i];
			if(velocities.size >0 && (particle.velocityData == null || particle.velocityData.length < velocities.size) ){
				particle.velocityData = new VelocityData[velocities.size];
			}
			
			for(int k=0; k < velocities.size; ++k){
				particle.velocityData[k] = velocities.items[k].allocData();
			}
			
			if(particle.velocity == null)
				particle.velocity = new Vector3();
		}
	}

	public void activateParticles (int startIndex, int count) {
		for(int i=startIndex, c = startIndex +count; i < c; ++i){
			Particle particle = controller.particles[i];
			for(int k=0; k < velocities.size; ++k){
				velocities.items[k].initData(particle.velocityData[k]);
			}
		}
	}
	
	@Override
	public void write (Json json) {
		json.writeValue("velocities", velocities, Array.class, VelocityValue.class);
	}
	
	@Override
	public void read (Json json, JsonValue jsonData) {
		velocities = json.readValue("velocities", Array.class, VelocityValue.class, jsonData);
	}
}
