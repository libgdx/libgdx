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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;

// BOZO - Javadoc.
// BOZO - Add a duplicate emitter button.

public class ParticleEmitter {
	
	// Variables which will not be serialized.
	static transient private final int UPDATE_SCALE = 1 << 0;
	static transient private final int UPDATE_ANGLE = 1 << 1;
	static transient private final int UPDATE_ROTATION = 1 << 2;
	static transient private final int UPDATE_VELOCITY = 1 << 3;
	static transient private final int UPDATE_WIND = 1 << 4;
	static transient private final int UPDATE_GRAVITY = 1 << 5;
	static transient private final int UPDATE_TINT = 1 << 6;
	
	private transient Particle[] particles;
	private transient boolean[] active;
	private transient Sprite sprite;
	
	private transient float x, y;
	private transient float accumulator;
	private transient int updateFlags;
	private transient int activeCount;
	
	private transient int emission, emissionDiff, emissionDelta;
	private transient int lifeOffset, lifeOffsetDiff;
	private transient int life, lifeDiff;
	private transient float spawnWidth, spawnWidthDiff;
	private transient float spawnHeight, spawnHeightDiff;
	private transient float delay, delayTimer;
	
	private transient boolean firstUpdate;
	private transient boolean flipX, flipY;
	
	public transient float duration = 1, durationTimer;

	// Variables which will be serialized.
	private RangedNumericValue delayValue;
	private ScaledNumericValue lifeOffsetValue;
	private RangedNumericValue durationValue;
	private ScaledNumericValue lifeValue;
	private ScaledNumericValue emissionValue;
	private ScaledNumericValue scaleValue;
	private ScaledNumericValue rotationValue;
	private ScaledNumericValue velocityValue;
	private ScaledNumericValue angleValue;
	private ScaledNumericValue initialAngleValue;
	private ScaledNumericValue windValue;
	private ScaledNumericValue gravityValue;
	private ScaledNumericValue transparencyValue;
	private GradientColorValue tintValue;
	private RangedNumericValue xOffsetValue;
	private RangedNumericValue yOffsetValue;
	private ScaledNumericValue spawnWidthValue;
	private ScaledNumericValue spawnHeightValue; 
	private SpawnShapeValue spawnShapeValue;

	private int minParticleCount, maxParticleCount = 4;
	private String name;
	private String imagePath;
	
	private boolean allowCompletion;
	private boolean attached;
	private boolean continuous;
	private boolean aligned;
	private boolean behind;
	private boolean additive = true;
	
	/** Empty constructor which will be invoked when de-serializing from JSON. */
	public ParticleEmitter () {
	}
	
	/** Constructor for creating a default {@link ParticleEmitter}.
	 * @param init Whether to initialize this {@link ParticleEmitter} or not. */
	public ParticleEmitter (boolean init) {
		if(init) initialize();
	}
	
	/** Constructor will be invoked when reading a file using the old format. 
	 * @param reader The {@link BufferedReader} which has opened the file. */
	public ParticleEmitter (BufferedReader reader) throws IOException {
		initialize();
		load(reader);
	}
	
