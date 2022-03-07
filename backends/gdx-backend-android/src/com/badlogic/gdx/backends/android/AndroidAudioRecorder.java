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

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioRecorder;

import java.util.Arrays;

/** {@link AudioRecorder} implementation for the android system based on AudioRecord
 * @author badlogicgames@gmail.com */
public class AndroidAudioRecorder implements AudioRecorder, ActivityCompat.OnRequestPermissionsResultCallback {
	/** the audio track we read samples from **/
	private AudioRecord recorder;

	private final Context context;
	private final Activity activity;
	private static final String permission = Manifest.permission.RECORD_AUDIO;
	private static final String[] permissions = new String[] {permission};
	private static final int REQUEST_CODE = 1;

	public AndroidAudioRecorder (int samplingRate, boolean isMono, Context context) {
		this(samplingRate, isMono, context, true);
	}

	int channelConfig, minBufferSize, samplingRate;

	public AndroidAudioRecorder (int samplingRate, boolean isMono, Context context, boolean requestPermission) {
		this.context = context;
		this.activity = (Activity)context;

		this.samplingRate = samplingRate;
		channelConfig = isMono ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO;
		minBufferSize = AudioRecord.getMinBufferSize(samplingRate, channelConfig, AudioFormat.ENCODING_PCM_16BIT);

		if (requestPermission) requestPermission();
	}

	@Override
	public void read (short[] samples, int offset, int numSamples) {
		if (recorder != null && recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
			int read = 0;
			while (read != numSamples) {
				read += recorder.read(samples, offset + read, numSamples - read);
			}
		} else {
			Arrays.fill(samples, (short)0);
		}
	}

	@Override
	public Permissions hasPermission () {
		if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
			return Permissions.GRANTED;
		} else if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
			return Permissions.RATIONALE;
		} else {
			return Permissions.DENIED;
		}
	}

	@Override
	public Permissions requestPermission () {
		Permissions permissionStatus = hasPermission();
		if (Build.VERSION.SDK_INT >= 23 && permissionStatus != Permissions.GRANTED) {
			ActivityCompat.requestPermissions(activity, permissions, REQUEST_CODE);
		}
		return permissionStatus;
	}

	@Override
	public void dispose () {
		if (recorder.getState() == AudioRecord.STATE_INITIALIZED) {
			recorder.stop();
			recorder.release();
		}
	}

	@Override
	public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults) {
		Gdx.app.log("pine", "a");
		if (requestCode == REQUEST_CODE && Arrays.equals(permissions, AndroidAudioRecorder.permissions)) {
			recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, samplingRate, channelConfig, AudioFormat.ENCODING_PCM_16BIT,
				minBufferSize);
			recorder.startRecording();
		}
	}

}
