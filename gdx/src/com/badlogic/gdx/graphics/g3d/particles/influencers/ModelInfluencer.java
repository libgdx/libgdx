package com.badlogic.gdx.graphics.g3d.particles.influencers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.particles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.particles.ModelInstanceParticle;
import com.badlogic.gdx.graphics.g3d.particles.Particle;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.renderers.BillboardRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class ModelInfluencer extends Influencer<ModelInstanceParticle> {
	Model model;
	
	/** All the regions must be defined on the same Texture */
	public ModelInfluencer(Model model){
		set(model);
	}
	
	private void set (Model model) {
		this.model = model;
	}

	@Override
	public void init () {
		ModelInstanceParticle[] particles = controller.particles;
		for(int i=0; i < controller.emitter.maxParticleCount; ++i){
			particles[i].instance = new ModelInstance(model);
		}
	}

	@Override
	public ModelInfluencer copy () {
		return new ModelInfluencer(model);
	}
}
