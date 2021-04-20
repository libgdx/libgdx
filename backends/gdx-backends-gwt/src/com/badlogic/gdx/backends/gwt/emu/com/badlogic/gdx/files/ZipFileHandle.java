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

package com.badlogic.gdx.files;

import com.badlogic.gdx.utils.GdxRuntimeException;

import java.io.File;
import java.io.InputStream;
import java.util.zip.ZipFile;

public class ZipFileHandle extends FileHandle {

	public ZipFileHandle (ZipFile archive, File file) {
	}

	public ZipFileHandle (ZipFile archive, String fileName) {
	}

	@Override
	public FileHandle child (String name) {
		throw new GdxRuntimeException("Stub");
	}

	@Override
	public FileHandle sibling (String name) {
		throw new GdxRuntimeException("Stub");
	}

	@Override
	public FileHandle parent () {
		throw new GdxRuntimeException("Stub");
	}

	@Override
	public InputStream read () {
		throw new GdxRuntimeException("Stub");
	}

	@Override
	public boolean exists () {
		throw new GdxRuntimeException("Stub");
	}

	@Override
	public long length () {
		throw new GdxRuntimeException("Stub");
	}

	@Override
	public long lastModified () {
		throw new GdxRuntimeException("Stub");
	}
}
