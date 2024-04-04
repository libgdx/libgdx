
package com.badlogic.gdx.backends.iosrobovm.objectal;

import com.badlogic.gdx.audio.AudioDevice;
import org.robovm.rt.bro.Struct;
import org.robovm.rt.bro.ptr.ShortPtr;
import org.robovm.rt.bro.ptr.VoidPtr;

import java.nio.ShortBuffer;
import java.util.ArrayList;

/** @author Jile Gao
 * @author Bernstanio */
class OALIOSAudioDevice implements AudioDevice {
	private ALSource alSource;
	private ArrayList<ALBuffer> alBuffers = new ArrayList<>();
	private ArrayList<ALBuffer> alBuffersFree = new ArrayList<>();
	private final int samplingRate;
	private final boolean isMono;
	private final int format;
	private final ShortBuffer tmpBuffer;
	private final int minSize;
	private final int latency;

	OALIOSAudioDevice (int samplingRate, boolean isMono, int minSize, int bufferCount) {
		this.samplingRate = samplingRate;
		this.isMono = isMono;
		this.format = isMono ? 0x1101 : 0x1103; // AL_FORMAT_MONO16 : AL_FORMAT_STEREO16
		this.minSize = minSize;

		tmpBuffer = ShortBuffer.allocate(minSize);
		latency = minSize / (isMono ? 1 : 2) / bufferCount;
		alSource = new ALSource();
		for (int i = 0; i < bufferCount; i++) {
			ALBuffer buffer = new ALBuffer().initWithNameDataSizeFormatFrequency("test", Struct.allocate(VoidPtr.class, 1), 2,
				format, samplingRate);
			alBuffersFree.add(buffer);
		}
	}

	@Override
	public boolean isMono () {
		return isMono;
	}

	@Override
	public void writeSamples (short[] samples, int offset, int numSamples) {
		if (numSamples < 0) throw new IllegalArgumentException("numSamples cannot be < 0.");
		ShortPtr shortPtr;
		if (numSamples + tmpBuffer.position() >= minSize) {
			shortPtr = Struct.allocate(ShortPtr.class, numSamples + tmpBuffer.position());

			shortPtr.set(tmpBuffer.array(), 0, tmpBuffer.position());
			shortPtr.next(tmpBuffer.position()).set(samples, offset, numSamples);
			numSamples += tmpBuffer.position();
			tmpBuffer.position(0);
		} else {
			tmpBuffer.put(samples, offset, numSamples);
			return;
		}

		if (alBuffersFree.isEmpty()) {
			if (OALAudioSession.sharedInstance().interrupted()) {
				try {
					Thread.sleep(2);
				} catch (InterruptedException ignored) {
				}
				return;
			}
			boolean freedBuffer = false;
			int toFree = Math.min(alSource.buffersProcessed(), alBuffers.size());
			int j = 0;
			while (!freedBuffer) {
				ALBuffer alBuffer = alBuffers.get(j);
				if (alSource.unqueueBuffer(alBuffer)) {
					alBuffersFree.add(alBuffer);
					alBuffers.remove(alBuffer);
					freedBuffer = true;
				} else {
					j += 1;
					if (j >= toFree) {
						j = 0;
						try {
							Thread.sleep(2);
						} catch (InterruptedException ignored) {
						}
					}
				}
			}
		}
		ALBuffer buffer = alBuffersFree.remove(0);
		ALWrapper.bufferData(buffer.bufferId(), format, shortPtr.as(VoidPtr.class), numSamples * 2, samplingRate);
		if (alSource.queueBuffer(buffer)) {
			alBuffers.add(buffer);
		}
		if (!alSource.playing()) {
			alSource.play();
		}
	}

	@Override
	public void writeSamples (float[] samples, int offset, int numSamples) {
		short[] shortSamples = new short[samples.length];

		for (int i = offset, j = 0; i < samples.length; i++, j++) {
			float fValue = samples[i];
			if (fValue > 1) fValue = 1;
			if (fValue < -1) fValue = -1;
			short value = (short)(fValue * Short.MAX_VALUE);
			shortSamples[j] = value;
		}
		writeSamples(shortSamples, offset, numSamples);
	}

	@Override
	public int getLatency () {
		return latency;
	}

	@Override
	public void dispose () {
		alSource.stop();
		alBuffers = null;
		alBuffersFree = null;
		alSource = null;
	}

	@Override
	public void setVolume (float volume) {
		alSource.setVolume(volume);
	}

	@Override
	public void pause () {
		alSource.setPaused(true);
	}

	@Override
	public void resume () {
		alSource.setPaused(false);
	}
}
