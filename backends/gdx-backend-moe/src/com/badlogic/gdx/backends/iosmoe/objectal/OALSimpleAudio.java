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
import org.moe.natj.general.ann.MappedReturn;
import org.moe.natj.general.ann.NUInt;
import org.moe.natj.general.ann.Owned;
import org.moe.natj.general.ann.Runtime;
import org.moe.natj.objc.ObjCRuntime;
import org.moe.natj.objc.ann.ObjCBlock;
import org.moe.natj.objc.ann.ObjCClassBinding;
import org.moe.natj.objc.ann.Selector;
import org.moe.natj.objc.map.ObjCObjectMapper;
import apple.NSObject;
import apple.foundation.NSArray;
import apple.foundation.NSURL;

@Generated
@Runtime(ObjCRuntime.class)
@ObjCClassBinding
public class OALSimpleAudio extends NSObject {
	static {
		NatJ.register();
	}

	@Generated
	protected OALSimpleAudio(Pointer peer) {
		super(peer);
	}

	@Generated
	@Owned
	@Selector("alloc")
	public static native OALSimpleAudio alloc();

	@Generated
	@Selector("allowIpod")
	public native boolean allowIpod();

	@Generated
	@Selector("backgroundTrack")
	public native OALAudioTrack backgroundTrack();

	@Generated
	@Selector("backgroundTrackURL")
	public native NSURL backgroundTrackURL();

	@Generated
	@Selector("bgMuted")
	public native boolean bgMuted();

	@Generated
	@Selector("bgPaused")
	public native boolean bgPaused();

	@Generated
	@Selector("bgPlaying")
	public native boolean bgPlaying();

	@Generated
	@Selector("bgVolume")
	public native float bgVolume();

	@Generated
	@Selector("channel")
	public native ALChannelSource channel();

	@Generated
	@Selector("context")
	public native ALContext context();

	@Generated
	@Selector("device")
	public native ALDevice device();

	@Generated
	@Selector("effectsMuted")
	public native boolean effectsMuted();

	@Generated
	@Selector("effectsPaused")
	public native boolean effectsPaused();

	@Generated
	@Selector("effectsVolume")
	public native float effectsVolume();

	@Generated
	@Selector("honorSilentSwitch")
	public native boolean honorSilentSwitch();

	@Generated
	@Selector("init")
	public native OALSimpleAudio init();

	@Generated
	@Selector("initWithReservedSources:monoSources:stereoSources:")
	public native OALSimpleAudio initWithReservedSourcesMonoSourcesStereoSources(
			int reservedSources, int monoSources, int stereoSources);

	@Generated
	@Selector("initWithSources:")
	public native OALSimpleAudio initWithSources(int reservedSources);

	@Generated
	@Selector("interrupted")
	public native boolean interrupted();

	@Generated
	@Selector("manuallySuspended")
	public native boolean manuallySuspended();

	@Generated
	@Selector("muted")
	public native boolean muted();

	@Generated
	@Selector("paused")
	public native boolean paused();

	@Generated
	@Selector("playBg")
	public native boolean playBg();

	@Generated
	@Selector("playBg:")
	public native boolean playBg(String path);

	@Generated
	@Selector("playBg:loop:")
	public native boolean playBgLoop(String path, boolean loop);

	@Generated
	@Selector("playBg:volume:pan:loop:")
	public native boolean playBgVolumePanLoop(String filePath, float volume,
			float pan, boolean loop);

	@Generated
	@Selector("playBgWithLoop:")
	public native boolean playBgWithLoop(boolean loop);

	@Generated
	@Selector("playBuffer:volume:pitch:pan:loop:")
	@MappedReturn(ObjCObjectMapper.class)
	public native ALSource playBufferVolumePitchPanLoop(ALBuffer buffer,
			float volume, float pitch, float pan, boolean loop);

	@Generated
	@Selector("playEffect:")
	@MappedReturn(ObjCObjectMapper.class)
	public native ALSource playEffect(String filePath);

	@Generated
	@Selector("playEffect:loop:")
	@MappedReturn(ObjCObjectMapper.class)
	public native ALSource playEffectLoop(String filePath, boolean loop);

	@Generated
	@Selector("playEffect:volume:pitch:pan:loop:")
	@MappedReturn(ObjCObjectMapper.class)
	public native ALSource playEffectVolumePitchPanLoop(String filePath,
			float volume, float pitch, float pan, boolean loop);

	@Generated
	@Selector("preloadBg:")
	public native boolean preloadBg(String path);

	@Generated
	@Selector("preloadBg:seekTime:")
	public native boolean preloadBgSeekTime(String path, double seekTime);

