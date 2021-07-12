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
import org.moe.natj.general.ann.Owned;
import org.moe.natj.general.ann.Runtime;
import org.moe.natj.objc.ObjCRuntime;
import org.moe.natj.objc.SEL;
import org.moe.natj.objc.ann.ObjCClassBinding;
import org.moe.natj.objc.ann.Selector;
import org.moe.natj.objc.map.ObjCObjectMapper;
import apple.NSObject;
import apple.foundation.NSArray;

@Generated
@Runtime(ObjCRuntime.class)
@ObjCClassBinding
public class ALChannelSource extends NSObject implements ALSoundSource {
	static {
		NatJ.register();
	}

	@Generated
	protected ALChannelSource(Pointer peer) {
		super(peer);
	}

	@Generated
	@Selector("addChannel:")
	public native void addChannel(ALChannelSource channel);

	@Generated
	@Selector("addSource:")
	public native void addSource(
			@Mapped(ObjCObjectMapper.class) ALSoundSource source);

	@Generated
	@Owned
	@Selector("alloc")
	public static native ALChannelSource alloc();

	@Generated
	@Selector("channelWithSources:")
	@MappedReturn(ObjCObjectMapper.class)
	public static native Object channelWithSources(int reservedSources);

	@Generated
	@Selector("clear")
	public native void clear();

	@Generated
	@Selector("clearUnusedBuffers")
	public native NSArray<?> clearUnusedBuffers();

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
	public native ALChannelSource init();

	@Generated
	@Selector("initWithSources:")
	public native ALChannelSource initWithSources(int reservedSources);

	@Generated
	@Selector("interruptible")
	public native boolean interruptible();

	@Generated
	@Selector("looping")
	public native boolean looping();

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
	@Selector("referenceDistance")
	public native float referenceDistance();

	@Generated
	@Selector("removeBuffersNamed:")
	public native boolean removeBuffersNamed(String name);

	@Generated
	@Selector("removeSource:")
	@MappedReturn(ObjCObjectMapper.class)
	public native ALSoundSource removeSource(
			@Mapped(ObjCObjectMapper.class) ALSoundSource source);

	@Generated
	@Selector("reservedSources")
	public native int reservedSources();

	@Generated
	@Selector("resetToDefault")
	public native void resetToDefault();

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
	@Selector("setConeInnerAngle:")
	public native void setConeInnerAngle(float value);

	@Generated
	@Selector("setConeOuterAngle:")
	public native void setConeOuterAngle(float value);

	@Generated
	@Selector("setConeOuterGain:")
	public native void setConeOuterGain(float value);

	@Generated
	@Selector("setDefaultsFromSource:")
	public native void setDefaultsFromSource(
			@Mapped(ObjCObjectMapper.class) ALSoundSource source);

	@Generated
	@Selector("setDirection:")
	public native void setDirection(@ByValue ALVector value);

	@Generated
	@Selector("setGain:")
	public native void setGain(float value);

	@Generated
	@Selector("setInterruptible:")
	public native void setInterruptible(boolean value);

	@Generated
	@Selector("setLooping:")
	public native void setLooping(boolean value);

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
	@Selector("setReservedSources:")
	public native void setReservedSources(int value);

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
	@Selector("setVelocity:")
	public native void setVelocity(@ByValue ALVector value);

	@Generated
	@Selector("setVolume:")
	public native void setVolume(float value);

	@Generated
	@Selector("sourcePool")
	public native ALSoundSourcePool sourcePool();

	@Generated
	@Selector("sourceRelative")
	public native int sourceRelative();

	@Generated
	@Selector("sourceType")
	public native int sourceType();

	@Generated
	@Selector("splitChannelWithSources:")
	public native ALChannelSource splitChannelWithSources(int numSources);

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
	@Selector("velocity")
	@ByValue
	public native ALVector velocity();

	@Generated
	@Selector("volume")
	public native float volume();
}
