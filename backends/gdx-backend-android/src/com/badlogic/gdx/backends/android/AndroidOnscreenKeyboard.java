/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.backends.android;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.MovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.badlogic.gdx.Input.Peripheral;

/** Responsible for showing and hiding the Android onscreen keyboard (aka softkeyboard). Uses a dialog with an invisible TextView
 * and injects key down/up and typed events into AndroidInput. Only the delete and back keys will trigger key down/up events.
 * Alphanumeric keys will be directly injected as key typed events which is sufficient to implement things like text fields.
 * 
 * Since the input mechanism for softkeyboards is a bit complex, we don't directly get key events from the softkeyboard. Instead
 * we intercept calls to the Editable of the invisible TextView which we translate into delete key events and key typed events.
 * 
 * @author mzechner */
class AndroidOnscreenKeyboard implements OnKeyListener, OnTouchListener {
	final Context context;
	final Handler handler;
	final AndroidInput input;
	Dialog dialog;
	TextView textView;

	public AndroidOnscreenKeyboard (Context context, Handler handler, AndroidInput input) {
		this.context = context;
		this.handler = handler;
		this.input = input;
	}

	Dialog createDialog () {
		textView = createView(context);
		textView.setOnKeyListener(this);
		FrameLayout.LayoutParams textBoxLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
			FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM);
		textView.setLayoutParams(textBoxLayoutParams);
		textView.setFocusable(true);
		textView.setFocusableInTouchMode(true);
		textView.setImeOptions(textView.getImeOptions() | EditorInfo.IME_FLAG_NO_EXTRACT_UI);

		final FrameLayout layout = new FrameLayout(context);
		ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
		layout.setLayoutParams(layoutParams);
		layout.addView(textView);
		layout.setOnTouchListener(this);

		dialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
		dialog.setContentView(layout);
		return dialog;
	}

	public static TextView createView (Context context) {
		final TextView view = new TextView(context) {
			Editable editable = new PassThroughEditable();

			@Override
			protected boolean getDefaultEditable () {
				return true;
			}

			@Override
			public Editable getEditableText () {
				return editable;
			}

			@Override
			protected MovementMethod getDefaultMovementMethod () {
				return ArrowKeyMovementMethod.getInstance();
			}

			@Override
			public boolean onKeyDown (int keyCode, KeyEvent event) {
				Log.d("Test", "down keycode: " + event.getKeyCode());
				return super.onKeyDown(keyCode, event);
			}

			@Override
			public boolean onKeyUp (int keyCode, KeyEvent event) {
				Log.d("Test", "up keycode: " + event.getKeyCode());
				return super.onKeyUp(keyCode, event);
			}
		};
// view.setCursorVisible(false);
		return view;
	}

	public void setVisible (boolean visible) {
		if (visible && dialog != null) {
			dialog.dismiss();
			dialog = null;
		}
		if (visible && dialog == null && !input.isPeripheralAvailable(Peripheral.HardwareKeyboard)) {
			handler.post(new Runnable() {
				@Override
				public void run () {
					dialog = createDialog();
					dialog.show();

					handler.post(new Runnable() {
						@Override
						public void run () {
							dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
							InputMethodManager input = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
							if (input != null) input.showSoftInput(textView, InputMethodManager.SHOW_FORCED);
						}
					});

					final View content = dialog.getWindow().findViewById(Window.ID_ANDROID_CONTENT);
					content.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
						int[] screenloc = new int[2];
						private int keyboardHeight;
						private boolean keyboardShowing;

						@Override
						public boolean onPreDraw () {
							content.getLocationOnScreen(screenloc);
							keyboardHeight = Math.abs(screenloc[1]);
							if (keyboardHeight > 0) keyboardShowing = true;
							if (keyboardHeight == 0 && keyboardShowing) {
								dialog.dismiss();
								dialog = null;
							}
							return true;
						}
					});
				}
			});
		} else {
			if (!visible && dialog != null) {
				dialog.dismiss();
			}
		}
	}

	public static class PassThroughEditable implements Editable {

		@Override
		public char charAt (int index) {
			Log.d("Editable", "charAt");
			return 0;
		}

		@Override
		public int length () {
			Log.d("Editable", "length");
			return 0;
		}

		@Override
		public CharSequence subSequence (int start, int end) {
			Log.d("Editable", "subSequence");
			return null;
		}

		@Override
		public void getChars (int start, int end, char[] dest, int destoff) {
			Log.d("Editable", "getChars");
		}

		@Override
		public void removeSpan (Object what) {
			Log.d("Editable", "removeSpan");
		}

		@Override
		public void setSpan (Object what, int start, int end, int flags) {
			Log.d("Editable", "setSpan");
		}

		@Override
		public int getSpanEnd (Object tag) {
			Log.d("Editable", "getSpanEnd");
			return 0;
		}

		@Override
		public int getSpanFlags (Object tag) {
			Log.d("Editable", "getSpanFlags");
			return 0;
		}

		@Override
		public int getSpanStart (Object tag) {
			Log.d("Editable", "getSpanStart");
			return 0;
		}

		@Override
		public <T> T[] getSpans (int arg0, int arg1, Class<T> arg2) {
			Log.d("Editable", "getSpans");
			return null;
		}

		@Override
		public int nextSpanTransition (int start, int limit, Class type) {
			Log.d("Editable", "nextSpanTransition");
			return 0;
		}

		@Override
		public Editable append (CharSequence text) {
			Log.d("Editable", "append: " + text);
			return this;
		}

		@Override
		public Editable append (char text) {
			Log.d("Editable", "append: " + text);
			return this;
		}

		@Override
		public Editable append (CharSequence text, int start, int end) {
			Log.d("Editable", "append: " + text);
			return this;
		}

		@Override
		public void clear () {
			Log.d("Editable", "clear");
		}

		@Override
		public void clearSpans () {
			Log.d("Editable", "clearSpanes");
		}

		@Override
		public Editable delete (int st, int en) {
			Log.d("Editable", "delete, " + st + ", " + en);
			return this;
		}

		@Override
		public InputFilter[] getFilters () {
			Log.d("Editable", "getFilters");
			return new InputFilter[0];
		}

		@Override
		public Editable insert (int where, CharSequence text) {
			Log.d("Editable", "insert: " + text);
			return this;
		}

		@Override
		public Editable insert (int where, CharSequence text, int start, int end) {
			Log.d("Editable", "insert: " + text);
			return this;
		}

		@Override
		public Editable replace (int st, int en, CharSequence text) {
			Log.d("Editable", "replace: " + text);
			return this;
		}

		@Override
		public Editable replace (int st, int en, CharSequence source, int start, int end) {
			Log.d("Editable", "replace: " + source);
			return this;
		}

		@Override
		public void setFilters (InputFilter[] filters) {
			Log.d("Editable", "setFilters");
		}
	}

	@Override
	public boolean onTouch (View view, MotionEvent e) {
		return false;
	}

	@Override
	public boolean onKey (View view, int keycode, KeyEvent e) {
		return false;
	}
}