	/** Create a {@link ParticleEmitter} using an existing {@link ParticleEmitter}.
	 * @param emitter The {@link ParticleEmitter} to "copy". */
	public ParticleEmitter (ParticleEmitter emitter) {
		sprite = emitter.sprite;
		name = emitter.name;
		setMaxParticleCount(emitter.maxParticleCount);
		minParticleCount = emitter.minParticleCount;
		delayValue.load(emitter.delayValue);
		durationValue.load(emitter.durationValue);
		emissionValue.load(emitter.emissionValue);
		lifeValue.load(emitter.lifeValue);
		lifeOffsetValue.load(emitter.lifeOffsetValue);
		scaleValue.load(emitter.scaleValue);
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
	
	/** Instantiate important variables for this {@link ParticleEmitter}. */
	private void initialize () {
		delayValue = new RangedNumericValue();
		lifeOffsetValue = new ScaledNumericValue();
		durationValue = new RangedNumericValue();
		lifeValue = new ScaledNumericValue();
		emissionValue = new ScaledNumericValue();
		scaleValue = new ScaledNumericValue();
		rotationValue = new ScaledNumericValue();
		velocityValue = new ScaledNumericValue();
		angleValue = new ScaledNumericValue();
		initialAngleValue = new ScaledNumericValue();
		windValue = new ScaledNumericValue();
		gravityValue = new ScaledNumericValue();
		transparencyValue = new ScaledNumericValue();
		tintValue = new GradientColorValue();
		xOffsetValue = new ScaledNumericValue();
		yOffsetValue = new ScaledNumericValue();
		spawnWidthValue = new ScaledNumericValue();
		spawnHeightValue = new ScaledNumericValue();
		spawnShapeValue = new SpawnShapeValue();
		
		durationValue.setAlwaysActive(true);
		emissionValue.setAlwaysActive(true);
		lifeValue.setAlwaysActive(true);
		scaleValue.setAlwaysActive(true);
		transparencyValue.setAlwaysActive(true);
		spawnShapeValue.setAlwaysActive(true);
		spawnWidthValue.setAlwaysActive(true);
		spawnHeightValue.setAlwaysActive(true);
	}

	/** Compares this {@link ParticleEmitter} with the one given as argument. */
	public int cmp(ParticleEmitter em) {
		
		System.out.println("Comparing emitter:");
		int valueTests = getDelay().cmp(em.getDelay()) + getLifeOffset().cmp(em.getLifeOffset()) + getDuration().cmp(em.getDuration()) + getLife().cmp(em.getLife())
			+ getEmission().cmp(em.getEmission()) + getScale().cmp(em.getScale()) + getRotation().cmp(em.getRotation()) + getVelocity().cmp(em.getVelocity())
			+ getAngle().cmp(em.getAngle()) + getInitialAngle().cmp(em.getInitialAngle()) + getWind().cmp(em.getWind()) + getGravity().cmp(em.getGravity())
			+ getTransparency().cmp(em.getTransparency()) + getTint().cmp(em.getTint()) + getXOffsetValue().cmp(em.getXOffsetValue()) + getYOffsetValue().cmp(em.getYOffsetValue())
			+ getSpawnWidth().cmp(em.getSpawnWidth()) + getSpawnHeight().cmp(em.getSpawnHeight()) + getSpawnShape().cmp(em.getSpawnShape());
		
		System.out.println("	Value comparison failed: " + valueTests);
		
		int failed = 0;
			
		if(getMaxParticleCount() != em.getMaxParticleCount()) failed++;
		if(getMinParticleCount() != em.getMinParticleCount()) failed++;
		if(!getName().equals(em.getName())) failed++;
		if(!getImagePath().equals(em.getImagePath())) failed++;
		if(isAttached() != em.isAttached()) failed++;
		if(isContinuous() != em.isContinuous()) failed++;
		if(isAligned() != em.isAligned()) failed++;
		if(isBehind() != em.isBehind()) failed++;
		if(isAdditive() != em.isAdditive()) failed++;
		
		System.out.println("	General comparisons failed: " + failed);
		System.out.println("	Total failed: " + (failed + valueTests));
		
		return (failed + valueTests);
	}

	public void setMaxParticleCount (int maxParticleCount) {
		this.maxParticleCount = maxParticleCount;
		active = new boolean[maxParticleCount];
		activeCount = 0;
		particles = new Particle[maxParticleCount];
	}

	public void addParticle () {
		int activeCount = this.activeCount;
		if (activeCount == maxParticleCount) return;
		boolean[] active = this.active;
		for (int i = 0, n = active.length; i < n; i++) {
			if (!active[i]) {
				activateParticle(i);
				active[i] = true;
				this.activeCount = activeCount + 1;
				break;
			}
		}
	}

	public void addParticles (int count) {
		count = Math.min(count, maxParticleCount - activeCount);
		if (count == 0) return;
		boolean[] active = this.active;
		int index = 0, n = active.length;
		outer:
		for (int i = 0; i < count; i++) {
			for (; index < n; index++) {
				if (!active[index]) {
					activateParticle(index);
					active[index++] = true;
					continue outer;
				}
			}
			break;
		}
		this.activeCount += count;
	}

	public void update (float delta) {
		accumulator += Math.min(delta * 1000, 250);
		if (accumulator < 1) return;
		int deltaMillis = (int)accumulator;
		accumulator -= deltaMillis;

		boolean[] active = this.active;
		int activeCount = this.activeCount;
		for (int i = 0, n = active.length; i < n; i++) {
			if (active[i] && !updateParticle(particles[i], delta, deltaMillis)) {
				active[i] = false;
				activeCount--;
			}
		}
		this.activeCount = activeCount;

		if (delayTimer < delay) {
			delayTimer += deltaMillis;
			return;
		}

		if (firstUpdate) {
			firstUpdate = false;
			addParticle();
		}

		if (durationTimer < duration)
			durationTimer += deltaMillis;
		else {
			if (!continuous || allowCompletion) return;
			restart();
		}

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

	public void draw (SpriteBatch spriteBatch) {
		if (additive) spriteBatch.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE);

		Particle[] particles = this.particles;
		boolean[] active = this.active;
		int activeCount = this.activeCount;

		for (int i = 0, n = active.length; i < n; i++)
			if (active[i]) particles[i].draw(spriteBatch);
		this.activeCount = activeCount;

		if (additive) spriteBatch.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
	}

	/** Updates and draws the particles. This is slightly more efficient than calling {@link #update(float)} and
	 * {@link #draw(SpriteBatch)} separately. */
	public void draw (SpriteBatch spriteBatch, float delta) {
		accumulator += Math.min(delta * 1000, 250);
		if (accumulator < 1) {
			draw(spriteBatch);
			return;
		}
		int deltaMillis = (int)accumulator;
		accumulator -= deltaMillis;

		if (additive) spriteBatch.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE);

		Particle[] particles = this.particles;
		boolean[] active = this.active;
		int activeCount = this.activeCount;
		for (int i = 0, n = active.length; i < n; i++) {
			if (active[i]) {
				Particle particle = particles[i];
				if (updateParticle(particle, delta, deltaMillis))
					particle.draw(spriteBatch);
				else {
					active[i] = false;
					activeCount--;
				}
			}
		}
		this.activeCount = activeCount;

		if (additive) spriteBatch.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		if (delayTimer < delay) {
			delayTimer += deltaMillis;
			return;
		}

		if (firstUpdate) {
			firstUpdate = false;
			addParticle();
		}

		if (durationTimer < duration)
			durationTimer += deltaMillis;
		else {
			if (!continuous || allowCompletion) return;
			restart();
		}

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

	public void start () {
		firstUpdate = true;
		allowCompletion = false;
		restart();
	}

	public void reset () {
		emissionDelta = 0;
		durationTimer = 0;
		start();
	}

	private void restart () {
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

		spawnWidth = spawnWidthValue.newLowValue();
		spawnWidthDiff = spawnWidthValue.newHighValue();
		if (!spawnWidthValue.isRelative()) spawnWidthDiff -= spawnWidth;

		spawnHeight = spawnHeightValue.newLowValue();
		spawnHeightDiff = spawnHeightValue.newHighValue();
		if (!spawnHeightValue.isRelative()) spawnHeightDiff -= spawnHeight;

		updateFlags = 0;
		if (angleValue.active && angleValue.timeline.length > 1) updateFlags |= UPDATE_ANGLE;
		if (velocityValue.active && velocityValue.active) updateFlags |= UPDATE_VELOCITY;
		if (scaleValue.timeline.length > 1) updateFlags |= UPDATE_SCALE;
		if (rotationValue.active && rotationValue.timeline.length > 1) updateFlags |= UPDATE_ROTATION;
		if (windValue.active) updateFlags |= UPDATE_WIND;
		if (gravityValue.active) updateFlags |= UPDATE_GRAVITY;
		if (tintValue.timeline.length > 1) updateFlags |= UPDATE_TINT;
	}

	protected Particle newParticle (Sprite sprite) {
		return new Particle(sprite);
	}

	private void activateParticle (int index) {
		Particle particle = particles[index];
		if (particle == null) {
			particles[index] = particle = newParticle(sprite);
			particle.flip(flipX, flipY);
		}

		float percent = durationTimer / (float)duration;
		int updateFlags = this.updateFlags;

		particle.currentLife = particle.life = life + (int)(lifeDiff * lifeValue.getScale(percent));

		if (velocityValue.active) {
			particle.velocity = velocityValue.newLowValue();
			particle.velocityDiff = velocityValue.newHighValue();
			if (!velocityValue.isRelative()) particle.velocityDiff -= particle.velocity;
		}

		particle.angle = angleValue.newLowValue();
		particle.angleDiff = angleValue.newHighValue();
		if (!angleValue.isRelative()) particle.angleDiff -= particle.angle;
		float angle = 0;
		if ((updateFlags & UPDATE_ANGLE) == 0) {
			angle = particle.angle + particle.angleDiff * angleValue.getScale(0);
			particle.angle = angle;
			particle.angleCos = MathUtils.cosDeg(angle);
			particle.angleSin = MathUtils.sinDeg(angle);
		}

		float spriteWidth = sprite.getWidth();
		particle.scale = scaleValue.newLowValue() / spriteWidth;
		particle.scaleDiff = scaleValue.newHighValue() / spriteWidth;
		if (!scaleValue.isRelative()) particle.scaleDiff -= particle.scale;
		particle.setScale(particle.scale + particle.scaleDiff * scaleValue.getScale(0));

		if (rotationValue.active) {
			particle.rotation = rotationValue.newLowValue();
			particle.rotationDiff = rotationValue.newHighValue();
			if (!rotationValue.isRelative()) particle.rotationDiff -= particle.rotation;
			float rotation = particle.rotation + particle.rotationDiff * rotationValue.getScale(0);
			if (aligned) rotation += angle;
			particle.setRotation(rotation);
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

		float[] color = particle.tint;
		if (color == null) particle.tint = color = new float[3];
		float[] temp = tintValue.getColor(0);
		color[0] = temp[0];
		color[1] = temp[1];
		color[2] = temp[2];

		particle.transparency = transparencyValue.newLowValue();
		particle.transparencyDiff = transparencyValue.newHighValue() - particle.transparency;

		// Spawn.
		float x = this.x;
		if (xOffsetValue.active) x += xOffsetValue.newLowValue();
		float y = this.y;
		if (yOffsetValue.active) y += yOffsetValue.newLowValue();
		switch (spawnShapeValue.shape) {
		case square: {
			float width = spawnWidth + (spawnWidthDiff * spawnWidthValue.getScale(percent));
			float height = spawnHeight + (spawnHeightDiff * spawnHeightValue.getScale(percent));
			x += MathUtils.random(width) - width / 2;
			y += MathUtils.random(height) - height / 2;
			break;
		}
		case ellipse: {
			float width = spawnWidth + (spawnWidthDiff * spawnWidthValue.getScale(percent));
			float height = spawnHeight + (spawnHeightDiff * spawnHeightValue.getScale(percent));
			float radiusX = width / 2;
			float radiusY = height / 2;
			if (radiusX == 0 || radiusY == 0) break;
			float scaleY = radiusX / (float)radiusY;
			if (spawnShapeValue.edges) {
				float spawnAngle;
				switch (spawnShapeValue.side) {
				case top:
					spawnAngle = -MathUtils.random(179f);
					break;
				case bottom:
					spawnAngle = MathUtils.random(179f);
					break;
				default:
					spawnAngle = MathUtils.random(360f);
					break;
				}
				float cosDeg = MathUtils.cosDeg(spawnAngle);
				float sinDeg = MathUtils.sinDeg(spawnAngle);
				x += cosDeg * radiusX;
				y += sinDeg * radiusX / scaleY;
				if ((updateFlags & UPDATE_ANGLE) == 0) {
					particle.angle = spawnAngle;
					particle.angleCos = cosDeg;
					particle.angleSin = sinDeg;
				}
			} else {
				float radius2 = radiusX * radiusX;
				while (true) {
					float px = MathUtils.random(width) - radiusX;
					float py = MathUtils.random(width) - radiusX;
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
			float width = spawnWidth + (spawnWidthDiff * spawnWidthValue.getScale(percent));
			float height = spawnHeight + (spawnHeightDiff * spawnHeightValue.getScale(percent));
			if (width != 0) {
				float lineX = width * MathUtils.random();
				x += lineX;
				y += lineX * (height / (float)width);
			} else
				y += height * MathUtils.random();
			break;
		}
		}

		float spriteHeight = sprite.getHeight();
		particle.setBounds(x - spriteWidth / 2, y - spriteHeight / 2, spriteWidth, spriteHeight);

		int offsetTime = (int)(lifeOffset + lifeOffsetDiff * lifeOffsetValue.getScale(percent));
		if (offsetTime > 0) {
			if (offsetTime >= particle.currentLife) offsetTime = particle.currentLife - 1;
			updateParticle(particle, offsetTime / 1000f, offsetTime);
		}
	}

	private boolean updateParticle (Particle particle, float delta, int deltaMillis) {
		int life = particle.currentLife - deltaMillis;
		if (life <= 0) return false;
		particle.currentLife = life;

		float percent = 1 - particle.currentLife / (float)particle.life;
		int updateFlags = this.updateFlags;

		if ((updateFlags & UPDATE_SCALE) != 0)
			particle.setScale(particle.scale + particle.scaleDiff * scaleValue.getScale(percent));

		if ((updateFlags & UPDATE_VELOCITY) != 0) {
			float velocity = (particle.velocity + particle.velocityDiff * velocityValue.getScale(percent)) * delta;

			float velocityX, velocityY;
			if ((updateFlags & UPDATE_ANGLE) != 0) {
				float angle = particle.angle + particle.angleDiff * angleValue.getScale(percent);
				velocityX = velocity * MathUtils.cosDeg(angle);
				velocityY = velocity * MathUtils.sinDeg(angle);
				if ((updateFlags & UPDATE_ROTATION) != 0) {
					float rotation = particle.rotation + particle.rotationDiff * rotationValue.getScale(percent);
					if (aligned) rotation += angle;
					particle.setRotation(rotation);
				}
			} else {
				velocityX = velocity * particle.angleCos;
				velocityY = velocity * particle.angleSin;
				if (aligned || (updateFlags & UPDATE_ROTATION) != 0) {
					float rotation = particle.rotation + particle.rotationDiff * rotationValue.getScale(percent);
					if (aligned) rotation += particle.angle;
					particle.setRotation(rotation);
				}
			}

			if ((updateFlags & UPDATE_WIND) != 0)
				velocityX += (particle.wind + particle.windDiff * windValue.getScale(percent)) * delta;

			if ((updateFlags & UPDATE_GRAVITY) != 0)
				velocityY += (particle.gravity + particle.gravityDiff * gravityValue.getScale(percent)) * delta;

			particle.translate(velocityX, velocityY);
		} else {
			if ((updateFlags & UPDATE_ROTATION) != 0)
				particle.setRotation(particle.rotation + particle.rotationDiff * rotationValue.getScale(percent));
		}

		float[] color;
		if ((updateFlags & UPDATE_TINT) != 0)
			color = tintValue.getColor(percent);
		else
			color = particle.tint;
		particle.setColor(color[0], color[1], color[2],
			particle.transparency + particle.transparencyDiff * transparencyValue.getScale(percent));

		return true;
	}

	public void setPosition (float x, float y) {
		if (attached) {
			float xAmount = x - this.x;
			float yAmount = y - this.y;
			boolean[] active = this.active;
			for (int i = 0, n = active.length; i < n; i++)
				if (active[i]) particles[i].translate(xAmount, yAmount);
		}
		this.x = x;
		this.y = y;
	}

	public void setSprite (Sprite sprite) {
		this.sprite = sprite;
		if (sprite == null) return;
		float originX = sprite.getOriginX();
		float originY = sprite.getOriginY();
		Texture texture = sprite.getTexture();
		for (int i = 0, n = particles.length; i < n; i++) {
			Particle particle = particles[i];
			if (particle == null) break;
			particle.setTexture(texture);
			particle.setOrigin(originX, originY);
		}
	}

	/** Ignores the {@link #setContinuous(boolean) continuous} setting until the emitter is started again. This allows the emitter
	 * to stop smoothly. */
	public void allowCompletion () {
		allowCompletion = true;
		durationTimer = duration;
	}

	public Sprite getSprite () {
		return sprite;
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

	public ScaledNumericValue getScale () {
		return scaleValue;
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
	
	public ScaledNumericValue getInitialAngle() {
		return initialAngleValue;
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

	public float getX () {
		return x;
	}

	public float getY () {
		return y;
	}

	public int getActiveCount () {
		return activeCount;
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
		if (particles == null) return;
		for (int i = 0, n = particles.length; i < n; i++) {
			Particle particle = particles[i];
			if (particle != null) particle.flip(flipX, flipY);
		}
	}

	public void load (BufferedReader reader) throws IOException {
		try {
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
			scaleValue.load(reader);
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

	static class Particle extends Sprite {
		int life, currentLife;
		float scale, scaleDiff;
		float rotation, rotationDiff;
		float velocity, velocityDiff;
		float angle, angleDiff;
		float angleCos, angleSin;
		float transparency, transparencyDiff;
		float wind, windDiff;
		float gravity, gravityDiff;
		float[] tint;

		public Particle (Sprite sprite) {
			super(sprite);
		}
	}

	static public class ParticleValue {
		boolean active;
		boolean alwaysActive;
		
		public int cmp(ParticleValue value) {
			int failed = 0;
			if(isActive() != value.isActive()) failed++;
			if(isAlwaysActive() != value.isAlwaysActive()) failed++;
			printFailed(failed);
			
			return failed;
			
		}

		public void setAlwaysActive (boolean alwaysActive) {
			this.alwaysActive = alwaysActive;
		}

		public boolean isAlwaysActive () {
			return alwaysActive;
		}

		public boolean isActive () {
			return alwaysActive || active;
		}

		public void setActive (boolean active) {
			this.active = active;
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
		
		public void printFailed(int failed) {
			if(failed > 0) {
				System.out.println(this.getClass().getName() +" failed: " + failed);
			}
		}
		
	}

	static public class NumericValue extends ParticleValue {
		private float value;
		
		public int cmp(NumericValue value) {
			int failed = 0;
			if(getValue() != value.getValue()) failed++;
			printFailed(failed);
			return failed;
		}

		public float getValue () {
			return value;
		}

		public void setValue (float value) {
			this.value = value;
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
		
		public int cmp(RangedNumericValue value) {
			int failed = 0;
			if(getLowMin() != value.getLowMin()) failed++;
			if(getLowMax() != value.getLowMax()) failed++;
			printFailed(failed);
			return failed;
		}

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
		private boolean relative;
		
		public int cmp(ScaledNumericValue value) {
			int failed = 0;
			if(value.getHighMax() != getHighMax()) failed++;
			if(value.getHighMin() != getHighMin()) failed++;
			if(value.isRelative() != isRelative()) failed++;
			
			if(getScaling().length != value.getScaling().length) failed++;
			else {
				for (int i = 0; i < getScaling().length; i++) {
					if(value.getScaling()[i] != scaling[i]) failed++;
				}
			}
			
			if(getTimeline().length != value.getTimeline().length) failed++;
			else {
				for (int i = 0; i < getTimeline().length; i++) {
					if(value.getTimeline()[i] != getTimeline()[i]) failed++;
				}
			}
			printFailed(failed);
			
			return failed;
		}

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
		
		public int cmp(GradientColorValue value) {
			int failed = 0;
			
			if(getColors().length != value.getColors().length) failed++;
			else {
				for (int i = 0; i < getColors().length; i++) {
					if(value.getColors()[i] != getColors()[i]) failed++;
				}
			}
			
			if(getTimeline().length != value.getTimeline().length) failed++;
			else {
				for (int i = 0; i < getTimeline().length; i++) {
					if(value.getTimeline()[i] != getTimeline()[i]) failed++;
				}
			}
			printFailed(failed);
			
			return failed;
		}

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

	static public class SpawnShapeValue extends ParticleValue {
		SpawnShape shape = SpawnShape.point;
		boolean edges;
		SpawnEllipseSide side = SpawnEllipseSide.both;
		
		public int cmp(SpawnShapeValue value) {
			int failed = 0;
			if(!getShape().equals(value.getShape())) failed++;
			if(isEdges() != value.isEdges()) failed++;
			if(!getSide().equals(value.getSide())) failed++;
			
			printFailed(failed);
			
			return failed;
		}

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
