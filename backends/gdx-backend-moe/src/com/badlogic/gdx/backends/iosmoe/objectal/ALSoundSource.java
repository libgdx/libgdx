
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

import org.moe.natj.general.ann.ByValue;
import org.moe.natj.general.ann.Generated;
import org.moe.natj.general.ann.Mapped;
import org.moe.natj.general.ann.MappedReturn;
import org.moe.natj.general.ann.Runtime;
import org.moe.natj.objc.ObjCRuntime;
import org.moe.natj.objc.SEL;
import org.moe.natj.objc.ann.ObjCProtocolName;
import org.moe.natj.objc.ann.Selector;
import org.moe.natj.objc.map.ObjCObjectMapper;

@Generated
@Runtime(ObjCRuntime.class)
@ObjCProtocolName("ALSoundSource")
public interface ALSoundSource {
	@Generated
	@Selector("clear")
	void clear ();

	@Generated
	@Selector("coneInnerAngle")
	float coneInnerAngle ();

	@Generated
	@Selector("coneOuterAngle")
	float coneOuterAngle ();

	@Generated
	@Selector("coneOuterGain")
	float coneOuterGain ();

	@Generated
	@Selector("direction")
	@ByValue
	ALVector direction ();

	@Generated
	@Selector("fadeTo:duration:target:selector:")
	void fadeToDurationTargetSelector (float gain, float duration, @Mapped(ObjCObjectMapper.class) Object target, SEL selector);

	@Generated
	@Selector("gain")
	float gain ();

	@Generated
	@Selector("interruptible")
	boolean interruptible ();

	@Generated
	@Selector("looping")
	boolean looping ();

	@Generated
	@Selector("maxDistance")
	float maxDistance ();

	@Generated
	@Selector("maxGain")
	float maxGain ();

	@Generated
	@Selector("minGain")
	float minGain ();

	@Generated
	@Selector("muted")
	boolean muted ();

	@Generated
	@Selector("pan")
	float pan ();

	@Generated
	@Selector("panTo:duration:target:selector:")
	void panToDurationTargetSelector (float pan, float duration, @Mapped(ObjCObjectMapper.class) Object target, SEL selector);

	@Generated
	@Selector("paused")
	boolean paused ();

	@Generated
	@Selector("pitch")
	float pitch ();

	@Generated
	@Selector("pitchTo:duration:target:selector:")
	void pitchToDurationTargetSelector (float pitch, float duration, @Mapped(ObjCObjectMapper.class) Object target, SEL selector);

	@Generated
	@Selector("play:")
	@MappedReturn(ObjCObjectMapper.class)
	ALSoundSource play (ALBuffer buffer);

	@Generated
	@Selector("play:gain:pitch:pan:loop:")
	@MappedReturn(ObjCObjectMapper.class)
	ALSoundSource playGainPitchPanLoop (ALBuffer buffer, float gain, float pitch, float pan, boolean loop);

	@Generated
	@Selector("play:loop:")
	@MappedReturn(ObjCObjectMapper.class)
	ALSoundSource playLoop (ALBuffer buffer, boolean loop);

	@Generated
	@Selector("playing")
	boolean playing ();

	@Generated
	@Selector("position")
	@ByValue
	ALPoint position ();

	@Generated
	@Selector("referenceDistance")
	float referenceDistance ();

	@Generated
	@Selector("reverbObstruction")
	float reverbObstruction ();

	@Generated
	@Selector("reverbOcclusion")
	float reverbOcclusion ();

	@Generated
	@Selector("reverbSendLevel")
	float reverbSendLevel ();

	@Generated
	@Selector("rewind")
	void rewind ();

	@Generated
	@Selector("rolloffFactor")
	float rolloffFactor ();

	@Generated
	@Selector("setConeInnerAngle:")
	void setConeInnerAngle (float value);

	@Generated
	@Selector("setConeOuterAngle:")
	void setConeOuterAngle (float value);

	@Generated
	@Selector("setConeOuterGain:")
	void setConeOuterGain (float value);

	@Generated
	@Selector("setDirection:")
	void setDirection (@ByValue ALVector value);

	@Generated
	@Selector("setGain:")
	void setGain (float value);

	@Generated
	@Selector("setInterruptible:")
	void setInterruptible (boolean value);

	@Generated
	@Selector("setLooping:")
	void setLooping (boolean value);

	@Generated
	@Selector("setMaxDistance:")
	void setMaxDistance (float value);

	@Generated
	@Selector("setMaxGain:")
	void setMaxGain (float value);

	@Generated
	@Selector("setMinGain:")
	void setMinGain (float value);

	@Generated
	@Selector("setMuted:")
	void setMuted (boolean value);

	@Generated
	@Selector("setPan:")
	void setPan (float value);

	@Generated
	@Selector("setPaused:")
	void setPaused (boolean value);

	@Generated
	@Selector("setPitch:")
	void setPitch (float value);

	@Generated
	@Selector("setPosition:")
	void setPosition (@ByValue ALPoint value);

	@Generated
	@Selector("setReferenceDistance:")
	void setReferenceDistance (float value);

	@Generated
	@Selector("setReverbObstruction:")
	void setReverbObstruction (float value);

	@Generated
	@Selector("setReverbOcclusion:")
	void setReverbOcclusion (float value);

	@Generated
	@Selector("setReverbSendLevel:")
	void setReverbSendLevel (float value);

	@Generated
	@Selector("setRolloffFactor:")
	void setRolloffFactor (float value);

	@Generated
	@Selector("setSourceRelative:")
	void setSourceRelative (int value);

	@Generated
	@Selector("setVelocity:")
	void setVelocity (@ByValue ALVector value);

	@Generated
	@Selector("setVolume:")
	void setVolume (float value);

	@Generated
	@Selector("sourceRelative")
	int sourceRelative ();

	@Generated
	@Selector("sourceType")
	int sourceType ();

	@Generated
	@Selector("stop")
	void stop ();

	@Generated
	@Selector("stopActions")
	void stopActions ();

	@Generated
	@Selector("stopFade")
	void stopFade ();

	@Generated
	@Selector("stopPan")
	void stopPan ();

	@Generated
	@Selector("stopPitch")
	void stopPitch ();

	@Generated
	@Selector("velocity")
	@ByValue
	ALVector velocity ();

	@Generated
	@Selector("volume")
	float volume ();
}
