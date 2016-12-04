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

import com.badlogic.gdx.graphics.g3d.particles.ParallelArray.ChannelDescriptor;
import com.badlogic.gdx.graphics.g3d.particles.ParallelArray.FloatChannel;
import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels;
import com.badlogic.gdx.graphics.g3d.particles.values.ScaledNumericValue;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/** It's an {@link Influencer} which controls a generic channel of the particles. It handles the interpolation through time using
 * {@link ScaledNumericValue}.
 * @author Inferno */
public abstract class SimpleInfluencer extends Influencer {

	public ScaledNumericValue value;
	FloatChannel valueChannel, interpolationChannel, lifeChannel;
	ChannelDescriptor valueChannelDescriptor;

	public SimpleInfluencer () {
		value = new ScaledNumericValue();
		value.setHigh(1);
	}

	public SimpleInfluencer (SimpleInfluencer billboardScaleinfluencer) {
		this();
		set(billboardScaleinfluencer);
	}

	private void set (SimpleInfluencer scaleInfluencer) {
		value.load(scaleInfluencer.value);
		valueChannelDescriptor = scaleInfluencer.valueChannelDescriptor;
	}

	@Override
	public void allocateChannels () {
		valueChannel = controller.particles.addChannel(valueChannelDescriptor);
		ParticleChannels.Interpolation.id = controller.particleChannels.newId();
		interpolationChannel = controller.particles.addChannel(ParticleChannels.Interpolation);
		lifeChannel = controller.particles.addChannel(ParticleChannels.Life);
	}

	@Override
	public void activateParticles (int startIndex, int count) {
		if (!value.isRelative()) {
			for (int i = startIndex * valueChannel.strideSize, a = startIndex * interpolationChannel.strideSize, c = i + count
				* valueChannel.strideSize; i < c; i += valueChannel.strideSize, a += interpolationChannel.strideSize) {
				float start = value.newLowValue();
				float diff = value.newHighValue() - start;
				interpolationChannel.data[a + ParticleChannels.InterpolationStartOffset] = start;
				interpolationChannel.data[a + ParticleChannels.InterpolationDiffOffset] = diff;
				valueChannel.data[i] = start + diff * value.getScale(0);
			}
		} else {
			for (int i = startIndex * valueChannel.strideSize, a = startIndex * interpolationChannel.strideSize, c = i + count
				* valueChannel.strideSize; i < c; i += valueChannel.strideSize, a += interpolationChannel.strideSize) {
				float start = value.newLowValue();
				float diff = value.newHighValue();
				interpolationChannel.data[a + ParticleChannels.InterpolationStartOffset] = start;
				interpolationChannel.data[a + ParticleChannels.InterpolationDiffOffset] = diff;
				valueChannel.data[i] = start + diff * value.getScale(0);
			}
		}
	}

	@Override
	public void update () {
		for (int i = 0, a = 0, l = ParticleChannels.LifePercentOffset, c = i + controller.particles.size * valueChannel.strideSize; i < c; i += valueChannel.strideSize, a += interpolationChannel.strideSize, l += lifeChannel.strideSize) {

			valueChannel.data[i] = interpolationChannel.data[a + ParticleChannels.InterpolationStartOffset]
				+ interpolationChannel.data[a + ParticleChannels.InterpolationDiffOffset] * value.getScale(lifeChannel.data[l]);
		}
	}

	@Override
	public void write (Json json) {
		json.writeValue("value", value);
	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		value = json.readValue("value", ScaledNumericValue.class, jsonData);
	}

}
