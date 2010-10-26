
package com.badlogic.gdx.graphics.particles;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.BitSet;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Sprite;
import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.utils.MathUtils;

// BOZO - Support point particles?
// BOZO - Add a duplicate emitter button.

public class ParticleEmitter {
	private RangedNumericValue delayValue = new RangedNumericValue();
	private ScaledNumericValue lifeOffsetValue = new ScaledNumericValue();
	private RangedNumericValue durationValue = new RangedNumericValue();
	private ScaledNumericValue lifeValue = new ScaledNumericValue();
	private ScaledNumericValue emissionValue = new ScaledNumericValue();
	private ScaledNumericValue sizeValue = new ScaledNumericValue();
	private ScaledNumericValue rotationValue = new ScaledNumericValue();
	private ScaledNumericValue velocityValue = new ScaledNumericValue();
	private ScaledNumericValue angleValue = new ScaledNumericValue();
	private ScaledNumericValue windValue = new ScaledNumericValue();
	private ScaledNumericValue gravityValue = new ScaledNumericValue();
	private ScaledNumericValue transparencyValue = new ScaledNumericValue();
	private GradientColorValue tintValue = new GradientColorValue();
	private RangedNumericValue xOffsetValue = new ScaledNumericValue();
	private RangedNumericValue yOffsetValue = new ScaledNumericValue();
	private ScaledNumericValue spawnWidthValue = new ScaledNumericValue();
	private ScaledNumericValue spawnHeightValue = new ScaledNumericValue();
	private SpawnShapeValue spawnShapeValue = new SpawnShapeValue();

	private Texture texture;
	private Particle[] particles;
	private int minParticleCount, maxParticleCount = 4;
	private float imageAspectRatio;
	private int x, y;
	private String name;
	private String imagePath;
	private int activeCount;
	private BitSet active;
	private boolean firstUpdate;
	private boolean flipX, flipY;

	private float emission, emissionDiff, emissionDelta;
	private float lifeOffset, lifeOffsetDiff;
	private float life, lifeDiff;
	private int spawnWidth, spawnWidthDiff;
	private int spawnHeight, spawnHeightDiff;
	public float duration = 1, durationTimer;
	private float delay, delayTimer;

	private boolean attached;
	private boolean continuous;
	private boolean aligned;
	private boolean behind;
	private boolean additive = true;

	public ParticleEmitter () {
		this((Texture)null);
	}

	public ParticleEmitter (Texture texture) {
		this.texture = texture;

		initialize();

		durationValue.setLow(3, 3);

		emissionValue.setHigh(10, 10);

		lifeValue.setHigh(1, 1);

		sizeValue.setHigh(32, 32);

		rotationValue.setLow(1, 360);
		rotationValue.setHigh(180, 180);
		rotationValue.setTimeline(new float[] {0, 1});
		rotationValue.setScaling(new float[] {0, 1});
		rotationValue.setRelative(true);

		angleValue.setHigh(1, 360);
		angleValue.setActive(true);

		velocityValue.setHigh(80, 80);
		velocityValue.setActive(true);

		transparencyValue.setHigh(1, 1);
		transparencyValue.setTimeline(new float[] {0, 0.2f, 0.8f, 1});
		transparencyValue.setScaling(new float[] {0, 1, 1, 0});
	}

	public ParticleEmitter (BufferedReader reader) throws IOException {
		initialize();
		load(reader);
	}

	public ParticleEmitter (ParticleEmitter emitter) {
		texture = emitter.texture;
		imageAspectRatio = emitter.imageAspectRatio;
		setMaxParticleCount(emitter.maxParticleCount);
		minParticleCount = emitter.minParticleCount;
		delayValue.load(emitter.delayValue);
		durationValue.load(emitter.durationValue);
		emissionValue.load(emitter.emissionValue);
		lifeValue.load(emitter.lifeValue);
		lifeOffsetValue.load(emitter.lifeOffsetValue);
		sizeValue.load(emitter.sizeValue);
		rotationValue.load(emitter.rotationValue);
		velocityValue.load(emitter.velocityValue);
		angleValue.load(emitter.angleValue);
		windValue.load(emitter.windValue);
		gravityValue.load(emitter.gravityValue);
		transparencyValue.load(emitter.transparencyValue);
		tintValue.load(emitter.tintValue);
		xOffsetValue.load(emitter.xOffsetValue);
		yOffsetValue.load(emitter.yOffsetValue);
		spawnWidthValue.load(emitter.spawnWidthValue);
		spawnHeightValue.load(emitter.spawnHeightValue);
		spawnShapeValue.load(emitter.spawnShapeValue);
		attached = emitter.attached;
		continuous = emitter.continuous;
		aligned = emitter.aligned;
		behind = emitter.behind;
		additive = emitter.additive;
	}

