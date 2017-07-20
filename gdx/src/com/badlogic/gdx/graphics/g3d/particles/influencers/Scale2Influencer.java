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
import com.badlogic.gdx.graphics.g3d.particles.ParticleControllerComponent;
import com.badlogic.gdx.graphics.g3d.particles.values.ScaledNumericValue;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/** It's an {@link Influencer} which controls the scale of the particles.
 * @author Inferno
 * @author Pieter Schaap - Changed the scaling from 1 dimensional to 3 dimensional scaling. */
public class Scale2Influencer extends Influencer {

	public ScaledNumericValue valueX, valueY;
	FloatChannel valueChannel, interpolationChannel, lifeChannel;
	ChannelDescriptor valueChannelDescriptor;

	public Scale2Influencer () {
		super();
		valueChannelDescriptor = ParticleChannels.Scale2;
	}

	@Override
	public void activateParticles (int startIndex, int count) {
		if (valueX.isRelative()) {

			if (valueY.isRelative()) {
				// Both X and Y scale are relative
				for (int i = startIndex * valueChannel.strideSize, a = startIndex * interpolationChannel.strideSize, c = i
					+ count * valueChannel.strideSize; i < c; i += valueChannel.strideSize, a += interpolationChannel.strideSize) {
					float startX = valueX.newLowValue() * controller.scale.x;
					float diffX = valueX.newHighValue() * controller.scale.x;
					float startY = valueY.newLowValue() * controller.scale.y;
					float diffY = valueY.newHighValue() * controller.scale.y;

					interpolationChannel.data[a + ParticleChannels.Interpolation4StartOffset + ParticleChannels.XOffset] = startX;
					interpolationChannel.data[a + ParticleChannels.Interpolation4DiffOffset + ParticleChannels.XOffset] = diffX;

					interpolationChannel.data[a + ParticleChannels.Interpolation4StartOffset + ParticleChannels.YOffset] = startY;
					interpolationChannel.data[a + ParticleChannels.Interpolation4DiffOffset + ParticleChannels.YOffset] = diffY;

					valueChannel.data[i + ParticleChannels.XOffset] = startX + diffX * valueX.getScale(0);
					valueChannel.data[i + ParticleChannels.YOffset] = startY + diffY * valueY.getScale(0);
				}
			} else {
				// Only X scale is relative
				for (int i = startIndex * valueChannel.strideSize, a = startIndex * interpolationChannel.strideSize, c = i
					+ count * valueChannel.strideSize; i < c; i += valueChannel.strideSize, a += interpolationChannel.strideSize) {
					float startX = valueX.newLowValue() * controller.scale.x;
					float diffX = valueX.newHighValue() * controller.scale.x;
					float startY = valueY.newLowValue() * controller.scale.y;
					float diffY = valueY.newHighValue() * controller.scale.y - startY;

					interpolationChannel.data[a + ParticleChannels.Interpolation4StartOffset + ParticleChannels.XOffset] = startX;
					interpolationChannel.data[a + ParticleChannels.Interpolation4DiffOffset + ParticleChannels.XOffset] = diffX;

					interpolationChannel.data[a + ParticleChannels.Interpolation4StartOffset + ParticleChannels.YOffset] = startY;
					interpolationChannel.data[a + ParticleChannels.Interpolation4DiffOffset + ParticleChannels.YOffset] = diffY;

					valueChannel.data[i + ParticleChannels.XOffset] = startX + diffX * valueX.getScale(0);
					valueChannel.data[i + ParticleChannels.YOffset] = startY + diffY * valueY.getScale(0);
				}
			}
		} else {

			if (valueY.isRelative()) {
				// Only Y is relative
				for (int i = startIndex * valueChannel.strideSize, a = startIndex * interpolationChannel.strideSize, c = i
					+ count * valueChannel.strideSize; i < c; i += valueChannel.strideSize, a += interpolationChannel.strideSize) {
					float startX = valueX.newLowValue() * controller.scale.x;
					float diffX = valueX.newHighValue() * controller.scale.x - startX;

					float startY = valueY.newLowValue() * controller.scale.y;
					float diffY = valueY.newHighValue() * controller.scale.y;

					interpolationChannel.data[a + ParticleChannels.Interpolation4StartOffset + ParticleChannels.XOffset] = startX;
					interpolationChannel.data[a + ParticleChannels.Interpolation4DiffOffset + ParticleChannels.XOffset] = diffX;

					interpolationChannel.data[a + ParticleChannels.Interpolation4StartOffset + ParticleChannels.YOffset] = startY;
					interpolationChannel.data[a + ParticleChannels.Interpolation4DiffOffset + ParticleChannels.YOffset] = diffY;

					valueChannel.data[i + ParticleChannels.XOffset] = startX + diffX * valueX.getScale(0);
					valueChannel.data[i + ParticleChannels.YOffset] = startY + diffY * valueY.getScale(0);
				}
			} else {
				// X and Y both not relative
				for (int i = startIndex * valueChannel.strideSize, a = startIndex * interpolationChannel.strideSize, c = i
					+ count * valueChannel.strideSize; i < c; i += valueChannel.strideSize, a += interpolationChannel.strideSize) {
					float startX = valueX.newLowValue() * controller.scale.x;
					float diffX = valueX.newHighValue() * controller.scale.x - startX;

					float startY = valueY.newLowValue() * controller.scale.y;
					float diffY = valueY.newHighValue() * controller.scale.y - startY;

					interpolationChannel.data[a + ParticleChannels.Interpolation4StartOffset + ParticleChannels.XOffset] = startX;
					interpolationChannel.data[a + ParticleChannels.Interpolation4DiffOffset + ParticleChannels.XOffset] = diffX;

					interpolationChannel.data[a + ParticleChannels.Interpolation4StartOffset + ParticleChannels.YOffset] = startY;
					interpolationChannel.data[a + ParticleChannels.Interpolation4DiffOffset + ParticleChannels.YOffset] = diffY;

					valueChannel.data[i + ParticleChannels.XOffset] = startX + diffX * valueX.getScale(0);
					valueChannel.data[i + ParticleChannels.YOffset] = startY + diffY * valueY.getScale(0);
				}
			}
		}
	}

