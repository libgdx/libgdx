/*DO NOT EDIT THIS FILE - it is machine generated*/

package com.badlogic.gdx.backends.iosrobovm;

import com.badlogic.gdx.ApplicationLogger;
import org.robovm.apple.foundation.Foundation;
import org.robovm.apple.foundation.NSString;

/** DO NOT EDIT THIS FILE - it is machine generated Default implementation of {@link ApplicationLogger} for ios */
public class IOSApplicationLogger implements ApplicationLogger {

	@Override
	public void log (String tag, String message) {
		Foundation.log("%@", new NSString("[info] " + tag + ": " + message));
	}

	@Override
	public void log (String tag, String message, Throwable exception) {
		Foundation.log("%@", new NSString("[info] " + tag + ": " + message));
		exception.printStackTrace();
	}

	@Override
	public void error (String tag, String message) {
		Foundation.log("%@", new NSString("[error] " + tag + ": " + message));
	}

	@Override
	public void error (String tag, String message, Throwable exception) {
		Foundation.log("%@", new NSString("[error] " + tag + ": " + message));
		exception.printStackTrace();
	}

	@Override
	public void debug (String tag, String message) {
		Foundation.log("%@", new NSString("[debug] " + tag + ": " + message));
	}

	@Override
	public void debug (String tag, String message, Throwable exception) {
		Foundation.log("%@", new NSString("[debug] " + tag + ": " + message));
		exception.printStackTrace();
	}
}