	private void initialize () {
		durationValue.setAlwaysActive(true);
		emissionValue.setAlwaysActive(true);
		lifeValue.setAlwaysActive(true);
		sizeValue.setAlwaysActive(true);
		transparencyValue.setAlwaysActive(true);
		spawnShapeValue.setAlwaysActive(true);
		spawnWidthValue.setAlwaysActive(true);
		spawnHeightValue.setAlwaysActive(true);
	}

	public void setMaxParticleCount (int maxParticleCount) {
		this.maxParticleCount = maxParticleCount;
		active = new BitSet(maxParticleCount);
		activeCount = 0;
		particles = new Particle[maxParticleCount];
	}

	public void addParticle () {
		int activeCount = this.activeCount;
		if (activeCount == maxParticleCount) return;
		BitSet active = this.active;
		int index = active.nextClearBit(0);
		activateParticle(index);
		active.set(index);
		this.activeCount = activeCount + 1;
	}

	public void addParticles (int count) {
		count = Math.min(count, maxParticleCount - activeCount);
		if (count == 0) return;
		BitSet active = this.active;
		for (int i = 0; i < count; i++) {
			int index = active.nextClearBit(0);
			activateParticle(index);
			active.set(index);
		}
		this.activeCount += count;
	}

	public void draw (SpriteBatch spriteBatch, float delta) {
		delta = Math.min(delta, 0.250f);

		if (additive) spriteBatch.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE);

		BitSet active = this.active;
		int activeCount = this.activeCount;
		int index = 0;
		while (true) {
			index = active.nextSetBit(index);
			if (index == -1) break;
			if (updateParticle(index, delta))
				particles[index].draw(spriteBatch);
			else {
				active.clear(index);
				activeCount--;
			}
			index++;
		}
		this.activeCount = activeCount;

		if (additive) spriteBatch.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		if (delayTimer < delay) {
			delayTimer += delta;
			return;
		}

		if (firstUpdate) {
			firstUpdate = false;
			addParticle();
		}

		if (durationTimer < duration)
			durationTimer += delta;
		else {
			if (!continuous) return;
			restart();
		}

