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
package com.badlogic.gdx.graphics.g3d.particles;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

import com.badlogic.gdx.math.MathUtils;

/** Abstract class representing an emitter.
 * This should be the parent class of every kind of custom emitter.
 * It handles the life cycle of particles, emission rate and delay.*/
public abstract class Emitter<T extends EmitObject> 
{
	public RangedNumericValue delayValue, durationValue;
	public ScaledNumericValue 	lifeOffsetValue,
								lifeValue, 
								emissionValue;

	//private float accumulator;
	protected T[] particles;
	protected int minParticleCount, maxParticleCount = 4;
	private String name;
	protected int activeCount = 0;
	private boolean firstUpdate;
	private boolean allowCompletion;

	protected int emission, emissionDiff, emissionDelta;
	protected int lifeOffset, lifeOffsetDiff;
	protected int life, lifeDiff;
	protected float duration = 1, delay, durationTimer, delayTimer;

	private boolean continuous;

	public Emitter () {
		initialize();
	}

	public Emitter (BufferedReader reader) throws IOException {
		initialize();
		load(reader);
	}

	public Emitter (Emitter<T> emitter) {
		initialize();
		name = emitter.name;
		setParticleCount(emitter.minParticleCount, emitter.maxParticleCount);
		delayValue.load(emitter.delayValue);
		durationValue.load(emitter.durationValue);
		emissionValue.load(emitter.emissionValue);
		lifeValue.load(emitter.lifeValue);
		lifeOffsetValue.load(emitter.lifeOffsetValue);
		continuous = emitter.continuous;
	}
	
