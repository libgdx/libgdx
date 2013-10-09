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
public class ALSource extends NSObject {

	static {
		ObjCRuntime.bind(ALSource.class);
	}

    private static final Selector stop = Selector.register("stop");
    @Bridge private native static void objc_stop(ALSource __self__, Selector __cmd__);
    public void stop() {
    	objc_stop(this, stop);
    }
	
    private static final Selector paused = Selector.register("paused");
    @Bridge private native static boolean objc_isPaused(ALSource __self__, Selector __cmd__);
    public boolean isPaused() {
        return objc_isPaused(this, paused);
    }
    
    private static final Selector setPaused$ = Selector.register("setPaused:");
    @Bridge private native static void objc_setPaused(ALSource __self__, Selector __cmd__, boolean paused);
    public void setPaused(boolean paused) {
    	objc_setPaused(this, setPaused$, paused);
    }
}
