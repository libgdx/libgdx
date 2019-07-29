package com.badlogic.gdx.backends.android.surfaceview;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.view.SurfaceView;

public class AndroidSurfaceView extends SurfaceView {

	public PointerIcon currentPointer;

	public AndroidSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AndroidSurfaceView(Context context) {
		super(context);
	}

	@TargetApi(24)
	@Override
	public PointerIcon onResolvePointerIcon (MotionEvent me, int pointerIndex) {
		return currentPointer;
	}
}