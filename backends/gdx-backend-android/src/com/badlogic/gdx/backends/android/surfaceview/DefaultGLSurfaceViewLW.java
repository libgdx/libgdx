package com.badlogic.gdx.backends.android.surfaceview;

import android.service.wallpaper.WallpaperService.Engine;
import android.util.AttributeSet;


public class DefaultGLSurfaceViewLW extends GLBaseSurfaceViewLW {

	final ResolutionStrategy resolutionStrategy;

	public DefaultGLSurfaceViewLW(Engine engine,
			ResolutionStrategy resolutionStrategy) {
		super(engine);
		this.resolutionStrategy = resolutionStrategy;
	}

	public DefaultGLSurfaceViewLW(Engine engine, AttributeSet attrs,
			ResolutionStrategy resolutionStrategy) {
		super(engine, attrs);
		this.resolutionStrategy = resolutionStrategy;
	}

	// @Override
	// protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	// ResolutionStrategy.MeasuredDimension measures =
	// resolutionStrategy.calcMeasures(widthMeasureSpec, heightMeasureSpec);
	// setMeasuredDimension(measures.width, measures.height);
	// }

}
