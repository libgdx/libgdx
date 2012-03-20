package com.badlogic.gdx.scenes.scene2d.ui.utils;


/** A very simple clipboard interface for text content.
 * @author mzechner */
public abstract class Clipboard {
	/** gets the current content of the clipboard if it contains text
	 * @return the clipboard content or null */
	public abstract String getContents ();

	/** Sets the content of the system clipboard.
	 * @param content the content */
	public abstract void setContents (String content);

	public static Clipboard getDefaultClipboard () {
		return new Clipboard() {
			@Override
			public String getContents () {
				return null;
			}

			@Override
			public void setContents (String content) {
			}
		};
	}
}
