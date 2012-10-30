
package com.badlogic.gdx.backends.android.livewallpaper;

public interface LibgdxWallpaperListener {
	public void offsetChange (float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset,
		int yPixelOffset);

	public void setIsPreview (boolean isPreview);
}
