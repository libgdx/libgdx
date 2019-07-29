package com.badlogic.gdx.backends.android.surfaceview;

import android.annotation.TargetApi;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.PointerIcon;

public class AndroidGLSurfaceView extends GLSurfaceView {

	public AndroidGLSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AndroidGLSurfaceView(Context context) {
		super(context);
	}

	public PointerIcon currentPointer;

	@TargetApi(24)
	@Override
	public PointerIcon onResolvePointerIcon (MotionEvent me, int pointerIndex) {
		return currentPointer;
	}
}