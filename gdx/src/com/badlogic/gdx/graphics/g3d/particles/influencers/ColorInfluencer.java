package com.badlogic.gdx.graphics.g3d.particles.influencers;

import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.particles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.particles.ModelInstanceParticle;
import com.badlogic.gdx.graphics.g3d.particles.PointSpriteParticle;
import com.badlogic.gdx.graphics.g3d.particles.values.GradientColorValue;
import com.badlogic.gdx.graphics.g3d.particles.values.ScaledNumericValue;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/** It's an {@link Influencer} which controls particles color and transparency. */
/** @author Inferno */
public abstract class ColorInfluencer<T> extends Influencer<T> {
	public static class BillboardColorInfluencer extends ColorInfluencer<BillboardParticle>{
		public BillboardColorInfluencer(){}
		
		public BillboardColorInfluencer(BillboardColorInfluencer billboardColorInfluencer){
			super(billboardColorInfluencer);
		}

		@Override
		public void activateParticles (int startIndex, int count) {
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
		public void activateParticles (int startIndex, int count) {
			for(int i=startIndex, c = startIndex +count; i < c; ++i){
				ModelInstanceParticle particle = controller.particles[i];
				particle.color = ((ColorAttribute)particle.instance.materials.get(0).get(ColorAttribute.Diffuse)).color;
				particle.blending = ((BlendingAttribute)particle.instance.materials.get(0).get(BlendingAttribute.Type));
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
				if(particle.blending != null)
					particle.blending.opacity = particle.alphaStart + particle.alphaDiff * alphaValue.getScale(particle.lifePercent);
			}
		}
		@Override
		public ModelInstanceColorInfluencer copy () {
			return new  ModelInstanceColorInfluencer(this);
		}
	}
	
	public static class PointSpriteColorInfluencer extends ColorInfluencer<PointSpriteParticle>{
		public PointSpriteColorInfluencer(){}
		
		public PointSpriteColorInfluencer(PointSpriteColorInfluencer billboardColorInfluencer){
			super(billboardColorInfluencer);
		}

		@Override
		public void activateParticles (int startIndex, int count) {
			for(int i=startIndex, c = startIndex +count; i < c; ++i){
				PointSpriteParticle particle = controller.particles[i];
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
				PointSpriteParticle particle = controller.particles[i];
				float[] temp = colorValue.getColor(particle.lifePercent);
				particle.r = temp[0];
				particle.g = temp[1];
				particle.b = temp[2];
				particle.a = particle.alphaStart + particle.alphaDiff * alphaValue.getScale(particle.lifePercent);
			}
		}
		@Override
		public PointSpriteColorInfluencer copy () {
			return new  PointSpriteColorInfluencer(this);
		}
	}
	
	
	public ScaledNumericValue alphaValue;
	public GradientColorValue colorValue;
	
	public ColorInfluencer(){
		colorValue = new GradientColorValue();
		alphaValue = new ScaledNumericValue();
		alphaValue.setHigh(1);
	}

	public ColorInfluencer (ColorInfluencer billboardColorInfluencer) {
		this();
		set(billboardColorInfluencer);
	}

	public void set(ColorInfluencer colorInfluencer){
		this.colorValue.load(colorInfluencer.colorValue);
		this.alphaValue.load(colorInfluencer.alphaValue);
	}
	
	@Override
	public void write (Json json) {
		json.writeValue("alpha", alphaValue);
		json.writeValue("color", colorValue);
	}
	
	@Override
	public void read (Json json, JsonValue jsonData) {
		alphaValue = json.readValue("alpha", ScaledNumericValue.class, jsonData);
		colorValue = json.readValue("color", GradientColorValue.class, jsonData);
	}
}