		emissionDelta += delta;
		float emissionTime = emission + emissionDiff * emissionValue.getScale(durationTimer / (float)duration);
		if (emissionTime > 0) {
			emissionTime = 1 / emissionTime;
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

	public void start () {
		firstUpdate = true;
		restart();
	}

	private void restart () {
		delay = delayValue.active ? delayValue.newLowValue() : 0;
		delayTimer = 0;

		durationTimer -= duration;
		duration = durationValue.newLowValue();

		emission = emissionValue.newLowValue();
		emissionDiff = emissionValue.newHighValue();
		if (!emissionValue.isRelative()) emissionDiff -= emission;

		life = lifeValue.newLowValue();
		lifeDiff = lifeValue.newHighValue();
		if (!lifeValue.isRelative()) lifeDiff -= life;

		lifeOffset = lifeOffsetValue.active ? lifeOffsetValue.newLowValue() : 0;
		lifeOffsetDiff = lifeOffsetValue.newHighValue();
		if (!lifeOffsetValue.isRelative()) lifeOffsetDiff -= lifeOffset;

		spawnWidth = (int)spawnWidthValue.newLowValue();
		spawnWidthDiff = (int)spawnWidthValue.newHighValue();
		if (!spawnWidthValue.isRelative()) spawnWidthDiff -= spawnWidth;

		spawnHeight = (int)spawnHeightValue.newLowValue();
		spawnHeightDiff = (int)spawnHeightValue.newHighValue();
		if (!spawnHeightValue.isRelative()) spawnHeightDiff -= spawnHeight;
	}

	public void activateParticle (int index) {
		Particle particle = particles[index];
		if (particle == null) {
			particles[index] = particle = new Particle(texture);
			particle.flip(flipX, flipY);
		}

		float percent = durationTimer / (float)duration;

		float offsetTime = lifeOffset + lifeOffsetDiff * lifeOffsetValue.getScale(percent);
		particle.life = particle.currentLife = life + lifeDiff * lifeValue.getScale(percent);

		if (velocityValue.active) {
			particle.velocity = velocityValue.newLowValue();
			particle.velocityDiff = velocityValue.newHighValue();
			if (!velocityValue.isRelative()) particle.velocityDiff -= particle.velocity;
		}

		particle.angle = angleValue.newLowValue();
		particle.angleDiff = angleValue.newHighValue();
		if (!angleValue.isRelative()) particle.angleDiff -= particle.angle;

		particle.size = sizeValue.newLowValue() / texture.getWidth();
		particle.sizeDiff = sizeValue.newHighValue() / texture.getWidth();
		if (!sizeValue.isRelative()) particle.sizeDiff -= particle.size;

		if (rotationValue.active) {
			particle.rotation = particle.currentRotation = rotationValue.newLowValue();
			particle.rotationDiff = rotationValue.newHighValue();
			if (!rotationValue.isRelative()) particle.rotationDiff -= particle.rotation;
		}

		if (windValue.active) {
			particle.wind = windValue.newLowValue();
			particle.windDiff = windValue.newHighValue();
			if (!windValue.isRelative()) particle.windDiff -= particle.wind;
		}

		if (gravityValue.active) {
			particle.gravity = gravityValue.newLowValue();
			particle.gravityDiff = gravityValue.newHighValue();
			if (!gravityValue.isRelative()) particle.gravityDiff -= particle.gravity;
		}

		particle.transparency = transparencyValue.newLowValue();
		particle.transparencyDiff = transparencyValue.newHighValue() - particle.transparency;

		// Spawn.
		int x = this.x;
		if (xOffsetValue.active) x += (int)xOffsetValue.newLowValue();
		int y = this.y;
		if (yOffsetValue.active) y += (int)yOffsetValue.newLowValue();
		switch (spawnShapeValue.shape) {
		case square: {
			int width = spawnWidth + (int)(spawnWidthDiff * spawnWidthValue.getScale(percent));
			int height = spawnHeight + (int)(spawnHeightDiff * spawnHeightValue.getScale(percent));
			x += MathUtils.random(width) - width / 2;
			y += MathUtils.random(height) - height / 2;
			break;
		}
		case ellipse: {
			int width = spawnWidth + (int)(spawnWidthDiff * spawnWidthValue.getScale(percent));
			int height = spawnHeight + (int)(spawnHeightDiff * spawnHeightValue.getScale(percent));
			int radiusX = width / 2;
			int radiusY = height / 2;
			if (radiusX == 0 || radiusY == 0) break;
			float scaleY = radiusX / (float)radiusY;
			if (spawnShapeValue.edges) {
				float angle;
				switch (spawnShapeValue.side) {
				case top:
					angle = -MathUtils.random(179f);
					break;
				case bottom:
					angle = MathUtils.random(179f);
					break;
				default:
					angle = MathUtils.random(360f);
					break;
				}
				x += MathUtils.cosDeg(angle) * radiusX;
				y += MathUtils.sinDeg(angle) * radiusX / scaleY;
			} else {
				int radius2 = radiusX * radiusX;
				while (true) {
					int px = MathUtils.random(width) - radiusX;
					int py = MathUtils.random(width) - radiusX;
					if (px * px + py * py <= radius2) {
						x += px;
						y += py / scaleY;
						break;
					}
				}
			}
			break;
		}
		case line: {
			int width = spawnWidth + (int)(spawnWidthDiff * spawnWidthValue.getScale(percent));
			int height = spawnHeight + (int)(spawnHeightDiff * spawnHeightValue.getScale(percent));
			if (width != 0) {
				float lineX = width * MathUtils.random();
				x += lineX;
				y += lineX * (height / (float)width);
			}
			break;
		}
		}

		particle.setRotation(particle.currentRotation);
		particle.setBounds(x - texture.getWidth() / 2, y - texture.getHeight() / 2, texture.getWidth(), texture.getHeight());

		updateParticle(index, offsetTime);
	}

	public boolean updateParticle (int index, float delta) {
		Particle particle = particles[index];
		float life = particle.currentLife - delta;
		if (life <= 0) return false;
		particle.currentLife = life;

		float percent = 1 - particle.currentLife / particle.life;

		particle.setScale(particle.size + particle.sizeDiff * sizeValue.getScale(percent));

		float angle = particle.angle + particle.angleDiff * angleValue.getScale(percent);

		if (rotationValue.active) {
			float rotation = particle.rotation + particle.rotationDiff * rotationValue.getScale(percent);
			if (aligned) rotation += angle;
			rotation -= particle.currentRotation;
			if (rotation != 0) {
				particle.currentRotation += rotation;
				particle.rotate(rotation);
			}
		}

		if (velocityValue.active) {
			float velocity = (particle.velocity + particle.velocityDiff * velocityValue.getScale(percent)) * delta;
			float velocityX = velocity;
			float velocityY = velocity;
			if (angleValue.active) {
				velocityX *= MathUtils.cosDeg(angle);
				velocityY *= MathUtils.sinDeg(angle);
			}
			if (windValue.active) velocityX += (particle.wind + particle.windDiff * windValue.getScale(percent)) * delta;
			if (gravityValue.active)
				velocityY += (particle.gravity + particle.gravityDiff * gravityValue.getScale(percent)) * delta;
			particle.translate(velocityX, velocityY);
		}

		float[] color = tintValue.getColor(percent);
		particle.setColor(color[0], color[1], color[2],
			particle.transparency + particle.transparencyDiff * transparencyValue.getScale(percent));

		return true;
	}

	public void setPosition (int x, int y) {
		if (attached) {
			int xAmount = x - this.x;
			int yAmount = y - this.y;
			BitSet active = this.active;
			int index = 0;
			while (true) {
				index = active.nextSetBit(index);
				if (index == -1) break;
				Particle particle = particles[index];
				particle.translate(xAmount, yAmount);
				index++;
			}
		}
		this.x = x;
		this.y = y;
	}

	public void setTexture (Texture texture) {
		this.texture = texture;
		if (texture == null) return;
		imageAspectRatio = texture.getHeight() / (float)texture.getWidth();
		float originX = texture.getWidth() / 2;
		float originY = texture.getHeight() / 2;
		for (int i = 0, n = particles.length; i < n; i++) {
			Particle particle = particles[i];
			if (particle == null) break;
			particle.setTexture(texture);
			particle.setOrigin(originX, originY);
		}
	}

	public Texture getTexture () {
		return texture;
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

	public ScaledNumericValue getSize () {
		return sizeValue;
	}

	public ScaledNumericValue getRotation () {
		return rotationValue;
	}

	public GradientColorValue getTint () {
		return tintValue;
	}

	public ScaledNumericValue getVelocity () {
		return velocityValue;
	}

	public ScaledNumericValue getWind () {
		return windValue;
	}

	public ScaledNumericValue getGravity () {
		return gravityValue;
	}

	public ScaledNumericValue getAngle () {
		return angleValue;
	}

	public ScaledNumericValue getEmission () {
		return emissionValue;
	}

	public ScaledNumericValue getTransparency () {
		return transparencyValue;
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

	public RangedNumericValue getXOffsetValue () {
		return xOffsetValue;
	}

	public RangedNumericValue getYOffsetValue () {
		return yOffsetValue;
	}

	public ScaledNumericValue getSpawnWidth () {
		return spawnWidthValue;
	}

	public ScaledNumericValue getSpawnHeight () {
		return spawnHeightValue;
	}

	public SpawnShapeValue getSpawnShape () {
		return spawnShapeValue;
	}

	public boolean isAttached () {
		return attached;
	}

	public void setAttached (boolean attached) {
		this.attached = attached;
	}

	public boolean isContinuous () {
		return continuous;
	}

	public void setContinuous (boolean continuous) {
		this.continuous = continuous;
	}

	public boolean isAligned () {
		return aligned;
	}

	public void setAligned (boolean aligned) {
		this.aligned = aligned;
	}

	public boolean isAdditive () {
		return additive;
	}

	public void setAdditive (boolean additive) {
		this.additive = additive;
	}

	public boolean isBehind () {
		return behind;
	}

	public void setBehind (boolean behind) {
		this.behind = behind;
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

	public boolean isComplete () {
		if (delayTimer < delay) return false;
		return durationTimer >= duration && activeCount == 0;
	}

	public float getPercentComplete () {
		if (delayTimer < delay) return 0;
		return Math.min(1, durationTimer / (float)duration);
	}

	public int getX () {
		return x;
	}

	public int getY () {
		return y;
	}

	public int getActiveCount () {
		return activeCount;
	}

	public int getDrawCount () {
		return active.length();
	}

	public String getImagePath () {
		return imagePath;
	}

	public void setImagePath (String imagePath) {
		this.imagePath = imagePath;
	}

	public void setFlip (boolean flipX, boolean flipY) {
		this.flipX = flipX;
		this.flipY = flipY;
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
		output.write("- X Offset - \n");
		xOffsetValue.save(output);
		output.write("- Y Offset - \n");
		yOffsetValue.save(output);
		output.write("- Spawn Shape - \n");
		spawnShapeValue.save(output);
		output.write("- Spawn Width - \n");
		spawnWidthValue.save(output);
		output.write("- Spawn Height - \n");
		spawnHeightValue.save(output);
		output.write("- Size - \n");
		sizeValue.save(output);
		output.write("- Velocity - \n");
		velocityValue.save(output);
		output.write("- Angle - \n");
		angleValue.save(output);
		output.write("- Rotation - \n");
		rotationValue.save(output);
		output.write("- Wind - \n");
		windValue.save(output);
		output.write("- Gravity - \n");
		gravityValue.save(output);
		output.write("- Tint - \n");
		tintValue.save(output);
		output.write("- Transparency - \n");
		transparencyValue.save(output);
		output.write("- Options - \n");
		output.write("attached: " + attached + "\n");
		output.write("continuous: " + continuous + "\n");
		output.write("aligned: " + aligned + "\n");
		output.write("additive: " + additive + "\n");
		output.write("behind: " + behind + "\n");
	}

	public void load (BufferedReader reader) throws IOException {
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
		xOffsetValue.load(reader);
		reader.readLine();
		yOffsetValue.load(reader);
		reader.readLine();
		spawnShapeValue.load(reader);
		reader.readLine();
		spawnWidthValue.load(reader);
		reader.readLine();
		spawnHeightValue.load(reader);
		reader.readLine();
		sizeValue.load(reader);
		reader.readLine();
		velocityValue.load(reader);
		reader.readLine();
		angleValue.load(reader);
		reader.readLine();
		rotationValue.load(reader);
		reader.readLine();
		windValue.load(reader);
		reader.readLine();
		gravityValue.load(reader);
		reader.readLine();
		tintValue.load(reader);
		reader.readLine();
		transparencyValue.load(reader);
		reader.readLine();
		attached = readBoolean(reader, "attached");
		continuous = readBoolean(reader, "continuous");
		aligned = readBoolean(reader, "aligned");
		additive = readBoolean(reader, "additive");
		behind = readBoolean(reader, "behind");
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

	static class Particle extends Sprite {
		float life, currentLife;
		float size, sizeDiff;
		float rotation, currentRotation, rotationDiff;
		float velocity, velocityDiff;
		float angle, angleDiff;
		float transparency, transparencyDiff;
		float wind, windDiff;
		float gravity, gravityDiff;

		public Particle (Texture texture) {
			super(texture);
		}
	}

	static public class ParticleValue {
		boolean active;
		boolean alwaysActive;

		public void setAlwaysActive (boolean alwaysActive) {
			this.alwaysActive = alwaysActive;
		}

		public boolean isAlwaysActive () {
			return alwaysActive;
		}

		public boolean isActive () {
			return active;
		}

		public void setActive (boolean active) {
			this.active = active;
		}

		public void save (Writer output) throws IOException {
			if (!alwaysActive)
				output.write("active: " + active + "\n");
			else
				active = true;
		}

		public void load (BufferedReader reader) throws IOException {
			if (!alwaysActive)
				active = readBoolean(reader, "active");
			else
				active = true;
		}

		public void load (ParticleValue value) {
			active = value.active;
			alwaysActive = value.alwaysActive;
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
		private float[] timeline = {0};
		private float highMin, highMax;
		private boolean relative;

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
			float[] timeline = this.timeline;
			int n = timeline.length;
			for (int i = 1; i < n; i++) {
				float t = timeline[i];
				if (t > percent) {
					endIndex = i;
					break;
				}
			}
			if (endIndex == -1) return scaling[n - 1];
			float[] scaling = this.scaling;
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
		private float[] timeline = {0};

		public GradientColorValue () {
			alwaysActive = true;
		}

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
			colors = new float[3];
			System.arraycopy(value.colors, 0, colors, 0, colors.length);
			timeline = new float[value.timeline.length];
			System.arraycopy(value.timeline, 0, timeline, 0, timeline.length);
		}
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
			if (shape == SpawnShape.ellipse) {
				output.write("edges: " + edges + "\n");
				output.write("side: " + side + "\n");
			}
		}

		public void load (BufferedReader reader) throws IOException {
			super.load(reader);
			if (!active) return;
			shape = SpawnShape.valueOf(readString(reader, "shape"));
			if (shape == SpawnShape.ellipse) {
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
		point, line, square, ellipse
	}

	static public enum SpawnEllipseSide {
		both, top, bottom
	}
}
