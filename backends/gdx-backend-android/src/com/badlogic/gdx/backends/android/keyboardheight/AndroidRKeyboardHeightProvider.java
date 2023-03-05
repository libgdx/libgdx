
package com.badlogic.gdx.backends.android.keyboardheight;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Insets;
import android.os.Build;
import android.view.View;
import android.view.WindowInsets;

@androidx.annotation.RequiresApi(api = Build.VERSION_CODES.R)
public class AndroidRKeyboardHeightProvider implements KeyboardHeightProvider {

	private final View view;
	private final Activity activity;
	private KeyboardHeightObserver observer;

	/** The cached landscape height of the keyboard */
	private static int keyboardLandscapeHeight;

	/** The cached portrait height of the keyboard */
	private static int keyboardPortraitHeight;

	public AndroidRKeyboardHeightProvider (final Activity activity) {
		this.view = activity.findViewById(android.R.id.content);
		this.activity = activity;
	}

	@Override
	public void start () {
		view.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
			@Override
			public WindowInsets onApplyWindowInsets (View v, WindowInsets windowInsets) {
				if (observer == null) return windowInsets;
				int orientation = activity.getResources().getConfiguration().orientation;
				boolean isVisible = windowInsets.isVisible(WindowInsets.Type.ime());
				if (isVisible) {
					Insets insets = windowInsets.getInsets(WindowInsets.Type.ime());
					if (orientation == Configuration.ORIENTATION_PORTRAIT) {
						keyboardPortraitHeight = insets.bottom;
					} else {
						keyboardLandscapeHeight = insets.bottom;
					}

					// I don't know whether I went completly insane now, but WindowInsets.Type.all() isn't existing?
					int	leftInset = windowInsets.getInsets(0xFFFFFFFF).left;
					int	rightInset = windowInsets.getInsets(0xFFFFFFFF).right;

					observer.onKeyboardHeightChanged(insets.bottom, leftInset, rightInset, orientation);
				} else {
					observer.onKeyboardHeightChanged(0, 0, 0, orientation);
				}

				return windowInsets;
			}
		});
	}

	@Override
	public void close () {
		view.setOnApplyWindowInsetsListener(null);
	}

	@Override
	public void setKeyboardHeightObserver (KeyboardHeightObserver observer) {
		this.observer = observer;
	}

	@Override
	public int getKeyboardLandscapeHeight () {
		return keyboardLandscapeHeight;
	}

	@Override
	public int getKeyboardPortraitHeight () {
		return keyboardPortraitHeight;
	}
}
