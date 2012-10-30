
package com.badlogic.gdx.backends.android.livewallpaper;

import com.badlogic.gdx.ApplicationListener;

public abstract class LibgdxWallpaperApp implements ApplicationListener, LibgdxWallpaperListener {
	protected boolean isPreview;

	@Override
	public void setIsPreview (boolean isPreview) {
		this.isPreview = isPreview;
	}
}