	protected void initialize(){
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
	
	protected abstract T[] allocParticles(int particleCount);

	private void addParticle () {
		if (activeCount == maxParticleCount) return;
		
		activateParticleAux(activeCount);
		++activeCount;
	}

	private void addParticles (int count) {
		count = Math.min(count, maxParticleCount - activeCount);
		if (count == 0) return;
		for (int i = 0; i < count; ++i) activateParticleAux(activeCount+i);
		activeCount += count;
	}
	

	private void activateParticleAux (int index) {
		T particle = particles[index];
		float percent = durationTimer / (float)duration;
		particle.currentLife = particle.life = life + (int)(lifeDiff * lifeValue.getScale(percent));
		activateParticle(particle);
		int offsetTime = (int)(lifeOffset + lifeOffsetDiff * lifeOffsetValue.getScale(percent));
		if (offsetTime > 0) 
		{
			if (offsetTime >= particle.currentLife) offsetTime = particle.currentLife - 1;
			particle.currentLife -= offsetTime;
		}
	}
	
	protected abstract void activateParticle(T particle);

	public void update (float delta) {
		int deltaMillis = (int)(delta * 1000);
		
		if (delayTimer < delay) {
			delayTimer += deltaMillis;
		} else {
			boolean done = false;
			if (firstUpdate) {
				firstUpdate = false;
				addParticle();
			}
	
			if (durationTimer < duration) durationTimer += deltaMillis;
			else 
			{
				if (!continuous || allowCompletion) done = true;
				else restart();
			}

			if(!done) 
			{
				emissionDelta += deltaMillis;
				float emissionTime = emission + emissionDiff * emissionValue.getScale(durationTimer / (float)duration);
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
				if (activeCount < minParticleCount) addParticles(minParticleCount - activeCount);
			}
		}

		int activeCount = this.activeCount;
		T[] particles = this.particles;
		for (int i = 0; i < activeCount;) 
		{
			T particle = particles[i];
			particle.currentLife -= deltaMillis;
			if (particle.currentLife <= 0) 
			{
				//swap the particle
				int lastIndex = activeCount-1;
				if(i != lastIndex)
				{
					particles[i] = particles[lastIndex];
					particles[lastIndex] = particle;
				}
				--activeCount;
				continue;
			}
			++i;
		}
		this.activeCount = activeCount;
	}

	public void start () {
		firstUpdate = true;
		allowCompletion = false;
		restart();
	}

	public void reset () {
		emissionDelta = 0;
		durationTimer = duration;
		activeCount = 0;
		start();
	}

	protected void restart () {
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

	//protected abstract void updateParticle(T particle, float delta, int deltaMillis);

	

	/** Ignores the {@link #setContinuous(boolean) continuous} setting until the emitter is started again. This allows the emitter
	 * to stop smoothly. */
	public void allowCompletion () {
		allowCompletion = true;
		durationTimer = duration;
	}


	public String getName () {
		return name;
	}

	public void setName (String name) {
		this.name = name;
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

	public int getMinParticleCount () {
		return minParticleCount;
	}

	public void setMinParticleCount (int minParticleCount) {
		this.minParticleCount = minParticleCount;
	}

	public int getMaxParticleCount () {
		return maxParticleCount;
	}
	
	public void setMaxParticleCount (int maxParticleCount) {
		this.maxParticleCount = maxParticleCount;
		//if(particles == null || particles.length < maxParticleCount)
		particles = allocParticles(maxParticleCount);
		activeCount = 0;
	}
	
	public void setParticleCount(int aMin, int aMax){
		setMinParticleCount(aMin);
		setMaxParticleCount(aMax);
	}

	public boolean isComplete () {
		if (delayTimer < delay) return false;
		return durationTimer >= duration && activeCount == 0;
	}

	public float getPercentComplete () {
		if (delayTimer < delay) return 0;
		return Math.min(1, durationTimer / (float)duration);
	}

	public int getActiveCount () {
		return activeCount;
	}
	
	public void save (Writer output) throws IOException {
		output.write(name + "\n");
		output.write("- Delay -\n");
		delayValue.save(output);
		output.write("- Duration - \n");
		durationValue.save(output);
		output.write("- Count - \n");
		output.write("min: " + minParticleCount + "\n");
		output.write("max: " + maxParticleCount + "\n");
		output.write("- Emission - \n");
		emissionValue.save(output);
		output.write("- Life - \n");
		lifeValue.save(output);
		output.write("- Life Offset - \n");
		lifeOffsetValue.save(output);
		output.write("- Options - \n");
		output.write("continuous: " + continuous + "\n");
	}

	public void load (BufferedReader reader) throws IOException {
		try 
		{
			name = readString(reader, "name");
			reader.readLine();
			delayValue.load(reader);
			reader.readLine();
			durationValue.load(reader);
			reader.readLine();
			setMinParticleCount(readInt(reader, "minParticleCount"));
			setMaxParticleCount(readInt(reader, "maxParticleCount"));
			reader.readLine();
			emissionValue.load(reader);
			reader.readLine();
			lifeValue.load(reader);
			reader.readLine();
			lifeOffsetValue.load(reader);
			reader.readLine();
			continuous = readBoolean(reader, "continuous");
		} catch (RuntimeException ex) {
			if (name == null) throw ex;
			throw new RuntimeException("Error parsing emitter: " + name, ex);
		}
	}

	static String readString (BufferedReader reader, String name) throws IOException {
		String line = reader.readLine();
		if (line == null) throw new IOException("Missing value: " + name);
		return line.substring(line.indexOf(":") + 1).trim();
	}

	static boolean readBoolean (BufferedReader reader, String name) throws IOException {
		return Boolean.parseBoolean(readString(reader, name));
	}

	static int readInt (BufferedReader reader, String name) throws IOException {
		return Integer.parseInt(readString(reader, name));
	}

	static float readFloat (BufferedReader reader, String name) throws IOException {
		return Float.parseFloat(readString(reader, name));
	}

	static public class ParticleValue {
		boolean active;

		public boolean isActive () 
		{
			return active;
		}

		public void setActive (boolean active) {
			this.active = active;
		}

		public void save (Writer output) throws IOException {
				output.write("active: " + active + "\n");
		}

		public void load (BufferedReader reader) throws IOException {
				active = readBoolean(reader, "active");
		}

		public void load (ParticleValue value) 
		{
			active = value.active;
		}
	}

	static public class NumericValue extends ParticleValue {
		private float value;

		public float getValue () {
			return value;
		}

		public void setValue (float value) {
			this.value = value;
		}

		public void save (Writer output) throws IOException {
			super.save(output);
			if (!active) return;
			output.write("value: " + value + "\n");
		}

		public void load (BufferedReader reader) throws IOException {
			super.load(reader);
			if (!active) return;
			value = readFloat(reader, "value");
		}

		public void load (NumericValue value) {
			super.load(value);
			this.value = value.value;
		}
	}

	static public class RangedNumericValue extends ParticleValue {
		private float lowMin, lowMax;

		public float newLowValue () {
			return lowMin + (lowMax - lowMin) * MathUtils.random();
		}

		public void setLow (float value) {
			lowMin = value;
			lowMax = value;
		}

		public void setLow (float min, float max) {
			lowMin = min;
			lowMax = max;
		}

		public float getLowMin () {
			return lowMin;
		}

		public void setLowMin (float lowMin) {
			this.lowMin = lowMin;
		}

		public float getLowMax () {
			return lowMax;
		}

		public void setLowMax (float lowMax) {
			this.lowMax = lowMax;
		}

		public void save (Writer output) throws IOException {
			super.save(output);
			if (!active) return;
			output.write("lowMin: " + lowMin + "\n");
			output.write("lowMax: " + lowMax + "\n");
		}

		public void load (BufferedReader reader) throws IOException {
			super.load(reader);
			if (!active) return;
			lowMin = readFloat(reader, "lowMin");
			lowMax = readFloat(reader, "lowMax");
		}

		public void load (RangedNumericValue value) {
			super.load(value);
			lowMax = value.lowMax;
			lowMin = value.lowMin;
		}
	}

	static public class ScaledNumericValue extends RangedNumericValue {
		private float[] scaling = {1};
		float[] timeline = {0};
		private float highMin, highMax;
		private boolean relative = false;

		public float newHighValue () {
			return highMin + (highMax - highMin) * MathUtils.random();
		}

		public void setHigh (float value) {
			highMin = value;
			highMax = value;
		}

		public void setHigh (float min, float max) {
			highMin = min;
			highMax = max;
		}

		public float getHighMin () {
			return highMin;
		}

		public void setHighMin (float highMin) {
			this.highMin = highMin;
		}

		public float getHighMax () {
			return highMax;
		}

		public void setHighMax (float highMax) {
			this.highMax = highMax;
		}

		public float[] getScaling () {
			return scaling;
		}

		public void setScaling (float[] values) {
			this.scaling = values;
		}

		public float[] getTimeline () {
			return timeline;
		}

		public void setTimeline (float[] timeline) {
			this.timeline = timeline;
		}

		public boolean isRelative () {
			return relative;
		}

		public void setRelative (boolean relative) {
			this.relative = relative;
		}

		public float getScale (float percent) {
			int endIndex = -1;
			int n = timeline.length;
			for (int i = 1; i < n; i++) {
				float t = timeline[i];
				if (t > percent) {
					endIndex = i;
					break;
				}
			}
			if (endIndex == -1) return scaling[n - 1];
			int startIndex = endIndex - 1;
			float startValue = scaling[startIndex];
			float startTime = timeline[startIndex];
			return startValue + (scaling[endIndex] - startValue) * ((percent - startTime) / (timeline[endIndex] - startTime));
		}

		public void save (Writer output) throws IOException {
			super.save(output);
			if (!active) return;
			output.write("highMin: " + highMin + "\n");
			output.write("highMax: " + highMax + "\n");
			output.write("relative: " + relative + "\n");
			output.write("scalingCount: " + scaling.length + "\n");
			for (int i = 0; i < scaling.length; i++)
				output.write("scaling" + i + ": " + scaling[i] + "\n");
			output.write("timelineCount: " + timeline.length + "\n");
			for (int i = 0; i < timeline.length; i++)
				output.write("timeline" + i + ": " + timeline[i] + "\n");
		}

		public void load (BufferedReader reader) throws IOException {
			super.load(reader);
			if (!active) return;
			highMin = readFloat(reader, "highMin");
			highMax = readFloat(reader, "highMax");
			relative = readBoolean(reader, "relative");
			scaling = new float[readInt(reader, "scalingCount")];
			for (int i = 0; i < scaling.length; i++)
				scaling[i] = readFloat(reader, "scaling" + i);
			timeline = new float[readInt(reader, "timelineCount")];
			for (int i = 0; i < timeline.length; i++)
				timeline[i] = readFloat(reader, "timeline" + i);
		}

		public void load (ScaledNumericValue value) {
			super.load(value);
			highMax = value.highMax;
			highMin = value.highMin;
			scaling = new float[value.scaling.length];
			System.arraycopy(value.scaling, 0, scaling, 0, scaling.length);
			timeline = new float[value.timeline.length];
			System.arraycopy(value.timeline, 0, timeline, 0, timeline.length);
			relative = value.relative;
		}
	}

	static public class GradientColorValue extends ParticleValue {
		static private float[] temp = new float[4];

		private float[] colors = {1, 1, 1};
		float[] timeline = {0};

		public float[] getTimeline () {
			return timeline;
		}

		public void setTimeline (float[] timeline) {
			this.timeline = timeline;
		}

		public float[] getColors () {
			return colors;
		}

		public void setColors (float[] colors) {
			this.colors = colors;
		}

		public float[] getColor (float percent) {
			int startIndex = 0, endIndex = -1;
			float[] timeline = this.timeline;
			int n = timeline.length;
			for (int i = 1; i < n; i++) {
				float t = timeline[i];
				if (t > percent) {
					endIndex = i;
					break;
				}
				startIndex = i;
			}
			float startTime = timeline[startIndex];
			startIndex *= 3;
			float r1 = colors[startIndex];
			float g1 = colors[startIndex + 1];
			float b1 = colors[startIndex + 2];
			if (endIndex == -1) {
				temp[0] = r1;
				temp[1] = g1;
				temp[2] = b1;
				return temp;
			}
			float factor = (percent - startTime) / (timeline[endIndex] - startTime);
			endIndex *= 3;
			temp[0] = r1 + (colors[endIndex] - r1) * factor;
			temp[1] = g1 + (colors[endIndex + 1] - g1) * factor;
			temp[2] = b1 + (colors[endIndex + 2] - b1) * factor;
			return temp;
		}

		public void save (Writer output) throws IOException {
			super.save(output);
			if (!active) return;
			output.write("colorsCount: " + colors.length + "\n");
			for (int i = 0; i < colors.length; i++)
				output.write("colors" + i + ": " + colors[i] + "\n");
			output.write("timelineCount: " + timeline.length + "\n");
			for (int i = 0; i < timeline.length; i++)
				output.write("timeline" + i + ": " + timeline[i] + "\n");
		}

		public void load (BufferedReader reader) throws IOException {
			super.load(reader);
			if (!active) return;
			colors = new float[readInt(reader, "colorsCount")];
			for (int i = 0; i < colors.length; i++)
				colors[i] = readFloat(reader, "colors" + i);
			timeline = new float[readInt(reader, "timelineCount")];
			for (int i = 0; i < timeline.length; i++)
				timeline[i] = readFloat(reader, "timeline" + i);
		}

		public void load (GradientColorValue value) {
			super.load(value);
			colors = new float[value.colors.length];
			System.arraycopy(value.colors, 0, colors, 0, colors.length);
			timeline = new float[value.timeline.length];
			System.arraycopy(value.timeline, 0, timeline, 0, timeline.length);
		}
	}
	
	static public class VelocityValue extends ParticleValue{
		VelocityType type = VelocityType.polar;
		ScaledNumericValue strength;
		ScaledNumericValue theta;
		ScaledNumericValue phi;
		
		public VelocityValue(){
			strength = new ScaledNumericValue();
			theta = new ScaledNumericValue();
			phi = new ScaledNumericValue();
		}
		
		public VelocityType getType(){
			return type;
		}
		
		public void setType(VelocityType aType){
			type = aType;
		}
		
		public ScaledNumericValue getStrength(){
			return strength;
		}
		
		public ScaledNumericValue getTheta(){
			return theta;
		}
		
		public ScaledNumericValue getPhi(){
			return phi;
		}
		
		public void save (Writer output) throws IOException {
			super.save(output);
			if (!active) return;
			output.write("type: " + type+ "\n");
			output.write("strength:\n");
			strength.save(output);
			output.write("theta:\n");
			theta.save(output);
			output.write("phi:\n");
			phi.save(output);
		}

		public void load (BufferedReader reader) throws IOException {
			super.load(reader);
			if (!active) return;
			type = VelocityType.valueOf(readString(reader, "type"));
			reader.readLine();
			strength.load(reader);
			reader.readLine();
			theta.load(reader);
			reader.readLine();
			phi.load(reader);
		}

		public void load (VelocityValue value) {
			super.load(value);
			type = value.type;
			strength.load(value.strength);
			theta.load(value.theta);
			phi.load(value.phi);
		}
		
	}
	
	static public enum VelocityType {
		centripetal, tangential, polar
	}
	
	
	static public class SpawnShapeValue extends ParticleValue {
		SpawnShape shape = SpawnShape.point;
		boolean edges;
		SpawnEllipseSide side = SpawnEllipseSide.both;

		public SpawnShape getShape () {
			return shape;
		}

		public void setShape (SpawnShape shape) {
			this.shape = shape;
		}

		public boolean isEdges () {
			return edges;
		}

		public void setEdges (boolean edges) {
			this.edges = edges;
		}

		public SpawnEllipseSide getSide () {
			return side;
		}

		public void setSide (SpawnEllipseSide side) {
			this.side = side;
		}

		public void save (Writer output) throws IOException {
			super.save(output);
			if (!active) return;
			output.write("shape: " + shape + "\n");
			if (shape == SpawnShape.sphere || shape == SpawnShape.cylinder || shape == SpawnShape.rectangle) 
			{
				output.write("edges: " + edges + "\n");
				output.write("side: " + side + "\n");
			}
		}

		public void load (BufferedReader reader) throws IOException {
			super.load(reader);
			if (!active) return;
			shape = SpawnShape.valueOf(readString(reader, "shape"));
			if (shape == SpawnShape.sphere || shape == SpawnShape.cylinder || shape == SpawnShape.rectangle ) 
			{
				edges = readBoolean(reader, "edges");
				side = SpawnEllipseSide.valueOf(readString(reader, "side"));
			}
		}

		public void load (SpawnShapeValue value) {
			super.load(value);
			shape = value.shape;
			edges = value.edges;
			side = value.side;
		}
	}

	static public enum SpawnShape {
		point, line, rectangle, sphere, cylinder
	}

	static public enum SpawnEllipseSide {
		both, top, bottom
	}
	
	static public class AlignmentValue extends ParticleValue {
		Align align = Align.screen;

		public Align getAlign () {
			return align;
		}

		public void setAlign (Align aAlign) {
			align = aAlign;
		}

		public void save (Writer output) throws IOException {
			super.save(output);
			if (!active) return;
			output.write("align: " + align+ "\n");
		}

		public void load (BufferedReader reader) throws IOException {
			super.load(reader);
			if (!active) return;
			align = Align.valueOf(readString(reader, "align"));
		}

		public void load (AlignmentValue value) 
		{
			super.load(value);
			align = value.align;
		}
	}
	
	static public enum Align {
		screen, viewPoint, particleDirection
	}

}
