package com.badlogic.gdx.backends.iosrobovm;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.AudioRecorder;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.iosrobovm.objectal.OALAudioTrack;
import com.badlogic.gdx.backends.iosrobovm.objectal.OALSimpleAudio;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class IOSAudio implements Audio {

	public IOSAudio() {
		OALSimpleAudio.sharedInstance().setAllowIpod(false);
		OALSimpleAudio.sharedInstance().setHonorSilentSwitch(true);
	}
	
	@Override
	public AudioDevice newAudioDevice (int samplingRate, boolean isMono) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AudioRecorder newAudioRecorder (int samplingRate, boolean isMono) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Sound newSound (FileHandle fileHandle) {
		return new IOSSound(fileHandle);
	}

	@Override
	public Music newMusic (FileHandle fileHandle) {
		String path = fileHandle.file().getPath().replace('\\', '/');
		OALAudioTrack track = OALAudioTrack.create();
		if (track != null) {
			if (track.preloadFile(path)) {
				return new IOSMusic(track);
			}
		}
		throw new GdxRuntimeException("Error opening music file at " + path);
	}

}