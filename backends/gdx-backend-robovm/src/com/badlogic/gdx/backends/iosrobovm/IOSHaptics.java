package com.badlogic.gdx.backends.iosrobovm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import org.robovm.apple.audiotoolbox.AudioServices;
import org.robovm.apple.corehaptic.CHHapticEngine;
import org.robovm.apple.corehaptic.CHHapticEventParameter;
import org.robovm.apple.corehaptic.CHHapticEventParameterID;
import org.robovm.apple.corehaptic.CHHapticEventType;
import org.robovm.apple.corehaptic.CHHapticPattern;
import org.robovm.apple.corehaptic.CHHapticPatternDict;
import org.robovm.apple.foundation.NSArray;
import org.robovm.apple.foundation.NSError;
import org.robovm.apple.foundation.NSErrorException;
import org.robovm.apple.foundation.NSProcessInfo;
import org.robovm.apple.uikit.UIImpactFeedbackGenerator;
import org.robovm.apple.uikit.UIImpactFeedbackStyle;
import org.robovm.objc.block.VoidBlock1;

public class IOSHaptics {

	private CHHapticEngine hapticEngine;
	private boolean hapticsSupport;
	
	public IOSHaptics () {
		if (NSProcessInfo.getSharedProcessInfo().getOperatingSystemVersion().getMajorVersion() >= 13) {
			hapticsSupport = CHHapticEngine.capabilitiesForHardware().supportsHaptics();
			if (hapticsSupport) {
				NSError.NSErrorPtr ptr = new NSError.NSErrorPtr();
				hapticEngine = new CHHapticEngine(ptr);
				if (ptr.get() != null) {
					Gdx.app.error("IOSHaptics", "Error creating CHHapticEngine. Haptics will be disabled.");
					hapticsSupport = false;
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
								Gdx.app.error("IOSHaptics", "Error restarting CHHapticEngine. Haptics will be disabled.");
								hapticsSupport = false;
							}
						});
					}
				});
			}
		}
	}
	
	public void vibrate(int milliseconds) {
		if (hapticsSupport) {
			CHHapticPatternDict dict = new CHHapticPatternDict();
			NSArray<CHHapticEventParameter> parameters = new NSArray<>();
			parameters.add(new CHHapticEventParameter(CHHapticEventParameterID.HapticIntensity, 0.5f));
			dict.setEventType(CHHapticEventType.HapticTransient);
			dict.setEventParameters(parameters);
			dict.setEventDuration(milliseconds / 1000f);
			dict.setTime(0);
			try {
				CHHapticPattern pattern = new CHHapticPattern(dict);
				NSError.NSErrorPtr ptr = new NSError.NSErrorPtr();
				hapticEngine.createPlayer(pattern).start(0, ptr);
				if (ptr.get() != null) {
					Gdx.app.error("IOSHaptics", "Error starting haptics pattern.");
				}
			} catch (NSErrorException e) {
				Gdx.app.error("IOSHaptics", "Error creating haptics player. " + e.getMessage());
			}
		} else {
			AudioServices.playSystemSound(4095);
		}
	}
	
	public void vibrate(int milliseconds, int amplitude, boolean fallback) {
		if (hapticsSupport) {
			float intensity = MathUtils.clamp(amplitude / 255f, 0, 1);
			CHHapticPatternDict dict = new CHHapticPatternDict();
			NSArray<CHHapticEventParameter> parameters = new NSArray<>();
			parameters.add(new CHHapticEventParameter(CHHapticEventParameterID.HapticIntensity, intensity));
			dict.setEventType(CHHapticEventType.HapticTransient);
			dict.setEventParameters(parameters);
			dict.setEventDuration(milliseconds / 1000f);
			dict.setTime(0);
			try {
				CHHapticPattern pattern = new CHHapticPattern(dict);
				NSError.NSErrorPtr ptr = new NSError.NSErrorPtr();
				hapticEngine.createPlayer(pattern).start(0, ptr);
				if (ptr.get() != null) {
					Gdx.app.error("IOSHaptics", "Error starting haptics pattern.");
				}
			} catch (NSErrorException e) {
				Gdx.app.error("IOSHaptics", "Error creating haptics player. " + e.getMessage());
			}
		} else if (fallback) {
			vibrate(milliseconds);
		}
	}

	public void vibrate (Input.VibrationType vibrationType, boolean fallback) {
		final int DEFAULT_LENGTH = 50;
		if (hapticsSupport) {
			UIImpactFeedbackStyle uiImpactFeedbackStyle;
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
			UIImpactFeedbackGenerator uiImpactFeedbackGenerator = new UIImpactFeedbackGenerator(uiImpactFeedbackStyle);
			uiImpactFeedbackGenerator.impactOccurred();
		} else if (fallback) {
			vibrate(DEFAULT_LENGTH);
		}
	}
	
	public boolean isHapticsSupported() {
		return hapticsSupport;
	}
	
	
}
