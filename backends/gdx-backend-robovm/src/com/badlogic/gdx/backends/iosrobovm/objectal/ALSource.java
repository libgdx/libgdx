/*******************************************************************************
 * Copyright 2013 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.backends.iosrobovm.objectal;

import org.robovm.apple.foundation.NSObject;
import org.robovm.objc.ObjCRuntime;
import org.robovm.objc.annotation.Method;
import org.robovm.objc.annotation.NativeClass;
import org.robovm.objc.annotation.Property;
import org.robovm.rt.bro.NativeObject;
import org.robovm.rt.bro.annotation.Library;

/**
 * @author Niklas Therning
 */
@Library(Library.INTERNAL)
@NativeClass
public class ALSource extends NSObject {

	static {
		ObjCRuntime.bind(ALSource.class);
	}
	
	@Property(selector = "sourceId")
	public native int getSourceId ();

	@Method(selector = "stop")
	public native void stop ();

	@Property(selector = "paused")
	public native boolean isPaused ();

	@Property(selector = "setPaused:")
	public native void setPaused (boolean paused);
	
	@Method(selector = "setVolume:")
	public native void setVolume (float volume);

	@Method(selector = "setPitch:")
	public native void setPitch (float pitch);
	
	@Method(selector = "setPan:")
	public native void setPan (float pan);
	
	@Method(selector = "setLooping:")
	public native void setLooping (boolean shouldLoop);
	
}
