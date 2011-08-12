package com.badlogic.gdx.utils;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;

/**
 * Simple logger that uses the {@link Application} logging facilities
 * to output messages.
 * @author mzechner
 *
 */
public class Logger {
	private final String tag;
	private boolean enabled = true;
	
	public Logger(String tag) {
		this.tag = tag;
	}
	
	public void log(String message) {
		if(enabled) {
			Gdx.app.log(tag, message);
		}
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;;
	}
}
