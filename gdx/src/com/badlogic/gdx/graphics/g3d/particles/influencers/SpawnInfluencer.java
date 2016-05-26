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

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.particles.ParallelArray.FloatChannel;
import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels;
import com.badlogic.gdx.graphics.g3d.particles.ResourceData;
import com.badlogic.gdx.graphics.g3d.particles.values.PointSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.particles.values.SpawnShapeValue;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/** It's an {@link Influencer} which controls where the particles will be spawned.
 * @author Inferno */
public class SpawnInfluencer extends Influencer {

	public SpawnShapeValue spawnShapeValue;
	FloatChannel positionChannel;

	public SpawnInfluencer () {
		spawnShapeValue = new PointSpawnShapeValue();
	}

	public SpawnInfluencer (SpawnShapeValue spawnShapeValue) {
		this.spawnShapeValue = spawnShapeValue;
	}

	public SpawnInfluencer (SpawnInfluencer source) {
		spawnShapeValue = source.spawnShapeValue.copy();
	}

	@Override
	public void init () {
		spawnShapeValue.init();
	}

	@Override
	public void allocateChannels () {
		positionChannel = controller.particles.addChannel(ParticleChannels.Position);
	}

	@Override
	public void start () {
		spawnShapeValue.start();
	}

	@Override
	public void activateParticles (int startIndex, int count) {
		for (int i = startIndex * positionChannel.strideSize, c = i + count * positionChannel.strideSize; i < c; i += positionChannel.strideSize) {
			spawnShapeValue.spawn(TMP_V1, controller.emitter.percent);
			TMP_V1.mul(controller.transform);
			positionChannel.data[i + ParticleChannels.XOffset] = TMP_V1.x;
			positionChannel.data[i + ParticleChannels.YOffset] = TMP_V1.y;
			positionChannel.data[i + ParticleChannels.ZOffset] = TMP_V1.z;
		}
	}

	@Override
	public SpawnInfluencer copy () {
		return new SpawnInfluencer(this);
	}

	@Override
	public void write (Json json) {
		json.writeValue("spawnShape", spawnShapeValue, SpawnShapeValue.class);
	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		spawnShapeValue = json.readValue("spawnShape", SpawnShapeValue.class, jsonData);
	}

	@Override
	public void save (AssetManager manager, ResourceData data) {
		spawnShapeValue.save(manager, data);
	}

	@Override
	public void load (AssetManager manager, ResourceData data) {
		spawnShapeValue.load(manager, data);
	}
}
