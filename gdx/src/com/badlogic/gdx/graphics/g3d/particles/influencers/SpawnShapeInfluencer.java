package com.badlogic.gdx.graphics.g3d.particles.influencers;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.particles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.particles.ModelInstanceParticle;
import com.badlogic.gdx.graphics.g3d.particles.ParticleControllerParticle;
import com.badlogic.gdx.graphics.g3d.particles.ParticleControllerComponent;
import com.badlogic.gdx.graphics.g3d.particles.PointSpriteParticle;
import com.badlogic.gdx.graphics.g3d.particles.ResourceData;
import com.badlogic.gdx.graphics.g3d.particles.values.PointSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.particles.values.SpawnShapeValue;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/** It's an {@link Influencer} which controls where the particles will be spawned in the space when activated.*/
/** @author Inferno */
public abstract class SpawnShapeInfluencer<T> extends Influencer<T> {

	public static class BillboardSpawnInfluencer extends SpawnShapeInfluencer<BillboardParticle> {
			public BillboardSpawnInfluencer () {}
			public BillboardSpawnInfluencer (BillboardSpawnInfluencer source) {
				super(source);
			}
		
			public BillboardSpawnInfluencer (SpawnShapeValue spawnShapeValue) {
				super(spawnShapeValue);
			}
			
			@Override
			public void activateParticles (int startIndex, int count) {
				for(int i=startIndex, c = startIndex +count; i < c; ++i){
					BillboardParticle particle = controller.particles[i];
					spawnShapeValue.spawn(TMP_V1, controller.emitter.percent);
					TMP_V1.mul(controller.transform);
					particle.x = TMP_V1.x;
					particle.y = TMP_V1.y;
					particle.z = TMP_V1.z;
				}
			}
			
			@Override
			public ParticleControllerComponent copy () {
				return new BillboardSpawnInfluencer(this);
			}
	}
	
	public static class ModelInstanceSpawnInfluencer extends SpawnShapeInfluencer<ModelInstanceParticle> {
		public ModelInstanceSpawnInfluencer () {}
		public ModelInstanceSpawnInfluencer (ModelInstanceSpawnInfluencer source) {
			super(source);
		}

		public ModelInstanceSpawnInfluencer (SpawnShapeValue spawnShapeValue) {
			super(spawnShapeValue);
		}

		@Override
		public void activateParticles (int startIndex, int count) {
			for(int i=startIndex, c = startIndex +count; i < c; ++i){
				ModelInstanceParticle particle = controller.particles[i];
				spawnShapeValue.spawn(TMP_V1, controller.emitter.percent);
				TMP_V1.mul(controller.transform);
				particle.instance.transform.setTranslation(TMP_V1);
			}
		}

		@Override
		public ParticleControllerComponent copy () {
			return new ModelInstanceSpawnInfluencer(this);
		}
	}

	
	public static class ParticleControllerSpawnInfluencer extends SpawnShapeInfluencer<ParticleControllerParticle> {
		public ParticleControllerSpawnInfluencer () {}
		public ParticleControllerSpawnInfluencer (ParticleControllerSpawnInfluencer source) {
			super(source);
		}

		public ParticleControllerSpawnInfluencer (SpawnShapeValue spawnShapeValue) {
			super(spawnShapeValue);
		}

		@Override
		public void activateParticles (int startIndex, int count) {
			for(int i=startIndex, c = startIndex +count; i < c; ++i){
				ParticleControllerParticle particle = controller.particles[i];
				spawnShapeValue.spawn(TMP_V1, controller.emitter.percent);
				TMP_V1.mul(controller.transform);
				particle.controller.setTranslation(TMP_V1);
			}
		}

		@Override
		public ParticleControllerComponent copy () {
			return new ParticleControllerSpawnInfluencer(this);
		}
	}

	public static class PointSpriteSpawnInfluencer extends SpawnShapeInfluencer<PointSpriteParticle> {
		public PointSpriteSpawnInfluencer () {}
		public PointSpriteSpawnInfluencer (PointSpriteSpawnInfluencer source) {
			super(source);
		}

		public PointSpriteSpawnInfluencer (SpawnShapeValue spawnShapeValue) {
			super(spawnShapeValue);
		}

		@Override
		public void activateParticles (int startIndex, int count) {
			for(int i=startIndex, c = startIndex +count; i < c; ++i){
				PointSpriteParticle particle = controller.particles[i];
				spawnShapeValue.spawn(TMP_V1, controller.emitter.percent);
				TMP_V1.mul(controller.transform);
				particle.x = TMP_V1.x;
				particle.y = TMP_V1.y;
				particle.z = TMP_V1.z;
			}
		}

		@Override
		public ParticleControllerComponent copy () {
			return new PointSpriteSpawnInfluencer(this);
		}
	}

	public SpawnShapeValue spawnShapeValue;
	
	public SpawnShapeInfluencer(){
		spawnShapeValue = new PointSpawnShapeValue();
	}
	
	public SpawnShapeInfluencer(SpawnShapeValue spawnShapeValue){
		this.spawnShapeValue = spawnShapeValue;
	}
	
	public SpawnShapeInfluencer(SpawnShapeInfluencer source){
		spawnShapeValue = source.spawnShapeValue.copy();
	}
	
	@Override
	public void init () {
		spawnShapeValue.init();
	}
	
	@Override
	public void start () {
		spawnShapeValue.start();
	}

	@Override
	public void write (Json json) {
		json.writeValue("spawnShape", spawnShapeValue, SpawnShapeValue.class);
	}
	
	@Override
	public void read (Json json, JsonValue jsonData) {
		spawnShapeValue = json.readValue("spawnShape", SpawnShapeValue.class, jsonData);
	}
	
	@Override
	public void save (AssetManager manager, ResourceData data) {
		spawnShapeValue.save(manager, data);
	}
	
	@Override
	public void load (AssetManager manager, ResourceData data) {
		spawnShapeValue.load(manager, data);
	}
}
