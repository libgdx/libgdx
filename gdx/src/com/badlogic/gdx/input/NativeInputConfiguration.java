
package com.badlogic.gdx.input;

import com.badlogic.gdx.Input;

public class NativeInputConfiguration {

	private Input.OnscreenKeyboardType type = Input.OnscreenKeyboardType.Default;
	private boolean preventCorrection = false;

	private TextInputWrapper textInputWrapper;
	private boolean isMultiLine = false;
	private int maxLength = -1;
	private Input.InputStringValidator validator;
	private String placeholder = "";
	private boolean showPasswordButton = false;
	private String[] autoComplete = null;

	private NativeInputCloseCallback closeCallback = (confirm) -> false;

	public Input.OnscreenKeyboardType getType () {
		return type;
	}

	/** @param type which type of keyboard we wish to display. */
	public NativeInputConfiguration setType (Input.OnscreenKeyboardType type) {
		this.type = type;
		return this;
	}

	public boolean isPreventCorrection () {
		return preventCorrection;
	}

	/** @param preventCorrection Disable autocomplete/correction */
	public NativeInputConfiguration setPreventCorrection (boolean preventCorrection) {
		this.preventCorrection = preventCorrection;
		return this;
	}

	public TextInputWrapper getTextInputWrapper () {
		return textInputWrapper;
	}

	/** @param textInputWrapper Should provide access to the backed input field. */
	public NativeInputConfiguration setTextInputWrapper (TextInputWrapper textInputWrapper) {
		this.textInputWrapper = textInputWrapper;
		return this;
	}

	public boolean isMultiLine () {
		return isMultiLine;
	}

	/** @param multiLine whether the keyboard should accept multiple lines. */
	public NativeInputConfiguration setMultiLine (boolean multiLine) {
		isMultiLine = multiLine;
		return this;
	}

	public int getMaxLength () {
		return maxLength;
	}

	/** @param maxLength What the text length limit should be. -1 for no max length */
	public NativeInputConfiguration setMaxLength (int maxLength) {
		this.maxLength = maxLength;
		return this;
	}

	public Input.InputStringValidator getValidator () {
		return validator;
	}

	/** @param validator Can validate the input from the keyboard and reject. */
	public NativeInputConfiguration setValidator (Input.InputStringValidator validator) {
		this.validator = validator;
		return this;
	}

	public String getPlaceholder () {
		return placeholder;
	}

	/** @param placeholder String to show, if nothing is inputted yet. */
	public NativeInputConfiguration setPlaceholder (String placeholder) {
		this.placeholder = placeholder;
		return this;
	}

	public boolean isShowPasswordButton () {
		return showPasswordButton;
	}

	/** @param showPasswordButton Whether to show a button to show unhidden password */
	public NativeInputConfiguration setShowPasswordButton (boolean showPasswordButton) {
		this.showPasswordButton = showPasswordButton;
		return this;
	}

	public String[] getAutoComplete () {
		return autoComplete;
	}

	public NativeInputConfiguration setAutoComplete (String[] autoComplete) {
		this.autoComplete = autoComplete;
		return this;
	}

	public NativeInputCloseCallback getCloseCallback () {
		return closeCallback;
	}

	/** Installing a callback for when the native input is closed. See {@link NativeInputCloseCallback} for more information */
	public NativeInputConfiguration setCloseCallback (NativeInputCloseCallback closeCallback) {
		this.closeCallback = closeCallback;
		return this;
	}

	public void validate () {
		String message = null;
		if (type == null) message = "OnscreenKeyboardType needs to be non null";
		if (textInputWrapper == null) message = "TextInputWrapper needs to be non null";
		if (showPasswordButton && type != Input.OnscreenKeyboardType.Password)
			message = "ShowPasswordButton only works with OnscreenKeyboardType.Password";
		if (placeholder == null) message = "Placeholder needs to be non null";
		if (autoComplete != null && type != Input.OnscreenKeyboardType.Default)
			message = "AutoComplete should only be used with OnscreenKeyboardType.Default";
		if (autoComplete != null && isMultiLine) message = "AutoComplete shouldn't be used with multiline";
		if (closeCallback == null) message = "CloseCallback needs to be non null";

		if (message != null) {
			throw new IllegalArgumentException("NativeInputConfiguration validation failed: " + message);
		}
	}

	public interface NativeInputCloseCallback {
		/** This will be called on the main thread, when the closing of a native input is processed. This does not mean, that the
		 * keyboard is already hidden. You can schedule a new `openTextInputField` call here.
		 * @param confirmativeAction Whether the way the keyboard was closed can be considered a confirmative action e.g. to advance
		 *           the UI
		 * @return Whether the keyboard should be kept open to be opened again soon. e.g. when advancing through multiple text
		 *         fields */
		boolean onClose (boolean confirmativeAction);
	}
}
