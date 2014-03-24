package com.badlogic.gdx.graphics.g3d.particles.influencers;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.particles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.particles.ModelInstanceParticle;
import com.badlogic.gdx.graphics.g3d.particles.ParticleControllerParticle;
import com.badlogic.gdx.graphics.g3d.particles.PointSpriteParticle;
import com.badlogic.gdx.graphics.g3d.particles.values.ScaledNumericValue;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/** It's an {@link Influencer} which controls the scale of the particles.*/
/** @author Inferno */
public abstract class ScaleInfluencer<T> extends Influencer<T> {
	
	public static class BillboardScaleInfluencer extends ScaleInfluencer<BillboardParticle> {
		public BillboardScaleInfluencer () {}
		public BillboardScaleInfluencer (BillboardScaleInfluencer billboardScaleinfluencer) {
			super(billboardScaleinfluencer);
		}

		@Override
		public void activateParticles (int startIndex, int count) {
			for(int i=startIndex, c = startIndex +count; i < c; ++i){
				BillboardParticle particle = controller.particles[i];
				particle.scaleStart = scaleValue.newLowValue() * controller.scale.x;
				particle.scaleDiff = scaleValue.newHighValue() * controller.scale.y;
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
		public void activateParticles (int startIndex, int count) {
			for(int i=startIndex, c = startIndex +count; i < c; ++i){
				ModelInstanceParticle particle = controller.particles[i];
				particle.scaleStart = scaleValue.newLowValue() * controller.scale.x;
				particle.scaleDiff = scaleValue.newHighValue() * controller.scale.y;
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
		public void activateParticles (int startIndex, int count) {
			for(int i=startIndex, c = startIndex +count; i < c; ++i){
				ParticleControllerParticle particle = controller.particles[i];
				particle.scaleStart = scaleValue.newLowValue() * controller.scale.x;
				particle.scaleDiff = scaleValue.newHighValue() * controller.scale.y;
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
	
	public static class PointSpriteScaleInfluencer extends ScaleInfluencer<PointSpriteParticle> {
		public PointSpriteScaleInfluencer () {}
		public PointSpriteScaleInfluencer (PointSpriteScaleInfluencer billboardScaleinfluencer) {
			super(billboardScaleinfluencer);
		}

		@Override
		public void activateParticles (int startIndex, int count) {
			for(int i=startIndex, c = startIndex +count; i < c; ++i){
				PointSpriteParticle particle = controller.particles[i];
				particle.scaleStart = scaleValue.newLowValue() * controller.scale.x;
				particle.scaleDiff = scaleValue.newHighValue() * controller.scale.y;
				if (!scaleValue.isRelative()) particle.scaleDiff -= particle.scaleStart;
				particle.scale = particle.scaleStart + particle.scaleDiff * scaleValue.getScale(0);
				
			}
		}

		@Override
		public void update () {
			for(int i=0, c = controller.emitter.activeCount; i < c; ++i){
				PointSpriteParticle particle = controller.particles[i];
				particle.scale = particle.scaleStart + particle.scaleDiff * scaleValue.getScale(particle.lifePercent);
			}
		}
		
		@Override
		public ScaleInfluencer copy () {
			return new PointSpriteScaleInfluencer(this);
		}
	}

	public ScaledNumericValue scaleValue;
	
	public ScaleInfluencer(){
		scaleValue = new ScaledNumericValue();
		scaleValue.setHigh(1);
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

	@Override
	public void write (Json json) {
		json.writeValue("scale", scaleValue);
	}
	
	@Override
	public void read (Json json, JsonValue jsonData) {
		scaleValue = json.readValue("scale", ScaledNumericValue.class, jsonData);
	}
	
}
