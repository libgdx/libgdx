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
public final class OALSimpleAudio extends NSObject {

	static {
		ObjCRuntime.bind(OALSimpleAudio.class);
	}
	
    private static final ObjCClass objCClass = ObjCClass.getByType(OALSimpleAudio.class);

    private static final Selector sharedInstance = Selector.register("sharedInstance");
    @Bridge private native static OALSimpleAudio objc_sharedInstance(ObjCClass __self__, Selector __cmd__);
    public static OALSimpleAudio sharedInstance() {
    	return objc_sharedInstance(objCClass, sharedInstance);
    }
    
    private static final Selector preloadEffect$ = Selector.register("preloadEffect:");
    @Bridge private native static ALBuffer objc_preloadEffect(OALSimpleAudio __self__, Selector __cmd__, String filePath);
    public ALBuffer preloadEffect(String filePath) {
    	return objc_preloadEffect(this, preloadEffect$, filePath);
    }

    private static final Selector unloadEffect$ = Selector.register("unloadEffect:");
    @Bridge private native static boolean objc_unloadEffect(OALSimpleAudio __self__, Selector __cmd__, String filePath);
    public boolean unloadEffect(String filePath) {
    	return objc_unloadEffect(this, unloadEffect$, filePath);
    }

    private static final Selector playEffect$volume$pitch$pan$loop$ = Selector.register("playEffect:volume:pitch:pan:loop:");
    @Bridge private native static ALSource objc_playEffect(OALSimpleAudio __self__, Selector __cmd__, String filePath, float volume, float pitch, float pan, boolean loop);
    public ALSource playEffect(String filePath, float volume, float pitch, float pan, boolean loop) {
    	return objc_playEffect(this, playEffect$volume$pitch$pan$loop$, filePath, volume, pitch, pan, loop);
    }

    private static final Selector allowIpod = Selector.register("allowIpod");
    @Bridge private native static boolean objc_isAllowIpod(OALSimpleAudio __self__, Selector __cmd__);
    public boolean isAllowIpod() {
        return objc_isAllowIpod(this, allowIpod);
    }
    
    private static final Selector setAllowIpod$ = Selector.register("setAllowIpod:");
    @Bridge private native static void objc_setAllowIpod(OALSimpleAudio __self__, Selector __cmd__, boolean allowIpod);
    public void setAllowIpod(boolean allowIpod) {
    	objc_setAllowIpod(this, setAllowIpod$, allowIpod);
    }

    private static final Selector honorSilentSwitch = Selector.register("honorSilentSwitch");
    @Bridge private native static boolean objc_isHonorSilentSwitch(OALSimpleAudio __self__, Selector __cmd__);
    public boolean isHonorSilentSwitch() {
        return objc_isHonorSilentSwitch(this, honorSilentSwitch);
    }
    
    private static final Selector setHonorSilentSwitch$ = Selector.register("setHonorSilentSwitch:");
    @Bridge private native static void objc_setHonorSilentSwitch(OALSimpleAudio __self__, Selector __cmd__, boolean honorSilentSwitch);
    public void setHonorSilentSwitch(boolean honorSilentSwitch) {
    	objc_setHonorSilentSwitch(this, setHonorSilentSwitch$, honorSilentSwitch);
    }
}
