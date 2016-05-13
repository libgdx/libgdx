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

package com.badlogic.gdx.backends.iosmoe.custom;

import com.badlogic.gdx.utils.BufferUtils;
import com.intel.moe.natj.c.CRuntime;
import com.intel.moe.natj.c.ann.CFunction;
import com.intel.moe.natj.general.NatJ;
import com.intel.moe.natj.general.ann.Runtime;
import com.intel.moe.natj.general.ann.UncertainArgument;
import com.intel.moe.natj.general.ptr.BytePtr;
import com.intel.moe.natj.general.ptr.IntPtr;
import com.intel.moe.natj.general.ptr.impl.PtrFactory;
import com.intel.moe.natj.general.ptr.VoidPtr;

@Runtime(CRuntime.class)
public class HWMachine {
	static {
		NatJ.register();
	}

	@CFunction
	public static native int sysctlbyname(@UncertainArgument("Options: java.string, c.const-byte-ptr Fallback: java.string") String name,
		VoidPtr oldp, IntPtr oldlenp, VoidPtr newp, int newlen);

	public static String getMachineString () {
		String name = "hw.machine";
		IntPtr sizePtr = PtrFactory.newIntPtr(BufferUtils.newIntBuffer(1));
		sysctlbyname(name, null, sizePtr, null, 0);
		BytePtr machine = PtrFactory.newBytePtr(sizePtr.get(), true, true);
		sysctlbyname(name, machine, sizePtr, null, 0);
		return machine.toUTF8String();
	}
}
