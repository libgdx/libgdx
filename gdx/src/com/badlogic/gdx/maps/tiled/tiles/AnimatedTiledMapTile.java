
package com.badlogic.gdx.maps.tiled.tiles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public class AnimatedTiledMapTile implements TiledMapTile {

	private static long lastTiledMapRenderTime = 0;

	private int id;

	private BlendMode blendMode = BlendMode.ALPHA;

	private MapProperties properties;

	private Array<StaticTiledMapTile> frameTiles;

	private float animationInterval;
	private long frameCount = 0;
	private static final long initialTimeOffset = TimeUtils.millis();

	@Override
	public int getId () {
		return id;
	}

	@Override
	public void setId (int id) {
		this.id = id;
	}

	@Override
	public BlendMode getBlendMode () {
		return blendMode;
	}

	@Override
	public void setBlendMode (BlendMode blendMode) {
		this.blendMode = blendMode;
	}

	@Override
	public TextureRegion getTextureRegion () {
		long currentFrame = (lastTiledMapRenderTime / (long)(animationInterval * 1000f)) % frameCount;
		return frameTiles.get((int)currentFrame).getTextureRegion();
	}

	@Override
	public MapProperties getProperties () {
		if (properties == null) {
			properties = new MapProperties();
		}
		return properties;
	}

	/** Function is called by BatchTiledMapRenderer render(), lastTiledMapRenderTime is used to keep all of the tiles in lock-step
	 * animation and avoids having to call TimeUtils.millis() in getTextureRegion() */
	public static void updateAnimationBaseTime () {
		lastTiledMapRenderTime = TimeUtils.millis() - initialTimeOffset;
	}

	public AnimatedTiledMapTile (float interval, Array<StaticTiledMapTile> frameTiles) {
		this.frameTiles = frameTiles;
		this.animationInterval = interval;
		this.frameCount = frameTiles.size;
	}

}
