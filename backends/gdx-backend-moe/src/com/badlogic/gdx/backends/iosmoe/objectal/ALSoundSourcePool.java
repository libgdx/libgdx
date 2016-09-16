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
import org.moe.natj.general.ann.Mapped;
import org.moe.natj.general.ann.MappedReturn;
import org.moe.natj.general.ann.Owned;
import org.moe.natj.general.ann.Runtime;
import org.moe.natj.objc.ObjCRuntime;
import org.moe.natj.objc.ann.ObjCClassBinding;
import org.moe.natj.objc.ann.Selector;
import org.moe.natj.objc.map.ObjCObjectMapper;
import apple.NSObject;
import apple.foundation.NSArray;

@Generated
@Runtime(ObjCRuntime.class)
@ObjCClassBinding
public class ALSoundSourcePool extends NSObject {
	static {
		NatJ.register();
	}

	@Generated
	protected ALSoundSourcePool(Pointer peer) {
		super(peer);
	}

	@Generated
	@Selector("addSource:")
	public native void addSource(
			@Mapped(ObjCObjectMapper.class) ALSoundSource source);

	@Generated
	@Owned
	@Selector("alloc")
	public static native ALSoundSourcePool alloc();

	@Generated
	@Selector("getFreeSource:")
	@MappedReturn(ObjCObjectMapper.class)
	public native ALSoundSource getFreeSource(boolean attemptToInterrupt);

	@Generated
	@Selector("init")
	public native ALSoundSourcePool init();

	@Generated
	@Selector("pool")
	public static native ALSoundSourcePool pool();

	@Generated
	@Selector("removeSource:")
	public native void removeSource(
			@Mapped(ObjCObjectMapper.class) ALSoundSource source);

	@Generated
	@Selector("sources")
	public native NSArray<?> sources();
}
