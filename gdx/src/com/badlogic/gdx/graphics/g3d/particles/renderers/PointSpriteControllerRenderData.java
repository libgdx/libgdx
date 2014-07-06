package com.badlogic.gdx.graphics.g3d.particles.renderers;

import com.badlogic.gdx.graphics.g3d.particles.ParallelArray.FloatChannel;

/** Render data used by point sprites batches
 * @author Inferno */
public class PointSpriteControllerRenderData extends ParticleControllerRenderData{
	public FloatChannel 	regionChannel, 
												colorChannel, 
												scaleChannel,
												rotationChannel;
												
}
