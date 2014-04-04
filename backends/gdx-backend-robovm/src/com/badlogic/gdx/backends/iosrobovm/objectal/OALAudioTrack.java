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
import org.robovm.rt.bro.annotation.Library;

/**
 * @author Niklas Therning
 */
@Library(Library.INTERNAL)
@NativeClass
public class OALAudioTrack extends NSObject {

	static {
		ObjCRuntime.bind(OALAudioTrack.class);
	}

	@Method(selector = "track")
	public native static OALAudioTrack create ();

	@Method
	public native boolean preloadFile (String filePath);

	@Method
	public native void stop ();

	@Method
	public native void clear ();

	@Method
	public native boolean play ();

	@Property
	public native boolean isPaused ();

	@Property
	public native void setPaused (boolean paused);

	@Property
	public native boolean isPlaying ();

	@Property
	public native float getVolume ();

	@Property
	public native void setVolume (float volume);

	@Property
	public native float getPan ();

	@Property
	public native void setPan (float pan);

	@Property
	public native double getCurrentTime ();

	@Property
	public native void setCurrentTime (double currentTime);

	@Property
	public native int getNumberOfLoops ();

	@Property
	public native void setNumberOfLoops (int numberOfLoops);

	@Property
	public native AVAudioPlayerDelegate getDelegate ();

	@Property(strongRef = true)
	public native void setDelegate (AVAudioPlayerDelegate delegate);
}
