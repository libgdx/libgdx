package com.btdstudio.glbase;

public class IAnimationPlayer {
	public enum PlayMode {
		LOOP,
		PLAY_ONCE,
		KEEP_END
	}
	
    // @off
	/*JNI
	    #include "IanimationPlayer.h"
	    #include <time.h>
    */
	
	long handle;
	
	IAnimationPlayer(long handle) {
		this.handle = handle;
	}
	
	public void play( PlayMode playMode ){
		play(playMode, 0);
	}
	
	public void play( PlayMode playMode, int addStartTimeMS ){
		play(handle, playMode.ordinal(), addStartTimeMS);
	}
	
	public void replay(){
		replay(handle);
	}
	
	public void stop(){
		stop(handle);
	}
	
	public void rewind(){
		rewind(handle);
	}
	
	public void unsetAnimation(){
		unsetAnimation(handle);
	}
	
	public boolean isPlaying(){
		return isPlaying(handle);
	}
	
	public void update(){
		update(handle);
	}
	
	public void playSync(PlayMode playMode, IAnimationPlayer parent){
		playSync(handle, playMode.ordinal(), parent.handle);
	}
	
	public void stopSync(IAnimationPlayer parent){
		stopSync(handle, parent.handle);
	}
	
	public void replaySync(IAnimationPlayer parent){
		replaySync(handle, parent.handle);
	}
	
	public void dispose(){
		dispose(handle);
	}
	
	  
	// --- PRIVATE ---
	
	private static native void play(long handle, int playMode, int addStartTimeMS); /*
		((IAnimationPlayer*)handle)->play((IAnimationPlayer::PlayMode)playMode, addStartTimeMS);
	*/
	
	private static native void playSync(long handle, int playMode, long parentHandle); /*
		IAnimationPlayer* parent = (IAnimationPlayer*)parentHandle;
		((IAnimationPlayer*)handle)->playSync((IAnimationPlayer::PlayMode)playMode, parent);
	*/
	
	private static native void stopSync(long handle, long parentHandle); /*
		IAnimationPlayer* parent = (IAnimationPlayer*)parentHandle;
		((IAnimationPlayer*)handle)->stopSync(parent);
	*/
	
	private static native void replaySync(long handle, long parentHandle); /*
		IAnimationPlayer* parent = (IAnimationPlayer*)parentHandle;
		((IAnimationPlayer*)handle)->replaySync(parent);
	*/
	
	private static native void replay(long handle); /*
		((IAnimationPlayer*)handle)->replay();
	*/
	
	private static native void stop(long handle); /*
		((IAnimationPlayer*)handle)->stop();
	*/
	
	private static native void rewind(long handle); /*
		((IAnimationPlayer*)handle)->rewind();
	*/
	
	private static native void unsetAnimation(long handle); /*
		((IAnimationPlayer*)handle)->unsetAnimation();
	*/
	
	private static native boolean isPlaying(long handle); /*
		return ((IAnimationPlayer*)handle)->isPlaying();
	*/
	
	private static native void update(long handle); /*
		struct timeval timev;
		gettimeofday( &timev, NULL );
		((IAnimationPlayer*)handle)->update(timev);
	*/
	
	private static native void dispose(long handle); /*
		delete (IAnimationPlayer*)handle;
	*/
}
