package com.badlogic.gdx.graphics.g3d.newparticles.influencers;

import com.badlogic.gdx.graphics.g3d.newparticles.ModelInstanceParticle;
import com.badlogic.gdx.graphics.g3d.newparticles.ParticleSystem;
import com.badlogic.gdx.graphics.g3d.newparticles.influencers.RotationInfluencer.ModelInstanceRotationInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValue;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public abstract class RandomRotationInfluencer<T> extends Influencer<T> {
	protected static final int AXIS_OFFSET = 0,
										ANGLE_OFFSET = 3,
										ROTATION_DATA_SIZE = 5;
	
	public static ModelInstanceRandomRotationInfluencer modelInstance(){
		return new ModelInstanceRandomRotationInfluencer();
	}
	
	protected int rotationCount;
	protected float minVelocity, maxVelocity;

	public static class ModelInstanceRandomRotationInfluencer extends RandomRotationInfluencer<ModelInstanceParticle>{
		
		public ModelInstanceRandomRotationInfluencer(){}
		
		public ModelInstanceRandomRotationInfluencer(int rotationCount, float minVelocity, float maxVelocity){
			super(rotationCount, minVelocity, maxVelocity);
		}
		
		public ModelInstanceRandomRotationInfluencer (ModelInstanceRandomRotationInfluencer modelInstanceRandomRotationInfluencer) {
			super(modelInstanceRandomRotationInfluencer);
		}

		@Override
		public void init () {
			int velSize = rotationCount*ROTATION_DATA_SIZE;
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
				particle.rotation.idt();
				for(int k=0, dataIndex = 0; k < rotationCount; ++k, dataIndex += ROTATION_DATA_SIZE){
					TMP_V1.set(MathUtils.random(-1f, 1f), MathUtils.random(-1f, 1f), MathUtils.random(-1f, 1f)).nor();
					particle.rotationData[dataIndex + AXIS_OFFSET] = TMP_V1.x;
					particle.rotationData[dataIndex + AXIS_OFFSET+1] = TMP_V1.y;
					particle.rotationData[dataIndex + AXIS_OFFSET+2] = TMP_V1.z;
					particle.rotationData[dataIndex + ANGLE_OFFSET] = MathUtils.random(minVelocity, maxVelocity);
				}
			}
		}
		
		@Override
		public void update () {
			for(int i=0, activeCount = controller.emitter.activeCount; i < activeCount; ++i){
				ModelInstanceParticle particle = controller.particles[i];
				for(int k=0, dataIndex = 0; k < rotationCount; ++k, dataIndex += ROTATION_DATA_SIZE){
					TMP_V1.x = particle.rotationData[dataIndex + AXIS_OFFSET]; 
					TMP_V1.y = particle.rotationData[dataIndex + AXIS_OFFSET+1];
					TMP_V1.z = particle.rotationData[dataIndex + AXIS_OFFSET+2];
					particle.rotation.mulLeft(TMP_Q.set(TMP_V1, particle.rotationData[dataIndex + ANGLE_OFFSET] * controller.deltaTime));
				}
			}
		}
		
		@Override
		public ParticleSystem<ModelInstanceParticle> copy () {
			return new ModelInstanceRandomRotationInfluencer(this);
		}
	}
	
	public RandomRotationInfluencer(){}
	
	public RandomRotationInfluencer(int rotationCount, float minVelocity, float maxVelocity){
		this.rotationCount = rotationCount;
		this.minVelocity = minVelocity;
		this.maxVelocity = maxVelocity;
	}
	
	public RandomRotationInfluencer(RandomRotationInfluencer rotationInfluencer){
		this(rotationInfluencer.rotationCount, rotationInfluencer.minVelocity, rotationInfluencer.maxVelocity);
	}
	
	
	
}
