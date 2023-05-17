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

package com.badlogic.gdx.backends.android;

import android.annotation.SuppressLint;
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
	private boolean hapticsSupport;

	public AndroidHaptics (Context context) {
		vibratorSupport = false;
		hapticsSupport = false;
		this.vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
		if (vibrator != null && vibrator.hasVibrator()) {
			vibratorSupport = true;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
				if (vibrator.hasAmplitudeControl()) {
					hapticsSupport = true;
				}
				this.audioAttributes = new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
					.setUsage(AudioAttributes.USAGE_GAME).build();
			}
		}
	}

	@SuppressLint("MissingPermission")
	public void vibrate (int milliseconds) {
		if (vibratorSupport) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
				vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE));
			else
				vibrator.vibrate(milliseconds);
		}
	}

	@SuppressLint("MissingPermission")
	public void vibrate (Input.VibrationType vibrationType) {
		if (hapticsSupport) {
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
			}
		}
	}

	@SuppressLint("MissingPermission")
	public void vibrate (int milliseconds, int intensity, boolean fallback) {
		if (hapticsSupport) {
			intensity = MathUtils.clamp(intensity, 0, 255);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
				vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, intensity));
		} else if (fallback) vibrate(milliseconds);
	}

	public boolean hasVibratorAvailable () {
		return vibratorSupport;
	}

	public boolean hasHapticsSupport () {
		return hapticsSupport;
	}
}
