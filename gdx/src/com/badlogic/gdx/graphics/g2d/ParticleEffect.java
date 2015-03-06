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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.HashMap;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;

/** See <a href="http://www.badlogicgames.com/wordpress/?p=1255">http://www.badlogicgames.com/wordpress/?p=1255</a>
 * @author mzechner */
public class ParticleEffect implements Disposable {
	private final Array<ParticleEmitter> emitters;
	private BoundingBox bounds;
	private boolean ownsTexture;

	public ParticleEffect () {
		emitters = new Array(8);
	}

	public ParticleEffect (ParticleEffect effect) {
		emitters = new Array(true, effect.emitters.size);
		for (int i = 0, n = effect.emitters.size; i < n; i++)
			emitters.add(new ParticleEmitter(effect.emitters.get(i)));
	}

	public void start () {
		for (int i = 0, n = emitters.size; i < n; i++)
			emitters.get(i).start();
	}

	public void reset () {
		for (int i = 0, n = emitters.size; i < n; i++)
			emitters.get(i).reset();
	}

	public void update (float delta) {
		for (int i = 0, n = emitters.size; i < n; i++)
			emitters.get(i).update(delta);
	}

	public void draw (Batch spriteBatch) {
		for (int i = 0, n = emitters.size; i < n; i++)
			emitters.get(i).draw(spriteBatch);
	}

	public void draw (Batch spriteBatch, float delta) {
		for (int i = 0, n = emitters.size; i < n; i++)
			emitters.get(i).draw(spriteBatch, delta);
	}

	public void allowCompletion () {
		for (int i = 0, n = emitters.size; i < n; i++)
			emitters.get(i).allowCompletion();
	}

	public boolean isComplete () {
		for (int i = 0, n = emitters.size; i < n; i++) {
			ParticleEmitter emitter = emitters.get(i);
			if (!emitter.isComplete()) return false;
		}
		return true;
	}

	public void setDuration (int duration) {
		for (int i = 0, n = emitters.size; i < n; i++) {
			ParticleEmitter emitter = emitters.get(i);
			emitter.setContinuous(false);
			emitter.duration = duration;
			emitter.durationTimer = 0;
		}
	}

	public void setPosition (float x, float y) {
		for (int i = 0, n = emitters.size; i < n; i++)
			emitters.get(i).setPosition(x, y);
	}

	public void setFlip (boolean flipX, boolean flipY) {
		for (int i = 0, n = emitters.size; i < n; i++)
			emitters.get(i).setFlip(flipX, flipY);
	}

	public void flipY () {
		for (int i = 0, n = emitters.size; i < n; i++)
			emitters.get(i).flipY();
	}

	public Array<ParticleEmitter> getEmitters () {
		return emitters;
	}

	/** Returns the emitter with the specified name, or null. */
	public ParticleEmitter findEmitter (String name) {
		for (int i = 0, n = emitters.size; i < n; i++) {
			ParticleEmitter emitter = emitters.get(i);
			if (emitter.getName().equals(name)) return emitter;
		}
		return null;
	}

	public void save (Writer output) throws IOException {
		int index = 0;
		for (int i = 0, n = emitters.size; i < n; i++) {
			ParticleEmitter emitter = emitters.get(i);
			if (index++ > 0) output.write("\n\n");
			emitter.save(output);
		}
	}

	public void load (FileHandle effectFile, FileHandle imagesDir) {
		loadEmitters(effectFile);
		loadEmitterImages(imagesDir);
	}

	public void load (FileHandle effectFile, TextureAtlas atlas) {
		load(effectFile, atlas, null);
	}

	public void load (FileHandle effectFile, TextureAtlas atlas, String atlasPrefix) {
		loadEmitters(effectFile);
		loadEmitterImages(atlas, atlasPrefix);
	}

