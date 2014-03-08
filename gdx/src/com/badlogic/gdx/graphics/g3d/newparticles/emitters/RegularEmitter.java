package com.badlogic.gdx.graphics.g3d.newparticles.emitters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.newparticles.Particle;
import com.badlogic.gdx.graphics.g3d.newparticles.ParticleController;
import com.badlogic.gdx.graphics.g3d.newparticles.ParticleSystem;
import com.badlogic.gdx.graphics.g3d.newparticles.values.RangedNumericValue;
import com.badlogic.gdx.graphics.g3d.newparticles.values.ScaledNumericValue;

public class RegularEmitter<T extends Particle> extends Emitter<T> {
	public RangedNumericValue delayValue, durationValue;
	public ScaledNumericValue 	lifeOffsetValue,
								lifeValue, 
								emissionValue;
	protected int emission, emissionDiff, emissionDelta;
	protected int lifeOffset, lifeOffsetDiff;
	protected int life, lifeDiff;
	protected float duration = 1, delay, durationTimer, delayTimer;
	private boolean continuous;

	public RegularEmitter(){
		delayValue = new RangedNumericValue(); 
		durationValue = new RangedNumericValue();
		lifeOffsetValue = new ScaledNumericValue();
		lifeValue = new ScaledNumericValue();
		emissionValue = new ScaledNumericValue();
		
		durationValue.setActive(true);
		emissionValue.setActive(true);
		lifeValue.setActive(true);
		continuous = true;
	}
	
	public RegularEmitter (RegularEmitter regularEmitter) {
		this();
		set(regularEmitter);
	}

	@Override
	public void start () {
		delay = delayValue.active ? delayValue.newLowValue() : 0;
		delayTimer = 0;

		durationTimer -= duration;
		duration = durationValue.newLowValue();

		emission = (int)emissionValue.newLowValue();
		emissionDiff = (int)emissionValue.newHighValue();
		if (!emissionValue.isRelative()) emissionDiff -= emission;

		life = (int)lifeValue.newLowValue();
		lifeDiff = (int)lifeValue.newHighValue();
		if (!lifeValue.isRelative()) lifeDiff -= life;

		lifeOffset = lifeOffsetValue.active ? (int)lifeOffsetValue.newLowValue() : 0;
		lifeOffsetDiff = (int)lifeOffsetValue.newHighValue();
		if (!lifeOffsetValue.isRelative()) lifeOffsetDiff -= lifeOffset;
	}
	
	public void init(){
		super.init();
		emissionDelta = 0;
		durationTimer = duration;
	}
	
	public void initParticles (int startIndex, int count){
		int 	currentTotaLife = life + (int)(lifeDiff * lifeValue.getScale(percent)),
				currentLife = currentTotaLife;
		int offsetTime = (int)(lifeOffset + lifeOffsetDiff * lifeOffsetValue.getScale(percent));
		if (offsetTime > 0) {
			if (offsetTime >= currentLife) 
				offsetTime = currentLife - 1;
			currentLife -= offsetTime;
		}
		float lifePercent = 1 - currentLife / (float)currentTotaLife;
		
		for(int i=startIndex, c = startIndex +count; i < c; ++i){
			Particle particle = controller.particles[i];
			particle.currentLife = currentLife;
			particle.life = currentTotaLife;
			particle.lifePercent = lifePercent;
		}
	}
	
	public void update () {
		int deltaMillis = (int)(controller.deltaTime * 1000);
		
		if (delayTimer < delay) {
			delayTimer += deltaMillis;
		} else {
			boolean done = false;
			//End check
			if (durationTimer < duration) {
				durationTimer += deltaMillis;
				percent = durationTimer / (float)duration;
			}
			else {
				if (continuous) 
					controller.start();
				else 
					done = true;
			}
			
			if(!done) {
				//Emit particles
				emissionDelta += deltaMillis;
				float emissionTime = emission + emissionDiff * emissionValue.getScale(percent);
				if (emissionTime > 0) {
					emissionTime = 1000 / emissionTime;
					if (emissionDelta >= emissionTime) {
						int emitCount = (int)(emissionDelta / emissionTime);
						emitCount = Math.min(emitCount, maxParticleCount - activeCount);
						emissionDelta -= emitCount * emissionTime;
						emissionDelta %= emissionTime;
						addParticles(emitCount);
					}
				}
				if (activeCount < minParticleCount)
					addParticles(minParticleCount - activeCount);
			}
			
			//Update particles
			int activeCount = this.activeCount;
			Particle[] particles = controller.particles;
			for (int i = 0; i < activeCount;) {
				Particle particle = particles[i];
				particle.currentLife -= deltaMillis;
				if (particle.currentLife <= 0) {
					//swap the particle
					int lastIndex = activeCount-1;
					if(i != lastIndex){
						particles[i] = particles[lastIndex];
						particles[lastIndex] = particle;
					}
					--activeCount;
					continue;
				}
				else {
					particle.lifePercent = 1 - particle.currentLife / (float)particle.life;
				}
				++i;
			}
			if(activeCount < this.activeCount){
				controller.killParticles(activeCount, this.activeCount - activeCount);
				this.activeCount = activeCount;
			}
		}
	}
	
	private void addParticles (int count) {
		count = Math.min(count, maxParticleCount - activeCount);
		if (count == 0) return;
		controller.initParticles (activeCount, count);
		activeCount += count;
	}
	
	public ScaledNumericValue getLife () {
		return lifeValue;
	}

	public ScaledNumericValue getEmission () {
		return emissionValue;
	}

	public RangedNumericValue getDuration () {
		return durationValue;
	}

	public RangedNumericValue getDelay () {
		return delayValue;
	}

	public ScaledNumericValue getLifeOffset () {
		return lifeOffsetValue;
	}

	public boolean isContinuous () {
		return continuous;
	}

	public void setContinuous (boolean continuous) {
		this.continuous = continuous;
	}
	
	public boolean isComplete () {
		if (delayTimer < delay) return false;
		return durationTimer >= duration && activeCount == 0;
	}

	public float getPercentComplete () {
		if (delayTimer < delay) return 0;
		return Math.min(1, durationTimer / (float)duration);
	}

	public void set (RegularEmitter emitter) {
		super.set(emitter);
		delayValue.load(emitter.delayValue); 
		durationValue.load(emitter.durationValue);
		lifeOffsetValue.load(emitter.lifeOffsetValue);
		lifeValue.load(emitter.lifeValue); 
		emissionValue.load(emitter.emissionValue);
		emission = emitter.emission;
		emissionDiff = emitter.emissionDiff; 
		emissionDelta = emitter.emissionDelta;
		lifeOffset = emitter.lifeOffset; 
		lifeOffsetDiff = emitter.lifeOffsetDiff;
		life = emitter.life; 
		lifeDiff = emitter.lifeDiff;
		duration = emitter.duration; 
		delay = emitter.delay; 
		durationTimer = emitter.durationTimer;
		delayTimer = emitter.delayTimer;
		continuous = emitter.continuous;
	}

	@Override
	public ParticleSystem copy () {
		return new RegularEmitter(this);
	}
	
}
