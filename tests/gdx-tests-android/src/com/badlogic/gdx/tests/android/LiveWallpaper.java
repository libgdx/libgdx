package com.badlogic.gdx.tests.android;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidLiveWallpaperService;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.tests.Box2DTest;
import com.badlogic.gdx.tests.WaterRipples;

public class LiveWallpaper extends AndroidLiveWallpaperService {
	@Override
	public ApplicationListener createListener () {
		return new WaterRipples();
	}

	@Override
	public AndroidApplicationConfiguration createConfig () {
		return new AndroidApplicationConfiguration();
	}

	@Override
	public void offsetChange (ApplicationListener listener, float xOffset, float yOffset, float xOffsetStep, float yOffsetStep,
		int xPixelOffset, int yPixelOffset) {
		Gdx.app.log("LiveWallpaper", "offset changed: " + xOffset + ", " + yOffset);
	}
}
