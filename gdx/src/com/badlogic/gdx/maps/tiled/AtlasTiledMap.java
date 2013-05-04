
package com.badlogic.gdx.maps.tiled;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;

/** A TiledMap that can own TextureAtlases.
 * 
 * @author Justin Shapcott */
public class AtlasTiledMap extends TiledMap {

	private Array<TextureAtlas> ownedAtlases;

	public void setOwnedAtlases (Array<TextureAtlas> atlases) {
		this.ownedAtlases = atlases;
	}

	@Override
	public void dispose () {
		super.dispose();
		if (ownedAtlases != null) {
			for (TextureAtlas atlas : ownedAtlases) {
				atlas.dispose();
			}
		}
	}

}
