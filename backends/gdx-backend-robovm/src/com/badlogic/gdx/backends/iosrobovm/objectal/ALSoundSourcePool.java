package com.badlogic.gdx.backends.iosrobovm.objectal;

import org.robovm.apple.foundation.NSArray;
import org.robovm.apple.foundation.NSObject;
import org.robovm.objc.ObjCRuntime;
import org.robovm.objc.annotation.NativeClass;
import org.robovm.objc.annotation.Property;
import org.robovm.rt.bro.annotation.Library;

@Library(Library.INTERNAL)
@NativeClass
public class ALSoundSourcePool extends NSObject {

	static {
		ObjCRuntime.bind(ALSoundSourcePool.class);
	}
	
	@Property(selector = "sources")
	public native NSArray<ALSource> getSources ();
	
}