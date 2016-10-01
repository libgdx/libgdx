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
package com.badlogic.gdx.backends.iosmoe.objectal;

import org.moe.natj.general.NatJ;
import org.moe.natj.general.Pointer;
import org.moe.natj.general.ann.Generated;
import org.moe.natj.general.ann.Owned;
import org.moe.natj.general.ann.Runtime;
import org.moe.natj.general.ptr.VoidPtr;
import org.moe.natj.objc.ObjCRuntime;
import org.moe.natj.objc.ann.ObjCClassBinding;
import org.moe.natj.objc.ann.Selector;
import apple.NSObject;

@Generated
@Runtime(ObjCRuntime.class)
@ObjCClassBinding
public class ALBuffer extends NSObject {
	static {
		NatJ.register();
	}

	@Generated
	protected ALBuffer(Pointer peer) {
		super(peer);
	}

	@Generated
	@Owned
	@Selector("alloc")
	public static native ALBuffer alloc();

	@Generated
	@Selector("bits")
	public native int bits();

	@Generated
	@Selector("bufferId")
	public native int bufferId();

	@Generated
	@Selector("bufferWithName:data:size:format:frequency:")
	public static native ALBuffer bufferWithNameDataSizeFormatFrequency(
			String name, VoidPtr data, int size, int format, int frequency);

	@Generated
	@Selector("channels")
	public native int channels();

	@Generated
	@Selector("device")
	public native ALDevice device();

	@Generated
	@Selector("duration")
	public native float duration();

	@Generated
	@Selector("format")
	public native int format();

	@Generated
	@Selector("freeDataOnDestroy")
	public native boolean freeDataOnDestroy();

	@Generated
	@Selector("frequency")
	public native int frequency();

	@Generated
	@Selector("init")
	public native ALBuffer init();

	@Generated
	@Selector("initWithName:data:size:format:frequency:")
	public native ALBuffer initWithNameDataSizeFormatFrequency(String name,
			VoidPtr data, int size, int format, int frequency);

	@Generated
	@Selector("name")
	public native String name();

	@Generated
	@Selector("parentBuffer")
	public native ALBuffer parentBuffer();

	@Generated
	@Selector("setFreeDataOnDestroy:")
	public native void setFreeDataOnDestroy(boolean value);

	@Generated
	@Selector("setName:")
	public native void setName(String value);

	@Generated
	@Selector("setParentBuffer:")
	public native void setParentBuffer(ALBuffer value);

	@Generated
	@Selector("size")
	public native int size();

	@Generated
	@Selector("sliceWithName:offset:size:")
	public native ALBuffer sliceWithNameOffsetSize(String sliceName,
			int offset, int size);
}
