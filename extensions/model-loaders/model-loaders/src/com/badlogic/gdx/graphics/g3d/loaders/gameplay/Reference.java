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
package com.badlogic.gdx.graphics.g3d.loaders.gameplay;

/**
 * A reference to an object in a Gameplay Bundle file, 
 * see <a href="https://github.com/blackberry/GamePlay/blob/master/gameplay/src/Bundle.h#L102">Bundle.h</a>
 * @author mzechner
 *
 */
public class Reference {
	private final String id;
	private final Type type;
	private final int offset;
	
	public Reference(String id, Type type, int offset) {
		this.id = id;
		this.type = type;
		this.offset = offset;
	}

	public String getId () {
		return id;
	}

	public Type getType () {
		return type;
	}

	public int getOffset () {
		return offset;
	}

	@Override
	public String toString () {
		return "Reference [id=" + id + ", type=" + type + ", offset=" + offset + "]";
	}
}