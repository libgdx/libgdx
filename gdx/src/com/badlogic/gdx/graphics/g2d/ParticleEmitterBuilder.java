/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

/**
 * I thought about using an enumeration for all the scaled values and so on but in the end since there 
 * are different types of attributes i didn't to reduce the complexity and improve the simplicity. 
 * I also dont want to have 2/3 Enums for the different attribute types so i just hacked all down.<br><br>
 * for setting just one High value enter for low and high the same value.
 */

package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.graphics.g2d.ParticleEmitter.ScaledNumericValue;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter.SpawnShape;

/** This class should be used to build and manipulate an {@link ParticleEmitter} on runtime. It allows chaining of setting for the
 * emitter */
public class ParticleEmitterBuilder {
	private ParticleEmitter emitter;

	public ParticleEmitterBuilder (ParticleEmitter emitter) {
		this.emitter = emitter;
	}

	public ParticleEmitter getEmitter () {
		return this.emitter;
	}

	/** Set and return the old emitter
	 * @param emitter
	 * @return this for chaining the old emitter */
	public ParticleEmitter setEmitter (ParticleEmitter emitter) {
		ParticleEmitter temp = this.emitter;
		this.emitter = emitter;
		return temp;
	}

	// ####################### Shape #################
	// not sure there could be setSide too
	/** Set the shape of the emitter
	 * @param shape
	 * @return this for chaining */
	public ParticleEmitterBuilder setShape (SpawnShape shape) {
		emitter.getSpawnShape().setShape(shape);
		return this;
	}

	// ####################### Color #################
	/** Set the color/colors of the emitter
	 * @param color one color is 3 floats between[0,1]
	 * @param timeline for 3 color floats 1 timeline float between [0,1]
	 * @return this for chaining */
	public ParticleEmitterBuilder setColor (float[] color, float[] timeline) {

		if (color.length != timeline.length * 3) {
			throw new IllegalArgumentException("colors array does not match to the timline array.");
		}
		emitter.getTint().setColors(color);
		emitter.getTint().setTimeline(timeline);
		return this;
	}

	// ####################### xOffsetValue #################
	/** set the X offset of the emitter. For Same value set min = max;
	 * @param min
	 * @param max
	 * @return this for chaining */
	public ParticleEmitterBuilder setXOffset (float min, float max) {
		emitter.getXOffsetValue().setLow(min, max);
		return this;
	}

	// ####################### yOffsetValue #################
	/** set the Y offset of the emitter. For Same value set min = max;
	 * @param min
	 * @param max
	 * @return this for chaining */
	public ParticleEmitterBuilder setYOffset (float min, float max) {
		emitter.getYOffsetValue().setLow(min, max);
		return this;
	}

	// ####################### duration #################
	/** set the duration of the emitter. For Same value set min = max;
	 * @param min
	 * @param max
	 * @return this for chaining */
	public ParticleEmitterBuilder setDuration (float min, float max) {
		emitter.getDuration().setLow(min, max);
		return this;
	}

	// ####################### delay #################
	/** set the delay of the emitter. For Same value set min = max;
	 * @param min
	 * @param max
	 * @return this for chaining */
	public ParticleEmitterBuilder setDelay (float min, float max) {
		emitter.getDelay().setLow(min, max);
		return this;
	}

	// ####################### LifeOffset #################
	/** set the life offset low value of the emitter. For Same value set min = max;
	 * @param min
	 * @param max
	 * @return this for chaining */
	public ParticleEmitterBuilder setLifeOffsetLow (float min, float max) {
		emitter.getLifeOffset().setLow(min, max);
		return this;
	}

	/** set the life offset high value of the emitter. For Same value set min = max;
	 * @param min
	 * @param max
	 * @return this for chaining */
	public ParticleEmitterBuilder setLifeOffsetHigh (float min, float max) {
		emitter.getLifeOffset().setLow(min, max);
		return this;
	}

	/** set the timeline for the life offset value of the emitter. Every float value should be between [0,1]
	 * @param timeline
	 * @return this for chaining */
	public ParticleEmitterBuilder setLifeOffsetTimeline (float[] timeline) {
		emitter.getLifeOffset().setTimeline(timeline);
		return this;
	}

