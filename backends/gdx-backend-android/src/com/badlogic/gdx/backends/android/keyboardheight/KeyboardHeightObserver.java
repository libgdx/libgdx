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

/** The observer that will be notified when the height of the keyboard has changed */
public interface KeyboardHeightObserver {

	/** Called when the keyboard height has changed, 0 means keyboard is closed, >= 1 means keyboard is opened.
	 *
	 * @param height The height of the keyboard in pixels
	 * @param orientation The orientation either: Configuration.ORIENTATION_PORTRAIT or Configuration.ORIENTATION_LANDSCAPE */
	void onKeyboardHeightChanged (int height, int leftInset, int rightInset, int orientation);
}
