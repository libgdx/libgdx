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

import com.badlogic.gdx.graphics.g3d.particles.ParallelArray.FloatChannel;
import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels;
import com.badlogic.gdx.graphics.g3d.particles.values.GradientColorValue;
import com.badlogic.gdx.graphics.g3d.particles.values.ScaledNumericValue;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/** It's an {@link Influencer} which controls particles color and transparency.
 * @author Inferno */
public abstract class ColorInfluencer extends Influencer {

	/** It's an {@link Influencer} which assigns a random color when a particle is activated. */
	public static class Random extends ColorInfluencer {
		FloatChannel colorChannel;

		@Override
		public void allocateChannels () {
			colorChannel = controller.particles.addChannel(ParticleChannels.Color);
		}

		@Override
		public void activateParticles (int startIndex, int count) {
			for (int i = startIndex * colorChannel.strideSize, c = i + count * colorChannel.strideSize; i < c; i += colorChannel.strideSize) {
				colorChannel.data[i + ParticleChannels.RedOffset] = MathUtils.random();
				colorChannel.data[i + ParticleChannels.GreenOffset] = MathUtils.random();
				colorChannel.data[i + ParticleChannels.BlueOffset] = MathUtils.random();
				colorChannel.data[i + ParticleChannels.AlphaOffset] = MathUtils.random();
			}
		}

		@Override
		public Random copy () {
			return new Random();
		}
	}

	/** It's an {@link Influencer} which manages the particle color during its life time. */
	public static class Single extends ColorInfluencer {
		FloatChannel alphaInterpolationChannel;
		FloatChannel lifeChannel;
		public ScaledNumericValue alphaValue;
		public GradientColorValue colorValue;

		public Single () {
			colorValue = new GradientColorValue();
			alphaValue = new ScaledNumericValue();
			alphaValue.setHigh(1);
		}

		public Single (Single billboardColorInfluencer) {
			this();
			set(billboardColorInfluencer);
		}

		public void set (Single colorInfluencer) {
			this.colorValue.load(colorInfluencer.colorValue);
			this.alphaValue.load(colorInfluencer.alphaValue);
		}

		@Override
		public void allocateChannels () {
			super.allocateChannels();
			// Hack this allows to share the channel descriptor structure but using a different id temporary
			ParticleChannels.Interpolation.id = controller.particleChannels.newId();
			alphaInterpolationChannel = controller.particles.addChannel(ParticleChannels.Interpolation);
			lifeChannel = controller.particles.addChannel(ParticleChannels.Life);
		}

		@Override
		public void activateParticles (int startIndex, int count) {
			for (int i = startIndex * colorChannel.strideSize, a = startIndex * alphaInterpolationChannel.strideSize, l = startIndex
				* lifeChannel.strideSize + ParticleChannels.LifePercentOffset, c = i + count * colorChannel.strideSize; i < c; i += colorChannel.strideSize, a += alphaInterpolationChannel.strideSize, l += lifeChannel.strideSize) {
				float alphaStart = alphaValue.newLowValue();
				float alphaDiff = alphaValue.newHighValue() - alphaStart;
				colorValue.getColor(0, colorChannel.data, i);
				colorChannel.data[i + ParticleChannels.AlphaOffset] = alphaStart + alphaDiff
					* alphaValue.getScale(lifeChannel.data[l]);
				alphaInterpolationChannel.data[a + ParticleChannels.InterpolationStartOffset] = alphaStart;
				alphaInterpolationChannel.data[a + ParticleChannels.InterpolationDiffOffset] = alphaDiff;
			}
		}

		@Override
		public void update () {
			for (int i = 0, a = 0, l = ParticleChannels.LifePercentOffset, c = i + controller.particles.size
				* colorChannel.strideSize; i < c; i += colorChannel.strideSize, a += alphaInterpolationChannel.strideSize, l += lifeChannel.strideSize) {

				float lifePercent = lifeChannel.data[l];
				colorValue.getColor(lifePercent, colorChannel.data, i);
				colorChannel.data[i + ParticleChannels.AlphaOffset] = alphaInterpolationChannel.data[a
					+ ParticleChannels.InterpolationStartOffset]
					+ alphaInterpolationChannel.data[a + ParticleChannels.InterpolationDiffOffset] * alphaValue.getScale(lifePercent);
			}
		}

		@Override
		public Single copy () {
			return new Single(this);
		}

		@Override
		public void write (Json json) {
			json.writeValue("alpha", alphaValue);
			json.writeValue("color", colorValue);
		}

		@Override
		public void read (Json json, JsonValue jsonData) {
			alphaValue = json.readValue("alpha", ScaledNumericValue.class, jsonData);
			colorValue = json.readValue("color", GradientColorValue.class, jsonData);
		}
	}

	FloatChannel colorChannel;

	@Override
	public void allocateChannels () {
		colorChannel = controller.particles.addChannel(ParticleChannels.Color);
	}
}
