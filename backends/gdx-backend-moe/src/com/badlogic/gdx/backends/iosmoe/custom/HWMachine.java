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
import org.moe.natj.c.CRuntime;
import org.moe.natj.c.StructObject;
import org.moe.natj.c.ann.CFunction;
import org.moe.natj.general.NatJ;
import org.moe.natj.general.ann.Runtime;
import org.moe.natj.general.ann.UncertainArgument;
import org.moe.natj.general.ptr.BytePtr;
import org.moe.natj.general.ptr.IntPtr;
import org.moe.natj.general.ptr.NUIntPtr;
import org.moe.natj.general.ptr.impl.PtrFactory;
import org.moe.natj.general.ptr.VoidPtr;

@Runtime(CRuntime.class)
public class HWMachine {
	static {
		NatJ.register();
	}

	@CFunction
	public static native int sysctlbyname(BytePtr name, VoidPtr oldp,
			NUIntPtr oldlenp, VoidPtr newp, long newlen);

	public static String getMachineString () {
		String name = "hw.machine";
		BytePtr namePtr = PtrFactory.newByteArray(name.getBytes());
		NUIntPtr sizePtr = PtrFactory.newNUIntReference();
		sysctlbyname(namePtr, null, sizePtr, null, 0);
		long longsize = sizePtr.get();
		BytePtr machinePtr = PtrFactory.newByteReference();
		machinePtr.setValue((byte) longsize);
		sysctlbyname(namePtr, machinePtr, sizePtr, null, 0);
		return machinePtr.toUTF8String();
	}
}
