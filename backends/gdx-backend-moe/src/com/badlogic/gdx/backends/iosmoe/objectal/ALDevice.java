/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
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

package com.badlogic.gdx.backends.iosmoe.objectal;

import com.intel.moe.natj.general.NatJ;
import com.intel.moe.natj.general.Pointer;
import com.intel.moe.natj.general.ann.Generated;
import com.intel.moe.natj.general.ann.Mapped;
import com.intel.moe.natj.general.ann.Owned;
import com.intel.moe.natj.general.ann.Runtime;
import com.intel.moe.natj.general.ptr.VoidPtr;
import com.intel.moe.natj.objc.ObjCRuntime;
import com.intel.moe.natj.objc.ann.ObjCClassBinding;
import com.intel.moe.natj.objc.ann.Selector;
import com.intel.moe.natj.objc.map.ObjCObjectMapper;
import ios.NSObject;
import ios.foundation.NSArray;

@Generated
@Runtime(ObjCRuntime.class)
@ObjCClassBinding
public class ALDevice extends NSObject implements OALSuspendManager {
	static {
		NatJ.register();
	}

	@Generated
	protected ALDevice(Pointer peer) {
		super(peer);
	}

	@Generated
	@Selector("addSuspendListener:")
	public native void addSuspendListener(
			@Mapped(ObjCObjectMapper.class) Object listener);

	@Generated
	@Owned
	@Selector("alloc")
	public static native ALDevice alloc();

	@Generated
	@Selector("clearBuffers")
	public native void clearBuffers();

	@Generated
	@Selector("contexts")
	public native NSArray<?> contexts();

	@Generated
	@Selector("device")
	public native VoidPtr device();

	@Generated
	@Selector("deviceWithDeviceSpecifier:")
	public static native ALDevice deviceWithDeviceSpecifier(
			String deviceSpecifier);

	@Generated
	@Selector("extensions")
	public native NSArray<?> extensions();

	@Generated
	@Selector("getProcAddress:")
	public native VoidPtr getProcAddress(String functionName);

	@Generated
	@Selector("init")
	public native ALDevice init();

	@Generated
	@Selector("initWithDeviceSpecifier:")
	public native ALDevice initWithDeviceSpecifier(String deviceSpecifier);

	@Generated
	@Selector("interrupted")
	public native boolean interrupted();

	@Generated
	@Selector("isExtensionPresent:")
	public native boolean isExtensionPresent(String name);

	@Generated
	@Selector("majorVersion")
	public native int majorVersion();

	@Generated
	@Selector("manuallySuspended")
	public native boolean manuallySuspended();

	@Generated
	@Selector("minorVersion")
	public native int minorVersion();

	@Generated
	@Selector("notifyContextDeallocating:")
	public native void notifyContextDeallocating(ALContext context);

	@Generated
	@Selector("notifyContextInitializing:")
	public native void notifyContextInitializing(ALContext context);

	@Generated
	@Selector("removeSuspendListener:")
	public native void removeSuspendListener(
			@Mapped(ObjCObjectMapper.class) Object listener);

	@Generated
	@Selector("setInterrupted:")
	public native void setInterrupted(boolean value);

	@Generated
	@Selector("setManuallySuspended:")
	public native void setManuallySuspended(boolean value);

	@Generated
	@Selector("suspended")
	public native boolean suspended();
}
