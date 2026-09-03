
package com.badlogic.gdx.input;

public interface TextInputWrapper {

	/** This method will be queried for the initial text. No guarantee of the calling thread is made */
	String getText ();

	/** This method will be queried for the initial text selection start. No guarantee of the calling thread is made Should be
	 * consistent with the text returned by {@link TextInputWrapper#getText()} */
	int getSelectionStart ();

	/** This method will be queried for the initial text selection end. No guarantee of the calling thread is made. Should be
	 * consistent with the text returned by {@link TextInputWrapper#getText()} */
	int getSelectionEnd ();

	/** This is called, when text was retrieved from the native input. Only use this to write back results. This will always be
	 * called on the main thread. For ({@link NativeInputConfiguration.WriteMode#ONLY_FINAL}) this is only called once, when the
	 * native input is closed. With {@link NativeInputConfiguration.WriteMode#TEXT_UPDATES} /
	 * {@link NativeInputConfiguration.WriteMode#ALL_UPDATES} it is additionally called on every change to the native input (while
	 * the IME assembles text, and for ALL_UPDATES on selection-only changes too), so do not assume the text is final here. For
	 * close/finalization logic use {@link NativeInputConfiguration#setCloseCallback} */
	void writeResults (String text, int selectionStart, int selectionEnd);
}
