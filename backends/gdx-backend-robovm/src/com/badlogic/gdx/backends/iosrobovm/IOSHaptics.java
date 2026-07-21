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

package com.badlogic.gdx.backends.iosrobovm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.input.Haptics;
import com.badlogic.gdx.math.MathUtils;
import org.robovm.apple.audiotoolbox.AudioServices;
import org.robovm.apple.corehaptic.CHHapticEngine;
import org.robovm.apple.corehaptic.CHHapticEventParameterID;
import org.robovm.apple.corehaptic.CHHapticEventType;
import org.robovm.apple.corehaptic.CHHapticPattern;
import org.robovm.apple.corehaptic.CHHapticPatternDict;
import org.robovm.apple.foundation.NSArray;
import org.robovm.apple.foundation.NSError;
import org.robovm.apple.foundation.NSErrorException;
import org.robovm.apple.foundation.NSObject;
import org.robovm.apple.foundation.NSProcessInfo;
import org.robovm.apple.uikit.UIDevice;
import org.robovm.apple.uikit.UIImpactFeedbackGenerator;
import org.robovm.apple.uikit.UIImpactFeedbackStyle;
import org.robovm.apple.uikit.UIUserInterfaceIdiom;
import org.robovm.objc.block.VoidBlock1;

public class IOSHaptics implements Haptics {

	private CHHapticEngine hapticEngine;
	private boolean hapticsSupport;
	private final boolean vibratorSupport;
	private boolean fallback;

	public IOSHaptics (boolean useHaptics, boolean fallback) {
		this.fallback = fallback;
		vibratorSupport = useHaptics && UIDevice.getCurrentDevice().getUserInterfaceIdiom() == UIUserInterfaceIdiom.Phone;
		if (NSProcessInfo.getSharedProcessInfo().getOperatingSystemVersion().getMajorVersion() >= 13) {
			hapticsSupport = useHaptics && CHHapticEngine.capabilitiesForHardware().supportsHaptics();
			if (hapticsSupport) {
				try {
					hapticEngine = new CHHapticEngine();
				} catch (NSErrorException e) {
					Gdx.app.error("IOSHaptics", "Error creating CHHapticEngine. Haptics will be disabled. " + e);
					hapticsSupport = false;
					return;
				}
				hapticEngine.setPlaysHapticsOnly(true);
				hapticEngine.setAutoShutdownEnabled(true);
				// The reset handler provides an opportunity to restart the engine.
				hapticEngine.setResetHandler(new Runnable() {
					@Override
					public void run () {
						// Try restarting the engine.
						hapticEngine.start(new VoidBlock1<NSError>() {
							@Override
							public void invoke (NSError nsError) {
								if (nsError != null) {
									Gdx.app.error("IOSHaptics", "Error restarting CHHapticEngine. Haptics will be disabled.");
									hapticsSupport = false;
								}
							}
						});
					}
				});
			}
		}
	}

	@Override
	public void vibrate (int milliseconds) {
		if (hapticsSupport) {
			CHHapticPatternDict hapticDict = getChHapticPatternDict(milliseconds, 0.5f);
			try {
				CHHapticPattern pattern = new CHHapticPattern(hapticDict);
				NSError.NSErrorPtr ptr = new NSError.NSErrorPtr();
				hapticEngine.createPlayer(pattern).start(0, ptr);
				if (ptr.get() != null) {
					Gdx.app.error("IOSHaptics", "Error starting haptics player. Error code: " + ptr.get().getLocalizedDescription());
				}
			} catch (NSErrorException e) {
				Gdx.app.error("IOSHaptics", "Error creating haptics pattern or player. " + e.getMessage());
			}
		} else if (fallback) {
			AudioServices.playSystemSound(4095);
		}
	}

	@Override
	public void vibrate (int milliseconds, int amplitude) {
		if (hapticsSupport) {
			float intensity = MathUtils.clamp(amplitude / 255f, 0, 1);
			CHHapticPatternDict hapticDict = getChHapticPatternDict(milliseconds, intensity);
			try {
				CHHapticPattern pattern = new CHHapticPattern(hapticDict);
				NSError.NSErrorPtr ptr = new NSError.NSErrorPtr();
				hapticEngine.createPlayer(pattern).start(0, ptr);
				if (ptr.get() != null) {
					Gdx.app.error("IOSHaptics", "Error starting haptics pattern.");
				}
			} catch (NSErrorException e) {
				Gdx.app.error("IOSHaptics", "Error creating haptics player. " + e.getMessage());
			}
		} else {
			vibrate(milliseconds);
		}
	}

	private CHHapticPatternDict getChHapticPatternDict (int milliseconds, float intensity) {
		return new CHHapticPatternDict().setPattern(new NSArray<NSObject>(new CHHapticPatternDict()
			.setEvent(new CHHapticPatternDict().setEventType(CHHapticEventType.HapticContinuous).setTime(0.0)
				.setEventDuration(milliseconds / 1000f)
				.setEventParameters(new NSArray<NSObject>(new CHHapticPatternDict()
					.setParameterID(CHHapticEventParameterID.HapticIntensity).setParameterValue(intensity).getDictionary())))
			.getDictionary()));
	}

	@Override
	public void impact (ImpactType impactType) {
		if (hapticsSupport) {
			UIImpactFeedbackStyle uiImpactFeedbackStyle;
			switch (impactType) {
			case LIGHT:
				uiImpactFeedbackStyle = UIImpactFeedbackStyle.Light;
				break;
			case MEDIUM:
				uiImpactFeedbackStyle = UIImpactFeedbackStyle.Medium;
				break;
			case HEAVY:
				uiImpactFeedbackStyle = UIImpactFeedbackStyle.Heavy;
				break;
			default:
				throw new IllegalArgumentException("Unknown VibrationType " + impactType);
			}
			UIImpactFeedbackGenerator uiImpactFeedbackGenerator = new UIImpactFeedbackGenerator(uiImpactFeedbackStyle);
			uiImpactFeedbackGenerator.impactOccurred();
		}
	}

	@Override
	public boolean isHapticsSupported () {
		return hapticsSupport;
	}

	@Override
	public boolean isVibratorSupported () {
		return vibratorSupport;
	}

	@Override
	public boolean isFallbackEnabled() {
		return fallback;
	}

	@Override
	public void setFallbackEnabled (boolean fallback) {
		this.fallback = fallback;
	}

	public CHHapticEngine getHapticEngine () {
		return hapticEngine;
	}

}
