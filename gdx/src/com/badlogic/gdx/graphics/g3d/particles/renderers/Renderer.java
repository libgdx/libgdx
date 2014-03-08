package com.badlogic.gdx.graphics.g3d.particles.renderers;

import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;

public abstract class Renderer<T> extends ParticleSystem<T> implements RenderableProvider{
	public float particlesRefScaleX = 1f, particlesRefScaleY = 1f;
}
