package com.badlogic.gdx.graphics.g3d.particles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public  class AspectTextureRegion{
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