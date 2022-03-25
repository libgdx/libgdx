package com.badlogic.gdx.backends.iosmoe;

import apple.coreaudiotypes.enums.Enums;
import apple.openal.c.OpenAL;
import com.android.org.conscrypt.OpenSSLMessageDigestJDK;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.backends.iosmoe.objectal.ALBuffer;
import com.badlogic.gdx.backends.iosmoe.objectal.ALDevice;
import com.badlogic.gdx.backends.iosmoe.objectal.ALSource;
import com.badlogic.gdx.backends.iosmoe.objectal.OALSimpleAudio;
import org.moe.natj.general.ptr.IntPtr;
import org.moe.natj.general.ptr.ShortPtr;
import org.moe.natj.general.ptr.VoidPtr;
import org.moe.natj.general.ptr.impl.PtrFactory;
import org.moe.natj.objc.ObjCRuntime;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;

import static apple.openal.c.OpenAL.*;

public class IOSAudioDevice implements AudioDevice {

	private ALSource alSource;
	private ArrayList<ALBuffer> alBuffers = new ArrayList<>();
	private ArrayList<ALBuffer> alBuffersFree = new ArrayList<>();
	private int samplingRate;
	private boolean isMono;
	private int format;
	private ShortBuffer tmpBuffer;
	private int minSize;
	private int latency;

	public IOSAudioDevice(int samplingRate, boolean isMono, int minSize, int bufferCount) {
		this.samplingRate = samplingRate;
		this.isMono = isMono;
		this.format = isMono ? 0x1101 : 0x1103;
		this.minSize = minSize;
		// This will use the native byte order. On iOS this should always be little endian.
		// This is relevant because it might be, that iOS OpenAL only supports little endian, contrary to the OpenAL specification.
		tmpBuffer = ShortBuffer.allocate(minSize);
		latency = minSize / (isMono ? 1 : 2) / bufferCount;
		alSource = ALSource.alloc().init();
		for (int i = 0; i < bufferCount; i++) {
			ALBuffer buffer = ALBuffer.alloc().initWithNameDataSizeFormatFrequency("test", null, 0, format, samplingRate);
			alBuffersFree.add(buffer);
		}
	}

	@Override
	public boolean isMono () {
		return isMono;
	}

	@Override
	public void writeSamples (short[] samples, int offset, int numSamples) {
		ShortPtr voidPtr;
		if (numSamples + tmpBuffer.position() >= minSize) {
			// We can now process the data from the temp buffer
			voidPtr = PtrFactory.newShortArray(numSamples + tmpBuffer.position());

			voidPtr.copyFrom(tmpBuffer.array(), 0, 0, tmpBuffer.position());
			voidPtr.copyFrom(samples, offset, tmpBuffer.position(), numSamples);
			numSamples += tmpBuffer.position();
			tmpBuffer.position(0);
		} else {
			tmpBuffer.put(samples, offset, numSamples);
			return;
		}

		if (alBuffersFree.size() == 0) {
			while (true) {
				int i = alSource.buffersProcessed();
				for (int j = 0; j < i; j++) {
					ALBuffer alBuffer = alBuffers.remove(j);
					alSource.unqueueBuffer(alBuffer);
					alBuffersFree.add(alBuffer);
				}
				if (i != 0) {
					break;
				}
			}
		}
		ALBuffer buffer = alBuffersFree.remove(0);
		alBufferData(buffer.bufferId(), format, voidPtr, numSamples * 2, samplingRate);
		alSource.queueBuffer(buffer);
		alBuffers.add(buffer);
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
		/*for (ALBuffer buffer : alBuffersFree) {
			ObjCRuntime.disposeObject(buffer);
		}
		for (ALBuffer buffer : alBuffers) {
			ObjCRuntime.disposeObject(buffer);
		}
		ObjCRuntime.disposeObject(alSource);*/
		// Maybe let GC handle the disposing?
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
