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

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.ResultReceiver;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.input.NativeInputConfiguration;
import com.badlogic.gdx.input.NativeInputConfiguration.NativeInputCloseCallback;
import com.badlogic.gdx.utils.Null;

/** Bundles the machinery backing {@link com.badlogic.gdx.Input#openTextInputField(NativeInputConfiguration)}: the native edit
 * text hierarchy, its per-open configuration, its layout relative to the keyboard and the result write-back. Created lazily on
 * the UI thread by {@link DefaultAndroidInput#openTextInputField(NativeInputConfiguration)} and kept for the lifetime of the
 * input: the view hierarchy is built once on construction and reused across sessions, with
 * {@link #open(NativeInputConfiguration)} and {@link #close(boolean, NativeInputCloseCallback)} reconfiguring and toggling it.
 * Keyboard events are handled and dispatched by {@link DefaultAndroidInput}, which instructs this to re-layout or close. */
public class AndroidNativeInput {
	protected final Context context;
	protected final Handler handle;
	protected final View view;

	/** The configuration of the active session, set in {@link #open(NativeInputConfiguration)} and nulled once
	 * {@link #close(boolean, NativeInputCloseCallback)} processes. */
	protected NativeInputConfiguration configuration;

	/** The view hierarchy holding the edit text: created on construction, reused across sessions and toggled visible/invisible. */
	protected final RelativeLayout relativeLayoutField;

	protected final AutoCompleteTextView textView;

	/** Must be created on the UI thread, as this attaches the edit text hierarchy to the view tree. */
	public AndroidNativeInput (Context context, Handler handle, View view) {
		this.context = context;
		this.handle = handle;
		this.view = view;
		this.relativeLayoutField = createDefaultEditText();
		this.textView = (AutoCompleteTextView)relativeLayoutField.getChildAt(0);
	}

	protected boolean isOpen () {
		// Can happen during innit
		if (relativeLayoutField == null) return false;
		return relativeLayoutField.getVisibility() == View.VISIBLE;
	}

	protected AutoCompleteTextView getEditTextForNativeInput () {
		return textView;
	}

	protected void setFieldY (float y) {
		relativeLayoutField.setY(y);
	}

	/** Configures the edit text for the given configuration and shows it together with the soft keyboard. Must run on the UI
	 * thread. */
	public void open (final NativeInputConfiguration configuration) {
		if (isOpen()) return;
		this.configuration = configuration;
		AutoCompleteTextView editText = textView;

		InputMethodManager manager = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);

