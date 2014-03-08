package com.badlogic.gdx.graphics.g3d.newparticles.influencers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.newparticles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.newparticles.ModelInstanceParticle;
import com.badlogic.gdx.graphics.g3d.newparticles.Particle;
import com.badlogic.gdx.graphics.g3d.newparticles.ParticleController;
import com.badlogic.gdx.graphics.g3d.newparticles.ParticleControllerParticle;
import com.badlogic.gdx.graphics.g3d.newparticles.ParticleSystem;
import com.badlogic.gdx.graphics.g3d.newparticles.PointParticle;
import com.badlogic.gdx.graphics.g3d.newparticles.values.VelocityValue;
import com.badlogic.gdx.graphics.g3d.newparticles.values.VelocityDatas.VelocityData;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public abstract class VelocityInfluencer<T> extends Influencer<T> {
	
	//Billboards
	
	public static class BillboardVelocityInfluencer extends VelocityInfluencer<BillboardParticle>{
		public BillboardVelocityInfluencer () {}

		public BillboardVelocityInfluencer (BillboardVelocityInfluencer billboardVelocityInfluencer) {
			super(billboardVelocityInfluencer);
		}
		
		@Override
		public void init () {
			int velSize = velocities.size;
			for(int i=0, activeCount = controller.emitter.maxParticleCount; i < activeCount; ++i){
				BillboardParticle particle = controller.particles[i];
				if(particle.velocityData == null || particle.velocityData.length < velSize){
					particle.velocityData = new VelocityData[velSize];
				}
				
				int k=0;
				for(VelocityValue value : velocities)
					particle.velocityData[k++] = value.allocData();
				
				if(particle.velocity == null)
					particle.velocity = new Vector3();
			}
		}

		public void initParticles (int startIndex, int count) {
			for(int i=startIndex, c = startIndex +count; i < c; ++i){
				BillboardParticle particle = controller.particles[i];
				for(int k=0; k < velocities.size; ++k){
					velocities.items[k].initData(particle.velocityData[k]);
				}
			}
		}

		public void update(){
			for(int i=0, activeCount = controller.emitter.activeCount; i < activeCount; ++i){
				BillboardParticle particle = controller.particles[i];
				particle.velocity.set(0, 0, 0);
				for(int k=0; k < velocities.size; ++k)
					velocities.items[k].addVelocity(controller, particle, particle.velocityData[k]);
				
				if(BillboardParticle.ROTATION_ACCUMULATOR != 0){
					float cosBeta = MathUtils.cosDeg(BillboardParticle.ROTATION_ACCUMULATOR),
							sinBeta = MathUtils.sinDeg(BillboardParticle.ROTATION_ACCUMULATOR);
					
					float cos = particle.cosRotation*cosBeta - particle.sinRotation*sinBeta,
							sin = particle.sinRotation*cosBeta + particle.cosRotation*sinBeta;
					
					particle.cosRotation = cos;
					particle.sinRotation = sin;
					
					BillboardParticle.ROTATION_ACCUMULATOR = 0;
				}
				
				particle.x += particle.velocity.x; 
				particle.y += particle.velocity.y; 
				particle.z += particle.velocity.z;
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
		public void init () {
			int velSize = velocities.size;
			for(int i=0, activeCount = controller.emitter.maxParticleCount; i < activeCount; ++i){
				ModelInstanceParticle particle = controller.particles[i];
				if(particle.velocityData == null || particle.velocityData.length < velSize){
					particle.velocityData = new VelocityData[velSize];
				}
				int k=0;
				for(VelocityValue value : velocities)
					particle.velocityData[k++] = value.allocData();
				if(particle.velocity == null)
					particle.velocity = new Vector3();
			}
		}
		
		@Override
		public void initParticles (int startIndex, int count) {
			for(int i=startIndex, c = startIndex +count; i < c; ++i){
				ModelInstanceParticle particle = controller.particles[i];
				particle.rotation.idt();
				for(int k=0; k < velocities.size; ++k){
					velocities.items[k].initData(particle.velocityData[k]);
				}
			}
		}

		@Override
		public void update(){
			for(int i=0, activeCount = controller.emitter.activeCount; i < activeCount; ++i){
				ModelInstanceParticle particle = controller.particles[i];
				particle.velocity.set(0, 0, 0);
				float[] val = particle.instance.transform.val;
				for(int k=0; k < velocities.size; ++k)
					velocities.items[k].addVelocity(controller, particle, particle.velocityData[k]);
				
				if(!ModelInstanceParticle.ROTATION_ACCUMULATOR.isIdentity()){
					particle.rotation.mulLeft(ModelInstanceParticle.ROTATION_ACCUMULATOR);
					ModelInstanceParticle.ROTATION_ACCUMULATOR.idt();
				}
				
				val[Matrix4.M03] += particle.velocity.x;
				val[Matrix4.M13] += particle.velocity.y;
				val[Matrix4.M23] += particle.velocity.z;
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
		public void init () {
			int velSize = velocities.size;
			for(int i=0, activeCount = controller.emitter.maxParticleCount; i < activeCount; ++i){
				ParticleControllerParticle particle = controller.particles[i];
				if(particle.velocityData == null || particle.velocityData.length < velSize){
					particle.velocityData = new VelocityData[velSize];
				}
				int k=0;
				for(VelocityValue value : velocities)
					particle.velocityData[k++] = value.allocData();
				if(particle.velocity == null)
					particle.velocity = new Vector3();
			}
		}

		@Override
		public void initParticles (int startIndex, int count) {
			for(int i=startIndex, c = startIndex +count; i < c; ++i){
				ParticleControllerParticle particle = controller.particles[i];
				particle.rotation.idt();
				for(int k=0; k < velocities.size; ++k){
					velocities.items[k].initData(particle.velocityData[k]);
				}
			}
		}

		@Override
		public void update(){
			for(int i=0, activeCount = controller.emitter.activeCount; i < activeCount; ++i){
				ParticleControllerParticle particle = controller.particles[i];
				particle.velocity.set(0, 0, 0);
				for(int k=0; k < velocities.size; ++k)
					velocities.items[k].addVelocity(controller, particle, particle.velocityData[k]);

				if(!ParticleControllerParticle.ROTATION_ACCUMULATOR.isIdentity()){
					particle.rotation.mulLeft(ParticleControllerParticle.ROTATION_ACCUMULATOR);
					ParticleControllerParticle.ROTATION_ACCUMULATOR.idt();
				}

				particle.x += particle.velocity.x;
				particle.y += particle.velocity.y;
				particle.z += particle.velocity.z;
			}
		}

		@Override
		public ParticleSystem<ParticleControllerParticle> copy () {
			return new ParticleControllerVelocityInfluencer(this);
		}
	}


	//Points

	public static class PointVelocityInfluencer extends VelocityInfluencer<PointParticle>{
		public PointVelocityInfluencer () {}

		public PointVelocityInfluencer (PointVelocityInfluencer billboardVelocityInfluencer) {
			super(billboardVelocityInfluencer);
		}

		@Override
		public void init () {
			int velSize = velocities.size;
			for(int i=0, activeCount = controller.emitter.maxParticleCount; i < activeCount; ++i){
				PointParticle particle = controller.particles[i];
				if(particle.velocityData == null || particle.velocityData.length < velSize){
					particle.velocityData = new VelocityData[velSize];
				}
				int k=0;
				for(VelocityValue value : velocities)
					particle.velocityData[k++] = value.allocData();
				if(particle.velocity == null)
					particle.velocity = new Vector3();
			}
		}

		public void initParticles (int startIndex, int count) {
			for(int i=startIndex, c = startIndex +count; i < c; ++i){
				PointParticle particle = controller.particles[i];
				for(int k=0; k < velocities.size; ++k){
					velocities.items[k].initData(particle.velocityData[k]);
				}
			}
		}

		public void update(){
			for(int i=0, activeCount = controller.emitter.activeCount; i < activeCount; ++i){
				PointParticle particle = controller.particles[i];
				particle.velocity.set(0, 0, 0);
				for(int k=0; k < velocities.size; ++k)
					velocities.items[k].addVelocity(controller, particle, particle.velocityData[k]);
				
				if(PointParticle.ROTATION_ACCUMULATOR != 0){
					float cosBeta = MathUtils.cosDeg(PointParticle.ROTATION_ACCUMULATOR),
							sinBeta = MathUtils.sinDeg(PointParticle.ROTATION_ACCUMULATOR);
					
					float cos = particle.cosRotation*cosBeta - particle.sinRotation*sinBeta,
							sin = particle.sinRotation*cosBeta + particle.cosRotation*sinBeta;
					
					particle.cosRotation = cos;
					particle.sinRotation = sin;
					
					PointParticle.ROTATION_ACCUMULATOR = 0;
				}
				
				particle.x += particle.velocity.x; 
				particle.y += particle.velocity.y; 
				particle.z += particle.velocity.z;
			}
		}

		@Override
		public ParticleSystem<PointParticle> copy () {
			return new PointVelocityInfluencer(this);
		}
	}






	public Array<VelocityValue> velocities;

	public VelocityInfluencer(VelocityValue...velocities){
		this.velocities = new Array<VelocityValue>(velocities);
	}
	
	public VelocityInfluencer(int velocities){
		this.velocities = new Array<VelocityValue>(false, velocities, VelocityValue.class);
	}

	public VelocityInfluencer (VelocityInfluencer<T> billboardVelocityInfluencer) {
		this.velocities = new Array<VelocityValue>(billboardVelocityInfluencer.velocities);
	}
	
}
