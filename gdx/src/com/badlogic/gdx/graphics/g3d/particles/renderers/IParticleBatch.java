package com.badlogic.gdx.graphics.g3d.particles.renderers;

import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;

public interface IParticleBatch<T> extends RenderableProvider {
	public void begin();
	public <K extends ParticleController<T>> void draw (K controller);
	public void end();
}
