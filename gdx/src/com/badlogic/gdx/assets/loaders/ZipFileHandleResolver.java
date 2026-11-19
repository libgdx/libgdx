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

package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.files.ZipFileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.io.IOException;
import java.util.zip.ZipFile;

public class ZipFileHandleResolver implements FileHandleResolver {
	private final ZipFile archive;

	public ZipFileHandleResolver (String path) {
		try {
			this.archive = new ZipFile(path);
		} catch (IOException exc) {
			throw new GdxRuntimeException("Error while creating a ZipFile: " + path, exc);
		}
	}

	@Override
	public FileHandle resolve (String fileName) {
		return new ZipFileHandle(archive, fileName);
	}
}
