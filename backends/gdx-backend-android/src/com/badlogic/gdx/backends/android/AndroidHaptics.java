package com.badlogic.gdx.backends.android;

import android.content.Context;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;

public class AndroidHaptics {

	private final Vibrator vibrator;
	private AudioAttributes audioAttributes;
	private boolean vibratorSupport;
	private boolean amplitudeSupport;

	public AndroidHaptics (Context context) {
		vibratorSupport = false;
		amplitudeSupport = false;
		this.vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
		if (vibrator != null && vibrator.hasVibrator()) {
			vibratorSupport = true;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				if (vibrator.hasAmplitudeControl()) {
					amplitudeSupport = true;
				}
				this.audioAttributes = new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).setUsage(AudioAttributes.USAGE_GAME).build();
			}
		}
	}

	public void vibrate (int milliseconds) {
		if (vibratorSupport) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
				vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE));
			else
				vibrator.vibrate(milliseconds);
		}
	}

	public void vibrate (Input.VibrationType vibrationType, boolean fallback) {
		final long DEFAULT_LENGTH = 50;
		if (amplitudeSupport) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
				int vibrationEffect;
				switch (vibrationType) {
				case LIGHT:
					vibrationEffect = VibrationEffect.EFFECT_TICK;
					break;
				case MEDIUM:
					vibrationEffect = VibrationEffect.EFFECT_CLICK;
					break;
				case HEAVY:
					vibrationEffect = VibrationEffect.EFFECT_HEAVY_CLICK;
					break;
				default:
					throw new IllegalArgumentException("Unknown VibrationType " + vibrationType);
				}
				vibrator.vibrate(VibrationEffect.createPredefined(vibrationEffect), audioAttributes);
			} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				int amplitude;
				switch (vibrationType) {
				case LIGHT:
					amplitude = 50;
					break;
				case MEDIUM:
					amplitude = VibrationEffect.DEFAULT_AMPLITUDE;
					break;
				case HEAVY:
					amplitude = 250;
					break;
				default:
					throw new IllegalArgumentException("Unknown VibrationType " + vibrationType);
				}
				vibrator.vibrate(VibrationEffect.createOneShot(DEFAULT_LENGTH, amplitude));
			}
		} else if (fallback) {
			vibrate((int) DEFAULT_LENGTH);
		}
	}

	public void vibrate (int milliseconds, int intensity, boolean fallback) {
		if (amplitudeSupport) {
			intensity = MathUtils.clamp(intensity, 0, 255);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
				vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, intensity));
		} else if (fallback)
			vibrate(milliseconds);
	}
	
	public boolean hasVibratorAvailable () {
		return vibratorSupport;
	}

	public boolean hasAmplitudeSupport () {
		return amplitudeSupport;
	}
}
