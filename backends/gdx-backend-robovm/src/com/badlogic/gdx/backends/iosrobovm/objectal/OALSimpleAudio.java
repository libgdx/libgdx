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
public final class OALSimpleAudio extends NSObject {

	static {
		ObjCRuntime.bind(OALSimpleAudio.class);
	}

	@Method
	public native static OALSimpleAudio sharedInstance ();

	@Method
	public native ALBuffer preloadEffect (String filePath);

	@Method
	public native boolean unloadEffect (String filePath);

	@Method(selector = "playEffect:volume:pitch:pan:loop:")
	public native ALSource playEffect (String filePath, float volume, float pitch, float pan, boolean loop);

	@Method(selector = "playBuffer:volume:pitch:pan:loop:")
	public native ALSource playBuffer (ALBuffer buffer, float volume, float pitch, float pan, boolean loop);
	
	@Property
	public native boolean isAllowIpod ();

	@Property
	public native void setAllowIpod (boolean allowIpod);

	@Property
	public native boolean isHonorSilentSwitch ();

	@Property
	public native void setHonorSilentSwitch (boolean honorSilentSwitch);

	@Property
	public native boolean isUseHardwareIfAvailable ();

	@Property
	public native void setUseHardwareIfAvailable (boolean useHardwareIfAvailable);
	
	@Property(selector = "channel")
	public native ALChannelSource getChannelSource ();
	
}
