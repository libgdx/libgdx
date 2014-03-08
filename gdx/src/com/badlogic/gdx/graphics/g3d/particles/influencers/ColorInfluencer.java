package com.badlogic.gdx.graphics.g3d.particles.influencers;

import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.particles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.particles.ModelInstanceParticle;
import com.badlogic.gdx.graphics.g3d.particles.Particle;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.PointParticle;
import com.badlogic.gdx.graphics.g3d.particles.values.GradientColorValue;
import com.badlogic.gdx.graphics.g3d.particles.values.ScaledNumericValue;

public abstract class ColorInfluencer<T> extends Influencer<T> {
	public static class BillboardColorInfluencer extends ColorInfluencer<BillboardParticle>{
		public BillboardColorInfluencer(){}
		
		public BillboardColorInfluencer(BillboardColorInfluencer billboardColorInfluencer){
			super(billboardColorInfluencer);
		}

		@Override
		public void initParticles (int startIndex, int count) {
			for(int i=startIndex, c = startIndex +count; i < c; ++i){
				BillboardParticle particle = controller.particles[i];
				float[] temp = colorValue.getColor(0);
				particle.r = temp[0];
				particle.g = temp[1];
				particle.b = temp[2];
				particle.alphaStart = alphaValue.newLowValue();
				particle.alphaDiff = alphaValue.newHighValue() - particle.alphaStart;
				particle.a = particle.alphaStart + particle.alphaDiff * alphaValue.getScale(particle.lifePercent);
			}
		}

		@Override
		public void update () {
			for(int i=0, c = controller.emitter.activeCount; i < c; ++i){
				BillboardParticle particle = controller.particles[i];
				float[] temp = colorValue.getColor(particle.lifePercent);
				particle.r = temp[0];
				particle.g = temp[1];
				particle.b = temp[2];
				particle.a = particle.alphaStart + particle.alphaDiff * alphaValue.getScale(particle.lifePercent);
			}
		}
		@Override
		public BillboardColorInfluencer copy () {
			return new  BillboardColorInfluencer(this);
		}
	}
	
	public static class ModelInstanceColorInfluencer extends ColorInfluencer<ModelInstanceParticle>{
		public ModelInstanceColorInfluencer(){}
		
		public ModelInstanceColorInfluencer(ModelInstanceColorInfluencer billboardColorInfluencer){
			super(billboardColorInfluencer);
		}

		@Override
		public void initParticles (int startIndex, int count) {
			for(int i=startIndex, c = startIndex +count; i < c; ++i){
				ModelInstanceParticle particle = controller.particles[i];
				particle.color = ((ColorAttribute)particle.instance.materials.get(0).get(ColorAttribute.Diffuse)).color;
				float[] temp = colorValue.getColor(0);
				particle.color.r = temp[0];
				particle.color.g = temp[1];
				particle.color.b = temp[2];
				particle.alphaStart = alphaValue.newLowValue();
				particle.alphaDiff = alphaValue.newHighValue() - particle.alphaStart;
				particle.color.a = particle.alphaStart + particle.alphaDiff * alphaValue.getScale(particle.lifePercent);
			}
		}

		@Override
		public void update () {
			for(int i=0, c = controller.emitter.activeCount; i < c; ++i){
				ModelInstanceParticle particle = controller.particles[i];
				float[] temp = colorValue.getColor(particle.lifePercent);
				particle.color.r = temp[0];
				particle.color.g = temp[1];
				particle.color.b = temp[2];
				particle.color.a = particle.alphaStart + particle.alphaDiff * alphaValue.getScale(particle.lifePercent);
			}
		}
		@Override
		public ModelInstanceColorInfluencer copy () {
			return new  ModelInstanceColorInfluencer(this);
		}
	}
	
	public static class PointColorInfluencer extends ColorInfluencer<PointParticle>{
		public PointColorInfluencer(){}
		
		public PointColorInfluencer(PointColorInfluencer billboardColorInfluencer){
			super(billboardColorInfluencer);
		}

		@Override
		public void initParticles (int startIndex, int count) {
			for(int i=startIndex, c = startIndex +count; i < c; ++i){
				PointParticle particle = controller.particles[i];
				float[] temp = colorValue.getColor(0);
				particle.r = temp[0];
				particle.g = temp[1];
				particle.b = temp[2];
				particle.alphaStart = alphaValue.newLowValue();
				particle.alphaDiff = alphaValue.newHighValue() - particle.alphaStart;
				particle.a = particle.alphaStart + particle.alphaDiff * alphaValue.getScale(particle.lifePercent);
			}
		}

		@Override
		public void update () {
			for(int i=0, c = controller.emitter.activeCount; i < c; ++i){
				PointParticle particle = controller.particles[i];
				float[] temp = colorValue.getColor(particle.lifePercent);
				particle.r = temp[0];
				particle.g = temp[1];
				particle.b = temp[2];
				particle.a = particle.alphaStart + particle.alphaDiff * alphaValue.getScale(particle.lifePercent);
			}
		}
		@Override
		public PointColorInfluencer copy () {
			return new  PointColorInfluencer(this);
		}
	}
	
	
	public ScaledNumericValue alphaValue;
	public GradientColorValue colorValue;
	
	public ColorInfluencer(){
		colorValue = new GradientColorValue();
		alphaValue = new ScaledNumericValue();
	}

	public ColorInfluencer (ColorInfluencer billboardColorInfluencer) {
		this();
		set(billboardColorInfluencer);
	}

	public void set(ColorInfluencer colorInfluencer){
		this.colorValue.load(colorInfluencer.colorValue);
		this.alphaValue.load(colorInfluencer.alphaValue);
	}
}
