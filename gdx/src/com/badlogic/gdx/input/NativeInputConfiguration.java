
package com.badlogic.gdx.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.CharArray;

public class NativeInputConfiguration {

	private Input.OnscreenKeyboardType type = Input.OnscreenKeyboardType.Default;
	private boolean preventCorrection = false;

	private TextInputWrapper textInputWrapper;
	private boolean isMultiLine = false;
	private int maxLength = -1;
	private Input.InputStringValidator validator;
	private String placeholder = "";
	private boolean maskInput = false;
	private boolean showUnmaskButton = false;
	private String[] autoComplete = null;
	private WriteMode writeMode = WriteMode.ONLY_FINAL;

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

	/** @param maskInput Whether to hide the text input while typing (usually for passwords) */
	public NativeInputConfiguration setMaskInput (boolean maskInput) {
		this.maskInput = maskInput;
		return this;
	}

	public boolean isMaskInput () {
		return maskInput;
	}

	public boolean isShowUnmaskButton () {
		return showUnmaskButton;
	}

	/** @param showUnmaskButton Whether to show a button to show unhidden password */
	public NativeInputConfiguration setShowUnmaskButton (boolean showUnmaskButton) {
		this.showUnmaskButton = showUnmaskButton;
		return this;
	}

	public String[] getAutoComplete () {
		return autoComplete;
	}

	/** Sets a list of autocompletable strings to present the user while typing */
	public NativeInputConfiguration setAutoComplete (String[] autoComplete) {
		this.autoComplete = autoComplete;
		return this;
	}

	public WriteMode getWriteMode () {
		return writeMode;
	}

	/** @param writeMode Controls how often {@link TextInputWrapper#writeResults(String, int, int)} is called while the native
	 *           input is open. See {@link WriteMode}. With anything other than {@link WriteMode#ONLY_FINAL} the backing input
	 *           field can mirror the in-progress text (and, with {@link WriteMode#ALL_UPDATES}, the caret) live. Finality is still
	 *           signalled separately via {@link NativeInputCloseCallback}. Defaults to {@link WriteMode#ONLY_FINAL}. */
	public NativeInputConfiguration setWriteMode (WriteMode writeMode) {
		this.writeMode = writeMode;
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
		CharArray message = new CharArray();

		if (type == null) message.append("OnscreenKeyboardType needs to be non null", "; ");
		if (textInputWrapper == null) message.append("TextInputWrapper needs to be non null", "; ");
		if (showUnmaskButton && !maskInput) message.append("ShowUnmaskButton only works with MaskInput", "; ");
		if (placeholder == null) message.append("Placeholder needs to be non null", "; ");
		if (autoComplete != null && type != Input.OnscreenKeyboardType.Default)
			message.append("AutoComplete should only be used with OnscreenKeyboardType.Default", "; ");
		if (autoComplete != null && isMultiLine) message.append("AutoComplete shouldn't be used with multiline", "; ");
		if (closeCallback == null) message.append("CloseCallback needs to be non null", "; ");
		if (writeMode == null) message.append("WriteMode needs to be non null", "; ");
		if (validator != null) {
			if (textInputWrapper != null && !validator.validate(textInputWrapper.getText()))
				message.append("getText() is not valid according to validator", "; ");
			if (autoComplete != null) {
				for (String s : autoComplete) {
					if (!validator.validate(s)) message.append("AutoComplete " + s + " is not valid according to validator", "; ");
				}
			}
		}

		if (message.notEmpty()) throw new IllegalArgumentException("NativeInputConfiguration validation failed: " + message);
	}

	/** Controls how often {@link TextInputWrapper#writeResults(String, int, int)} is invoked while a native input field is
	 * open. */
	public enum WriteMode {
		/** {@link TextInputWrapper#writeResults(String, int, int)} is called only once, when the native input is closed. This is
		 * the default. */
		ONLY_FINAL,
		/** {@link TextInputWrapper#writeResults(String, int, int)} is additionally called on every text change while the IME
		 * assembles text. The payload carries the current text together with the caret/selection, so the caret is tracked on each
		 * keystroke. */
		TEXT_UPDATES,
		/** Like {@link #TEXT_UPDATES}, but {@link TextInputWrapper#writeResults(String, int, int)} is also called when only the
		 * selection/caret changes (e.g. the user taps to move the caret or drag-selects without typing). Does not work on iOS <
		 * 13. */
		ALL_UPDATES
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
