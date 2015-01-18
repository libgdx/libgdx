/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
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

package com.badlogic.gdx.backends.iosrobovm.custom;

import org.robovm.rt.bro.Bro;
import org.robovm.rt.bro.Struct;
import org.robovm.rt.bro.annotation.Bridge;
import org.robovm.rt.bro.annotation.Library;
import org.robovm.rt.bro.annotation.MachineSizedSInt;
import org.robovm.rt.bro.ptr.BytePtr;
import org.robovm.rt.bro.ptr.MachineSizedUIntPtr;
import org.robovm.rt.bro.ptr.VoidPtr;

@Library("c")
public class HWMachine {
	static {
		Bro.bind();
	}

	@Bridge
	public static native int sysctlbyname (BytePtr name, VoidPtr oldp,
			MachineSizedUIntPtr oldlenp, VoidPtr newp,
			@MachineSizedSInt long newlen);
	
	public static String getMachineString () {
		BytePtr name = BytePtr.toBytePtrAsciiZ("hw.machine");
		MachineSizedUIntPtr size = new MachineSizedUIntPtr();
		sysctlbyname(name, null, size, null, 0);
		BytePtr machine = Struct.allocate(BytePtr.class, (int) size.get());
		sysctlbyname(name, machine.as(VoidPtr.class), size, null, 0);
		return machine.toStringAsciiZ();
	}

}