	public void loadEmitters (FileHandle effectFile) {
		InputStream input = effectFile.read();
		emitters.clear();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(input), 512);
			while (true) {
				ParticleEmitter emitter = new ParticleEmitter(reader);
				emitters.add(emitter);
				if (reader.readLine() == null) break;
				if (reader.readLine() == null) break;
			}
		} catch (IOException ex) {
			throw new GdxRuntimeException("Error loading effect: " + effectFile, ex);
		} finally {
			StreamUtils.closeQuietly(reader);
		}
	}

	public void loadEmitterImages (TextureAtlas atlas) {
		loadEmitterImages(atlas, null);
	}

	public void loadEmitterImages (TextureAtlas atlas, String atlasPrefix) {
		for (int i = 0, n = emitters.size; i < n; i++) {
			ParticleEmitter emitter = emitters.get(i);
			String imagePath = emitter.getImagePath();
			if (imagePath == null) continue;
			String imageName = new File(imagePath.replace('\\', '/')).getName();
			int lastDotIndex = imageName.lastIndexOf('.');
			if (lastDotIndex != -1) imageName = imageName.substring(0, lastDotIndex);
			if (atlasPrefix != null) imageName = atlasPrefix + imageName;
			Sprite sprite = atlas.createSprite(imageName);
			if (sprite == null) throw new IllegalArgumentException("SpriteSheet missing image: " + imageName);
			emitter.setSprite(sprite);
		}
	}

	public void loadEmitterImages (FileHandle imagesDir) {
		ownsTexture = true;
		HashMap<String, Sprite> loadedSprites = new HashMap<String, Sprite>(emitters.size);
		for (int i = 0, n = emitters.size; i < n; i++) {
			ParticleEmitter emitter = emitters.get(i);
			String imagePath = emitter.getImagePath();
			if (imagePath == null) continue;
			String imageName = new File(imagePath.replace('\\', '/')).getName();
			Sprite sprite = loadedSprites.get(imageName);
			if (sprite == null) {
				sprite = new Sprite(loadTexture(imagesDir.child(imageName)));
				loadedSprites.put(imageName, sprite);
			}
			emitter.setSprite(sprite);
		}
	}

	protected Texture loadTexture (FileHandle file) {
		return new Texture(file, false);
	}

	/** Disposes the texture for each sprite for each ParticleEmitter. */
	public void dispose () {
		if (!ownsTexture) return;
		for (int i = 0, n = emitters.size; i < n; i++) {
			ParticleEmitter emitter = emitters.get(i);
			emitter.getSprite().getTexture().dispose();
		}
	}

	/** Returns the bounding box for all active particles. z axis will always be zero. */
	public BoundingBox getBoundingBox () {
		if (bounds == null) bounds = new BoundingBox();

		BoundingBox bounds = this.bounds;
		bounds.inf();
		for (ParticleEmitter emitter : this.emitters)
			bounds.ext(emitter.getBoundingBox());
		return bounds;
	}

	public void scaleEffect (float scaleFactor) {
		for (ParticleEmitter particleEmitter : emitters) {
			particleEmitter.getScale().setHigh(particleEmitter.getScale().getHighMin() * scaleFactor,
				particleEmitter.getScale().getHighMax() * scaleFactor);
			particleEmitter.getScale().setLow(particleEmitter.getScale().getLowMin() * scaleFactor,
				particleEmitter.getScale().getLowMax() * scaleFactor);

			particleEmitter.getVelocity().setHigh(particleEmitter.getVelocity().getHighMin() * scaleFactor,
				particleEmitter.getVelocity().getHighMax() * scaleFactor);
			particleEmitter.getVelocity().setLow(particleEmitter.getVelocity().getLowMin() * scaleFactor,
				particleEmitter.getVelocity().getLowMax() * scaleFactor);

			particleEmitter.getGravity().setHigh(particleEmitter.getGravity().getHighMin() * scaleFactor,
				particleEmitter.getGravity().getHighMax() * scaleFactor);
			particleEmitter.getGravity().setLow(particleEmitter.getGravity().getLowMin() * scaleFactor,
				particleEmitter.getGravity().getLowMax() * scaleFactor);

			particleEmitter.getWind().setHigh(particleEmitter.getWind().getHighMin() * scaleFactor,
				particleEmitter.getWind().getHighMax() * scaleFactor);
			particleEmitter.getWind().setLow(particleEmitter.getWind().getLowMin() * scaleFactor,
				particleEmitter.getWind().getLowMax() * scaleFactor);

			particleEmitter.getSpawnWidth().setHigh(particleEmitter.getSpawnWidth().getHighMin() * scaleFactor,
				particleEmitter.getSpawnWidth().getHighMax() * scaleFactor);
			particleEmitter.getSpawnWidth().setLow(particleEmitter.getSpawnWidth().getLowMin() * scaleFactor,
				particleEmitter.getSpawnWidth().getLowMax() * scaleFactor);

			particleEmitter.getSpawnHeight().setHigh(particleEmitter.getSpawnHeight().getHighMin() * scaleFactor,
				particleEmitter.getSpawnHeight().getHighMax() * scaleFactor);
			particleEmitter.getSpawnHeight().setLow(particleEmitter.getSpawnHeight().getLowMin() * scaleFactor,
				particleEmitter.getSpawnHeight().getLowMax() * scaleFactor);

			particleEmitter.getXOffsetValue().setLow(particleEmitter.getXOffsetValue().getLowMin() * scaleFactor,
				particleEmitter.getXOffsetValue().getLowMax() * scaleFactor);

			particleEmitter.getYOffsetValue().setLow(particleEmitter.getYOffsetValue().getLowMin() * scaleFactor,
				particleEmitter.getYOffsetValue().getLowMax() * scaleFactor);
		}
	}

	/** Sets the {@link com.badlogic.gdx.graphics.g2d.ParticleEmitter#setCleansUpBlendFunction(boolean) cleansUpBlendFunction}
	 * parameter on all {@link com.badlogic.gdx.graphics.g2d.ParticleEmitter ParticleEmitters} currently in this ParticleEffect.
	 * <p>
	 * IMPORTANT: If set to false and if the next object to use this Batch expects alpha blending, you are responsible for setting
	 * the Batch's blend function to (GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA) before that next object is drawn.
	 * @param cleanUpBlendFunction */
	public void setEmittersCleanUpBlendFunction (boolean cleanUpBlendFunction) {
		for (int i = 0, n = emitters.size; i < n; i++) {
			emitters.get(i).setCleansUpBlendFunction(cleanUpBlendFunction);
		}
	}
}