	/** Set the timeline inclusive the scaling for life offset of the emitter. Every float value should be between [0,1]
	 * @param timeline
	 * @param scaling
	 * @return this for chaining */
	public ParticleEmitterBuilder setLifeOffsetTimeline (float[] timeline, float[] scaling) {
		this.setTimeline(emitter.getLifeOffset(), timeline, scaling);
		return this;
	}

	/** Set the scaling for the lifeOffset. Every float value should be between [0,1]
	 * @param scaling
	 * @return this for chaining */
	public ParticleEmitterBuilder setLifeOffsetScaling (float[] scaling) {
		emitter.getLifeOffset().setScaling(scaling);
		return this;
	}

	/** Set the life offset height of the emitter in one step.
	 * @param scaling the scaling for the timeline
	 * @param timeline the timline values
	 * @param lowMin the low min
	 * @param lowMax the low max
	 * @param highMin the high min
	 * @param highMax the high max
	 * @return this for chaining */
	public ParticleEmitterBuilder lifeOffset (float[] scaling, float[] timeline, float lowMin, float lowMax, float highMin,
		float highMax) {
		scaledNumericValue(emitter.getLifeOffset(), scaling, timeline, lowMin, lowMax, highMin, highMax);
		return this;
	}

	// ####################### SpawnHeight #################
	/** set the height high value of the emitter. For Same value set min = max;
	 * @param min
	 * @param max
	 * @return this for chaining */
	public ParticleEmitterBuilder SpawnHeightHigh (float min, float max) {
		emitter.getSpawnHeight().setLow(min, max);
		return this;
	}

	/** set the height low value of the emitter. For Same value set min = max;
	 * @param min
	 * @param max
	 * @return this for chaining */
	public ParticleEmitterBuilder setSpawnHeightHighLow (float min, float max) {
		emitter.getSpawnHeight().setLow(min, max);
		return this;
	}

	/** set the timeline for the spawn height value of the emitter. Every float value should be between [0,1]
	 * @param timeline
	 * @return this for chaining */
	public ParticleEmitterBuilder setSpawnHeightTimeline (float[] timeline) {
		emitter.getSpawnHeight().setTimeline(timeline);
		return this;
	}

	/** Set the timeline inclusive the scaling for the spawn height of the emitter. Every float value should be between [0,1]
	 * @param timeline
	 * @param scaling
	 * @return this for chaining */
	public ParticleEmitterBuilder setSpawnHeightTimeline (float[] timeline, float[] scaling) {
		this.setTimeline(emitter.getSpawnHeight(), timeline, scaling);
		return this;
	}

	/** Set the spawn height of the emitter in one step.
	 * @param scaling the scaling for the timeline
	 * @param timeline the timline values
	 * @param lowMin the low min
	 * @param lowMax the low max
	 * @param highMin the high min
	 * @param highMax the high max
	 * @return this for chaining */
	public ParticleEmitterBuilder spawnHeight (float[] scaling, float[] timeline, float lowMin, float lowMax, float highMin,
		float highMax) {
		scaledNumericValue(emitter.getSpawnHeight(), scaling, timeline, lowMin, lowMax, highMin, highMax);
		return this;
	}

	// ####################### SpawnWidth #################
	/** set the spawn width low value of the emitter. For Same value set min = max;
	 * @param min
	 * @param max
	 * @return this for chaining */
	public ParticleEmitterBuilder setSpawnWidthLow (float min, float max) {
		emitter.getSpawnWidth().setLow(min, max);
		return this;
	}

	/** set the spawn width high value of the emitter. For Same value set min = max;
	 * @param min
	 * @param max
	 * @return this for chaining */
	public ParticleEmitterBuilder setSpawnWidthHigh (float min, float max) {
		emitter.getSpawnWidth().setLow(min, max);
		return this;
	}

	/** set the timeline for the spawn width value of the emitter. Every float value should be between [0,1]
	 * @param timeline
	 * @return this for chaining */
	public ParticleEmitterBuilder setSpawnWidthTimeline (float[] timeline) {
		emitter.getSpawnWidth().setTimeline(timeline);
		return this;
	}

