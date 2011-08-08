package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.Texture;

public class TextureParameter implements AssetLoaderParameters<Texture>{
	/** the format of the final Texture. Uses the source images format if null **/
	public Format format = null;
	/** whether to generate mipmaps **/
	public boolean genMipMaps = false;
	/** The texture to put the {@link TextureData} in **/
	public Texture texture = null;
}
