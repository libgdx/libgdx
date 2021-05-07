package com.badlogic.gdx.maps.tiled;

import com.badlogic.gdx.utils.TimeUtils;

public class DefaultTileAnimator implements TileAnimator {

	private final long initialTimeOffset = TimeUtils.millis();

	@Override public long getAnimationBaseTime () {
		return TimeUtils.millis() - initialTimeOffset;
	}
}