	@Override
	public void update () {
		for (int i = 0, a = 0, l = ParticleChannels.LifePercentOffset, c = i + controller.particles.size
			* valueChannel.strideSize; i < c; i += valueChannel.strideSize, a += interpolationChannel.strideSize, l += lifeChannel.strideSize) {

			valueChannel.data[i + ParticleChannels.XOffset] = interpolationChannel.data[a
				+ ParticleChannels.Interpolation4StartOffset + ParticleChannels.XOffset]
				+ interpolationChannel.data[a + ParticleChannels.Interpolation4DiffOffset + ParticleChannels.XOffset]
					* valueX.getScale(lifeChannel.data[l]);

			valueChannel.data[i + ParticleChannels.YOffset] = interpolationChannel.data[a
				+ ParticleChannels.Interpolation4StartOffset + ParticleChannels.YOffset]
				+ interpolationChannel.data[a + ParticleChannels.Interpolation4DiffOffset + ParticleChannels.YOffset]
					* valueY.getScale(lifeChannel.data[l]);

		}
	}

	public Scale2Influencer (Scale2Influencer scaleInfluencer) {
		this();
		set(scaleInfluencer);
	}

	@Override
	public ParticleControllerComponent copy () {
		return new Scale2Influencer(this);
	}

	private void set (Scale2Influencer scaleInfluencer) {
		valueX.load(scaleInfluencer.valueX);
		valueY.load(scaleInfluencer.valueY);
		valueChannelDescriptor = scaleInfluencer.valueChannelDescriptor;
	}

	@Override
	public void allocateChannels () {
		valueChannel = controller.particles.addChannel(valueChannelDescriptor);
		ParticleChannels.Interpolation6.id = controller.particleChannels.newId();
		interpolationChannel = controller.particles.addChannel(ParticleChannels.Interpolation4);
		lifeChannel = controller.particles.addChannel(ParticleChannels.Life);
	}

	@Override
	public void write (Json json) {
		json.writeValue("valueX", valueX);
		json.writeValue("valueY", valueY);
	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		valueX = json.readValue("valueX", ScaledNumericValue.class, jsonData);
		valueY = json.readValue("valueY", ScaledNumericValue.class, jsonData);
	}

}