	/** Set the timeline inclusive the scaling for the spawn width of the emitter. Every float value should be between [0,1]
	 * @param timeline
	 * @param scaling
	 * @return this for chaining */
	public ParticleEmitterBuilder setSpawnWidthTimeline (float[] timeline, float[] scaling) {
		this.setTimeline(emitter.getSpawnWidth(), timeline, scaling);
		return this;
	}

	/** Set the scaling for the spawn width of the emitter. Every float value should be between [0,1]
	 * @param scaling
	 * @return this for chaining */
	public ParticleEmitterBuilder setSpawnWidthScaling (float[] scaling) {
		emitter.getSpawnWidth().setScaling(scaling);
		return this;
	}

	/** Set the SpawnWidth of the emitter in one step.
	 * @param scaling the scaling for the timeline
	 * @param timeline the timline values
	 * @param lowMin the low min
	 * @param lowMax the low max
	 * @param highMin the high min
	 * @param highMax the high max
	 * @return this for chaining */
	public ParticleEmitterBuilder spawnWidth (float[] scaling, float[] timeline, float lowMin, float lowMax, float highMin,
		float highMax) {
		scaledNumericValue(emitter.getSpawnWidth(), scaling, timeline, lowMin, lowMax, highMin, highMax);
		return this;
	}

	// ####################### Wind #################
	/** set the wind low value of the emitter. For Same value set min = max;
	 * @param min
	 * @param max
	 * @return this for chaining */
	public ParticleEmitterBuilder setWindLow (float min, float max) {
		emitter.getWind().setLow(min, max);
		return this;
	}

	/** set the wind high value of the emitter. For Same value set min = max;
	 * @param min
	 * @param max
	 * @return this for chaining */
	public ParticleEmitterBuilder setWindHigh (float min, float max) {
		emitter.getEmission().setLow(min, max);
		return this;
	}

	/** set the timeline for the w value of the emitter. Every float value should be between [0,1]
	 * @param timeline
	 * @return this for chaining */
	public ParticleEmitterBuilder setWindTimeline (float[] timeline) {
		emitter.getWind().setTimeline(timeline);
		return this;
	}

	/** Set the timeline inclusive the wind for the spawn height of the emitter. Every float value should be between [0,1]
	 * @param timeline
	 * @param scaling
	 * @return this for chaining */
	public ParticleEmitterBuilder setWindTimeline (float[] timeline, float[] scaling) {
		this.setTimeline(emitter.getWind(), timeline, scaling);
		return this;
	}

	/** Set the scaling for the wind of the emitter. Every float value should be between [0,1]
	 * @param scaling
	 * @return this for chaining */
	public ParticleEmitterBuilder setWindScaling (float[] scaling) {
		emitter.getGravity().setScaling(scaling);
		return this;
	}

	/** Set the wind of the emitter in one step.
	 * @param scaling the scaling for the timeline
	 * @param timeline the timline values
	 * @param lowMin the low min
	 * @param lowMax the low max
	 * @param highMin the high min
	 * @param highMax the high max
	 * @return this for chaining */
	public ParticleEmitterBuilder wind (float[] scaling, float[] timeline, float lowMin, float lowMax, float highMin, float highMax) {
		scaledNumericValue(emitter.getWind(), scaling, timeline, lowMin, lowMax, highMin, highMax);
		return this;
	}

	// ####################### Angle #################

	/** set the Gravity low value of the emitter. For Same value set min = max;
	 * @param min
	 * @param max
	 * @return this for chaining */
	public ParticleEmitterBuilder setAngleLow (float min, float max) {
		emitter.getAngle().setLow(min, max);
		return this;
	}

	/** set the angle high value of the emitter. For Same value set min = max;
	 * @param min
	 * @param max
	 * @return this for chaining */
	public ParticleEmitterBuilder setAngleHigh (float min, float max) {
		emitter.getAngle().setLow(min, max);
		return this;
	}

	/** set the timeline for the angle value of the emitter. Every float value should be between [0,1]
	 * @param timeline
	 * @return this for chaining */
	public ParticleEmitterBuilder setAngleTimeline (float[] timeline) {
		emitter.getAngle().setTimeline(timeline);
		return this;
	}

