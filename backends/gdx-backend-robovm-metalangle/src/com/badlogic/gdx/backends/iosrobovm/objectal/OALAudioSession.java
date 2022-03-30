
package com.badlogic.gdx.backends.iosrobovm.objectal;

import org.robovm.apple.foundation.NSObject;
import org.robovm.objc.ObjCRuntime;
import org.robovm.objc.annotation.Method;
import org.robovm.objc.annotation.NativeClass;
import org.robovm.rt.bro.annotation.Library;

@Library(Library.INTERNAL)
@NativeClass
public final class OALAudioSession extends NSObject {
	static {
		ObjCRuntime.bind(OALAudioSession.class);
	}

	@Method
	public native static OALAudioSession sharedInstance ();

	@Method
	public native void forceEndInterruption ();
}
