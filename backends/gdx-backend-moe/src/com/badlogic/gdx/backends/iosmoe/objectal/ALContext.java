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

import org.moe.natj.general.NatJ;
import org.moe.natj.general.Pointer;
import org.moe.natj.general.ann.Generated;
import org.moe.natj.general.ann.Mapped;
import org.moe.natj.general.ann.Owned;
import org.moe.natj.general.ann.Runtime;
import org.moe.natj.general.ptr.VoidPtr;
import org.moe.natj.objc.ObjCRuntime;
import org.moe.natj.objc.ann.ObjCClassBinding;
import org.moe.natj.objc.ann.Selector;
import org.moe.natj.objc.map.ObjCObjectMapper;
import apple.NSObject;
import apple.foundation.NSArray;

@Generated
@Runtime(ObjCRuntime.class)
@ObjCClassBinding
public class ALContext extends NSObject implements OALSuspendManager {
	static {
		NatJ.register();
	}

	@Generated
	protected ALContext(Pointer peer) {
		super(peer);
	}

	@Generated
	@Selector("addSuspendListener:")
	public native void addSuspendListener(
			@Mapped(ObjCObjectMapper.class) Object listener);

	@Generated
	@Selector("alVersion")
	public native String alVersion();

	@Generated
	@Owned
	@Selector("alloc")
	public static native ALContext alloc();

	@Generated
	@Selector("attributes")
	public native NSArray<?> attributes();

	@Generated
	@Selector("clearBuffers")
	public native void clearBuffers();

	@Generated
	@Selector("context")
	public native VoidPtr context();

	@Generated
	@Selector("contextOnDevice:attributes:")
	public static native ALContext contextOnDeviceAttributes(ALDevice device,
			NSArray<?> attributes);

	@Generated
	@Selector("contextOnDevice:outputFrequency:refreshIntervals:synchronousContext:monoSources:stereoSources:")
	public static native ALContext contextOnDeviceOutputFrequencyRefreshIntervalsSynchronousContextMonoSourcesStereoSources(
			ALDevice device, int outputFrequency, int refreshIntervals,
			boolean synchronousContext, int monoSources, int stereoSources);

	@Generated
	@Selector("device")
	public native ALDevice device();

	@Generated
	@Selector("distanceModel")
	public native int distanceModel();

	@Generated
	@Selector("dopplerFactor")
	public native float dopplerFactor();

	@Generated
	@Selector("ensureContextIsCurrent")
	public native void ensureContextIsCurrent();

	@Generated
	@Selector("extensions")
	public native NSArray<?> extensions();

	@Generated
	@Selector("getProcAddress:")
	public native VoidPtr getProcAddress(String functionName);

	@Generated
	@Selector("init")
	public native ALContext init();

	@Generated
	@Selector("initOnDevice:attributes:")
	public native ALContext initOnDeviceAttributes(ALDevice device,
			NSArray<?> attributes);

	@Generated
	@Selector("initOnDevice:outputFrequency:refreshIntervals:synchronousContext:monoSources:stereoSources:")
	public native ALContext initOnDeviceOutputFrequencyRefreshIntervalsSynchronousContextMonoSourcesStereoSources(
			ALDevice device, int outputFrequency, int refreshIntervals,
			boolean synchronousContext, int monoSources, int stereoSources);

	@Generated
	@Selector("interrupted")
	public native boolean interrupted();

	@Generated
	@Selector("isExtensionPresent:")
	public native boolean isExtensionPresent(String name);

	@Generated
	@Selector("listener")
	public native ALListener listener();

	@Generated
	@Selector("manuallySuspended")
	public native boolean manuallySuspended();

	@Generated
	@Selector("notifySourceDeallocating:")
	public native void notifySourceDeallocating(ALSource source);

	@Generated
	@Selector("notifySourceInitializing:")
	public native void notifySourceInitializing(ALSource source);

	@Generated
	@Selector("process")
	public native void process();

	@Generated
	@Selector("removeSuspendListener:")
	public native void removeSuspendListener(
			@Mapped(ObjCObjectMapper.class) Object listener);

	@Generated
	@Selector("renderer")
	public native String renderer();

	@Generated
	@Selector("setDistanceModel:")
	public native void setDistanceModel(int value);

	@Generated
	@Selector("setDopplerFactor:")
	public native void setDopplerFactor(float value);

	@Generated
	@Selector("setInterrupted:")
	public native void setInterrupted(boolean value);

	@Generated
	@Selector("setManuallySuspended:")
	public native void setManuallySuspended(boolean value);

	@Generated
	@Selector("setSpeedOfSound:")
	public native void setSpeedOfSound(float value);

	@Generated
	@Selector("sources")
	public native NSArray<?> sources();

	@Generated
	@Selector("speedOfSound")
	public native float speedOfSound();

	@Generated
	@Selector("stopAllSounds")
	public native void stopAllSounds();

	@Generated
	@Selector("suspended")
	public native boolean suspended();

	@Generated
	@Selector("vendor")
	public native String vendor();
}
