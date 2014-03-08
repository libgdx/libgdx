package com.badlogic.gdx.graphics.g3d.newparticles.influencers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.newparticles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.newparticles.Particle;
import com.badlogic.gdx.graphics.g3d.newparticles.ParticleController;
import com.badlogic.gdx.graphics.g3d.newparticles.influencers.RandomRegionInfluencer.AspectTextureRegion;
import com.badlogic.gdx.graphics.g3d.newparticles.renderers.BillboardRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class RegionInfluencer extends Influencer<BillboardParticle> {
	AspectTextureRegion region;
	Texture texture;
	
	public RegionInfluencer(){
		region = new AspectTextureRegion();
	}
	
	/** All the regions must be defined on the same Texture */
	public RegionInfluencer(TextureRegion region){
		this();
		set(region);
	}

	public RegionInfluencer (Texture texture) {
		this();
		set(texture);
	}
	
	public RegionInfluencer(RegionInfluencer regionInfluencer){
		this();
		region.set(regionInfluencer.region);
		texture = regionInfluencer.texture;
	}

	public void set (TextureRegion region) {
		texture = region.getTexture();
		this.region.set(region);
	}
	
	public void set (Texture texture) {
		set(new TextureRegion(texture));
	}
	
	public Texture getTexture(){
		return texture;
	}
	
	@Override
	public void init () {
		BillboardRenderer renderer = (BillboardRenderer) controller.renderer;
		renderer.setTexture(texture);
		for(int i=0, c = controller.emitter.maxParticleCount; i < c; ++i){
			BillboardParticle particle = controller.particles[i];
			particle.u = region.u; particle.v = region.v;
			particle.u2 = region.u2; particle.v2 = region.v2;
			particle.halfWidth = 0.5f;
			particle.halfHeight = region.halfInvAspectRatio;
		}
	}

	@Override
	public RegionInfluencer copy () {
		return new RegionInfluencer(this);
	}
}
