
package com.badlogic.gdx.backends.android.keyboardheight;

import android.annotation.SuppressLint;
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
				int orientation = activity.getResources().getConfiguration().orientation;
				boolean isVisible = windowInsets.isVisible(WindowInsetsCompat.Type.ime());
				if (isVisible) {
					Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
					if (orientation == Configuration.ORIENTATION_PORTRAIT) {
						keyboardPortraitHeight = insets.bottom;
					} else {
						keyboardLandscapeHeight = insets.bottom;
					}

					// I don't know whether I went completly insane now, but WindowInsets.Type.all() isn't existing?
					@SuppressLint("WrongConstant")
					int leftInset = windowInsets.getInsets(0xFFFFFFFF).left;
					@SuppressLint("WrongConstant")
					int rightInset = windowInsets.getInsets(0xFFFFFFFF).right;

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
		ViewCompat.setOnApplyWindowInsetsListener(view, null);
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
