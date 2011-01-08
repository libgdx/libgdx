package com.badlogic.gdx.backends.android.surfaceview;

import android.view.View;

/**
 * This {@link ResolutionStrategy} will strech the GLSurfaceView to full screen.
 * FillResolutionStrategy is the default {@link ResolutionStrategy} if none is specified.
 */
public class FillResolutionStrategy implements ResolutionStrategy {


    @Override
    public MeasuredDimension calcMeasures(int widthMeasureSpec, int heightMeasureSpec) {

        final int width = View.MeasureSpec.getSize(widthMeasureSpec);
        final int height = View.MeasureSpec.getSize(heightMeasureSpec);

        return new MeasuredDimension(width, height);
    }
}
