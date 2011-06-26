package com.badlogic.gdx.scenes.scene2d.ui.utils;

/**
 * A very simple clipboard interface for text content.
 * @author mzechner
 *
 */
public interface Clipboard {
	/** 
	 * gets the current content of the clipboard if it contains text
	 * @return the clipboard content or null
	 */
	public String getContents();
	
	/**
	 * Sets the content of the system clipboard.
	 * @param content the content
	 */
	public void setContents(String content);
}
