package com.badlogic.gdx.graphics.g3d.particles.influencers;

import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.particles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.particles.ModelInstanceParticle;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.graphics.g3d.particles.PointParticle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public abstract class RandomColorInfluencer<T> extends Influencer<T>{

	public static class BillboardRandomColorInfluencer extends RandomColorInfluencer<BillboardParticle>{
		@Override
		public void activateParticles (int startIndex, int count) {
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
		public void activateParticles (int startIndex, int count) {
			for(int i=startIndex, c = startIndex +count; i < c; ++i){
				ModelInstanceParticle particle = controller.particles[i];
				particle.color = ((ColorAttribute)particle.instance.materials.get(0).get(ColorAttribute.Diffuse)).color;
				particle.blending = ((BlendingAttribute)particle.instance.materials.get(0).get(BlendingAttribute.Type));
				particle.color.r = MathUtils.random();
				particle.color.g = MathUtils.random();
				particle.color.b = MathUtils.random();
				if(particle.blending != null)
					particle.blending.opacity = MathUtils.random();
				else 
					particle.color.a = MathUtils.random();
			}
		}

		@Override
		public ParticleSystem<ModelInstanceParticle> copy () {
			return new ModelInstanceRandomColorInfluencer();
		}
	}
	
	public static class PointSpriteRandomColorInfluencer extends RandomColorInfluencer<PointParticle>{
		@Override
		public void activateParticles (int startIndex, int count) {
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
			return new PointSpriteRandomColorInfluencer();
		}
	}
}
