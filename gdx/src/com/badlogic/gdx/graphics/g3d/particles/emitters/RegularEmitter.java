package com.badlogic.gdx.graphics.g3d.particles.emitters;

import com.badlogic.gdx.graphics.g3d.particles.ParallelArray.FloatChannel;
import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels;
import com.badlogic.gdx.graphics.g3d.particles.ParticleControllerComponent;
import com.badlogic.gdx.graphics.g3d.particles.values.RangedNumericValue;
import com.badlogic.gdx.graphics.g3d.particles.values.ScaledNumericValue;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/** It's a generic use {@link Emitter} which fits most of the particles simulation scenarios. 
 * @author Inferno */
public class RegularEmitter extends Emitter implements Json.Serializable {
	
	/** Possible emission modes. Emission mode does not affect already emitted particles. */
	public enum EmissionMode {
		/** New particles can be emitted. */
		Enabled,
		/** Only valid for continuous emitters. 
		 * It will only emit particles until the end of the effect duration. 
		 * After that emission cycle will not be restarted.*/
		EnabledUntilCycleEnd,
		/** Prevents new particle emission.*/
		Disabled
	}
	
	public RangedNumericValue delayValue, durationValue;
	public ScaledNumericValue 	lifeOffsetValue,
								lifeValue, 
								emissionValue;
	protected int emission, emissionDiff, emissionDelta;
	protected int lifeOffset, lifeOffsetDiff;
	protected int life, lifeDiff;
	protected float duration, delay, durationTimer, delayTimer;
	private boolean continuous;
	private EmissionMode emissionMode;
	
	private FloatChannel lifeChannel;

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
		emissionMode = EmissionMode.Enabled;
	}
	
	public RegularEmitter (RegularEmitter regularEmitter) {
		this();
		set(regularEmitter);
	}
	
	@Override
	public void allocateChannels() {
		lifeChannel = controller.particles.addChannel(ParticleChannels.Life);
	}

	@Override
	public void start () {
		delay = delayValue.active ? delayValue.newLowValue() : 0;
		delayTimer = 0;
		durationTimer = 0f;
		
		duration = durationValue.newLowValue();
		percent = durationTimer / (float)duration;
		
		emission = (int)emissionValue.newLowValue();
		emissionDiff = (int)emissionValue.newHighValue();
		if (!emissionValue.isRelative()) 
			emissionDiff -= emission;

		life = (int)lifeValue.newLowValue();
		lifeDiff = (int)lifeValue.newHighValue();
		if (!lifeValue.isRelative()) 
			lifeDiff -= life;

		lifeOffset = lifeOffsetValue.active ? (int)lifeOffsetValue.newLowValue() : 0;
		lifeOffsetDiff = (int)lifeOffsetValue.newHighValue();
		if (!lifeOffsetValue.isRelative()) 
			lifeOffsetDiff -= lifeOffset;
	}
	
	public void init(){
		super.init();
		emissionDelta = 0; 
		durationTimer = duration;
	}
	
	public void activateParticles (int startIndex, int count){
		int 	currentTotaLife = life + (int)(lifeDiff * lifeValue.getScale(percent)),
				currentLife = currentTotaLife;
		int offsetTime = (int)(lifeOffset + lifeOffsetDiff * lifeOffsetValue.getScale(percent));
		if (offsetTime > 0) {
			if (offsetTime >= currentLife) 
				offsetTime = currentLife - 1;
			currentLife -= offsetTime;
		}
		float lifePercent = 1 - currentLife / (float)currentTotaLife;
		
		for(int i=startIndex*lifeChannel.strideSize, c = i +count*lifeChannel.strideSize; i < c;  i+=lifeChannel.strideSize){
			lifeChannel.data[i+ParticleChannels.CurrentLifeOffset] = currentLife;
			lifeChannel.data[i+ParticleChannels.TotalLifeOffset] = currentTotaLife;
			lifeChannel.data[i+ParticleChannels.LifePercentOffset] = lifePercent;
		}
	}
	
	public void update () {
		int deltaMillis = (int)(controller.deltaTime * 1000);
		
		if (delayTimer < delay) {
			delayTimer += deltaMillis;
		} else {
			boolean emit = emissionMode != EmissionMode.Disabled;
			//End check
			if (durationTimer < duration) {
				durationTimer += deltaMillis;
				percent = durationTimer / (float)duration;
			}
			else {
				if (continuous && emit && emissionMode == EmissionMode.Enabled) 
					controller.start();
				else 
					emit = false;
			}
			
			if(emit) {
				//Emit particles
				emissionDelta += deltaMillis;
				float emissionTime = emission + emissionDiff * emissionValue.getScale(percent);
				if (emissionTime > 0) {
					emissionTime = 1000 / emissionTime;
					if (emissionDelta >= emissionTime) {
						int emitCount = (int)(emissionDelta / emissionTime);
						emitCount = Math.min(emitCount, maxParticleCount - controller.particles.size);
						emissionDelta -= emitCount * emissionTime;
						emissionDelta %= emissionTime;
						addParticles(emitCount);
					}
				}
				if (controller.particles.size < minParticleCount)
					addParticles(minParticleCount - controller.particles.size);
			}
		}

		//Update particles
		int activeParticles = controller.particles.size;
		for (int i = 0, k=0; i < controller.particles.size; ) {
			if ( (lifeChannel.data[k+ParticleChannels.CurrentLifeOffset] -= deltaMillis) <= 0) {
				controller.particles.removeElement(i);
				continue;
			}
			else {
				lifeChannel.data[k+ParticleChannels.LifePercentOffset] = 1- lifeChannel.data[k+ParticleChannels.CurrentLifeOffset]/lifeChannel.data[k+ParticleChannels.TotalLifeOffset];  
			}
			++i;
			k+=lifeChannel.strideSize;
		}
		
		if(controller.particles.size < activeParticles){
			controller.killParticles(controller.particles.size, activeParticles - controller.particles.size);
		}
	}
	
	private void addParticles (int count) {
		count = Math.min(count, maxParticleCount - controller.particles.size);
		if (count <= 0) return;
		controller.activateParticles (controller.particles.size, count);
		controller.particles.size += count;
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
	
	/** Gets current emission mode.
	 * @return Current emission mode.*/
	public EmissionMode getEmissionMode(){
		return emissionMode;
	}
	
	/** Sets emission mode. Emission mode does not affect already emitted particles.
	 * @param emissionMode Emission mode to set.*/
	public void setEmissionMode(EmissionMode emissionMode){
		this.emissionMode = emissionMode;
	}
	
	public boolean isComplete () {
		if (delayTimer < delay) return false;
		return durationTimer >= duration && controller.particles.size == 0;
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
	public ParticleControllerComponent copy () {
		return new RegularEmitter(this);
	}

	@Override
	public void write (Json json) {
		super.write(json);
		json.writeValue("continous", continuous);
		json.writeValue("emission", emissionValue);
		json.writeValue("delay", delayValue);
		json.writeValue("duration", durationValue);
		json.writeValue("life", lifeValue);
		json.writeValue("lifeOffset", lifeOffsetValue);
	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		super.read(json, jsonData);
		continuous = json.readValue("continous", boolean.class, jsonData);
		emissionValue = json.readValue("emission", ScaledNumericValue.class, jsonData);
		delayValue = json.readValue("delay", RangedNumericValue.class, jsonData);
		durationValue = json.readValue("duration", RangedNumericValue.class, jsonData);
		lifeValue = json.readValue("life", ScaledNumericValue.class, jsonData);
		lifeOffsetValue = json.readValue("lifeOffset", ScaledNumericValue.class, jsonData);
	}	
}
