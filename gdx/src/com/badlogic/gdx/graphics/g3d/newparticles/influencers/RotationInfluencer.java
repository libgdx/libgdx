package com.badlogic.gdx.graphics.g3d.newparticles.influencers;

import com.badlogic.gdx.graphics.g3d.newparticles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.newparticles.ModelInstanceParticle;
import com.badlogic.gdx.graphics.g3d.newparticles.Particle;
import com.badlogic.gdx.graphics.g3d.newparticles.ParticleController;
import com.badlogic.gdx.graphics.g3d.newparticles.ParticleSystem;
import com.badlogic.gdx.graphics.g3d.newparticles.influencers.VelocityInfluencer.ModelInstanceVelocityInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.values.ScaledNumericValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValue.VelocityType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public abstract class RotationInfluencer<T> extends Influencer<T> {

	public static BillboardRotationInfluencer billboard(){
		return new BillboardRotationInfluencer();
	}
	
	public static ModelInstanceRotationInfluencer modelInstance(){
		return new ModelInstanceRotationInfluencer();
	}
	
	public static class BillboardRotationInfluencer extends RotationInfluencer<BillboardParticle>{
		public ScaledNumericValue 	rotationValue;
		public BillboardRotationInfluencer (BillboardRotationInfluencer billboardRotationInfluencer) {
			this();
			set(billboardRotationInfluencer);
		}

		public BillboardRotationInfluencer () {
			rotationValue = new ScaledNumericValue();
		}
		
		public void set(BillboardRotationInfluencer influencer){
			this.rotationValue.load(influencer.rotationValue);
		}
		

		@Override
		public void initParticles (int startIndex, int count) {
			for(int i=startIndex, c = startIndex +count; i < c; ++i){
				BillboardParticle particle = controller.particles[i];
				particle.rotationStart = rotationValue.newLowValue();
				particle.rotationDiff = rotationValue.newHighValue();
				if (!rotationValue.isRelative()) particle.rotationDiff -= particle.rotationStart;
				float rotation = particle.rotationStart + particle.rotationDiff * rotationValue.getScale(0);
				particle.cosRotation = MathUtils.cosDeg(rotation);
				particle.sinRotation = MathUtils.sinDeg(rotation);
			}
		}

		@Override
		public void update () {
			for(int i=0, c = controller.emitter.activeCount; i < c; ++i){
				BillboardParticle particle = controller.particles[i];
				float rotation = particle.rotationStart + particle.rotationDiff * rotationValue.getScale(particle.lifePercent);
				particle.cosRotation = MathUtils.cosDeg(rotation);
				particle.sinRotation = MathUtils.sinDeg(rotation);
			}
		}

		@Override
		public BillboardRotationInfluencer copy () {
			return new BillboardRotationInfluencer(this);
		}
	}
	
	public static class ModelInstanceRotationInfluencer extends RotationInfluencer<ModelInstanceParticle>{
		public Array<VelocityValue> velocities;
		public ModelInstanceRotationInfluencer(VelocityValue...velocities){
			this.velocities = new Array<VelocityValue>(velocities);
		}
		
		public ModelInstanceRotationInfluencer (ModelInstanceRotationInfluencer billboardVelocityInfluencer) {
			this.velocities = new Array<VelocityValue>(billboardVelocityInfluencer.velocities);
		}

		@Override
		public void init () {
			int velSize = velocities.size*VelocityInfluencer.VEL_DATA_SIZE;
			for(int i=0, activeCount = controller.emitter.maxParticleCount; i < activeCount; ++i){
				ModelInstanceParticle particle = controller.particles[i];
				if(particle.rotationData == null || particle.rotationData.length < velSize)
					particle.rotationData = new float[velSize];
			}
		}
		
		@Override
		public void initParticles (int startIndex, int count) {
			for(int i=startIndex, c = startIndex +count; i < c; ++i){
				ModelInstanceParticle particle = controller.particles[i];
				for(int k=0; k < velocities.size; ++k){
					VelocityInfluencer.setVelocityData(velocities.items[k], particle.rotationData);
					particle.rotation.idt();
				}
			}
		}
		
		@Override
		public void update () {
			for(int i=0, activeCount = controller.emitter.activeCount; i < activeCount; ++i){
				ModelInstanceParticle particle = controller.particles[i];
				for(int k=0; k < velocities.size; ++k){
					addRotation(particle.rotation, velocities.items[k], particle.rotationData, k*VelocityInfluencer.VEL_DATA_SIZE, particle.lifePercent);
				}
			}
		}
		
		protected void addRotation(Quaternion outRotation, VelocityValue velocityValue, float[] velocityData,  int dataIndex, float percent){
			float strength = (velocityData[dataIndex+ VelocityInfluencer.VEL_STRENGTH_INDEX] + velocityData[dataIndex +VelocityInfluencer.VEL_STRENGTH_INDEX+1] * velocityValue.strength.getScale(percent)) * controller.deltaTime;
			float phi = velocityData[dataIndex+VelocityInfluencer.VEL_PHI_INDEX] + velocityData[dataIndex+VelocityInfluencer.VEL_PHI_INDEX+1] * velocityValue.phi.getScale(percent),  
				theta = velocityData[dataIndex+VelocityInfluencer.VEL_THETA_INDEX] + velocityData[dataIndex+VelocityInfluencer.VEL_THETA_INDEX+1] * velocityValue.theta.getScale(percent);

			TMP_V3.set(Vector3.X).rotate(Vector3.Y, theta);
			TMP_V2.set(TMP_V3).crs(Vector3.Y).nor();
			TMP_V3.rotate(TMP_V2, phi);
			if(!velocityValue.isGlobal)
				TMP_V3.rot(controller.transform);//.nor();
			outRotation.mulLeft(TMP_Q.set(TMP_V3, strength));
		}

		@Override
		public ParticleSystem<ModelInstanceParticle> copy () {
			return new ModelInstanceRotationInfluencer(this);
		}
	}
	
}
