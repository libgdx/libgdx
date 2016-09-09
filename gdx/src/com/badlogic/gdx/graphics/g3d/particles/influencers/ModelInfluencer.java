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

package com.badlogic.gdx.graphics.g3d.particles.influencers;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.particles.ParallelArray.ObjectChannel;
import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels;
import com.badlogic.gdx.graphics.g3d.particles.ResourceData;
import com.badlogic.gdx.graphics.g3d.particles.ResourceData.SaveData;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

/** It's an {@link Influencer} which controls which {@link Model} will be assigned to the particles as {@link ModelInstance}.
 * @author Inferno */
public abstract class ModelInfluencer extends Influencer {

	/** Assigns the first model of {@link ModelInfluencer#models} to the particles. */
	public static class Single extends ModelInfluencer {

		public Single () {
			super();
		}

		public Single (Single influencer) {
			super(influencer);
		}

		public Single (Model... models) {
			super(models);
		}

		@Override
		public void init () {
			Model first = models.first();
			for (int i = 0, c = controller.emitter.maxParticleCount; i < c; ++i) {
				modelChannel.data[i] = new ModelInstance(first);
			}
		}

		@Override
		public Single copy () {
			return new Single(this);
		}
	}

	/** Assigns a random model of {@link ModelInfluencer#models} to the particles. */
	public static class Random extends ModelInfluencer {
		private class ModelInstancePool extends Pool<ModelInstance> {
			public ModelInstancePool () {
			}

			@Override
			public ModelInstance newObject () {
				return new ModelInstance(models.random());
			}
		}

		ModelInstancePool pool;

		public Random () {
			super();
			pool = new ModelInstancePool();
		}

		public Random (Random influencer) {
			super(influencer);
			pool = new ModelInstancePool();
		}

		public Random (Model... models) {
			super(models);
			pool = new ModelInstancePool();
		}

		@Override
		public void init () {
			pool.clear();
		}

		@Override
		public void activateParticles (int startIndex, int count) {
			for (int i = startIndex, c = startIndex + count; i < c; ++i) {
				modelChannel.data[i] = pool.obtain();
			}
		}

		@Override
		public void killParticles (int startIndex, int count) {
			for (int i = startIndex, c = startIndex + count; i < c; ++i) {
				pool.free(modelChannel.data[i]);
				modelChannel.data[i] = null;
			}
		}

		@Override
		public Random copy () {
			return new Random(this);
		}
	}

	public Array<Model> models;
	ObjectChannel<ModelInstance> modelChannel;

	public ModelInfluencer () {
		this.models = new Array<Model>(true, 1, Model.class);
	}

	public ModelInfluencer (Model... models) {
		this.models = new Array<Model>(models);
	}

	public ModelInfluencer (ModelInfluencer influencer) {
		this((Model[])influencer.models.toArray(Model.class));
	}

	@Override
	public void allocateChannels () {
		modelChannel = controller.particles.addChannel(ParticleChannels.ModelInstance);
	}

	@Override
	public void save (AssetManager manager, ResourceData resources) {
		SaveData data = resources.createSaveData();
		for (Model model : models)
			data.saveAsset(manager.getAssetFileName(model), Model.class);
	}

	@Override
	public void load (AssetManager manager, ResourceData resources) {
		SaveData data = resources.getSaveData();
		AssetDescriptor descriptor;
		while ((descriptor = data.loadAsset()) != null) {
			Model model = (Model)manager.get(descriptor);
			if (model == null) throw new RuntimeException("Model is null");
			models.add(model);
		}
	}
}
