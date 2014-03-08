package com.badlogic.gdx.graphics.g3d.newparticles.influencers;

import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.newparticles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.newparticles.ModelInstanceParticle;
import com.badlogic.gdx.graphics.g3d.newparticles.Particle;
import com.badlogic.gdx.graphics.g3d.newparticles.ParticleController;
import com.badlogic.gdx.graphics.g3d.newparticles.ParticleSystem;
import com.badlogic.gdx.graphics.g3d.newparticles.PointParticle;
import com.badlogic.gdx.graphics.g3d.newparticles.influencers.ColorInfluencer.BillboardColorInfluencer;
import com.badlogic.gdx.math.MathUtils;

public abstract class RandomColorInfluencer<T> extends Influencer<T>{

	public static class BillboardRandomColorInfluencer extends RandomColorInfluencer<BillboardParticle>{
		@Override
		public void initParticles (int startIndex, int count) {
			for(int i=startIndex, c = startIndex +count; i < c; ++i){
				BillboardParticle particle = controller.particles[i];
				particle.r = MathUtils.random();
				particle.g = MathUtils.random();
				particle.b = MathUtils.random();
				particle.a = MathUtils.random();
			}
		}

		@Override
		public ParticleSystem<BillboardParticle> copy () {
			return new BillboardRandomColorInfluencer();
		}
	}
	
	public static class ModelInstanceRandomColorInfluencer extends RandomColorInfluencer<ModelInstanceParticle>{
		@Override
		public void initParticles (int startIndex, int count) {
			for(int i=startIndex, c = startIndex +count; i < c; ++i){
				ModelInstanceParticle particle = controller.particles[i];
				particle.color = ((ColorAttribute)particle.instance.materials.get(0).get(ColorAttribute.Diffuse)).color;
				particle.color.r = MathUtils.random();
				particle.color.g = MathUtils.random();
				particle.color.b = MathUtils.random();
				particle.color.a = MathUtils.random();
			}
		}

		@Override
		public ParticleSystem<ModelInstanceParticle> copy () {
			return new ModelInstanceRandomColorInfluencer();
		}
	}
	
	public static class PointRandomColorInfluencer extends RandomColorInfluencer<PointParticle>{
		@Override
		public void initParticles (int startIndex, int count) {
			for(int i=startIndex, c = startIndex +count; i < c; ++i){
				PointParticle particle = controller.particles[i];
				particle.r = MathUtils.random();
				particle.g = MathUtils.random();
				particle.b = MathUtils.random();
				particle.a = MathUtils.random();
			}
		}

		@Override
		public ParticleSystem<PointParticle> copy () {
			return new PointRandomColorInfluencer();
		}
	}
	
	
}
