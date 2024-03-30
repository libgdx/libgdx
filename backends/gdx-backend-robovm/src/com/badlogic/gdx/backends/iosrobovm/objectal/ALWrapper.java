
package com.badlogic.gdx.backends.iosrobovm.objectal;

import org.robovm.apple.foundation.NSObject;
import org.robovm.objc.ObjCRuntime;
import org.robovm.objc.annotation.Method;
import org.robovm.objc.annotation.NativeClass;
import org.robovm.rt.bro.annotation.Library;
import org.robovm.rt.bro.ptr.VoidPtr;

/** @author Jile Gao */
@Library(Library.INTERNAL)
@NativeClass
public class ALWrapper extends NSObject {
	static {
		ObjCRuntime.bind(ALWrapper.class);
	}

	@Method
	public static native boolean bufferData (int bufferId, int format, VoidPtr data, int size, int frequency);
}
