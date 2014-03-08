package com.badlogic.gdx.graphics.g3d.newparticles.influencers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.newparticles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.newparticles.Particle;
import com.badlogic.gdx.graphics.g3d.newparticles.ParticleController;
import com.badlogic.gdx.graphics.g3d.newparticles.PointParticle;
import com.badlogic.gdx.graphics.g3d.newparticles.influencers.RandomRegionInfluencer.AspectTextureRegion;
import com.badlogic.gdx.graphics.g3d.newparticles.renderers.BillboardRenderer;
import com.badlogic.gdx.graphics.g3d.newparticles.renderers.PointRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class PointRegionInfluencer extends Influencer<PointParticle> {
	Texture texture;
	
	public PointRegionInfluencer(){}

	public PointRegionInfluencer (Texture texture) {
		this();
		set(texture);
	}
	
	public PointRegionInfluencer(PointRegionInfluencer regionInfluencer){
		this();
		texture = regionInfluencer.texture;
	}

	public void set (TextureRegion region) {
		texture = region.getTexture();
	}
	
	public void set (Texture texture) {
		set(new TextureRegion(texture));
	}
	
	public Texture getTexture(){
		return texture;
	}
	
	@Override
	public void init () {
		PointRenderer renderer = (PointRenderer) controller.renderer;
		renderer.setTexture(texture);
	}

	@Override
	public PointRegionInfluencer copy () {
		return new PointRegionInfluencer(this);
	}
}
