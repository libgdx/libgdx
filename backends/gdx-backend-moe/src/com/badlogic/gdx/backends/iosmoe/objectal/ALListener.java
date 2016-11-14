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
import org.moe.natj.general.ann.ByValue;
import org.moe.natj.general.ann.Generated;
import org.moe.natj.general.ann.Mapped;
import org.moe.natj.general.ann.Owned;
import org.moe.natj.general.ann.Runtime;
import org.moe.natj.objc.ObjCRuntime;
import org.moe.natj.objc.ann.ObjCClassBinding;
import org.moe.natj.objc.ann.Selector;
import org.moe.natj.objc.map.ObjCObjectMapper;
import apple.NSObject;

@Generated
@Runtime(ObjCRuntime.class)
@ObjCClassBinding
public class ALListener extends NSObject implements OALSuspendManager {
	static {
		NatJ.register();
	}

	@Generated
	protected ALListener(Pointer peer) {
		super(peer);
	}

	@Generated
	@Selector("addSuspendListener:")
	public native void addSuspendListener(
			@Mapped(ObjCObjectMapper.class) Object listener);

	@Generated
	@Owned
	@Selector("alloc")
	public static native ALListener alloc();

	@Generated
	@Selector("context")
	public native ALContext context();

	@Generated
	@Selector("gain")
	public native float gain();

	@Generated
	@Selector("globalReverbLevel")
	public native float globalReverbLevel();

	@Generated
	@Selector("init")
	public native ALListener init();

	@Generated
	@Selector("initWithContext:")
	public native ALListener initWithContext(ALContext context);

	@Generated
	@Selector("interrupted")
	public native boolean interrupted();

	@Generated
	@Selector("listenerForContext:")
	public static native ALListener listenerForContext(ALContext context);

	@Generated
	@Selector("manuallySuspended")
	public native boolean manuallySuspended();

	@Generated
	@Selector("muted")
	public native boolean muted();

	@Generated
	@Selector("orientation")
	@ByValue
	public native ALOrientation orientation();

	@Generated
	@Selector("position")
	@ByValue
	public native ALPoint position();

	@Generated
	@Selector("removeSuspendListener:")
	public native void removeSuspendListener(
			@Mapped(ObjCObjectMapper.class) Object listener);

	@Generated
	@Selector("reverbEQBandwidth")
	public native float reverbEQBandwidth();

	@Generated
	@Selector("reverbEQFrequency")
	public native float reverbEQFrequency();

	@Generated
	@Selector("reverbEQGain")
	public native float reverbEQGain();

	@Generated
	@Selector("reverbOn")
	public native boolean reverbOn();

	@Generated
	@Selector("reverbRoomType")
	public native int reverbRoomType();

	@Generated
	@Selector("setGain:")
	public native void setGain(float value);

	@Generated
	@Selector("setGlobalReverbLevel:")
	public native void setGlobalReverbLevel(float value);

	@Generated
	@Selector("setInterrupted:")
	public native void setInterrupted(boolean value);

	@Generated
	@Selector("setManuallySuspended:")
	public native void setManuallySuspended(boolean value);

	@Generated
	@Selector("setMuted:")
	public native void setMuted(boolean value);

	@Generated
	@Selector("setOrientation:")
	public native void setOrientation(@ByValue ALOrientation value);

	@Generated
	@Selector("setPosition:")
	public native void setPosition(@ByValue ALPoint value);

	@Generated
	@Selector("setReverbEQBandwidth:")
	public native void setReverbEQBandwidth(float value);

	@Generated
	@Selector("setReverbEQFrequency:")
	public native void setReverbEQFrequency(float value);

	@Generated
	@Selector("setReverbEQGain:")
	public native void setReverbEQGain(float value);

	@Generated
	@Selector("setReverbOn:")
	public native void setReverbOn(boolean value);

	@Generated
	@Selector("setReverbRoomType:")
	public native void setReverbRoomType(int value);

	@Generated
	@Selector("setVelocity:")
	public native void setVelocity(@ByValue ALVector value);

	@Generated
	@Selector("suspended")
	public native boolean suspended();

	@Generated
	@Selector("velocity")
	@ByValue
	public native ALVector velocity();
}
