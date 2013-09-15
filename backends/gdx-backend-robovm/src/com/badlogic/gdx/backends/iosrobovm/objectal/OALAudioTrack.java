/*******************************************************************************
 * Copyright 2013 See AUTHORS file.
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

package com.badlogic.gdx.backends.iosrobovm.objectal;

import org.robovm.cocoatouch.foundation.NSObject;
import org.robovm.objc.ObjCClass;
import org.robovm.objc.ObjCObject;
import org.robovm.objc.ObjCRuntime;
import org.robovm.objc.Selector;
import org.robovm.objc.annotation.NativeClass;
import org.robovm.rt.bro.annotation.Bridge;
import org.robovm.rt.bro.annotation.Library;

/**
 * @author Niklas Therning
 */
@Library(Library.INTERNAL)
@NativeClass
public class OALAudioTrack extends NSObject {

	static {
		ObjCRuntime.bind(OALAudioTrack.class);
	}
	
    private static final ObjCClass objCClass = ObjCClass.getByType(OALAudioTrack.class);

    private static final Selector track = Selector.register("track");
    @Bridge private native static OALAudioTrack objc_track(ObjCClass __self__, Selector __cmd__);
    public static OALAudioTrack create() {
    	return objc_track(objCClass, track);
    }

    private static final Selector preloadFile$ = Selector.register("preloadFile:");
    @Bridge private native static boolean objc_preloadFile(OALAudioTrack __self__, Selector __cmd__, String filePath);
    public boolean preloadFile(String filePath) {
    	return objc_preloadFile(this, preloadFile$, filePath);
    }

    private static final Selector stop = Selector.register("stop");
    @Bridge private native static void objc_stop(OALAudioTrack __self__, Selector __cmd__);
    public void stop() {
    	objc_stop(this, stop);
    }

    private static final Selector clear = Selector.register("clear");
    @Bridge private native static void objc_clear(OALAudioTrack __self__, Selector __cmd__);
    public void clear() {
    	objc_clear(this, clear);
    }

    private static final Selector play = Selector.register("play");
    @Bridge private native static boolean objc_play(OALAudioTrack __self__, Selector __cmd__);
    public boolean play() {
    	return objc_play(this, play);
    }
    
    private static final Selector paused = Selector.register("paused");
    @Bridge private native static boolean objc_isPaused(OALAudioTrack __self__, Selector __cmd__);
    public boolean isPaused() {
        return objc_isPaused(this, paused);
    }
    
    private static final Selector setPaused$ = Selector.register("setPaused:");
    @Bridge private native static void objc_setPaused(OALAudioTrack __self__, Selector __cmd__, boolean paused);
    public void setPaused(boolean paused) {
    	objc_setPaused(this, setPaused$, paused);
    }

    private static final Selector playing = Selector.register("playing");
    @Bridge private native static boolean objc_isPlaying(OALAudioTrack __self__, Selector __cmd__);
    public boolean isPlaying() {
        return objc_isPlaying(this, playing);
    }
    
    private static final Selector volume = Selector.register("volume");
    @Bridge private native static float objc_getVolume(OALAudioTrack __self__, Selector __cmd__);
    public float getVolume() {
        return objc_getVolume(this, volume);
    }
    
    private static final Selector setVolume$ = Selector.register("setVolume:");
    @Bridge private native static void objc_setVolume(OALAudioTrack __self__, Selector __cmd__, float volume);
    public void setVolume(float volume) {
    	objc_setVolume(this, setVolume$, volume);
    }

    private static final Selector pan = Selector.register("pan");
    @Bridge private native static float objc_getPan(OALAudioTrack __self__, Selector __cmd__);
    public float getPan() {
        return objc_getPan(this, pan);
    }
    
    private static final Selector setPan$ = Selector.register("setPan:");
    @Bridge private native static void objc_setPan(OALAudioTrack __self__, Selector __cmd__, float pan);
    public void setPan(float pan) {
    	objc_setPan(this, setPan$, pan);
    }

    private static final Selector currentTime = Selector.register("currentTime");
    @Bridge private native static double objc_getCurrentTime(OALAudioTrack __self__, Selector __cmd__);
    public double getCurrentTime() {
        return objc_getCurrentTime(this, currentTime);
    }
    
    private static final Selector setCurrentTime$ = Selector.register("setCurrentTime:");
    @Bridge private native static void objc_setCurrentTime(OALAudioTrack __self__, Selector __cmd__, double currentTime);
    public void setCurrentTime(double currentTime) {
    	objc_setCurrentTime(this, setCurrentTime$, currentTime);
    }
    
    private static final Selector numberOfLoops = Selector.register("numberOfLoops");
    @Bridge private native static int objc_getNumberOfLoops(OALAudioTrack __self__, Selector __cmd__);
    public int getNumberOfLoops() {
        return objc_getNumberOfLoops(this, numberOfLoops);
    }
    
    private static final Selector setNumberOfLoops$ = Selector.register("setNumberOfLoops:");
    @Bridge private native static void objc_setNumberOfLoops(OALAudioTrack __self__, Selector __cmd__, int numberOfLoops);
    public void setNumberOfLoops(int numberOfLoops) {
    	objc_setNumberOfLoops(this, setNumberOfLoops$, numberOfLoops);
    }

    private static final Selector delegate = Selector.register("delegate");
    @Bridge private native static AVAudioPlayerDelegate objc_getDelegate(OALAudioTrack __self__, Selector __cmd__);
    public AVAudioPlayerDelegate getDelegate() {
        return objc_getDelegate(this, delegate);
    }

    private static final Selector setDelegate$ = Selector.register("setDelegate:");
    @Bridge private native static void objc_setDelegate(OALAudioTrack __self__, Selector __cmd__, AVAudioPlayerDelegate delegate);
    public void setDelegate(AVAudioPlayerDelegate delegate) {
    	this.addStrongRef((ObjCObject) delegate);
    	objc_setDelegate(this, setDelegate$, delegate);
    }

}
