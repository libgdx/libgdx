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

package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.graphics.g2d.ParticleEmitter.ScaledNumericValue;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter.SpawnShape;

/** This class should be used to build and manipulate an {@link ParticleEmitter} on runtime. It allows chaining of setting for the
 * emitter */
public class ParticleEmitterBuilder {
	private ParticleEmitter emitter;

	public ParticleEmitterBuilder () {
		this.emitter = new ParticleEmitter();
	}

	public ParticleEmitterBuilder (ParticleEmitter emitter) {
		this.emitter = emitter;
	}

	/** @return the current build emitter */
	public ParticleEmitter create () {
		return this.emitter;
	}

	/** start building a new emitter */
	public void newEmitter () {
		this.emitter = new ParticleEmitter();
	}

	/** Method to set the emitter for using one instance of ParticleEmitterBuilder.
	 * @param emitter */
	public ParticleEmitter setEmitter (ParticleEmitter emitter) {
		ParticleEmitter temp = this.emitter;
		this.emitter = emitter;
		return temp;
	}

	public ParticleEmitterBuilder shape (SpawnShape shape) {
		emitter.getSpawnShape().setShape(shape);
		return this;
	}

	/** Set the color/colors of the emitter
	 * @param color one color is 3 floats between[0,1]
	 * @param timeline for 3 color floats 1 timeline float between [0,1] */
	public ParticleEmitterBuilder color (float[] color, float[] timeline) {

		if (color.length != timeline.length * 3) {
			throw new IllegalArgumentException("colors array does not match to the timline array.");
		}
		emitter.getTint().setColors(color);
		emitter.getTint().setTimeline(timeline);
		return this;
	}

	public ParticleEmitterBuilder xOffset (float min, float max) {
		emitter.getXOffsetValue().setLow(min, max);
		return this;
	}

	public ParticleEmitterBuilder yOffset (float min, float max) {
		emitter.getYOffsetValue().setLow(min, max);
		return this;
	}

	public ParticleEmitterBuilder duration (float min, float max) {
		emitter.getDuration().setLow(min, max);
		return this;
	}

	public ParticleEmitterBuilder delay (float min, float max) {
		emitter.getDelay().setLow(min, max);
		return this;
	}

	public ParticleEmitterBuilder lifeOffset (float[] scaling, float[] timeline, float lowMin, float lowMax, float highMin,
		float highMax) {
		scaledNumericValue(emitter.getLifeOffset(), scaling, timeline, lowMin, lowMax, highMin, highMax);
		return this;
	}

	public ParticleEmitterBuilder spawnHeight (float[] scaling, float[] timeline, float lowMin, float lowMax, float highMin,
		float highMax) {
		scaledNumericValue(emitter.getSpawnHeight(), scaling, timeline, lowMin, lowMax, highMin, highMax);
		return this;
	}

	public ParticleEmitterBuilder spawnWidth (float[] scaling, float[] timeline, float lowMin, float lowMax, float highMin,
		float highMax) {
		scaledNumericValue(emitter.getSpawnWidth(), scaling, timeline, lowMin, lowMax, highMin, highMax);
		return this;
	}

	public ParticleEmitterBuilder wind (float[] scaling, float[] timeline, float lowMin, float lowMax, float highMin, float highMax) {
		scaledNumericValue(emitter.getWind(), scaling, timeline, lowMin, lowMax, highMin, highMax);
		return this;
	}

	public ParticleEmitterBuilder angle (float[] scaling, float[] timeline, float lowMin, float lowMax, float highMin,
		float highMax) {
		scaledNumericValue(emitter.getAngle(), scaling, timeline, lowMin, lowMax, highMin, highMax);
		return this;
	}

	public ParticleEmitterBuilder scale (float[] scaling, float[] timeline, float lowMin, float lowMax, float highMin,
		float highMax) {
		scaledNumericValue(emitter.getEmission(), scaling, timeline, lowMin, lowMax, highMin, highMax);
		return this;
	}

	public ParticleEmitterBuilder emission (float[] scaling, float[] timeline, float lowMin, float lowMax, float highMin,
		float highMax) {
		scaledNumericValue(emitter.getEmission(), scaling, timeline, lowMin, lowMax, highMin, highMax);
		return this;
	}

	public ParticleEmitterBuilder life (float[] scaling, float[] timeline, float lowMin, float lowMax, float highMin, float highMax) {
		scaledNumericValue(emitter.getLife(), scaling, timeline, lowMin, lowMax, highMin, highMax);
		return this;
	}

	public ParticleEmitterBuilder transparency (float[] scaling, float[] timeline, float lowMin, float lowMax, float highMin,
		float highMax) {
		scaledNumericValue(emitter.getTransparency(), scaling, timeline, lowMin, lowMax, highMin, highMax);
		return this;
	}

	public ParticleEmitterBuilder gravity (float[] scaling, float[] timeline, float lowMin, float lowMax, float highMin,
		float highMax) {
		scaledNumericValue(emitter.getGravity(), scaling, timeline, lowMin, lowMax, highMin, highMax);
		return this;
	}

	/** Set a compleat {@link ScaledNumericValue} */
	private void scaledNumericValue (ScaledNumericValue value, float[] scaling, float[] timeline, float lowMin, float lowMax,
		float highMin, float highMax) {
		value.setScaling(scaling);
		value.setTimeline(timeline);
		value.setLow(lowMin, lowMax);
		value.setHigh(highMin, highMax);
	}
}
