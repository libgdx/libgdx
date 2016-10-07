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

import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.ParticleControllerComponent;
import com.badlogic.gdx.graphics.g3d.particles.batches.ParticleBatch;

/** It's a {@link ParticleControllerComponent} which determines how the particles are rendered. It's the base class of every
 * particle renderer.
 * @author Inferno */
public abstract class ParticleControllerRenderer<D extends ParticleControllerRenderData, T extends ParticleBatch<D>> extends
	ParticleControllerComponent {
	protected T batch;
	protected D renderData;

	protected ParticleControllerRenderer () {
	}

	protected ParticleControllerRenderer (D renderData) {
		this.renderData = renderData;
	}

	@Override
	public void update () {
		batch.draw(renderData);
	}

	@SuppressWarnings("unchecked")
	public boolean setBatch (ParticleBatch<?> batch) {
		if (isCompatible(batch)) {
			this.batch = (T)batch;
			return true;
		}
		return false;
	}

	public abstract boolean isCompatible (ParticleBatch<?> batch);

	@Override
	public void set (ParticleController particleController) {
		super.set(particleController);
		if (renderData != null) renderData.controller = controller;
	}
}
