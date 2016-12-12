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
import org.moe.natj.general.ann.MappedReturn;
import org.moe.natj.general.ann.NUInt;
import org.moe.natj.general.ann.Owned;
import org.moe.natj.general.ann.Runtime;
import org.moe.natj.objc.ObjCRuntime;
import org.moe.natj.objc.ann.IsOptional;
import org.moe.natj.objc.ann.ObjCClassBinding;
import org.moe.natj.objc.ann.Selector;
import org.moe.natj.objc.map.ObjCObjectMapper;
import apple.NSObject;
import apple.avfoundation.protocol.AVAudioSessionDelegate;

@Generated
@Runtime(ObjCRuntime.class)
@ObjCClassBinding
public class OALAudioSession extends NSObject implements
		AVAudioSessionDelegate, OALSuspendManager {
	static {
		NatJ.register();
	}

	@Generated
	protected OALAudioSession(Pointer peer) {
		super(peer);
	}

	@Generated
	@Selector("addSuspendListener:")
	public native void addSuspendListener(
			@Mapped(ObjCObjectMapper.class) Object listener);

	@Generated
	@Owned
	@Selector("alloc")
	public static native OALAudioSession alloc();

	@Generated
	@Selector("allowIpod")
	public native boolean allowIpod();

	@Generated
	@Deprecated
	@Selector("audioRoute")
	public native String audioRoute();

	@Generated
	@Selector("audioSessionActive")
	public native boolean audioSessionActive();

	@Generated
	@Selector("audioSessionCategory")
	public native String audioSessionCategory();

	@Generated
	@Selector("audioSessionDelegate")
	@MappedReturn(ObjCObjectMapper.class)
	public native AVAudioSessionDelegate audioSessionDelegate();

	@Generated
	@IsOptional
	@Selector("beginInterruption")
	public native void beginInterruption();

	@Generated
	@IsOptional
	@Selector("endInterruption")
	public native void endInterruption();

	@Generated
	@IsOptional
	@Selector("endInterruptionWithFlags:")
	public native void endInterruptionWithFlags(@NUInt long flags);

	@Generated
	@Selector("forceEndInterruption")
	public native void forceEndInterruption();

	@Generated
	@Selector("handleInterruptions")
	public native boolean handleInterruptions();

	@Generated
	@Deprecated
	@Selector("hardwareMuted")
	public native boolean hardwareMuted();

	@Generated
	@Deprecated
	@Selector("hardwareVolume")
	public native float hardwareVolume();

	@Generated
	@Selector("honorSilentSwitch")
	public native boolean honorSilentSwitch();

	@Generated
	@Selector("init")
	public native OALAudioSession init();

	@Generated
	@IsOptional
	@Selector("inputIsAvailableChanged:")
	public native void inputIsAvailableChanged(boolean isInputAvailable);

	@Generated
	@Selector("interrupted")
	public native boolean interrupted();

	@Generated
	@Selector("ipodDucking")
	public native boolean ipodDucking();

	@Generated
	@Deprecated
	@Selector("ipodPlaying")
	public native boolean ipodPlaying();

	@Generated
	@Selector("manuallySuspended")
	public native boolean manuallySuspended();

	@Generated
	@Deprecated
	@Selector("preferredIOBufferDuration")
	public native float preferredIOBufferDuration();

	@Generated
	@Selector("purgeSharedInstance")
	public static native void purgeSharedInstance();

	@Generated
	@Selector("removeSuspendListener:")
	public native void removeSuspendListener(
			@Mapped(ObjCObjectMapper.class) Object listener);

	@Generated
	@Selector("setAllowIpod:")
	public native void setAllowIpod(boolean value);

	@Generated
	@Selector("setAudioSessionActive:")
	public native void setAudioSessionActive(boolean value);

	@Generated
	@Selector("setAudioSessionCategory:")
	public native void setAudioSessionCategory(String value);

	@Generated
	@Selector("setAudioSessionDelegate:")
	public native void setAudioSessionDelegate_unsafe(
			@Mapped(ObjCObjectMapper.class) AVAudioSessionDelegate value);

	@Generated
	public void setAudioSessionDelegate(
			@Mapped(ObjCObjectMapper.class) AVAudioSessionDelegate value) {
		Object __old = audioSessionDelegate();
		if (value != null) {
			ObjCRuntime.associateObjCObject(this,
					value);
		}
		setAudioSessionDelegate_unsafe(value);
		if (__old != null) {
			ObjCRuntime.dissociateObjCObject(this,
					__old);
		}
	}

	@Generated
	@Selector("setHandleInterruptions:")
	public native void setHandleInterruptions(boolean value);

	@Generated
	@Selector("setHonorSilentSwitch:")
	public native void setHonorSilentSwitch(boolean value);

	@Generated
	@Selector("setInterrupted:")
	public native void setInterrupted(boolean value);

	@Generated
	@Selector("setIpodDucking:")
	public native void setIpodDucking(boolean value);

	@Generated
	@Selector("setManuallySuspended:")
	public native void setManuallySuspended(boolean value);

	@Generated
	@Deprecated
	@Selector("setPreferredIOBufferDuration:")
	public native void setPreferredIOBufferDuration(float value);

	@Generated
	@Selector("setUseHardwareIfAvailable:")
	public native void setUseHardwareIfAvailable(boolean value);

	@Generated
	@Selector("sharedInstance")
	public static native OALAudioSession sharedInstance();

	@Generated
	@Selector("suspended")
	public native boolean suspended();

	@Generated
	@Selector("useHardwareIfAvailable")
	public native boolean useHardwareIfAvailable();
}
