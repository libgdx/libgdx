package com.dozingcatsoftware.bouncy;

import org.robovm.cocoatouch.foundation.NSAutoreleasePool;
import org.robovm.cocoatouch.uikit.UIApplication;

import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration;

public class BouncyIOS extends IOSApplication.Delegate {
	@Override
	protected IOSApplication createApplication () {
		IOSApplicationConfiguration config = new IOSApplicationConfiguration();
		return new IOSApplication(new Bouncy(), config);
	}

	public static void main (String[] argv) {
		NSAutoreleasePool pool = new NSAutoreleasePool();
      UIApplication.main(argv, null, BouncyIOS.class);
      pool.drain();
	}

}
