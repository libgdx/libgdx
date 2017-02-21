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

package com.badlogic.gdx.graphics.g3d.particles.renderers;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.particles.ParallelArray.ObjectChannel;
import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.ParticleControllerComponent;
import com.badlogic.gdx.graphics.g3d.particles.batches.ModelInstanceParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.batches.ParticleBatch;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** A {@link ParticleControllerRenderer} which will render the {@link ParticleController} of each particle.
 * @author Inferno */
@SuppressWarnings("rawtypes")
public class ParticleControllerControllerRenderer extends ParticleControllerRenderer {
	ObjectChannel<ParticleController> controllerChannel;

	@Override
	public void init () {
		controllerChannel = controller.particles.getChannel(ParticleChannels.ParticleController);
		if (controllerChannel == null)
			throw new GdxRuntimeException(
				"ParticleController channel not found, specify an influencer which will allocate it please.");
	}

	@Override
	public void update () {
		for (int i = 0, c = controller.particles.size; i < c; ++i) {
			controllerChannel.data[i].draw();
		}
	}

	@Override
	public ParticleControllerComponent copy () {
		return new ParticleControllerControllerRenderer();
	}

	@Override
	public boolean isCompatible (ParticleBatch batch) {
		return false;
	}

}
