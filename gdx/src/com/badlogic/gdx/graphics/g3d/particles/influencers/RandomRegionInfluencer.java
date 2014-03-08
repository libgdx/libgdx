package com.badlogic.gdx.graphics.g3d.particles.influencers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.particles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.particles.Particle;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.renderers.BillboardRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class RandomRegionInfluencer extends Influencer<BillboardParticle> {
	public  static class AspectTextureRegion{
		float u, v, u2, v2;
		float halfInvAspectRatio;
		
		public AspectTextureRegion(){}
		
		public AspectTextureRegion( AspectTextureRegion aspectTextureRegion){
			set(aspectTextureRegion);
		}
		
		public AspectTextureRegion(TextureRegion region){
			set(region);
		}
		
		public void set(TextureRegion region){
			this.u = region.getU();
			this.v = region.getV();
			this.u2 = region.getU2();
			this.v2 = region.getV2();
			this.halfInvAspectRatio = 0.5f*((float)region.getRegionHeight()/region.getRegionWidth());
		}
		
		public void set(AspectTextureRegion aspectTextureRegion){
			u = aspectTextureRegion.u;
			v = aspectTextureRegion.v;
			u2 = aspectTextureRegion.u2;
			v2 = aspectTextureRegion.v2;
			halfInvAspectRatio = aspectTextureRegion.halfInvAspectRatio;
		}
	}
	
	Array<AspectTextureRegion> regions;
	Texture texture;
	
	public RandomRegionInfluencer(){
		this.regions = new Array<RandomRegionInfluencer.AspectTextureRegion>(false, 3, AspectTextureRegion.class);
	}
	
	/** All the regions must be defined on the same Texture */
	public RandomRegionInfluencer(TextureRegion...regions){
		this.regions = new Array<AspectTextureRegion>( false, regions.length, AspectTextureRegion.class);
		add(regions);
	}
	
	public RandomRegionInfluencer(RandomRegionInfluencer regionInfluencer){
		this();
		regions.ensureCapacity(regionInfluencer.regions.size);
		for(AspectTextureRegion region : regionInfluencer.regions)
			regions.add(new AspectTextureRegion(region));
		this.texture = regionInfluencer.texture;
	}
	
	public void add (TextureRegion...regions) {
		this.regions.ensureCapacity(regions.length);
		texture = regions[0].getTexture();
		for(TextureRegion region : regions){
			this.regions.add(new AspectTextureRegion(region));
		}
	}

	@Override
	public void init () {
		BillboardRenderer renderer = (BillboardRenderer) controller.renderer;
		renderer.setTexture(texture);
	}
	
	@Override
	public void initParticles (int startIndex, int count) {
		for(int i=startIndex, c = startIndex +count; i < c; ++i){
			BillboardParticle particle = controller.particles[i];
			AspectTextureRegion region = regions.get(MathUtils.random(regions.size-1));
			particle.u = region.u; particle.v = region.v;
			particle.u2 = region.u2; particle.v2 = region.v2;
			particle.halfWidth = 0.5f;
			particle.halfHeight = region.halfInvAspectRatio;
		}
	}

	@Override
	public RandomRegionInfluencer copy () {
		return new RandomRegionInfluencer(this);
	}
}
