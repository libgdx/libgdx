package com.badlogic.gdx.tools.particleeditor3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.resolvers.AbsoluteFileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.ExternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.particles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.influencers.RegionInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.values.ParticleValue;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.tools.particleeditor3d.ParticleEditor3D.ResourceLoadListener;

public class RegionInfluencerPanel extends ImagePanel {

	public RegionInfluencerPanel (ParticleEditor3D editor, RegionInfluencer regionInfluencer) {
		super(editor, "Region Influencer", "Sets the billboard texture");
		//set(regionInfluencer);
	}

	@Override
	protected void setDefaultImage () {
		ParticleController<BillboardParticle> emitter = editor.getEmitter();
		final RegionInfluencer influencer = emitter.findInfluencer(RegionInfluencer.class);
		final String currentTexturePath = editor.assetManager.getAssetFileName(influencer.getTexture());
		if(currentTexturePath != ParticleEditor3D.DEFAULT_BILLBOARD_PARTICLE){
			setTexture(influencer, (Texture)editor.assetManager.get(ParticleEditor3D.DEFAULT_BILLBOARD_PARTICLE), currentTexturePath);
		}
	}
	

	@Override
	protected void onImageFileSelected (String absolutePath) {
		ParticleController<BillboardParticle> emitter = editor.getEmitter();
		final RegionInfluencer influencer = emitter.findInfluencer(RegionInfluencer.class);

		final String currentTexturePath = editor.assetManager.getAssetFileName(influencer.getTexture());
		if(currentTexturePath != absolutePath){
			setTexture(influencer, editor.load(absolutePath, Texture.class, new TextureLoader(new AbsoluteFileHandleResolver())), currentTexturePath);
			/*
			editor.load(absolutePath, Texture.class, new TextureLoader(new AbsoluteFileHandleResolver()), new ResourceLoadListener<Texture>(){
				@Override
				public void onLoad (String resourcePath, Texture resource) {
					setTexture(influencer, resource, currentTexturePath);
				}
			});
			*/
		}
	}
	
	protected void setTexture (RegionInfluencer influencer, Texture resource, String currentTexturePath) {
		if(resource != null){
			influencer.set(resource);
			influencer.start();
			editor.assetManager.setReferenceCount(currentTexturePath, editor.assetManager.getReferenceCount(currentTexturePath)-1);
		}
	}

}
