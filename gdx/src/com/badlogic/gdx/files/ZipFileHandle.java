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

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipFileHandle extends FileHandle {
	final ZipFile archive;
	final ZipEntry archiveEntry;

	public ZipFileHandle (ZipFile archive, File file) {
		super(file, FileType.Classpath);
		this.archive = archive;
		archiveEntry = this.archive.getEntry(file.getPath().replace('\\', '/'));
	}

	public ZipFileHandle (ZipFile archive, String fileName) {
		super(fileName.replace('\\', '/'), FileType.Classpath);
		this.archive = archive;
		this.archiveEntry = archive.getEntry(fileName.replace('\\', '/'));
	}

	@Override
	public FileHandle child (String name) {
		name = name.replace('\\', '/');
		if (file.getPath().length() == 0)
			return new ZipFileHandle(archive, new File(name));
		return new ZipFileHandle(archive, new File(file, name));
	}

	@Override
	public FileHandle sibling (String name) {
		name = name.replace('\\', '/');
		if (file.getPath().length() == 0)
			throw new GdxRuntimeException("Cannot get the sibling of the root.");
		return new ZipFileHandle(archive, new File(file.getParent(), name));
	}

	@Override
	public FileHandle parent () {
		File parent = file.getParentFile();
		if (parent == null) {
			if (type == FileType.Absolute)
				parent = new File("/");
			else
				parent = new File("");
		}
		return new ZipFileHandle(archive, parent);
	}

	@Override
	public InputStream read () {
		try {
			return archive.getInputStream(archiveEntry);
		} catch (IOException e) {
			throw new GdxRuntimeException("File not found: " + file + " (Archive)");
		}
	}

	@Override
	public boolean exists () {
		return archiveEntry != null;
	}

	@Override
	public long length () {
		return archiveEntry.getSize();
	}

	@Override
	public long lastModified () {
		return archiveEntry.getTime();
	}
}
