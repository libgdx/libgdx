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

import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels;
import com.badlogic.gdx.graphics.g3d.particles.ParticleControllerComponent;

/** It's an {@link Influencer} which controls the scale of the particles.
 * @author Inferno
 * @author Pieter Schaap - Changed the scaling from 1 dimensional to 3 dimensional scaling. */
public class ScaleInfluencer extends SimpleInfluencer {

	public ScaleInfluencer () {
		super();
		valueChannelDescriptor = ParticleChannels.Scale;
	}

	@Override
	public void activateParticles (int startIndex, int count) {
		if (value.isRelative()) {
			for (int i = startIndex * valueChannel.strideSize, a = startIndex * interpolationChannel.strideSize, c = i
				+ count * valueChannel.strideSize; i < c; i += valueChannel.strideSize, a += interpolationChannel.strideSize) {
				float startX = value.newLowValue() * controller.scale.x;
				float diffX = value.newHighValue() * controller.scale.x;
				float startY = value.newLowValue() * controller.scale.y;
				float diffY = value.newHighValue() * controller.scale.y;
				float startZ = value.newLowValue() * controller.scale.z;
				float diffZ = value.newHighValue() * controller.scale.z;

				interpolationChannel.data[a + ParticleChannels.Interpolation6StartOffset + ParticleChannels.XOffset] = startX;
				interpolationChannel.data[a + ParticleChannels.Interpolation6DiffOffset + ParticleChannels.XOffset] = diffX;

				interpolationChannel.data[a + ParticleChannels.Interpolation6StartOffset + ParticleChannels.YOffset] = startY;
				interpolationChannel.data[a + ParticleChannels.Interpolation6DiffOffset + ParticleChannels.YOffset] = diffY;

				interpolationChannel.data[a + ParticleChannels.Interpolation6StartOffset + ParticleChannels.ZOffset] = startZ;
				interpolationChannel.data[a + ParticleChannels.Interpolation6DiffOffset + ParticleChannels.ZOffset] = diffZ;

				valueChannel.data[i + ParticleChannels.XOffset] = startX + diffX * value.getScale(0);
				valueChannel.data[i + ParticleChannels.YOffset] = startY + diffY * value.getScale(0);
				valueChannel.data[i + ParticleChannels.ZOffset] = startZ + diffZ * value.getScale(0);
			}
		} else {
			for (int i = startIndex * valueChannel.strideSize, a = startIndex * interpolationChannel.strideSize, c = i
				+ count * valueChannel.strideSize; i < c; i += valueChannel.strideSize, a += interpolationChannel.strideSize) {
				float startX = value.newLowValue() * controller.scale.x;
				float diffX = value.newHighValue() * controller.scale.x - startX;

				float startY = value.newLowValue() * controller.scale.y;
				float diffY = value.newHighValue() * controller.scale.y - startY;

				float startZ = value.newLowValue() * controller.scale.z;
				float diffZ = value.newHighValue() * controller.scale.z - startZ;

				interpolationChannel.data[a + ParticleChannels.Interpolation6StartOffset + ParticleChannels.XOffset] = startX;
				interpolationChannel.data[a + ParticleChannels.Interpolation6DiffOffset + ParticleChannels.XOffset] = diffX;

				interpolationChannel.data[a + ParticleChannels.Interpolation6StartOffset + ParticleChannels.YOffset] = startY;
				interpolationChannel.data[a + ParticleChannels.Interpolation6DiffOffset + ParticleChannels.YOffset] = diffY;

				interpolationChannel.data[a + ParticleChannels.Interpolation6StartOffset + ParticleChannels.ZOffset] = startZ;
				interpolationChannel.data[a + ParticleChannels.Interpolation6DiffOffset + ParticleChannels.ZOffset] = diffZ;

				valueChannel.data[i + ParticleChannels.XOffset] = startX + diffX * value.getScale(0);
				valueChannel.data[i + ParticleChannels.YOffset] = startY + diffY * value.getScale(0);
				valueChannel.data[i + ParticleChannels.ZOffset] = startZ + diffZ * value.getScale(0);
			}
		}
	}

	public ScaleInfluencer (ScaleInfluencer scaleInfluencer) {
		super(scaleInfluencer);
	}

	@Override
	public ParticleControllerComponent copy () {
		return new ScaleInfluencer(this);
	}

}
