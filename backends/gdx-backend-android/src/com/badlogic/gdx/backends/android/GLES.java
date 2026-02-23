/*******************************************************************************
 * Copyright 2024 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.backends.android;

import androidx.annotation.NonNull;

public enum GLES {
	GLES20(2, 0), GLES30(3, 0), GLES31(3, 1), GLES32(3, 2);

	public final int major, minor;

	private GLES (int major, int minor) {
		this.major = major;
		this.minor = minor;
	}

	public String getVersionString () {
		return major + "." + minor;
	}

	public GLES lower () {
		switch (this) {
		case GLES20:
			throw new IllegalStateException("Can't go lower than GLES20");
		case GLES30:
			return GLES20;
		case GLES31:
			return GLES30;
		case GLES32:
			return GLES31;
		default:
			throw new IllegalStateException("Unknown GLES version");
		}
	}

	@NonNull
	@Override
	public String toString () {
		return "OpenGL ES " + getVersionString();
	}
}
