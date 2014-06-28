package com.badlogic.gdx.graphics.g3d.particles.renderers;

import com.badlogic.gdx.graphics.g3d.particles.ParallelArray.FloatChannel;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;

/** Render data used by particle controller renderer
 * @author Inferno */
public abstract class ParticleControllerRenderData {
	public ParticleController controller;
	public FloatChannel positionChannel;
}