	/** Set the timeline inclusive the angle for the spawn height of the emitter. Every float value should be between [0,1]
	 * @param timeline
	 * @param scaling
	 * @return this for chaining */
	public ParticleEmitterBuilder setAngleTimeline (float[] timeline, float[] scaling) {
		this.setTimeline(emitter.getAngle(), timeline, scaling);
		return this;
	}

	/** Set the scaling for the of the emitter. Every float value should be between [0,1]
	 * @param scaling
	 * @return this for chaining */
	public ParticleEmitterBuilder setAngleScaling (float[] scaling) {
		emitter.getAngle().setScaling(scaling);
		return this;
	}

	/** Set the ang of the emitter in one step.
	 * @param scaling the scaling for the timeline
	 * @param timeline the timline values
	 * @param lowMin the low min
	 * @param lowMax the low max
	 * @param highMin the high min
	 * @param highMax the high max
	 * @return this for chaining */
	public ParticleEmitterBuilder angle (float[] scaling, float[] timeline, float lowMin, float lowMax, float highMin,
		float highMax) {
		scaledNumericValue(emitter.getAngle(), scaling, timeline, lowMin, lowMax, highMin, highMax);
		return this;
	}

	// ####################### Scale #################

	/** set the Scale low value of the emitter. For Same value set min = max;
	 * @param min
	 * @param max
	 * @return this for chaining */
	public ParticleEmitterBuilder setScaleLow (float min, float max) {
		emitter.getScale().setLow(min, max);
		return this;
	}

	/** set the scale high value of the emitter. For Same value set min = max;
	 * @param min
	 * @param max
	 * @return this for chaining */
	public ParticleEmitterBuilder setScaleHigh (float min, float max) {
		emitter.getScale().setLow(min, max);
		return this;
	}

	/** set the timeline for the Scale value of the emitter. Every float value should be between [0,1]
	 * @param timeline
	 * @return this for chaining */
	public ParticleEmitterBuilder setScaleTimeline (float[] timeline) {
		emitter.getScale().setTimeline(timeline);
		return this;
	}

	/** Set the timeline inclusive the scaling for scale of the emitter. Every float value should be between [0,1]
	 * @param timeline
	 * @param scaling
	 * @return this for chaining */
	public ParticleEmitterBuilder setScaleTimeline (float[] timeline, float[] scaling) {
		this.setTimeline(emitter.getScale(), timeline, scaling);
		return this;
	}

	/** Set the scaling for the scale of the emitter. Every float value should be between [0,1]
	 * @param scaling
	 * @return this for chaining */
	public ParticleEmitterBuilder setScaleScaling (float[] scaling) {
		emitter.getScale().setScaling(scaling);
		return this;
	}

	/** Set the scale of the emitter in one step.
	 * @param scaling the scaling for the timeline
	 * @param timeline the timline values
	 * @param lowMin the low min
	 * @param lowMax the low max
	 * @param highMin the high min
	 * @param highMax the high max
	 * @return this for chaining */
	public ParticleEmitterBuilder scale (float[] scaling, float[] timeline, float lowMin, float lowMax, float highMin,
		float highMax) {
		scaledNumericValue(emitter.getEmission(), scaling, timeline, lowMin, lowMax, highMin, highMax);
		return this;
	}

	// ####################### Emission #################
	/** set the emission low value of the emitter. For Same value set min = max;
	 * @param min
	 * @param max
	 * @return this for chaining */
	public ParticleEmitterBuilder setEmissionLow (float min, float max) {
		emitter.getEmission().setLow(min, max);
		return this;
	}

	/** set the emission high value of the emitter. For Same value set min = max;
	 * @param min
	 * @param max
	 * @return this for chaining */
	public ParticleEmitterBuilder setEmissionHigh (float min, float max) {
		emitter.getEmission().setLow(min, max);
		return this;
	}

	/** set the timeline for the emission value of the emitter. Every float value should be between [0,1]
	 * @param timeline
	 * @return this for chaining */
	public ParticleEmitterBuilder setEmissionTimeline (float[] timeline) {
		emitter.getEmission().setTimeline(timeline);
		return this;
	}

