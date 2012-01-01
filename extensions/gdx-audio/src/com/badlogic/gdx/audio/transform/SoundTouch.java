package com.badlogic.gdx.audio.transform;

import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.SharedLibraryLoader;

public class SoundTouch implements Disposable {
	/** Enable/disable anti-alias filter in pitch transposer (0 = disable) **/
	public static int SETTING_USE_AA_FILTER = 0;

	/** Pitch transposer anti-alias filter length (8 .. 128 taps, default = 32) **/
	public static int SETTING_AA_FILTER_LENGTH = 1;

	/** Enable/disable quick seeking algorithm in tempo changer routine
	 * (enabling quick seeking lowers CPU utilization but causes a minor sound
	 * quality compromising) 
	 */
	public static int SETTING_USE_QUICKSEEK = 2;

	/** Time-stretch algorithm single processing sequence length in milliseconds. This determines 
	 * to how long sequences the original sound is chopped in the time-stretch algorithm. 
	 * See "STTypes.h" or README for more information.
	 */
	public static int SETTING_SEQUENCE_MS = 3;

	/** Time-stretch algorithm seeking window length in milliseconds for algorithm that finds the 
	 * best possible overlapping location. This determines from how wide window the algorithm 
	 * may look for an optimal joining location when mixing the sound sequences back together. 
	 * See "STTypes.h" or README for more information.
	 */
	public static int SETTING_SEEKWINDOW_MS = 4;

	/** Time-stretch algorithm overlap length in milliseconds. When the chopped sound sequences 
	 * are mixed back together, to form a continuous sound stream, this parameter defines over 
	 * how long period the two consecutive sequences are let to overlap each other. 
	 * See "STTypes.h" or README for more information.
	 */
	public static int SETTING_OVERLAP_MS = 5;


	/** Call "getSetting" with this ID to query nominal average processing sequence
	 * size in samples. This value tells approcimate value how many input samples 
	 * SoundTouch needs to gather before it does DSP processing run for the sample batch.
	 *
	 * Notices: 
	 * - This is read-only parameter, i.e. setSetting ignores this parameter
	 * - Returned value is approximate average value, exact processing batch
	 *   size may wary from time to time
	 * - This parameter value is not constant but may change depending on 
	 *   tempo/pitch/rate/samplerate settings.
	 */
	public static int SETTING_NOMINAL_INPUT_SEQUENCE = 6;


	/** Call "getSetting" with this ID to query nominal average processing output 
	 * size in samples. This value tells approcimate value how many output samples 
	 * SoundTouch outputs once it does DSP processing run for a batch of input samples.
	 *	
	 * Notices: 
	 * - This is read-only parameter, i.e. setSetting ignores this parameter
	 * - Returned value is approximate average value, exact processing batch
	 *   size may wary from time to time
	 * - This parameter value is not constant but may change depending on 
	 *   tempo/pitch/rate/samplerate settings.
	 */
	public static int SETTING_NOMINAL_OUTPUT_SEQUENCE = 7;

	/** the address of the C++ object **/
	private final long addr;
	
	/*JNI
	#include "SoundTouch.h"
	using namespace soundtouch;
	 */
	
	/**
	 * Creates a new SoundTouch object. Needs to be disposed via {@link #dispose()}.
	 */
	public SoundTouch() {
		new SharedLibraryLoader().load("gdx-audio");
		addr = newSoundTouchJni();
	}

	private native long newSoundTouchJni(); /*
		return (jlong)(new SoundTouch());
	*/
	
	@Override
	public void dispose() {
		disposeJni(addr);
	}
	
	private native void disposeJni(long addr); /*
		delete (SoundTouch*)addr;
	*/
	
	/** Sets new rate control value. Normal rate = 1.0, smaller values
     * represent slower rate, larger faster rates.
     */
    public void setRate(float newRate) {
    	setRateJni(addr, newRate);
    }
    
    private native void setRateJni(long addr, float newRate); /*
    	((SoundTouch*)addr)->setRate(newRate);
    */

    /** Sets new tempo control value. Normal tempo = 1.0, smaller values
     * represent slower tempo, larger faster tempo.
     */
    public void setTempo(float newTempo) {
    	setTempoJni(addr, newTempo);
    }
    
    private native void setTempoJni(long addr, float newTempo); /*
    	((SoundTouch*)addr)->setTempo(newTempo);
    */

    /** Sets new rate control value as a difference in percents compared
     * to the original rate (-50 .. +100 %)
     */
    public void setRateChange(float newRate) {
    	setRateChangeJni(addr, newRate);
    }
    
    private native void setRateChangeJni(long addr, float newRate); /*
    	((SoundTouch*)addr)->setRateChange(newRate);
    */

    /** Sets new tempo control value as a difference in percents compared
     * to the original tempo (-50 .. +100 %)
     */
    public void setTempoChange(float newTempo) {
    	setTempoJni(addr, newTempo);
    }
    
    private native void setTempoChange(long addr, float newTempo); /*
    	((SoundTouch*)addr)->setTempoChange(newTempo);
    */

