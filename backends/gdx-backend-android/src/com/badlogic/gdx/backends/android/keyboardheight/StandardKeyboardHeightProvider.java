/*
 * This file is part of Siebe Projects samples.
 *
 * Siebe Projects samples is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Siebe Projects samples is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the Lesser GNU General Public License
 * along with Siebe Projects samples.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.badlogic.gdx.backends.android.keyboardheight;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

/** The keyboard height provider, this class uses a PopupWindow to calculate the window height when the floating keyboard is
 * opened and closed. */
public class StandardKeyboardHeightProvider extends PopupWindow implements KeyboardHeightProvider {

	/** The tag for logging purposes */
	private final static String TAG = "sample_KeyboardHeightProvider";

	/** The keyboard height observer */
	private KeyboardHeightObserver observer;

	/** The cached landscape height of the keyboard */
	private static int keyboardLandscapeHeight;

	/** The cached portrait height of the keyboard */
	private static int keyboardPortraitHeight;

	/** The view that is used to calculate the keyboard height */
	private View popupView;

	/** The parent view */
	private View parentView;

	/** The root activity that uses this KeyboardHeightProvider */
	private Activity activity;

	/** Construct a new KeyboardHeightProvider
	 *
	 * @param activity The parent activity */
	public StandardKeyboardHeightProvider (Activity activity) {
		super(activity);
		this.activity = activity;

		LayoutInflater inflator = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout linearLayout = new LinearLayout(inflator.getContext());
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
			LinearLayout.LayoutParams.MATCH_PARENT);
		linearLayout.setLayoutParams(layoutParams);
		this.popupView = linearLayout;
		setContentView(popupView);

		setSoftInputMode(LayoutParams.SOFT_INPUT_ADJUST_RESIZE | LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);

		setWidth(0);
		setHeight(android.view.ViewGroup.LayoutParams.MATCH_PARENT);

		popupView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout () {
				if (popupView != null) {
					handleOnGlobalLayout();
				}
			}
		});
	}

	/** Start the KeyboardHeightProvider, this must be called after the onResume of the Activity. PopupWindows are not allowed to
	 * be registered before the onResume has finished of the Activity. */
	@Override
	public void start () {
		parentView = activity.findViewById(android.R.id.content);
		if (!isShowing() && parentView.getWindowToken() != null) {
			setBackgroundDrawable(new ColorDrawable(0));
			showAtLocation(parentView, Gravity.NO_GRAVITY, 0, 0);
		}
	}

	/** Close the keyboard height provider, this provider will not be used anymore. */
	@Override
	public void close () {
		this.observer = null;
		dismiss();
	}

	/** Set the keyboard height observer to this provider. The observer will be notified when the keyboard height has changed. For
	 * example when the keyboard is opened or closed.
	 *
	 * @param observer The observer to be added to this provider. */
	@Override
	public void setKeyboardHeightObserver (KeyboardHeightObserver observer) {
		this.observer = observer;
	}

	/** Get the screen orientation
	 *
	 * @return the screen orientation */
	private int getScreenOrientation () {
		return activity.getResources().getConfiguration().orientation;
	}

	/** Popup window itself is as big as the window of the Activity. The keyboard can then be calculated by extracting the popup
	 * view bottom from the activity window height. */
	private void handleOnGlobalLayout () {

		Point screenSize = new Point();
		activity.getWindowManager().getDefaultDisplay().getSize(screenSize);

		Rect rect = new Rect();
		popupView.getWindowVisibleDisplayFrame(rect);

		// REMIND, you may like to change this using the fullscreen size of the phone
		// and also using the status bar and navigation bar heights of the phone to calculate
		// the keyboard height. But this worked fine on a Nexus.
		int orientation = getScreenOrientation();
		int keyboardHeight = screenSize.y - rect.bottom;
		int leftInset = rect.left;
		int rightInset = Math.abs(screenSize.x - rect.right + rect.left);

		if (keyboardHeight == 0) {
			notifyKeyboardHeightChanged(0, leftInset, rightInset, orientation);
		} else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			keyboardPortraitHeight = keyboardHeight;
			notifyKeyboardHeightChanged(keyboardPortraitHeight, leftInset, rightInset, orientation);
		} else {
			keyboardLandscapeHeight = keyboardHeight;
			notifyKeyboardHeightChanged(keyboardLandscapeHeight, leftInset, rightInset, orientation);
		}
	}

	/**
	 *
	 */
	private void notifyKeyboardHeightChanged (int height, int leftInset, int rightInset, int orientation) {
		if (observer != null) {
			observer.onKeyboardHeightChanged(height, leftInset, rightInset, orientation);
		}
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
