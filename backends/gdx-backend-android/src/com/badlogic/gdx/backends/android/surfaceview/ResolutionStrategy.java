package com.badlogic.gdx.backends.android.surfaceview;

public interface ResolutionStrategy {


    public MeasuredDimension calcMeasures( final int widthMeasureSpec, final int heightMeasureSpec);


    public static class MeasuredDimension {
        public final int width;
        public final int height;

        public MeasuredDimension(int width, int height) {
            this.width = width;
            this.height = height;
        }

    }




}