		final int imeAction = getAndroidImeAction(configuration.getReturnKeyType());
		editText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction (TextView textView, int actionId, android.view.KeyEvent keyEvent) {
				if (actionId == imeAction) {
					Gdx.input.closeTextInputField(true);
					return true;
				}
				return true;
			}
		});

		// Needs to be done first, for some reason...
		if (!configuration.isMaskInput()) {
			editText.setTransformationMethod(null);
		}

		editText.setInputType(DefaultAndroidInput.getAndroidInputType(configuration.getType(), false));

		if (configuration.isPreventCorrection()) {
			editText.setInputType(editText.getInputType() | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
			editText.setInputType(editText.getInputType() & ~InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
		} else {
			editText.setInputType(editText.getInputType() | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
			editText.setInputType(editText.getInputType() & ~InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		}

		// Subtract all capitilisations out first
		editText.setInputType(editText.getInputType() & ~(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
			| InputType.TYPE_TEXT_FLAG_CAP_WORDS | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS));
		switch (configuration.getAutocapitalization()) {
		case SENTENCES:
			editText.setInputType(editText.getInputType() | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
			break;
		case WORDS:
			editText.setInputType(editText.getInputType() | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
			break;
		case CHARACTERS:
			editText.setInputType(editText.getInputType() | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
			break;
		case NONE:
			break;
		}

		if (Build.VERSION.SDK_INT >= 26 && configuration.getContentType() != null) {
			editText.setAutofillHints(getAndroidAutofillHints(configuration.getContentType()));
		}

		editText.setImeOptions(imeAction);
		if (configuration.isMultiLine()) {
			editText.setInputType(editText.getInputType() | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
			editText.setImeOptions(editText.getImeOptions() | EditorInfo.IME_FLAG_NO_FULLSCREEN);
			// For cursor control support
			editText.setWidth(Gdx.graphics.getWidth());
			editText.setLines(3);
		} else {
			editText.setImeOptions(editText.getImeOptions() | EditorInfo.IME_FLAG_NO_FULLSCREEN);
			editText.setSingleLine();
		}
		// Reset filters to not run into a issue, where the max length filter messes with setText
		// But, we can't set the correct filters here, because that leads to problems for some apparent reason nobody will
		// ever understand
		editText.setFilters(new InputFilter[] {});
		editText.setText(configuration.getTextInputWrapper().getText());
		editText.setHint(configuration.getPlaceholder());

		InputFilter filter = new InputFilter() {
			@Override
			public CharSequence filter (CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
				boolean keepOriginal = true;
				StringBuilder sb = new StringBuilder(end - start);
				for (int i = start; i < end; i++) {
					char c = source.charAt(i);
					// TODO: 02.08.2022 There is a backend incosistenty between iOS and android. On Autocomplete
					// iOS would delete whole words, while android only deletes characters. We should make it
					// consistent. However that seems not that trivial and it first needs to be decided, which
					// behavior the correct one is.
					if (configuration.getValidator() == null || configuration.getValidator().validate(c + ""))
						sb.append(c);
					else
						keepOriginal = false;
				}
				if (keepOriginal)
					return null;
				else {
					if (source instanceof Spanned) {
						SpannableString sp = new SpannableString(sb);
						TextUtils.copySpansFrom((Spanned)source, start, sb.length(), null, sp, 0);
						return sp;
					} else {
						return sb;
					}
				}
			}
		};
		InputFilter[] filters = new InputFilter[] {filter};
		if (configuration.getMaxLength() != -1) {
			filters = new InputFilter[] {filter, new LengthFilter(configuration.getMaxLength())};
		}

		editText.setFilters(filters);

		if (configuration.getAutoComplete() != null) {
			ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line,
				configuration.getAutoComplete());
			editText.setAdapter(adapter);
		} else {
			editText.setAdapter(null);
		}

		float density = context.getResources().getDisplayMetrics().density;
		float radius = configuration.getCornerRadius() * density;
		GradientDrawable background = new GradientDrawable();
		background.setColor(com.badlogic.gdx.graphics.Color.argb8888(configuration.getBackgroundColor()));
		background.setCornerRadius(radius);
		editText.setBackground(background);
		int horizontalPadding = (int)(configuration.getTextMargin() * density);
		editText.setPadding(horizontalPadding, editText.getPaddingTop(), horizontalPadding, editText.getPaddingBottom());
		editText.setTextColor(com.badlogic.gdx.graphics.Color.argb8888(configuration.getTextColor()));
		editText.setHintTextColor(com.badlogic.gdx.graphics.Color.argb8888(configuration.getPlaceholderColor()));

		if (configuration.isMaskInput()) {
			// For some reason this needs to be done last, otherwise it won't work
			editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
			if (configuration.isShowUnmaskButton()) {
				final ImageView imageView = new ImageView(context);

				imageView.setImageResource(com.badlogic.gdx.backends.android.R.drawable.design_ic_visibility);
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
				params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				params.rightMargin = 10;
				params.height = editText.getHeight();
				params.width = editText.getHeight();

				imageView.setLayoutParams(params);
				imageView.setOnClickListener(new View.OnClickListener() {
					private boolean isHidding = true;

					@Override
					public void onClick (View v) {
						int start = editText.getSelectionStart();
						int end = editText.getSelectionStart();
						isHidding = !isHidding;
						if (isHidding) {
							editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
							imageView.setImageResource(com.badlogic.gdx.backends.android.R.drawable.design_ic_visibility);
						} else {
							editText.setTransformationMethod(null);
							imageView.setImageResource(com.badlogic.gdx.backends.android.R.drawable.design_ic_visibility_off);
						}
						// Seems to get reset by "setTransformationMethod"
						editText.setSelection(start, end);
					}
				});
				imageView.setAlpha(0.5f);
				imageView.setPadding(5, 5, 5, 5);
				relativeLayoutField.addView(imageView);
			}
		}

		// One wonders why here? I don't know!
		editText.setSelection(configuration.getTextInputWrapper().getSelectionStart(),
			configuration.getTextInputWrapper().getSelectionEnd());

		if (configuration.getFieldCustomizer() != null) configuration.getFieldCustomizer().customize(this);

		relativeLayoutField.setVisibility(View.VISIBLE);

		editText.requestFocus();
		manager.showSoftInput(editText, 0);
	}

	/** Hides the edit text and writes back the results, ending the active session. Must run on the UI thread. */
	public void close (boolean isConfirmative, @Null NativeInputCloseCallback callback) {
		if (!isOpen()) return;
		view.requestFocus();

		EditText editText = textView;
		String text = editText.getText().toString();
		int selectionStart = editText.getSelectionStart();
		int selectionEnd = editText.getSelectionEnd();
		NativeInputConfiguration config = configuration;

		Gdx.app.postRunnable( () -> {
			config.getTextInputWrapper().writeResults(text, selectionStart, selectionEnd);

			boolean keepOpen = config.getCloseCallback().onClose(isConfirmative);
			if (callback != null) keepOpen |= callback.onClose(isConfirmative);

			if (!keepOpen) {
				handle.post( () -> {
					if (isOpen()) return;
					InputMethodManager manager = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
					manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
				});
			}
		});

		configuration = null;

		relativeLayoutField.setVisibility(View.INVISIBLE);
	}

	protected void layoutFieldAboveKeyboard (int height, int leftInset, int rightInset) {
		// @off
		if ((((Activity)context).getWindow().getAttributes().softInputMode & WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST) != WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING) {
			height = 0;
		}

		// This is legit insanity. If we want to animate over `scaleX`, FOR REASONS NO-ONE WILL EVER UNDERSTAND, the keyboard doesn't push the views up if `scaleX` < 1 at start.
		// Unless you are very familiar with android views and animations, do _not_ touch this code, unless absolutely necessary.
		FrameLayout.LayoutParams containerParams = (FrameLayout.LayoutParams) relativeLayoutField.getLayoutParams();

		int parentWidth = ((View)relativeLayoutField.getParent()).getWidth();
		int fallbackInset = (int)(parentWidth * configuration.getHorizontalInsetFraction());
		containerParams.leftMargin = leftInset > 0 ? leftInset : fallbackInset;
		containerParams.rightMargin = rightInset > 0 ? rightInset : fallbackInset;
		relativeLayoutField.setLayoutParams(containerParams);

		relativeLayoutField.animate()
				.y(-height)
				.setDuration(100)
				.setListener(new Animator.AnimatorListener() {
					@Override
					public void onAnimationCancel(Animator animation) {}

					@Override
					public void onAnimationRepeat(Animator animation) {}

					@Override
					public void onAnimationStart(Animator animation) {}

					@Override
					public void onAnimationEnd(Animator animation) {
						if (textView.isPopupShowing()) {
							// In case it gets reopened
							textView.showDropDown();
						}
					}
				});

		// @on
	}

	protected void notifyNativeInputChanged (boolean selectionOnly) {
		if (!isOpen()) return;
		NativeInputConfiguration configuration = this.configuration;
		if (configuration == null) return;
		NativeInputConfiguration.WriteMode writeMode = configuration.getWriteMode();
		if (writeMode == NativeInputConfiguration.WriteMode.ONLY_FINAL) return;
		if (selectionOnly && writeMode != NativeInputConfiguration.WriteMode.ALL_UPDATES) return;
		AutoCompleteTextView editText = textView;
		String text = editText.getText().toString();
		int selectionStart = editText.getSelectionStart();
		int selectionEnd = editText.getSelectionEnd();
		Gdx.app.postRunnable( () -> configuration.getTextInputWrapper().writeResults(text, selectionStart, selectionEnd));
	}

	@Null
	public NativeInputConfiguration getConfiguration () {
		return configuration;
	}

	protected RelativeLayout createDefaultEditText () {
		FrameLayout frameLayout = view.getRootView().findViewById(android.R.id.content);
		final RelativeLayout relativeLayout = new RelativeLayout(context);
		relativeLayout.setGravity(Gravity.BOTTOM);
		// Why? Why isn't it working without?
		relativeLayout.setBackgroundColor(Color.TRANSPARENT);

		final AutoCompleteTextView editText = new AutoCompleteTextView(context) {

			// State resets itself
			private int count = 0;

			@Override
			public void onFilterComplete (int count) {
				this.count = count;
				super.onFilterComplete(count);
			}

			@Override
			protected void onSelectionChanged (int selStart, int selEnd) {
				super.onSelectionChanged(selStart, selEnd);
				notifyNativeInputChanged(true);
			}

			@Override
			public void showDropDown () {
				int size = 165 * count;
				if (size > relativeLayout.getHeight() + relativeLayout.getY() - getHeight())
					size = (int)(relativeLayout.getHeight() + relativeLayout.getY() - getHeight());
				if (size > 0) setDropDownHeight(size);
				setDropDownVerticalOffset(-getDropDownHeight() - getHeight());
				setDropDownWidth((int)(getWidth() * relativeLayout.getScaleX()));
				super.showDropDown();
			}

			@Override
			public boolean onKeyPreIme (int keyCode, android.view.KeyEvent event) {
				if (event.getKeyCode() == android.view.KeyEvent.KEYCODE_BACK) {
					Gdx.input.closeTextInputField(false);
				}
				return super.onKeyPreIme(keyCode, event);
			}

			@Override
			public InputConnection onCreateInputConnection (EditorInfo outAttrs) {
				return new InputConnectionWrapper(super.onCreateInputConnection(outAttrs), true) {

					// Why? Is this correct handling? I mean, this can't be right! Why shouldn't it work out of the box?
					// This is needed for multiline delete
					@Override
					public boolean sendKeyEvent (android.view.KeyEvent event) {
						if (configuration.isMultiLine() && event.getAction() == android.view.KeyEvent.ACTION_DOWN) {
							if (event.getKeyCode() == android.view.KeyEvent.KEYCODE_DEL) {
								super.deleteSurroundingText(1, 0);
								return true;
							} else if (event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER) {
								commitText("\n", 0);
								return true;
							}
						}

						return super.sendKeyEvent(event);
					}
				};
			}
		};

		RelativeLayout.LayoutParams editTextParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
			RelativeLayout.LayoutParams.WRAP_CONTENT);

		editText.setLayoutParams(editTextParams);

		editText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged (CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged (CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged (Editable s) {
				notifyNativeInputChanged(false);
			}
		});

		relativeLayout.setVisibility(View.INVISIBLE);
		relativeLayout.addView(editText);
		relativeLayout.requestLayout();

		frameLayout.addView(relativeLayout);
		return relativeLayout;
	}

	protected int getAndroidImeAction (NativeInputConfiguration.ReturnKeyType returnKeyType) {
		switch (returnKeyType) {
		case GO:
			return EditorInfo.IME_ACTION_GO;
		case SEARCH:
			return EditorInfo.IME_ACTION_SEARCH;
		case SEND:
			return EditorInfo.IME_ACTION_SEND;
		case NEXT:
			return EditorInfo.IME_ACTION_NEXT;
		case DONE:
			return EditorInfo.IME_ACTION_DONE;
		default:
			throw new IllegalArgumentException(returnKeyType.name());
		}
	}

	protected String[] getAndroidAutofillHints (NativeInputConfiguration.ContentType contentType) {
		switch (contentType) {
		case USERNAME:
			return new String[] {View.AUTOFILL_HINT_USERNAME};
		case PASSWORD:
			return new String[] {View.AUTOFILL_HINT_PASSWORD};
		case NEW_PASSWORD:
			return new String[] {"newPassword"};
		case ONE_TIME_CODE:
			return new String[] {"smsOTPCode", "emailOTPCode", "2faAppOTPCode"};
		case PHONE:
			return new String[] {View.AUTOFILL_HINT_PHONE, "phoneNumber"};
		case EMAIL:
			return new String[] {View.AUTOFILL_HINT_EMAIL_ADDRESS};
		default:
			throw new IllegalArgumentException(contentType.name());
		}
	}
}
