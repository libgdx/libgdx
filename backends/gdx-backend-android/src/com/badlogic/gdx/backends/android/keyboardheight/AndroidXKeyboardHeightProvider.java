
package com.badlogic.gdx.backends.android.keyboardheight;

import android.app.Activity;
import android.content.res.Configuration;
import android.view.View;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import org.jetbrains.annotations.NotNull;

public class AndroidXKeyboardHeightProvider implements KeyboardHeightProvider {

	private final Activity activity;
	private View view;
	private KeyboardHeightObserver observer;

	/** The cached landscape height of the keyboard */
	private static int keyboardLandscapeHeight;

	/** The cached portrait height of the keyboard */
	private static int keyboardPortraitHeight;

	/** The cached visible value of the keyboard */
	private static boolean cachedVisible;
	/** The cached inset to the left */
	private static int cachedInsetLeft;
	/** The cached inset to the right */
	private static int cachedInsetRight;
	/** The cached inset to the bottom */
	private static int cachedBottomInset;
	/** The cached orientation of the app */
	private static int cachedOrientation;

	public AndroidXKeyboardHeightProvider (final Activity activity) {
		this.activity = activity;
	}

	@Override
	public void start () {
		this.view = activity.findViewById(android.R.id.content);
		ViewCompat.setOnApplyWindowInsetsListener(view, new OnApplyWindowInsetsListener() {
			@NotNull
			@Override
			public WindowInsetsCompat onApplyWindowInsets (@NotNull View v, @NotNull WindowInsetsCompat windowInsets) {
				if (observer == null) return windowInsets;
				int bottomInset = 0;
				int leftInset = 0;
				int rightInset = 0;

				int orientation = activity.getResources().getConfiguration().orientation;
				boolean isVisible = windowInsets.isVisible(WindowInsetsCompat.Type.ime());
				if (isVisible) {
					int inset = WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime()
						| WindowInsetsCompat.Type.displayCutout() | WindowInsetsCompat.Type.mandatorySystemGestures();

					Insets insets = windowInsets.getInsets(inset);
					if (orientation == Configuration.ORIENTATION_PORTRAIT) {
						keyboardPortraitHeight = insets.bottom;
					} else {
						keyboardLandscapeHeight = insets.bottom;
					}

					bottomInset = insets.bottom;
					leftInset = insets.left;
					rightInset = insets.right;
				}

				if (isVisible == cachedVisible && bottomInset == cachedBottomInset && leftInset == cachedInsetLeft
					&& rightInset == cachedInsetRight && orientation == cachedOrientation) return windowInsets;

				cachedVisible = isVisible;
				cachedBottomInset = bottomInset;
				cachedInsetLeft = leftInset;
				cachedInsetRight = rightInset;
				cachedOrientation = orientation;

				observer.onKeyboardHeightChanged(isVisible, bottomInset, leftInset, rightInset, orientation);

				return windowInsets;
			}
		});
	}

	@Override
	public void close () {
		if (view != null) ViewCompat.setOnApplyWindowInsetsListener(view, null);
		this.observer = null;
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
