package com.badlogic.gdx.maps.tiled;

import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.utils.TimeUtils;

public class DefaultTileAnimator implements TileAnimator {

	private final long initialTimeOffset = TimeUtils.millis();

	@Override public void updateAnimationBaseTime () {
		AnimatedTiledMapTile.setAnimationTime(TimeUtils.millis() - initialTimeOffset);
	}
}
