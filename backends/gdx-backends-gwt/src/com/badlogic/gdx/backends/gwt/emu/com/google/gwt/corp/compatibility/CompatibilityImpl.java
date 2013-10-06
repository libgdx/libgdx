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

 /*
Copyright (C) 2010 Copyright 2010 Google Inc.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.google.gwt.corp.compatibility;

public class CompatibilityImpl implements Compatibility.Impl {

	public CompatibilityImpl () {
	}

	public int floatToIntBits (float f) {
		return Numbers.floatToIntBits(f);
	}

	public float intBitsToFloat (int i) {
		return Numbers.intBitsToFloat(i);
	}

	@Override
	public String createString (byte[] b, int ofs, int length) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getOriginatingServerAddress () {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void printStackTrace (Throwable e) {
		// TODO Auto-generated method stub

	}

	@Override
	public String createString (byte[] b, String encoding) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sleep (int i) {
		// TODO Auto-generated method stub

	}
}