	/** Set the timeline inclusive the scaling for the emission of the emitter. Every float value should be between [0,1]
	 * @param timeline
	 * @param scaling
	 * @return this for chaining */
	public ParticleEmitterBuilder setEmissionTimeline (float[] timeline, float[] scaling) {
		this.setTimeline(emitter.getEmission(), timeline, scaling);
		return this;
	}

	/** Set the scaling for the emission of the emitter. Every float value should be between [0,1]
	 * @param scaling
	 * @return this for chaining */
	public ParticleEmitterBuilder setEmissionScaling (float[] scaling) {
		emitter.getEmission().setScaling(scaling);
		return this;
	}

	/** Set the emission of the emitter in one step.
	 * @param scaling the scaling for the timeline
	 * @param timeline the timline values
	 * @param lowMin the low min
	 * @param lowMax the low max
	 * @param highMin the high min
	 * @param highMax the high max
	 * @return this for chaining */
	public ParticleEmitterBuilder emission (float[] scaling, float[] timeline, float lowMin, float lowMax, float highMin,
		float highMax) {
		scaledNumericValue(emitter.getEmission(), scaling, timeline, lowMin, lowMax, highMin, highMax);
		return this;
	}

	// ####################### LIFE #################
	/** set the lifetime low value of the emitter. For Same value set min = max;
	 * @param min
	 * @param max
	 * @return this for chaining */
	public ParticleEmitterBuilder setLifeLow (float min, float max) {
		emitter.getLife().setLow(min, max);
		return this;
	}

	/** set the lifetime high value of the emitter. For Same value set min = max;
	 * @param min
	 * @param max
	 * @return this for chaining */
	public ParticleEmitterBuilder setLifeHigh (float min, float max) {
		emitter.getLife().setLow(min, max);
		return this;
	}

	/** set the timeline for the life value of the emitter. Every float value should be between [0,1]
	 * @param timeline
	 * @return this for chaining */
	public ParticleEmitterBuilder setLifeTimeline (float[] timeline) {
		emitter.getLife().setTimeline(timeline);
		return this;
	}

	/** Set the timeline inclusive the scaling for the life of the emitter. Every float value should be between [0,1]
	 * @param timeline
	 * @param scaling
	 * @return this for chaining */
	public ParticleEmitterBuilder setLifeTimeline (float[] timeline, float[] scaling) {
		this.setTimeline(emitter.getLife(), timeline, scaling);
		return this;
	}

	/** Set the scaling for the life of the emitter. Every float value should be between [0,1]
	 * @param scaling
	 * @return this for chaining */
	public ParticleEmitterBuilder setLifeScaling (float[] scaling) {
		emitter.getLife().setScaling(scaling);
		return this;
	}

	/** Set the life of the emitter in one step.
	 * @param scaling the scaling for the timeline
	 * @param timeline the timline values
	 * @param lowMin the low min
	 * @param lowMax the low max
	 * @param highMin the high min
	 * @param highMax the high max
	 * @return this for chaining */
	public ParticleEmitterBuilder life (float[] scaling, float[] timeline, float lowMin, float lowMax, float highMin, float highMax) {
		scaledNumericValue(emitter.getLife(), scaling, timeline, lowMin, lowMax, highMin, highMax);
		return this;
	}

	// ####################### Transparency #################
	/** set the Transparency low value of the emitter. For Same value set min = max;
	 * @param min
	 * @param max
	 * @return this for chaining */
	public ParticleEmitterBuilder setTransparencyLow (float min, float max) {
		emitter.getTransparency().setLow(min, max);
		return this;
	}

	/** set the Transparency high value of the emitter. For Same value set min = max;
	 * @param min
	 * @param max
	 * @return this for chaining */
	public ParticleEmitterBuilder setTransparencyHigh (float min, float max) {
		emitter.getTransparency().setLow(min, max);
		return this;
	}

	/** set the timeline for the Transparency value of the emitter. Every float value should be between [0,1]
	 * @param timeline
	 * @return this for chaining */
	public ParticleEmitterBuilder setTransparencyTimeline (float[] timeline) {
		emitter.getTransparency().setTimeline(timeline);
		return this;
	}

