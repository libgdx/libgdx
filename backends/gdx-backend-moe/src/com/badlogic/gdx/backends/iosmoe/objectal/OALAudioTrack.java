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
import org.moe.natj.general.ann.NInt;
import org.moe.natj.general.ann.NUInt;
import org.moe.natj.general.ann.Owned;
import org.moe.natj.general.ann.Runtime;
import org.moe.natj.objc.ObjCRuntime;
import org.moe.natj.objc.SEL;
import org.moe.natj.objc.ann.IsOptional;
import org.moe.natj.objc.ann.ObjCClassBinding;
import org.moe.natj.objc.ann.Selector;
import org.moe.natj.objc.map.ObjCObjectMapper;
import apple.NSObject;
import apple.avfoundation.AVAudioPlayer;
import apple.avfoundation.protocol.AVAudioPlayerDelegate;
import apple.foundation.NSError;
import apple.foundation.NSURL;

@Generated
@Runtime(ObjCRuntime.class)
@ObjCClassBinding
public class OALAudioTrack extends NSObject implements AVAudioPlayerDelegate,
		OALSuspendManager {
	static {
		NatJ.register();
	}

	@Generated
	protected OALAudioTrack(Pointer peer) {
		super(peer);
	}

	@Generated
	@Selector("addSuspendListener:")
	public native void addSuspendListener(
			@Mapped(ObjCObjectMapper.class) Object listener);

	@Generated
	@Owned
	@Selector("alloc")
	public static native OALAudioTrack alloc();

	@Generated
	@IsOptional
	@Deprecated
	@Selector("audioPlayerBeginInterruption:")
	public native void audioPlayerBeginInterruption(AVAudioPlayer player);

	@Generated
	@IsOptional
	@Selector("audioPlayerDecodeErrorDidOccur:error:")
	public native void audioPlayerDecodeErrorDidOccurError(
			AVAudioPlayer player, NSError error);

	@Generated
	@IsOptional
	@Selector("audioPlayerDidFinishPlaying:successfully:")
	public native void audioPlayerDidFinishPlayingSuccessfully(
			AVAudioPlayer player, boolean flag);

	@Generated
	@IsOptional
	@Deprecated
	@Selector("audioPlayerEndInterruption:")
	public native void audioPlayerEndInterruption(AVAudioPlayer player);

	@Generated
	@IsOptional
	@Deprecated
	@Selector("audioPlayerEndInterruption:withFlags:")
	public native void audioPlayerEndInterruptionWithFlags(
			AVAudioPlayer player, @NUInt long flags);

	@Generated
	@IsOptional
	@Deprecated
	@Selector("audioPlayerEndInterruption:withOptions:")
	public native void audioPlayerEndInterruptionWithOptions(
			AVAudioPlayer player, @NUInt long flags);

	@Generated
	@Selector("autoPreload")
	public native boolean autoPreload();

	@Generated
	@Selector("averagePowerForChannel:")
	public native float averagePowerForChannel(@NUInt long channelNumber);

	@Generated
	@Selector("clear")
	public native void clear();

	@Generated
	@Selector("currentTime")
	public native double currentTime();

	@Generated
	@Selector("currentlyLoadedUrl")
	public native NSURL currentlyLoadedUrl();

	@Generated
	@Selector("delegate")
	@MappedReturn(ObjCObjectMapper.class)
	public native AVAudioPlayerDelegate delegate();

	@Generated
	@Selector("deviceCurrentTime")
	public native double deviceCurrentTime();

	@Generated
	@Selector("duration")
	public native double duration();

	@Generated
	@Selector("fadeTo:duration:target:selector:")
	public native void fadeToDurationTargetSelector(float gain, float duration,
			@Mapped(ObjCObjectMapper.class) Object target, SEL selector);

	@Generated
	@Selector("gain")
	public native float gain();

	@Generated
	@Selector("init")
	public native OALAudioTrack init();

	@Generated
	@Selector("interrupted")
	public native boolean interrupted();

	@Generated
	@Selector("manuallySuspended")
	public native boolean manuallySuspended();

	@Generated
	@Selector("meteringEnabled")
	public native boolean meteringEnabled();

	@Generated
	@Selector("muted")
	public native boolean muted();

	@Generated
	@Selector("numberOfChannels")
	@NUInt
	public native long numberOfChannels();

	@Generated
	@Selector("numberOfLoops")
	@NInt
	public native long numberOfLoops();

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
	@Selector("peakPowerForChannel:")
	public native float peakPowerForChannel(@NUInt long channelNumber);

	@Generated
	@Selector("play")
	public native boolean play();

	@Generated
	@Selector("playAfterTrack:")
	public native boolean playAfterTrack(OALAudioTrack track);

	@Generated
	@Selector("playAfterTrack:timeAdjust:")
	public native boolean playAfterTrackTimeAdjust(OALAudioTrack track,
			double timeAdjust);

	@Generated
	@Selector("playAtTime:")
	public native boolean playAtTime(double time);

	@Generated
	@Selector("playFile:")
	public native boolean playFile(String path);

	@Generated
	@Selector("playFile:loops:")
	public native boolean playFileLoops(String path, @NInt long loops);

	@Generated
	@Selector("playFileAsync:loops:target:selector:")
	public native void playFileAsyncLoopsTargetSelector(String path,
			@NInt long loops, @Mapped(ObjCObjectMapper.class) Object target,
			SEL selector);

	@Generated
	@Selector("playFileAsync:target:selector:")
	public native void playFileAsyncTargetSelector(String path,
			@Mapped(ObjCObjectMapper.class) Object target, SEL selector);

	@Generated
	@Selector("playUrl:")
	public native boolean playUrl(NSURL url);

	@Generated
	@Selector("playUrl:loops:")
	public native boolean playUrlLoops(NSURL url, @NInt long loops);

	@Generated
	@Selector("playUrlAsync:loops:target:selector:")
	public native void playUrlAsyncLoopsTargetSelector(NSURL url,
			@NInt long loops, @Mapped(ObjCObjectMapper.class) Object target,
			SEL selector);

	@Generated
	@Selector("playUrlAsync:target:selector:")
	public native void playUrlAsyncTargetSelector(NSURL url,
			@Mapped(ObjCObjectMapper.class) Object target, SEL selector);

	@Generated
	@Selector("player")
	public native AVAudioPlayer player();

	@Generated
	@Selector("playing")
	public native boolean playing();

	@Generated
	@Selector("preloadFile:")
	public native boolean preloadFile(String path);

	@Generated
	@Selector("preloadFile:seekTime:")
	public native boolean preloadFileSeekTime(String path, double seekTime);

	@Generated
	@Selector("preloadFileAsync:seekTime:target:selector:")
	public native boolean preloadFileAsyncSeekTimeTargetSelector(String path,
			double seekTime, @Mapped(ObjCObjectMapper.class) Object target,
			SEL selector);

	@Generated
	@Selector("preloadFileAsync:target:selector:")
	public native boolean preloadFileAsyncTargetSelector(String path,
			@Mapped(ObjCObjectMapper.class) Object target, SEL selector);

	@Generated
	@Selector("preloadUrl:")
	public native boolean preloadUrl(NSURL url);

	@Generated
	@Selector("preloadUrl:seekTime:")
	public native boolean preloadUrlSeekTime(NSURL url, double seekTime);

	@Generated
	@Selector("preloadUrlAsync:seekTime:target:selector:")
	public native boolean preloadUrlAsyncSeekTimeTargetSelector(NSURL url,
			double seekTime, @Mapped(ObjCObjectMapper.class) Object target,
			SEL selector);

	@Generated
	@Selector("preloadUrlAsync:target:selector:")
	public native boolean preloadUrlAsyncTargetSelector(NSURL url,
			@Mapped(ObjCObjectMapper.class) Object target, SEL selector);

	@Generated
	@Selector("preloaded")
	public native boolean preloaded();

	@Generated
	@Selector("removeSuspendListener:")
	public native void removeSuspendListener(
			@Mapped(ObjCObjectMapper.class) Object listener);

	@Generated
	@Selector("setAutoPreload:")
	public native void setAutoPreload(boolean value);

	@Generated
	@Selector("setCurrentTime:")
	public native void setCurrentTime(double value);

	@Generated
	@Selector("setDelegate:")
	public native void setDelegate_unsafe(
			@Mapped(ObjCObjectMapper.class) AVAudioPlayerDelegate value);

	@Generated
	public void setDelegate(
			@Mapped(ObjCObjectMapper.class) AVAudioPlayerDelegate value) {
		Object __old = delegate();
		if (value != null) {
			ObjCRuntime.associateObjCObject(this,
					value);
		}
		setDelegate_unsafe(value);
		if (__old != null) {
			ObjCRuntime.dissociateObjCObject(this,
					__old);
		}
	}

	@Generated
	@Selector("setGain:")
	public native void setGain(float value);

	@Generated
	@Selector("setInterrupted:")
	public native void setInterrupted(boolean value);

	@Generated
	@Selector("setManuallySuspended:")
	public native void setManuallySuspended(boolean value);

	@Generated
	@Selector("setMeteringEnabled:")
	public native void setMeteringEnabled(boolean value);

	@Generated
	@Selector("setMuted:")
	public native void setMuted(boolean value);

	@Generated
	@Selector("setNumberOfLoops:")
	public native void setNumberOfLoops(@NInt long value);

	@Generated
	@Selector("setPan:")
	public native void setPan(float value);

	@Generated
	@Selector("setPaused:")
	public native void setPaused(boolean value);

	@Generated
	@Selector("setVolume:")
	public native void setVolume(float value);

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
	@Selector("suspended")
	public native boolean suspended();

	@Generated
	@Selector("track")
	public static native OALAudioTrack track();

	@Generated
	@Selector("updateMeters")
	public native void updateMeters();

	@Generated
	@Selector("volume")
	public native float volume();
}
