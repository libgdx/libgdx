
package com.badlogic.gdx.backends.android;

import android.os.Handler;
import com.badlogic.gdx.audio.Sound;
import java.util.concurrent.atomic.AtomicInteger;

public class AsynchronousSound implements Sound {

	private final Sound sound;
	private final Handler handler;
	private final AtomicInteger playedSoundsCounter = new AtomicInteger();
	private final int soundIdsCountToSave;
	private final long[] soundIds;

	public AsynchronousSound (Sound sound, Handler handler, int soundIdsCountToSave) {
		this.sound = sound;
		this.handler = handler;
		this.soundIdsCountToSave = soundIdsCountToSave;
		this.soundIds = new long[soundIdsCountToSave];
	}

	@Override
	public long play () {
		final int soundNumber = playedSoundsCounter.getAndIncrement();
		handler.post(new Runnable() {
			@Override
			public void run () {
				long soundId = sound.play();
				saveSoundId(soundNumber, soundId);
			}
		});
		return soundNumber;
	}

	@Override
	public long play (final float volume) {
		final int soundNumber = playedSoundsCounter.getAndIncrement();
		handler.post(new Runnable() {
			@Override
			public void run () {
				long soundId = sound.play(volume);
				saveSoundId(soundNumber, soundId);
			}
		});
		return soundNumber;
	}

	@Override
	public long play (final float volume, final float pitch, final float pan) {
		final int soundNumber = playedSoundsCounter.getAndIncrement();
		handler.post(new Runnable() {
			@Override
			public void run () {
				long soundId = sound.play(volume, pitch, pan);
				saveSoundId(soundNumber, soundId);
			}
		});
		return soundNumber;
	}

	@Override
	public long loop () {
		final int soundNumber = playedSoundsCounter.getAndIncrement();
		handler.post(new Runnable() {
			@Override
			public void run () {
				long soundId = sound.loop();
				saveSoundId(soundNumber, soundId);
			}
		});
		return soundNumber;
	}

	@Override
	public long loop (final float volume) {
		final int soundNumber = playedSoundsCounter.getAndIncrement();
		handler.post(new Runnable() {
			@Override
			public void run () {
				long soundId = sound.loop(volume);
				saveSoundId(soundNumber, soundId);
			}
		});
		return soundNumber;
	}

	@Override
	public long loop (final float volume, final float pitch, final float pan) {
		final int soundNumber = playedSoundsCounter.getAndIncrement();
		handler.post(new Runnable() {
			@Override
			public void run () {
				long soundId = sound.loop(volume, pitch, pan);
				saveSoundId(soundNumber, soundId);
			}
		});
		return soundNumber;
	}

	@Override
	public void stop () {
		sound.stop();
	}

	@Override
	public void pause () {
		sound.pause();
	}

	@Override
	public void resume () {
		sound.resume();
	}

	@Override
	public void dispose () {
		sound.dispose();
	}

	@Override
	public void stop (long soundId) {
		long realSoundId = getSoundId(soundId);
		sound.stop(realSoundId);
	}

	@Override
	public void pause (long soundId) {
		long realSoundId = getSoundId(soundId);
		sound.pause(realSoundId);
	}

	@Override
	public void resume (long soundId) {
		long realSoundId = getSoundId(soundId);
		sound.resume(realSoundId);
	}

	@Override
	public void setLooping (long soundId, boolean looping) {
		long realSoundId = getSoundId(soundId);
		sound.setLooping(realSoundId, looping);
	}

	@Override
	public void setPitch (long soundId, float pitch) {
		long realSoundId = getSoundId(soundId);
		sound.setPitch(realSoundId, pitch);
	}

	@Override
	public void setVolume (long soundId, float volume) {
		long realSoundId = getSoundId(soundId);
		sound.setVolume(realSoundId, volume);
	}

	@Override
	public void setPan (long soundId, float pan, float volume) {
		long realSoundId = getSoundId(soundId);
		sound.setPan(realSoundId, pan, volume);
	}

	private void saveSoundId(int soundNumber, long soundId) {
		soundIds[soundNumber % soundIdsCountToSave] = soundId;
	}

	private long getSoundId(long soundId) {
		return soundIds[(int)soundId % soundIdsCountToSave];
	}
}
