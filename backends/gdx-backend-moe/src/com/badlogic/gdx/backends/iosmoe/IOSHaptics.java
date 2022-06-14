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

package com.badlogic.gdx.backends.iosmoe;

import apple.audiotoolbox.c.AudioToolbox;
import apple.corehaptics.CHHapticEngine.Block_setResetHandler;
import apple.corehaptics.CHHapticEngine.Block_startWithCompletionHandler;
import apple.corehaptics.c.CoreHaptics;
import apple.corehaptics.protocol.CHHapticPatternPlayer;
import apple.foundation.NSDictionary;
import apple.foundation.NSNumber;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import apple.corehaptics.CHHapticEngine;
import apple.corehaptics.CHHapticPattern;
import apple.foundation.NSArray;
import apple.foundation.NSError;
import apple.foundation.NSProcessInfo;
import apple.uikit.UIDevice;
import apple.uikit.UIImpactFeedbackGenerator;
import apple.uikit.enums.UIImpactFeedbackStyle;
import apple.uikit.enums.UIUserInterfaceIdiom;
import org.moe.natj.general.ptr.Ptr;
import org.moe.natj.general.ptr.impl.PtrFactory;

public class IOSHaptics {

	private CHHapticEngine hapticEngine;
	private boolean hapticsSupport;
	private final boolean vibratorSupport;

	public IOSHaptics (boolean useHaptics) {
		vibratorSupport = useHaptics && UIDevice.currentDevice().userInterfaceIdiom() == UIUserInterfaceIdiom.Phone;
		if (NSProcessInfo.processInfo().operatingSystemVersion().majorVersion() >= 13) {
			hapticsSupport = useHaptics && CHHapticEngine.capabilitiesForHardware().supportsHaptics();
			if (hapticsSupport) {
				Ptr<NSError> nsErrorPtr = PtrFactory.newObjectPtr(NSError.class, 1, true, false);
				hapticEngine = CHHapticEngine.alloc().initAndReturnError(nsErrorPtr);
				if (nsErrorPtr.get() != null) {
					Gdx.app.error("IOSHaptics", "Error creating CHHapticEngine. Haptics will be disabled. " + nsErrorPtr.get().localizedDescription());
					hapticsSupport = false;
					return;
				}

				hapticEngine.setPlaysHapticsOnly(true);
				hapticEngine.setAutoShutdownEnabled(true);
				// The reset handler provides an opportunity to restart the engine.
				hapticEngine.setResetHandler(new Block_setResetHandler() {
					@Override
					public void call_setResetHandler () {
						// Try restarting the engine.
						hapticEngine.startWithCompletionHandler(new Block_startWithCompletionHandler() {
							@Override
							public void call_startWithCompletionHandler(NSError error) {
								if (error == null) return;
								Gdx.app.error("IOSHaptics", "Error restarting CHHapticEngine. Haptics will be disabled. " + error.localizedDescription());
								hapticsSupport = false;

							}
						});
					}
				});
			}
		}
	}

	public void vibrate (int milliseconds, boolean fallback) {
		if (hapticsSupport) {
			vibrate(milliseconds, 0.5f);
		} else if (fallback) {
			AudioToolbox.AudioServicesPlaySystemSound(4095);
		}
	}

	public void vibrate (int milliseconds, float intensity) {
		NSDictionary<String, ?> hapticDict = getChHapticPatternDict(milliseconds, intensity);
		Ptr<NSError> nsErrorPtr = PtrFactory.newObjectPtr(NSError.class, 1, true, false);
		CHHapticPattern pattern = CHHapticPattern.alloc().initWithDictionaryError(hapticDict, nsErrorPtr);
		if (nsErrorPtr.get() != null) {
			Gdx.app.error("IOSHaptics", "Error creating haptics pattern. " + nsErrorPtr.get().localizedDescription());
			return;
		}
		CHHapticPatternPlayer patternPlayer = hapticEngine.createPlayerWithPatternError(pattern, null);
		if (nsErrorPtr.get() != null) {
			Gdx.app.error("IOSHaptics", "Error creating haptics player. " + nsErrorPtr.get().localizedDescription());
			return;
		}
		patternPlayer.startAtTimeError(0, nsErrorPtr);
		if (nsErrorPtr.get() != null) {
			Gdx.app.error("IOSHaptics", "Error starting haptics player. " + nsErrorPtr.get().localizedDescription());
			return;
		}
	}

	public void vibrate (int milliseconds, int amplitude, boolean fallback) {
		if (hapticsSupport) {
			float intensity = MathUtils.clamp(amplitude / 255f, 0, 1);
			vibrate(milliseconds, intensity);
		} else {
			vibrate(milliseconds, fallback);
		}
	}

	private NSDictionary<String, ?> getChHapticPatternDict (int milliseconds, float intensity) {
		return (NSDictionary<String, ?>)NSDictionary.dictionaryWithObjectForKey(NSArray.arrayWithObject(NSDictionary.dictionaryWithObjectForKey(NSDictionary.dictionaryWithObjectsForKeys(
								NSArray.arrayWithObjects(CoreHaptics.CHHapticEventTypeHapticContinuous(), NSNumber.numberWithDouble(0.0),
										NSNumber.numberWithFloat(milliseconds / 1000f),
										NSArray.arrayWithObject(NSDictionary.dictionaryWithObjectsForKeys(
												NSArray.arrayWithObjects(CoreHaptics.CHHapticEventParameterIDHapticIntensity(),
														NSNumber.numberWithFloat(intensity), null),
												NSArray.arrayWithObjects(CoreHaptics.CHHapticPatternKeyParameterID(),
														CoreHaptics.CHHapticPatternKeyParameterValue(), null))), null),
								NSArray.arrayWithObjects(CoreHaptics.CHHapticPatternKeyEventType(),
										CoreHaptics.CHHapticPatternKeyTime(),
										CoreHaptics.CHHapticPatternKeyEventDuration(),
										CoreHaptics.CHHapticPatternKeyEventParameters(), null)),
						CoreHaptics.CHHapticPatternKeyEvent())),
				CoreHaptics.CHHapticPatternKeyPattern());
	}

	public void vibrate (Input.VibrationType vibrationType) {
		if (hapticsSupport) {
			long uiImpactFeedbackStyle;
			switch (vibrationType) {
			case LIGHT:
				uiImpactFeedbackStyle = UIImpactFeedbackStyle.Soft;
				break;
			case MEDIUM:
				uiImpactFeedbackStyle = UIImpactFeedbackStyle.Medium;
				break;
			case HEAVY:
				uiImpactFeedbackStyle = UIImpactFeedbackStyle.Heavy;
				break;
			default:
				throw new IllegalArgumentException("Unknown VibrationType " + vibrationType);
			}
			UIImpactFeedbackGenerator uiImpactFeedbackGenerator = UIImpactFeedbackGenerator.alloc().initWithStyle(uiImpactFeedbackStyle);
			uiImpactFeedbackGenerator.impactOccurred();
		}
	}

	public boolean isHapticsSupported () {
		return hapticsSupport;
	}

	public boolean isVibratorSupported () {
		return vibratorSupport;
	}

}
