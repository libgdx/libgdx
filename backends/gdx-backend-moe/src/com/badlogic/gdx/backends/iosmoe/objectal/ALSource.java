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
import org.moe.natj.general.ann.MappedReturn;
import org.moe.natj.general.ann.NUInt;
import org.moe.natj.general.ann.Owned;
import org.moe.natj.general.ann.Runtime;
import org.moe.natj.general.ptr.VoidPtr;
import org.moe.natj.objc.ObjCRuntime;
import org.moe.natj.objc.SEL;
import org.moe.natj.objc.ann.ObjCBlock;
import org.moe.natj.objc.ann.ObjCClassBinding;
import org.moe.natj.objc.ann.Selector;
import org.moe.natj.objc.map.ObjCObjectMapper;
import apple.NSObject;
import apple.foundation.NSArray;

@Generated
@Runtime(ObjCRuntime.class)
@ObjCClassBinding
public class ALSource extends NSObject implements ALSoundSource,
		OALSuspendManager {
	static {
		NatJ.register();
	}

	@Generated
	protected ALSource(Pointer peer) {
		super(peer);
	}

	@Generated
	@Selector("addSuspendListener:")
	public native void addSuspendListener(
			@Mapped(ObjCObjectMapper.class) Object listener);

	@Generated
	@Owned
	@Selector("alloc")
	public static native ALSource alloc();

	@Generated
	@Selector("buffer")
	public native ALBuffer buffer();

	@Generated
	@Selector("buffersProcessed")
	public native int buffersProcessed();

	@Generated
	@Selector("buffersQueued")
	public native int buffersQueued();

	@Generated
	@Selector("clear")
	public native void clear();

	@Generated
	@Selector("coneInnerAngle")
	public native float coneInnerAngle();

	@Generated
	@Selector("coneOuterAngle")
	public native float coneOuterAngle();

	@Generated
	@Selector("coneOuterGain")
	public native float coneOuterGain();

	@Generated
	@Selector("context")
	public native ALContext context();

	@Generated
	@Selector("direction")
	@ByValue
	public native ALVector direction();

	@Generated
	@Selector("fadeTo:duration:target:selector:")
	public native void fadeToDurationTargetSelector(float gain, float duration,
			@Mapped(ObjCObjectMapper.class) Object target, SEL selector);

	@Generated
	@Selector("gain")
	public native float gain();

	@Generated
	@Selector("init")
	public native ALSource init();

	@Generated
	@Selector("initOnContext:")
	public native ALSource initOnContext(ALContext context);

	@Generated
	@Selector("interrupted")
	public native boolean interrupted();

	@Generated
	@Selector("interruptible")
	public native boolean interruptible();

	@Generated
	@Selector("looping")
	public native boolean looping();

	@Generated
	@Selector("manuallySuspended")
	public native boolean manuallySuspended();

	@Generated
	@Selector("maxDistance")
	public native float maxDistance();

	@Generated
	@Selector("maxGain")
	public native float maxGain();

	@Generated
	@Selector("minGain")
	public native float minGain();

	@Generated
	@Selector("muted")
	public native boolean muted();

	@Generated
	@Selector("offsetInBytes")
	public native float offsetInBytes();

	@Generated
	@Selector("offsetInSamples")
	public native float offsetInSamples();

	@Generated
	@Selector("offsetInSeconds")
	public native float offsetInSeconds();

	@Generated
	@Selector("pan")
	public native float pan();

	@Generated
	@Selector("panTo:duration:target:selector:")
	public native void panToDurationTargetSelector(float pan, float duration,
			@Mapped(ObjCObjectMapper.class) Object target, SEL selector);

	@Generated
	@Selector("paused")
	public native boolean paused();

	@Generated
	@Selector("pitch")
	public native float pitch();

	@Generated
	@Selector("pitchTo:duration:target:selector:")
	public native void pitchToDurationTargetSelector(float pitch,
			float duration, @Mapped(ObjCObjectMapper.class) Object target,
			SEL selector);

	@Generated
	@Selector("play")
	@MappedReturn(ObjCObjectMapper.class)
	public native ALSoundSource play();

	@Generated
	@Selector("play:")
	@MappedReturn(ObjCObjectMapper.class)
	public native ALSoundSource play(ALBuffer buffer);

	@Generated
	@Selector("play:gain:pitch:pan:loop:")
	@MappedReturn(ObjCObjectMapper.class)
	public native ALSoundSource playGainPitchPanLoop(ALBuffer buffer, float gain,
			float pitch, float pan, boolean loop);

	@Generated
	@Selector("play:loop:")
	@MappedReturn(ObjCObjectMapper.class)
	public native ALSoundSource playLoop(ALBuffer buffer, boolean loop);

	@Generated
	@Selector("playing")
	public native boolean playing();

	@Generated
	@Selector("position")
	@ByValue
	public native ALPoint position();

	@Generated
	@Selector("queueBuffer:")
	public native boolean queueBuffer(ALBuffer buffer);

	@Generated
	@Selector("queueBuffer:repeats:")
	public native boolean queueBufferRepeats(ALBuffer buffer,
			@NUInt long repeats);

	@Generated
	@Selector("queueBuffers:")
	public native boolean queueBuffers(NSArray<?> buffers);

	@Generated
	@Selector("queueBuffers:repeats:")
	public native boolean queueBuffersRepeats(NSArray<?> buffers,
			@NUInt long repeats);

	@Generated
	@Selector("referenceDistance")
	public native float referenceDistance();

	@Generated
	@Selector("registerNotification:callback:userData:")
	public native void registerNotificationCallbackUserData(
			int notificationID,
			@ObjCBlock(name = "call_registerNotificationCallbackUserData") Block_registerNotificationCallbackUserData callback,
			VoidPtr userData);

	@Runtime(ObjCRuntime.class)
	@Generated
	public interface Block_registerNotificationCallbackUserData {
		@Generated
		void call_registerNotificationCallbackUserData (ALSource arg0, int arg1, VoidPtr arg2);
	}

	@Generated
	@Selector("removeSuspendListener:")
	public native void removeSuspendListener(
			@Mapped(ObjCObjectMapper.class) Object listener);

	@Generated
	@Selector("reverbObstruction")
	public native float reverbObstruction();

	@Generated
	@Selector("reverbOcclusion")
	public native float reverbOcclusion();

	@Generated
	@Selector("reverbSendLevel")
	public native float reverbSendLevel();

	@Generated
	@Selector("rewind")
	public native void rewind();

	@Generated
	@Selector("rolloffFactor")
	public native float rolloffFactor();

	@Generated
	@Selector("setBuffer:")
	public native void setBuffer(ALBuffer value);

	@Generated
	@Selector("setConeInnerAngle:")
	public native void setConeInnerAngle(float value);

	@Generated
	@Selector("setConeOuterAngle:")
	public native void setConeOuterAngle(float value);

	@Generated
	@Selector("setConeOuterGain:")
	public native void setConeOuterGain(float value);

	@Generated
	@Selector("setDirection:")
	public native void setDirection(@ByValue ALVector value);

	@Generated
	@Selector("setGain:")
	public native void setGain(float value);

	@Generated
	@Selector("setInterrupted:")
	public native void setInterrupted(boolean value);

	@Generated
	@Selector("setInterruptible:")
	public native void setInterruptible(boolean value);

	@Generated
	@Selector("setLooping:")
	public native void setLooping(boolean value);

	@Generated
	@Selector("setManuallySuspended:")
	public native void setManuallySuspended(boolean value);

	@Generated
	@Selector("setMaxDistance:")
	public native void setMaxDistance(float value);

	@Generated
	@Selector("setMaxGain:")
	public native void setMaxGain(float value);

	@Generated
	@Selector("setMinGain:")
	public native void setMinGain(float value);

	@Generated
	@Selector("setMuted:")
	public native void setMuted(boolean value);

	@Generated
	@Selector("setOffsetInBytes:")
	public native void setOffsetInBytes(float value);

	@Generated
	@Selector("setOffsetInSamples:")
	public native void setOffsetInSamples(float value);

	@Generated
	@Selector("setOffsetInSeconds:")
	public native void setOffsetInSeconds(float value);

	@Generated
	@Selector("setPan:")
	public native void setPan(float value);

	@Generated
	@Selector("setPaused:")
	public native void setPaused(boolean value);

	@Generated
	@Selector("setPitch:")
	public native void setPitch(float value);

	@Generated
	@Selector("setPosition:")
	public native void setPosition(@ByValue ALPoint value);

	@Generated
	@Selector("setReferenceDistance:")
	public native void setReferenceDistance(float value);

	@Generated
	@Selector("setReverbObstruction:")
	public native void setReverbObstruction(float value);

	@Generated
	@Selector("setReverbOcclusion:")
	public native void setReverbOcclusion(float value);

	@Generated
	@Selector("setReverbSendLevel:")
	public native void setReverbSendLevel(float value);

	@Generated
	@Selector("setRolloffFactor:")
	public native void setRolloffFactor(float value);

	@Generated
	@Selector("setSourceRelative:")
	public native void setSourceRelative(int value);

	@Generated
	@Selector("setState:")
	public native void setState(int value);

	@Generated
	@Selector("setVelocity:")
	public native void setVelocity(@ByValue ALVector value);

	@Generated
	@Selector("setVolume:")
	public native void setVolume(float value);

	@Generated
	@Selector("source")
	public static native ALSource source();

	@Generated
	@Selector("sourceId")
	public native int sourceId();

	@Generated
	@Selector("sourceOnContext:")
	public static native ALSource sourceOnContext(ALContext context);

	@Generated
	@Selector("sourceRelative")
	public native int sourceRelative();

	@Generated
	@Selector("sourceType")
	public native int sourceType();

	@Generated
	@Selector("state")
	public native int state();

	@Generated
	@Selector("stop")
	public native void stop();

	@Generated
	@Selector("stopActions")
	public native void stopActions();

	@Generated
	@Selector("stopFade")
	public native void stopFade();

	@Generated
	@Selector("stopPan")
	public native void stopPan();

	@Generated
	@Selector("stopPitch")
	public native void stopPitch();

	@Generated
	@Selector("suspended")
	public native boolean suspended();

	@Generated
	@Selector("unqueueBuffer:")
	public native boolean unqueueBuffer(ALBuffer buffer);

	@Generated
	@Selector("unqueueBuffers:")
	public native boolean unqueueBuffers(NSArray<?> buffers);

	@Generated
	@Selector("unregisterAllNotifications")
	public native void unregisterAllNotifications();

	@Generated
	@Selector("unregisterNotification:")
	public native void unregisterNotification(int notificationID);

	@Generated
	@Selector("velocity")
	@ByValue
	public native ALVector velocity();

	@Generated
	@Selector("volume")
	public native float volume();
}
