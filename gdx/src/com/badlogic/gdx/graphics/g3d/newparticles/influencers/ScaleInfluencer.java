package com.badlogic.gdx.graphics.g3d.newparticles.influencers;

import com.badlogic.gdx.graphics.g3d.newparticles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.newparticles.ModelInstanceParticle;
import com.badlogic.gdx.graphics.g3d.newparticles.Particle;
import com.badlogic.gdx.graphics.g3d.newparticles.ParticleController;
import com.badlogic.gdx.graphics.g3d.newparticles.ParticleControllerParticle;
import com.badlogic.gdx.graphics.g3d.newparticles.PointParticle;
import com.badlogic.gdx.graphics.g3d.newparticles.values.GradientColorValue;
import com.badlogic.gdx.graphics.g3d.newparticles.values.ScaledNumericValue;

public abstract class ScaleInfluencer<T> extends Influencer<T> {
	
	public static class BillboardScaleInfluencer extends ScaleInfluencer<BillboardParticle> {
		public BillboardScaleInfluencer () {}
		public BillboardScaleInfluencer (BillboardScaleInfluencer billboardScaleinfluencer) {
			super(billboardScaleinfluencer);
		}

		@Override
		public void initParticles (int startIndex, int count) {
			for(int i=startIndex, c = startIndex +count; i < c; ++i){
				BillboardParticle particle = controller.particles[i];
				particle.scaleStart = scaleValue.newLowValue();
				particle.scaleDiff = scaleValue.newHighValue();
				if (!scaleValue.isRelative()) particle.scaleDiff -= particle.scaleStart;
				particle.scale = particle.scaleStart + particle.scaleDiff * scaleValue.getScale(0);
				
			}
		}

		@Override
		public void update () {
			for(int i=0, c = controller.emitter.activeCount; i < c; ++i){
				BillboardParticle particle = controller.particles[i];
				particle.scale = particle.scaleStart + particle.scaleDiff * scaleValue.getScale(particle.lifePercent);
			}
		}
		
		@Override
		public ScaleInfluencer copy () {
			return new BillboardScaleInfluencer(this);
		}
	}
	
	public static class ModelInstanceScaleInfluencer extends ScaleInfluencer<ModelInstanceParticle> {
		public ModelInstanceScaleInfluencer () {}
		public ModelInstanceScaleInfluencer (ModelInstanceScaleInfluencer billboardScaleinfluencer) {
			super(billboardScaleinfluencer);
		}

		@Override
		public void initParticles (int startIndex, int count) {
			for(int i=startIndex, c = startIndex +count; i < c; ++i){
				ModelInstanceParticle particle = controller.particles[i];
				particle.scaleStart = scaleValue.newLowValue();
				particle.scaleDiff = scaleValue.newHighValue();
				if (!scaleValue.isRelative()) particle.scaleDiff -= particle.scaleStart;
				particle.scale = particle.scaleStart + particle.scaleDiff * scaleValue.getScale(0);
			}
		}

		@Override
		public void update () {
			for(int i=0, c = controller.emitter.activeCount; i < c; ++i){
				ModelInstanceParticle particle = controller.particles[i];
				particle.scale = particle.scaleStart + particle.scaleDiff * scaleValue.getScale(particle.lifePercent);
			}
		}
		
		@Override
		public ScaleInfluencer copy () {
			return new ModelInstanceScaleInfluencer(this);
		}
	}
	
	public static class ParticleControllerScaleInfluencer extends ScaleInfluencer<ParticleControllerParticle> {
		public ParticleControllerScaleInfluencer () {}
		public ParticleControllerScaleInfluencer (ParticleControllerScaleInfluencer billboardScaleinfluencer) {
			super(billboardScaleinfluencer);
		}

		@Override
		public void initParticles (int startIndex, int count) {
			for(int i=startIndex, c = startIndex +count; i < c; ++i){
				ParticleControllerParticle particle = controller.particles[i];
				particle.scaleStart = scaleValue.newLowValue();
				particle.scaleDiff = scaleValue.newHighValue();
				if (!scaleValue.isRelative()) particle.scaleDiff -= particle.scaleStart;
				particle.scale = particle.scaleStart + particle.scaleDiff * scaleValue.getScale(0);
			}
		}

		@Override
		public void update () {
			for(int i=0, c = controller.emitter.activeCount; i < c; ++i){
				ParticleControllerParticle particle = controller.particles[i];
				particle.scale = particle.scaleStart + particle.scaleDiff * scaleValue.getScale(particle.lifePercent);
			}
		}
		
		@Override
		public ScaleInfluencer copy () {
			return new ParticleControllerScaleInfluencer(this);
		}
	}
	
	public static class PointScaleInfluencer extends ScaleInfluencer<PointParticle> {
		public PointScaleInfluencer () {}
		public PointScaleInfluencer (PointScaleInfluencer billboardScaleinfluencer) {
			super(billboardScaleinfluencer);
		}

		@Override
		public void initParticles (int startIndex, int count) {
			for(int i=startIndex, c = startIndex +count; i < c; ++i){
				PointParticle particle = controller.particles[i];
				particle.scaleStart = scaleValue.newLowValue();
				particle.scaleDiff = scaleValue.newHighValue();
				if (!scaleValue.isRelative()) particle.scaleDiff -= particle.scaleStart;
				particle.scale = particle.scaleStart + particle.scaleDiff * scaleValue.getScale(0);
				
			}
		}

		@Override
		public void update () {
			for(int i=0, c = controller.emitter.activeCount; i < c; ++i){
				PointParticle particle = controller.particles[i];
				particle.scale = particle.scaleStart + particle.scaleDiff * scaleValue.getScale(particle.lifePercent);
			}
		}
		
		@Override
		public ScaleInfluencer copy () {
			return new PointScaleInfluencer(this);
		}
	}
	
	

	public ScaledNumericValue scaleValue;
	
	public ScaleInfluencer(){
		scaleValue = new ScaledNumericValue();
	}
	
	public ScaleInfluencer(ScaledNumericValue scaleValue){
		this.scaleValue.load(scaleValue);
	}
	
	public ScaleInfluencer (ScaleInfluencer<T> billboardScaleinfluencer) {
		this();
		set(billboardScaleinfluencer);
	}

	private void set (ScaleInfluencer scaleInfluencer) {
		scaleValue.load(scaleInfluencer.scaleValue);
	}

}
