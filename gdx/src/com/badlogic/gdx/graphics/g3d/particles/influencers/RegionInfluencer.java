package com.badlogic.gdx.graphics.g3d.particles.influencers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.particles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.particles.PointParticle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class RegionInfluencer<T> extends Influencer<T> {
	
	public static class PointSpriteSingleRegionInfluencer extends RegionInfluencer<PointParticle>{
		
		public PointSpriteSingleRegionInfluencer(){}

		public PointSpriteSingleRegionInfluencer (PointSpriteSingleRegionInfluencer regionInfluencer) {
			super(regionInfluencer);
		}

		public PointSpriteSingleRegionInfluencer (TextureRegion textureRegion) {
			super(textureRegion);
		}
		
		public PointSpriteSingleRegionInfluencer (Texture texture) {
			super(texture);
		}

		@Override
		public void init () {
			AspectTextureRegion region = regions.items[0];
			for(int i=0, c = controller.emitter.maxParticleCount; i < c; ++i){
				PointParticle particle = controller.particles[i];
				particle.u = region.u; particle.v = region.v;
				particle.u2 = region.u2; particle.v2 = region.v2;
			}
		}
		
		@Override
		public PointSpriteSingleRegionInfluencer copy () {
			return new PointSpriteSingleRegionInfluencer(this);
		}
	}
	
	public static class PointSpriteRandomRegionInfluencer extends RegionInfluencer<PointParticle>{
		public PointSpriteRandomRegionInfluencer(){}

		public PointSpriteRandomRegionInfluencer (PointSpriteRandomRegionInfluencer regionInfluencer) {
			super(regionInfluencer);
		}
		
		public PointSpriteRandomRegionInfluencer (TextureRegion textureRegion) {
			super(textureRegion);
		}
		
		public PointSpriteRandomRegionInfluencer (Texture texture) {
			super(texture);
		}

		@Override
		public void activateParticles (int startIndex, int count) {
			for(int i=startIndex, c = startIndex +count; i < c; ++i){
				PointParticle particle = controller.particles[i];
				AspectTextureRegion region = regions.random();
				particle.u = region.u; particle.v = region.v;
				particle.u2 = region.u2; particle.v2 = region.v2;
			}
		}
		
		@Override
		public PointSpriteRandomRegionInfluencer copy () {
			return new PointSpriteRandomRegionInfluencer(this);
		}
	}
	
	public static class PointSpriteAnimatedRegionInfluencer extends RegionInfluencer<PointParticle>{
		public PointSpriteAnimatedRegionInfluencer(){}
		public PointSpriteAnimatedRegionInfluencer (PointSpriteAnimatedRegionInfluencer regionInfluencer) {
			super(regionInfluencer);
		}

		public PointSpriteAnimatedRegionInfluencer (TextureRegion textureRegion) {
			super(textureRegion);
		}
		
		public PointSpriteAnimatedRegionInfluencer (Texture texture) {
			super(texture);
		}

		@Override
		public void update () {
			for(int i=0, c = controller.emitter.activeCount; i < c; ++i){
				PointParticle particle = controller.particles[i];
				AspectTextureRegion region = regions.get( (int)(particle.lifePercent*(regions.size-1)));
				particle.u = region.u; particle.v = region.v;
				particle.u2 = region.u2; particle.v2 = region.v2;
			}
		}
		
		@Override
		public PointSpriteAnimatedRegionInfluencer copy () {
			return new PointSpriteAnimatedRegionInfluencer(this);
		}
	}
	
	
	//Billboards
	
	public static class BillboardRandomRegionInfluencer extends RegionInfluencer<BillboardParticle>{
		public BillboardRandomRegionInfluencer(){}
		public BillboardRandomRegionInfluencer (BillboardRandomRegionInfluencer regionInfluencer) {
			super(regionInfluencer);
		}
		public BillboardRandomRegionInfluencer (TextureRegion textureRegion) {
			super(textureRegion);
		}
		
		public BillboardRandomRegionInfluencer (Texture texture) {
			super(texture);
		}

		@Override
		public void activateParticles (int startIndex, int count) {
			for(int i=startIndex, c = startIndex +count; i < c; ++i){
				BillboardParticle particle = controller.particles[i];
				AspectTextureRegion region = regions.random();
				particle.u = region.u; particle.v = region.v;
				particle.u2 = region.u2; particle.v2 = region.v2;
				particle.halfWidth = 0.5f;
				particle.halfHeight = region.halfInvAspectRatio;
			}
		}
		
		@Override
		public BillboardRandomRegionInfluencer copy () {
			return new BillboardRandomRegionInfluencer(this);
		}
	}
	
	public static class BillboardSingleRegionInfluencer extends RegionInfluencer<BillboardParticle>{
		public BillboardSingleRegionInfluencer(){}

		public BillboardSingleRegionInfluencer (BillboardSingleRegionInfluencer regionInfluencer) {
			super(regionInfluencer);
		}

		public BillboardSingleRegionInfluencer (TextureRegion textureRegion) {
			super(textureRegion);
		}
		
		public BillboardSingleRegionInfluencer (Texture texture) {
			super(texture);
		}

		@Override
		public void init () {
			AspectTextureRegion region = regions.items[0];
			for(int i=0, c = controller.emitter.maxParticleCount; i < c; ++i){
				BillboardParticle particle = controller.particles[i];
				particle.u = region.u; particle.v = region.v;
				particle.u2 = region.u2; particle.v2 = region.v2;
				particle.halfWidth = 0.5f;
				particle.halfHeight = region.halfInvAspectRatio;
			}
		}
		
		@Override
		public BillboardSingleRegionInfluencer copy () {
			return new BillboardSingleRegionInfluencer(this);
		}
	}
	
	public static class BillboardAnimatedRegionInfluencer extends RegionInfluencer<BillboardParticle>{
		public BillboardAnimatedRegionInfluencer(){}
		public BillboardAnimatedRegionInfluencer (BillboardAnimatedRegionInfluencer regionInfluencer) {
			super(regionInfluencer);
		}

		public BillboardAnimatedRegionInfluencer (TextureRegion textureRegion) {
			super(textureRegion);
		}
		
		public BillboardAnimatedRegionInfluencer (Texture texture) {
			super(texture);
		}

		@Override
		public void update () {
			for(int i=0, c = controller.emitter.activeCount; i < c; ++i){
				BillboardParticle particle = controller.particles[i];
				AspectTextureRegion region = regions.get( (int)(particle.lifePercent*(regions.size-1)));
				particle.u = region.u; particle.v = region.v;
				particle.u2 = region.u2; particle.v2 = region.v2;
				particle.halfWidth = 0.5f;
				particle.halfHeight = region.halfInvAspectRatio;
			}
		}
		
		@Override
		public BillboardAnimatedRegionInfluencer copy () {
			return new BillboardAnimatedRegionInfluencer(this);
		}
	}
	
	
	
	public static class AspectTextureRegion{
		public float u, v, u2, v2;
		public float halfInvAspectRatio;
		
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
	
	
	
	public Array<AspectTextureRegion> regions;
	
	public RegionInfluencer(int regionsCount){
		this.regions = new Array<AspectTextureRegion>(false, regionsCount, AspectTextureRegion.class);
	}
	
	public RegionInfluencer(){
		this(1);
		AspectTextureRegion aspectRegion = new AspectTextureRegion();
		aspectRegion.u = aspectRegion.v = 0;
		aspectRegion.u2 = aspectRegion.v2 = 1;
		aspectRegion.halfInvAspectRatio = 0.5f;
		regions.add(aspectRegion);
	}
	
	/** All the regions must be defined on the same Texture */
	public RegionInfluencer(TextureRegion...regions){
		this.regions = new Array<AspectTextureRegion>( false, regions.length, AspectTextureRegion.class);
		add(regions);
	}
	
	public RegionInfluencer(Texture texture){
		this(new TextureRegion(texture));
	}
	
	public RegionInfluencer(RegionInfluencer regionInfluencer){
		this(regionInfluencer.regions.size);
		regions.ensureCapacity(regionInfluencer.regions.size);
		for(int i=0; i < regionInfluencer.regions.size; ++i){
			regions.add(new AspectTextureRegion((AspectTextureRegion)regionInfluencer.regions.get(i)));
		}
	}
	
	public void add (TextureRegion...regions) {
		this.regions.ensureCapacity(regions.length);
		for(TextureRegion region : regions){
			this.regions.add(new AspectTextureRegion(region));
		}
	}
	
	public void clear(){
		regions.clear();
	}

	@Override
	public RegionInfluencer copy () {
		return new RegionInfluencer(this);
	}

	@Override
	public void write (Json json) {
		json.writeValue("regions", regions, Array.class, AspectTextureRegion.class);
	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		regions = json.readValue("regions", Array.class, AspectTextureRegion.class, jsonData);
	}
}
