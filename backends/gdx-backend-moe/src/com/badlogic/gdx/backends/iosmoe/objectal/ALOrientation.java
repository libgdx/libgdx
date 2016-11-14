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

import org.moe.natj.c.StructObject;
import org.moe.natj.c.ann.Structure;
import org.moe.natj.c.ann.StructureField;
import org.moe.natj.general.NatJ;
import org.moe.natj.general.Pointer;
import org.moe.natj.general.ann.ByValue;
import org.moe.natj.general.ann.Generated;

@Generated
@Structure()
public final class ALOrientation extends StructObject {
	static {
		NatJ.register();
	}
	private static long __natjCache;

	@Generated
	public ALOrientation() {
		super(ALOrientation.class);
	}

	@Generated
	protected ALOrientation(Pointer peer) {
		super(peer);
	}

	@Generated
	public ALOrientation(@ByValue ALVector at, @ByValue ALVector up) {
		super(ALOrientation.class);
		setAt(at);
		setUp(up);
	}

	@Generated
	@StructureField(order = 0, isGetter = true)
	@ByValue
	public native ALVector at();

	@Generated
	@StructureField(order = 0, isGetter = false)
	public native void setAt(@ByValue ALVector value);

	@Generated
	@StructureField(order = 1, isGetter = true)
	@ByValue
	public native ALVector up();

	@Generated
	@StructureField(order = 1, isGetter = false)
	public native void setUp(@ByValue ALVector value);
}
