package com.badlogic.gdx.backends.iosrobovm.objectal;

import org.robovm.apple.foundation.NSObject;
import org.robovm.objc.ObjCRuntime;
import org.robovm.objc.annotation.NativeClass;
import org.robovm.objc.annotation.Property;
import org.robovm.rt.bro.annotation.Library;

@Library(Library.INTERNAL)
@NativeClass
public final class ALChannelSource extends NSObject {

	static {
		ObjCRuntime.bind(ALChannelSource.class);
	}
	
	@Property(selector = "sourcePool")
	public native ALSoundSourcePool getSourcePool ();
	
}