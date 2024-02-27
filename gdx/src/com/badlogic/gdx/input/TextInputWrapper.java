
package com.badlogic.gdx.input;

public interface TextInputWrapper {

	String getText ();

	int getSelectionStart ();

	int getSelectionEnd ();

	void setText (String text);

	void setPosition (int position);

	boolean shouldClose ();
}
