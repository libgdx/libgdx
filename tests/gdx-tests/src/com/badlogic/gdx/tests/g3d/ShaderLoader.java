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

package com.badlogic.gdx.tests.g3d;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Scanner;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;

public class ShaderLoader {
	public FileHandle root;

	public ObjectMap<String, ObjectMap<String, String>> snippets = new ObjectMap<String, ObjectMap<String, String>>();
	private Array<String> includes = new Array<String>();

	public ShaderLoader (FileHandle root) {
		this.root = root;
	}

	public ShaderProgram load (String vertex, String fragment) {
		StringBuilder out = new StringBuilder();
		load(out, vertex);
		vertex = out.toString();
		includes.clear();
		out.setLength(0);
		load(out, fragment);
		fragment = out.toString();
		includes.clear();
		return new ShaderProgram(vertex, fragment);
	}

	public String load (final String name) {
		StringBuilder out = new StringBuilder();
		load(out, name);
		includes.clear();
		return out.toString();
	}

	protected void load (final StringBuilder out, final String name) {
		final int idx = name.lastIndexOf(':');
		final String fileName = idx < 0 ? name : name.substring(0, idx);
		final String snipName = idx < 0 || (idx >= name.length() - 1) ? "" : name.substring(idx + 1);
		ObjectMap<String, String> snips = snippets.get(fileName, null);
		if (snips == null) {
			snips = parse(root.child(fileName));
			snippets.put(fileName, snips);
		}
		String result = snips.get(snipName, null);
		if (result == null) throw new GdxRuntimeException("No snippet [" + snipName + "] in file " + root.child(fileName).path());
		parse(out, fileName, result);
	}

	protected void parse (final StringBuilder out, final String currentFile, final String code) {
		String[] lines = code.split("\n");
		int idx, jdx;
		for (final String line : lines) {
			if (((idx = line.indexOf("#include")) == 0) && ((idx = line.indexOf("\"", idx)) > 0)
				&& ((jdx = line.indexOf("\"", ++idx)) > idx)) {
				String name = line.substring(idx, jdx);
				if (name.length() > 0) {
					if (name.charAt(0) == ':') name = currentFile + name;
					if (!includes.contains(name, false)) {
						includes.add(name);
						load(out, name);
					}
				}
			} else
				out.append(line.trim()).append("\r\n");
		}
	}

	final static StringBuilder stringBuilder = new StringBuilder();

	protected ObjectMap<String, String> parse (final FileHandle file) {
		ObjectMap<String, String> result = new ObjectMap<String, String>();
		BufferedReader reader = file.reader(1024);
		String line;
		String snipName = "";
		stringBuilder.setLength(0);
		int idx;
		try {
			while ((line = reader.readLine()) != null) {
				if (line.length() > 3 && line.charAt(0) == '[' && (idx = line.indexOf(']')) > 1) {
					if (snipName.length() > 0 || stringBuilder.length() > 0) result.put(snipName, stringBuilder.toString());
					stringBuilder.setLength(0);
					snipName = line.substring(1, idx);
				} else
					stringBuilder.append(line.trim()).append("\r\n");
			}
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
		if (snipName.length() > 0 || stringBuilder.length() > 0) result.put(snipName, stringBuilder.toString());
		return result;
	}

	@Override
	public String toString () {
		stringBuilder.setLength(0);
		for (final ObjectMap.Entry<String, ObjectMap<String, String>> entry : snippets.entries()) {
			stringBuilder.append(entry.key).append(": {");
			for (final String snipname : entry.value.keys())
				stringBuilder.append(snipname).append(", ");
			stringBuilder.append("}\n");
		}
		return stringBuilder.toString();
	}
}
