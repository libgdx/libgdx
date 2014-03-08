package com.badlogic.gdx.graphics.g3d.newparticles.influencers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.newparticles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.newparticles.ModelInstanceParticle;
import com.badlogic.gdx.graphics.g3d.newparticles.Particle;
import com.badlogic.gdx.graphics.g3d.newparticles.ParticleController;
import com.badlogic.gdx.graphics.g3d.newparticles.PointParticle;
import com.badlogic.gdx.graphics.g3d.newparticles.influencers.RandomRegionInfluencer.AspectTextureRegion;
import com.badlogic.gdx.graphics.g3d.newparticles.renderers.BillboardRenderer;
import com.badlogic.gdx.graphics.g3d.newparticles.renderers.PointRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class RandomPointRegionInfluencer extends Influencer<PointParticle> {
	int rows, columns;
	float regionSizeX, regionSizeY;
	Texture texture;
	
	public RandomPointRegionInfluencer(){
	}
	
	public RandomPointRegionInfluencer (Texture texture, int rows, int columns) {
		this();
		this.rows = rows;
		this.columns = columns;
		this.regionSizeX = 1f/columns;
		this.regionSizeY = 1f/rows;
		set(texture);
	}
	
	public RandomPointRegionInfluencer(RandomPointRegionInfluencer regionInfluencer){
		this();
		texture = regionInfluencer.texture;
	}

	public void set (Texture texture) {
		this.texture = texture;
	}
	
	public Texture getTexture(){
		return texture;
	}
	
	@Override
	public void init () {
		PointRenderer renderer = (PointRenderer) controller.renderer;
		renderer.setTexture(texture, regionSizeX, regionSizeY);
	}
	
	public void initParticles(int startIndex, int count) {
		for(int i=startIndex, c = startIndex +count; i < c; ++i){
			PointParticle particle = controller.particles[i];
			particle.u = MathUtils.random(columns-1)*regionSizeX;
			particle.v = MathUtils.random(rows-1)*regionSizeY;
		}
	};
	

	@Override
	public RandomPointRegionInfluencer copy () {
		return new RandomPointRegionInfluencer(this);
	}
}