	@Generated
	@Selector("preloadCacheCount")
	@NUInt
	public native long preloadCacheCount();

	@Generated
	@Selector("preloadCacheEnabled")
	public native boolean preloadCacheEnabled();

	@Generated
	@Selector("preloadEffect:")
	public native ALBuffer preloadEffect(String filePath);

	@Generated
	@Selector("preloadEffect:reduceToMono:")
	public native ALBuffer preloadEffectReduceToMono(String filePath,
			boolean reduceToMono);

	@Generated
	@Selector("preloadEffect:reduceToMono:completionBlock:")
	public native boolean preloadEffectReduceToMonoCompletionBlock(
			String filePath,
			boolean reduceToMono,
			@ObjCBlock(name = "call_preloadEffectReduceToMonoCompletionBlock") Block_preloadEffectReduceToMonoCompletionBlock completionBlock);

	@Runtime(ObjCRuntime.class)
	@Generated
	public interface Block_preloadEffectReduceToMonoCompletionBlock {
		@Generated
		void call_preloadEffectReduceToMonoCompletionBlock (ALBuffer arg0);
	}

	@Generated
	@Selector("preloadEffects:reduceToMono:progressBlock:")
	public native void preloadEffectsReduceToMonoProgressBlock(
			NSArray<?> filePaths,
			boolean reduceToMono,
			@ObjCBlock(name = "call_preloadEffectsReduceToMonoProgressBlock") Block_preloadEffectsReduceToMonoProgressBlock progressBlock);

	@Runtime(ObjCRuntime.class)
	@Generated
	public interface Block_preloadEffectsReduceToMonoProgressBlock {
		@Generated
		void call_preloadEffectsReduceToMonoProgressBlock (@NUInt long arg0, @NUInt long arg1, @NUInt long arg2);
	}

	@Generated
	@Selector("purgeSharedInstance")
	public static native void purgeSharedInstance();

	@Generated
	@Selector("reservedSources")
	public native int reservedSources();

	@Generated
	@Selector("resetToDefault")
	public native void resetToDefault();

	@Generated
	@Selector("setAllowIpod:")
	public native void setAllowIpod(boolean value);

	@Generated
	@Selector("setBgMuted:")
	public native void setBgMuted(boolean value);

	@Generated
	@Selector("setBgPaused:")
	public native void setBgPaused(boolean value);

	@Generated
	@Selector("setBgVolume:")
	public native void setBgVolume(float value);

	@Generated
	@Selector("setEffectsMuted:")
	public native void setEffectsMuted(boolean value);

	@Generated
	@Selector("setEffectsPaused:")
	public native void setEffectsPaused(boolean value);

	@Generated
	@Selector("setEffectsVolume:")
	public native void setEffectsVolume(float value);

	@Generated
	@Selector("setHonorSilentSwitch:")
	public native void setHonorSilentSwitch(boolean value);

	@Generated
	@Selector("setManuallySuspended:")
	public native void setManuallySuspended(boolean value);

	@Generated
	@Selector("setMuted:")
	public native void setMuted(boolean value);

	@Generated
	@Selector("setPaused:")
	public native void setPaused(boolean value);

	@Generated
	@Selector("setPreloadCacheEnabled:")
	public native void setPreloadCacheEnabled(boolean value);

	@Generated
	@Selector("setReservedSources:")
	public native void setReservedSources(int value);

	@Generated
	@Selector("setUseHardwareIfAvailable:")
	public native void setUseHardwareIfAvailable(boolean value);

	@Generated
	@Selector("sharedInstance")
	public static native OALSimpleAudio sharedInstance();

	@Generated
	@Selector("sharedInstanceWithReservedSources:monoSources:stereoSources:")
	public static native OALSimpleAudio sharedInstanceWithReservedSourcesMonoSourcesStereoSources(
			int reservedSources, int monoSources, int stereoSources);

	@Generated
	@Selector("sharedInstanceWithSources:")
	public static native OALSimpleAudio sharedInstanceWithSources(int sources);

	@Generated
	@Selector("stopAllEffects")
	public native void stopAllEffects();

	@Generated
	@Selector("stopBg")
	public native void stopBg();

	@Generated
	@Selector("stopEverything")
	public native void stopEverything();

	@Generated
	@Selector("suspended")
	public native boolean suspended();

	@Generated
	@Selector("unloadAllEffects")
	public native void unloadAllEffects();

	@Generated
	@Selector("unloadEffect:")
	public native boolean unloadEffect(String filePath);

	@Generated
	@Selector("useHardwareIfAvailable")
	public native boolean useHardwareIfAvailable();
}