    /** Sets new pitch control value. Original pitch = 1.0, smaller values
     * represent lower pitches, larger values higher pitch.
     */
    public void setPitch(float newPitch) {
    	setPitchJni(addr, newPitch);
    }

    private native void setPitchJni(long addr, float newPitch); /*
		((SoundTouch*)addr)->setPitch(newPitch);
    */
	/** Sets pitch change in octaves compared to the original pitch  
     * (-1.00 .. +1.00)
     */
    public void setPitchOctaves(float newPitch) {
    	setPitchOctavesJni(addr, newPitch);
    }

    private native void setPitchOctavesJni(long addr, float newPitch); /*
    	((SoundTouch*)addr)->setPitchOctaves(newPitch);
    */

	/** Sets pitch change in semi-tones compared to the original pitch
     * (-12 .. +12)
     */
    public void setPitchSemiTones(int newPitch) {
    	setPitchSemiTonesJni(addr, newPitch);
    }
    
    private native void setPitchSemiTonesJni(long addr, int newPitch); /*
    	((SoundTouch*)addr)->setPitchSemiTones((int)newPitch);
    */

	/** Sets pitch change in semi-tones compared to the original pitch
     * (-12 .. +12)
     */
	public void setPitchSemiTones(float newPitch) {
		setPitchSemiTonesJni(addr, newPitch);
    }

    private native void setPitchSemiTonesJni(long addr, float newPitch); /*
    	((SoundTouch*)addr)->setPitchSemiTones((float)newPitch);
    */

	/** Sets the number of channels, 1 = mono, 2 = stereo **/ 
    public void setChannels(int numChannels) {
    	setChannelsJni(addr, numChannels);
    }

    private native void setChannelsJni(long addr, int numChannels); /*
    	((SoundTouch*)addr)->setChannels(numChannels);
    */

	/** Sets sample rate. **/
    public void setSampleRate(int srate) {
    	setSampleRateJni(addr, srate);
    }

    private native void setSampleRateJni(long addr, int srate); /*
    	((SoundTouch*)addr)->setSampleRate(srate);
    */

	/** Flushes the last samples from the processing pipeline to the output.
     * Clears also the internal processing buffers.
     *
     * Note: This function is meant for extracting the last samples of a sound
     * stream. This function may introduce additional blank samples in the end
     * of the sound stream, and thus it's not recommended to call this function
     * in the middle of a sound stream.
     */
    public void flush() {
    	flushJni(addr);
    }

    private native void flushJni(long addr); /*
    	((SoundTouch*)addr)->flush();
    */

	/** Adds 'numSamples' pcs of samples from the 'samples' memory position into
     * the input of the object. Notice that sample rate _has_to_ be set before
     * calling this function, otherwise throws a runtime_error exception.
     * 
     * Notice that in case of stereo-sound a single sample contains data for both channels.
     */
    public void putSamples(short[] samples, int offset, int numSamples) {
    	putSamplesJni(addr, samples, offset, numSamples);
    }

    private native void putSamplesJni(long addr, short[] samples, int offset, int numSamples); /*
    	((SoundTouch*)addr)->putSamples((const SAMPLETYPE *)samples + offset, numSamples);
	*/

	/** Clears all the samples in the object's output and internal processing
     * buffers.
     */
    public void clear() {
    	clearJni(addr);
    }

    private native void clearJni(long addr); /*
    	((SoundTouch*)addr)->clear();
    */

	/** Changes a setting controlling the processing system behaviour. See the
     * 'SETTING_...' defines for available setting ID's.
     * 
     * \return 'TRUE' if the setting was succesfully changed
     */
    public boolean setSetting(int settingId, int value) {
    	return setSettingJni(addr, settingId, value);
    }

    private native boolean setSettingJni(long addr, int settingId, int value); /*
    	return (jboolean)((SoundTouch*)addr)->setSetting(settingId, value);
    */
    
	/** Reads a setting controlling the processing system behaviour. See the
     * 'SETTING_...' defines for available setting ID's.
     *
     * \return the setting value.
     */
    public int getSetting(int settingId) {
    	return getSettingJni(addr, settingId);
    }

    private native int getSettingJni(long addr, int settingId); /*
    	return ((SoundTouch*)addr)->getSetting(settingId);
    */

	/** Returns number of samples currently unprocessed. **/
    public int numUnprocessedSamples() {
    	return numUnprocessedSamplesJni(addr);
    }

	private native int numUnprocessedSamplesJni(long addr); /*
		return ((SoundTouch*)addr)->numUnprocessedSamples();
	*/
	
    /** Returns number of samples currently available. **/  
	public int numSamples() {
		return numSamplesJni(addr);
	}

	private native int numSamplesJni(long addr); /*
		return ((SoundTouch*)addr)->numSamples();
	*/
	
	public static void main(String[] args) {
		SoundTouch soundTouch = new SoundTouch();
		soundTouch.setSampleRate(44100);
		soundTouch.setChannels(1);
		soundTouch.putSamples(new short[1024*10], 0, 1024*10);
		System.out.println(soundTouch.numSamples());
		soundTouch.dispose();
	}
}
