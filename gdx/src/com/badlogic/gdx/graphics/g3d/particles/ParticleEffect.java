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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;

/** It's a composition of particles emitters.
 * It can be updated, rendered, transformed which means the changes will be applied
 * on all the particles emitters.*/
public class ParticleEffect implements Disposable {
		private final Array<ParticleEmitter> emitters;
		private BoundingBox bounds;
		private boolean ownsTexture;

		public ParticleEffect () {
			emitters = new Array<ParticleEmitter>(8);
		}

		public ParticleEffect (ParticleEffect effect) {
			emitters = new Array<ParticleEmitter>(true, effect.emitters.size);
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

		public void render (ModelBatch batch){
			for (int i = 0, n = emitters.size; i < n; i++)
				emitters.get(i).render(batch);
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

		public void setPosition (float x, float y, float z) {
			for (int i = 0, n = emitters.size; i < n; i++)
				emitters.get(i).setPosition(x, y, z);
		}
		
		public void setPosition(Vector3 position) {
			for (int i = 0, n = emitters.size; i < n; i++)
				emitters.get(i).setPosition(position);
		}
		
		public void setOrientation (Quaternion orientation) {
			for (int i = 0, n = emitters.size; i < n; i++)
				emitters.get(i).setOrientation(orientation);
		}
		
		public void setOrientation(Vector3 axis, float angle) {
			for (int i = 0, n = emitters.size; i < n; i++)
				emitters.get(i).setOrientation(axis, angle);
		}
		
		public void rotate(Vector3 axis, float angle) {
			for (int i = 0, n = emitters.size; i < n; i++)
				emitters.get(i).rotate(axis, angle);
		}

		public void setScale(float scale) {
			for (int i = 0, n = emitters.size; i < n; i++)
				emitters.get(i).setScale(scale);
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

		public void save (File file) {
			Writer output = null;
			try {
				output = new FileWriter(file);
				int index = 0;
				for (int i = 0, n = emitters.size; i < n; i++) {
					ParticleEmitter emitter = emitters.get(i);
					if (index++ > 0) output.write("\n\n");
					emitter.save(output);
					output.write("- Image Path -\n");
					output.write(emitter.getImagePath() + "\n");
				}
			} catch (IOException ex) {
				throw new GdxRuntimeException("Error saving effect: " + file, ex);
			} finally {
				StreamUtils.closeQuietly(output);
			}
		}

		public void load (FileHandle effectFile, FileHandle imagesDir) {
			loadEmitters(effectFile);
			loadEmitterImages(imagesDir);
		}

		public void load (FileHandle effectFile, TextureAtlas atlas) {
			loadEmitters(effectFile);
			loadEmitterImages(atlas);
		}

		public void loadEmitters (FileHandle effectFile) {
			InputStream input = effectFile.read();
			emitters.clear();
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(input), 512);
				while (true) {
					ParticleEmitter emitter = new ParticleEmitter(reader);
					reader.readLine();
					emitter.setImagePath(reader.readLine());
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
			for (int i = 0, n = emitters.size; i < n; i++) {
				ParticleEmitter emitter = emitters.get(i);
				String imagePath = emitter.getImagePath();
				if (imagePath == null) continue;
				String imageName = new File(imagePath.replace('\\', '/')).getName();
				int lastDotIndex = imageName.lastIndexOf('.');
				if (lastDotIndex != -1) imageName = imageName.substring(0, lastDotIndex);
				TextureRegion region = atlas.findRegion(imageName);
				if (region == null) throw new IllegalArgumentException("SpriteSheet missing image: " + imageName);
				emitter.setRegion(region);
			}
		}

		public void loadEmitterImages (FileHandle imagesDir) {
			ownsTexture = true;
			for (int i = 0, n = emitters.size; i < n; i++) {
				ParticleEmitter emitter = emitters.get(i);
				String imagePath = emitter.getImagePath();
				if (imagePath == null) continue;
				String imageName = new File(imagePath.replace('\\', '/')).getName();
				emitter.setRegionFromTexture(loadTexture(imagesDir.child(imageName)));
			}
		}

		protected Texture loadTexture (FileHandle file) {
			return new Texture(file, false);
		}

		/** Disposes the texture for each sprite for each ParticleEmitter. */
		public void dispose () {
			if (!ownsTexture) return;
			for (int i = 0, n = emitters.size; i < n; i++) 
			{
				ParticleEmitter emitter = emitters.get(i);
				emitter.getRegion().getTexture().dispose();
				emitter.dispose();
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

}
