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

package com.badlogic.gwtref.client;

public class Parameter {
	final String name;
	final CachedTypeLookup type;
	final String jnsi;

	Parameter (String name, Class type, String jnsi) {
		this.name = name;
		this.type = new CachedTypeLookup(type);
		this.jnsi = jnsi;
	}

	public String getName () {
		return name;
	}

	public Type getType () {
		return type.getType();
	}

	public Class getClazz () {
		return type.clazz;
	}

	public String getJnsi () {
		return jnsi;
	}

	@Override
	public String toString () {
		return "Parameter [name=" + name + ", type=" + type + ", jnsi=" + jnsi + "]";
	}
}