	/** Set the timeline inclusive the scaling for the transparency of the emitter. Every float value should be between [0,1]
	 * @param timeline
	 * @param scaling
	 * @return this for chaining */
	public ParticleEmitterBuilder setTransparencyTimeline (float[] timeline, float[] scaling) {
		this.setTimeline(emitter.getTransparency(), timeline, scaling);
		return this;
	}

	/** Set the scaling for the transparency of the emitter. Every float value should be between [0,1]
	 * @param scaling
	 * @return this for chaining */
	public ParticleEmitterBuilder setTransparencyScaling (float[] scaling) {
		emitter.getTransparency().setScaling(scaling);
		return this;
	}

	/** Set the transparency of the emitter in one step.
	 * @param scaling the scaling for the timeline
	 * @param timeline the timline values
	 * @param lowMin the low min
	 * @param lowMax the low max
	 * @param highMin the high min
	 * @param highMax the high max
	 * @return this for chaining */
	public ParticleEmitterBuilder transparency (float[] scaling, float[] timeline, float lowMin, float lowMax, float highMin,
		float highMax) {
		scaledNumericValue(emitter.getTransparency(), scaling, timeline, lowMin, lowMax, highMin, highMax);
		return this;
	}

	// ####################### gravity #################
	/** set the gravity low value of the emitter. For Same value set min = max;
	 * @param min
	 * @param max
	 * @return this for chaining */
	public ParticleEmitterBuilder setGravityLow (float min, float max) {
		emitter.getGravity().setLow(min, max);
		return this;
	}

	/** set the gravity high value of the emitter. For Same value set min = max;
	 * @param min
	 * @param max
	 * @return this for chaining */
	public ParticleEmitterBuilder setGravityHigh (float min, float max) {
		emitter.getGravity().setLow(min, max);
		return this;
	}

	/** set the timeline for the gravity value of the emitter. Every float value should be between [0,1]
	 * @param timeline
	 * @return this for chaining */
	public ParticleEmitterBuilder setGravityTimeline (float[] timeline) {
		emitter.getGravity().setTimeline(timeline);
		return this;
	}

	/** Set the timeline inclusive the scaling for the gravity of the emitter. Every float value should be between [0,1]
	 * @param timeline
	 * @param scaling
	 * @return this for chaining */
	public ParticleEmitterBuilder setGravityTimeline (float[] timeline, float[] scaling) {
		this.setTimeline(emitter.getGravity(), timeline, scaling);
		return this;
	}

	/** Set the scaling for the gravity of the emitter. Every float value should be between [0,1]
	 * @param scaling
	 * @return this for chaining */
	public ParticleEmitterBuilder setGravityScaling (float[] scaling) {
		emitter.getGravity().setScaling(scaling);
		return this;
	}

	/** Set the gravity of the emitter in one step.
	 * @param scaling the scaling for the timeline
	 * @param timeline the timline values
	 * @param lowMin the low min
	 * @param lowMax the low max
	 * @param highMin the high min
	 * @param highMax the high max
	 * @return this for chaining */
	public ParticleEmitterBuilder gravity (float[] scaling, float[] timeline, float lowMin, float lowMax, float highMin,
		float highMax) {
		scaledNumericValue(emitter.getGravity(), scaling, timeline, lowMin, lowMax, highMin, highMax);
		return this;
	}

	/** @param timeline
	 * @param scaling */
	private void setTimeline (ScaledNumericValue value, float[] timeline, float[] scaling) {
		value.setTimeline(timeline);
		value.setScaling(scaling);
	}

	/** Set a compleat {@link ScaledNumericValue}
	 * @param value the ScaledNumericValue
	 * @param scaling
	 * @param timeline
	 * @param lowMin
	 * @param lowMax
	 * @param highMin
	 * @param highMax */
	private void scaledNumericValue (ScaledNumericValue value, float[] scaling, float[] timeline, float lowMin, float lowMax,
		float highMin, float highMax) {
		value.setScaling(scaling);
		value.setTimeline(timeline);
		value.setLow(lowMin, lowMax);
		value.setHigh(highMin, highMax);
	}
}
