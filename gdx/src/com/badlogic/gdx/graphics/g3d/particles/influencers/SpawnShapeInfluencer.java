package com.badlogic.gdx.graphics.g3d.particles.influencers;

import com.badlogic.gdx.graphics.g3d.particles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.particles.ModelInstanceParticle;
import com.badlogic.gdx.graphics.g3d.particles.Particle;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.ParticleControllerParticle;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.graphics.g3d.particles.PointParticle;
import com.badlogic.gdx.graphics.g3d.particles.values.SpawnShapeValue;

public abstract class SpawnShapeInfluencer<T> extends Influencer<T> {

	public static class BillboardSpawnSource extends SpawnShapeInfluencer<BillboardParticle> {
			public BillboardSpawnSource () {}
			public BillboardSpawnSource (BillboardSpawnSource source) {
				super(source);
			}
		
			public BillboardSpawnSource (SpawnShapeValue spawnShapeValue) {
				super(spawnShapeValue);
			}
			
			@Override
			public void initParticles (int startIndex, int count) {
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
			public ParticleSystem copy () {
				return new BillboardSpawnSource(this);
			}
	}
	
	public static class ModelInstanceSpawnSource extends SpawnShapeInfluencer<ModelInstanceParticle> {
		public ModelInstanceSpawnSource () {}
		public ModelInstanceSpawnSource (ModelInstanceSpawnSource source) {
			super(source);
		}

		public ModelInstanceSpawnSource (SpawnShapeValue spawnShapeValue) {
			super(spawnShapeValue);
		}

		@Override
		public void initParticles (int startIndex, int count) {
			for(int i=startIndex, c = startIndex +count; i < c; ++i){
				ModelInstanceParticle particle = controller.particles[i];
				spawnShapeValue.spawn(TMP_V1, controller.emitter.percent);
				TMP_V1.mul(controller.transform);
				particle.instance.transform.setTranslation(TMP_V1);
			}
		}

		@Override
		public ParticleSystem copy () {
			return new ModelInstanceSpawnSource(this);
		}
	}

	
	public static class ParticleControllerSpawnSource extends SpawnShapeInfluencer<ParticleControllerParticle> {
		public ParticleControllerSpawnSource () {}
		public ParticleControllerSpawnSource (ParticleControllerSpawnSource source) {
			super(source);
		}

		public ParticleControllerSpawnSource (SpawnShapeValue spawnShapeValue) {
			super(spawnShapeValue);
		}

		@Override
		public void initParticles (int startIndex, int count) {
			for(int i=startIndex, c = startIndex +count; i < c; ++i){
				ParticleControllerParticle particle = controller.particles[i];
				spawnShapeValue.spawn(TMP_V1, controller.emitter.percent);
				TMP_V1.mul(controller.transform);
				particle.x = TMP_V1.x;
				particle.y = TMP_V1.y;
				particle.z = TMP_V1.z;
			}
		}

		@Override
		public ParticleSystem copy () {
			return new ParticleControllerSpawnSource(this);
		}
	}

	public static class PointSpawnSource extends SpawnShapeInfluencer<PointParticle> {
		public PointSpawnSource () {}
		public PointSpawnSource (PointSpawnSource source) {
			super(source);
		}

		public PointSpawnSource (SpawnShapeValue spawnShapeValue) {
			super(spawnShapeValue);
		}

		@Override
		public void initParticles (int startIndex, int count) {
			for(int i=startIndex, c = startIndex +count; i < c; ++i){
				PointParticle particle = controller.particles[i];
				spawnShapeValue.spawn(TMP_V1, controller.emitter.percent);
				TMP_V1.mul(controller.transform);
				particle.x = TMP_V1.x;
				particle.y = TMP_V1.y;
				particle.z = TMP_V1.z;
			}
		}

		@Override
		public ParticleSystem copy () {
			return new PointSpawnSource(this);
		}
	}



	public SpawnShapeValue spawnShapeValue;
	
	public SpawnShapeInfluencer(){}
	
	public SpawnShapeInfluencer(SpawnShapeValue spawnShapeValue){
		this.spawnShapeValue = spawnShapeValue;
	}
	
	public SpawnShapeInfluencer(SpawnShapeInfluencer source){
		spawnShapeValue = source.spawnShapeValue.copy();
	}
	
	@Override
	public void start () {
		spawnShapeValue.start();
	}
	
	/*
	@Override
	public void restart () {
		spawnShapeValue.start();
	}
	*/
}
