
package com.badlogic.gdx.backends.iosrobovm.objectal;

import org.robovm.apple.foundation.NSObject;
import org.robovm.objc.ObjCRuntime;
import org.robovm.objc.annotation.NativeClass;
import org.robovm.rt.bro.annotation.Library;

/** @author Jile Gao */
@Library(Library.INTERNAL)
@NativeClass
public class ALSoundSource extends NSObject {
	static {
		ObjCRuntime.bind(ALSoundSource.class);
	}
}
