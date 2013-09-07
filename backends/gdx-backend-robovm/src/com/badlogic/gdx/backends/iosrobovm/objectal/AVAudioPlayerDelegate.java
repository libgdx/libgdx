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
import org.robovm.cocoatouch.foundation.NSObjectProtocol;
import org.robovm.objc.Selector;
import org.robovm.objc.annotation.BindSelector;
import org.robovm.objc.annotation.NotImplemented;
import org.robovm.rt.bro.annotation.Callback;

/**
 * @author Niklas Therning
 */
public interface AVAudioPlayerDelegate extends NSObjectProtocol {

	void didFinishPlaying(NSObject player, boolean success);
	
    public static class Adapter extends NSObject implements AVAudioPlayerDelegate {
        @NotImplemented("audioPlayerDidFinishPlaying:successfully:") public void didFinishPlaying(NSObject player, boolean success) { throw new UnsupportedOperationException(); }
    }
	
	static class Callbacks {
        @Callback @BindSelector("audioPlayerDidFinishPlaying:successfully:") public static void didFinishPlaying(AVAudioPlayerDelegate __self__, Selector __cmd__, NSObject player, boolean success) { __self__.didFinishPlaying(player, success); }
	}
	
}
