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
package com.badlogic.gdx.utils;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonWriter.OutputType;

/** Lightweight UBJSON parser.<br>
 * <br>
 * The default behavior is to parse the JSON into a DOM containing {@link JsonValue} objects. Extend this class and override
 * methods to perform event driven parsing. When this is done, the parse methods will return null.
 * @author Xoppa */
public class UBJsonReader implements BaseJsonReader {
	@Override
	public JsonValue parse (InputStream input) {
		try {
			return parse(new DataInputStream(input));
		} catch (IOException ex) {
			throw new SerializationException(ex);
		}
	}

	@Override
	public JsonValue parse (FileHandle file) {
		try {
			return parse(file.read());
		} catch (Exception ex) {
			throw new SerializationException("Error parsing file: " + file, ex);
		}
	}
	
	public JsonValue parse(final DataInputStream din) throws IOException {
		return parse(din, din.readByte());
	}
	
	protected JsonValue parse(final DataInputStream din, final byte type) throws IOException {
		if (type == '[')
			return parseArray(din);
		else if (type == '{')
			return parseObject(din);
		else if (type == 'Z')
			return new JsonValue(JsonValue.ValueType.nullValue);
		else if (type == 'T')
			return new JsonValue(true);
		else if (type == 'F')
			return new JsonValue(false);
		else if (type == 'B')
			return new JsonValue((long)readUChar(din));
		else if (type == 'i')
			return new JsonValue((long)din.readShort());
		else if (type == 'I')
			return new JsonValue((long)din.readInt());
		else if (type == 'L')
			return new JsonValue(din.readLong());
		else if (type == 'd')
			return new JsonValue(din.readFloat());
		else if (type == 'D')
			return new JsonValue(din.readDouble());
		else if (type == 's' || type == 'S')
			return new JsonValue(parseString(din, type));
		else if (type == 'a' || type == 'A')
			return parseData(din, type);
		else
			throw new GdxRuntimeException("Unrecognized data type");
	}
	
	protected JsonValue parseArray(final DataInputStream din) throws IOException {
		JsonValue result = new JsonValue(JsonValue.ValueType.array);
		byte type = din.readByte();
		JsonValue prev = null;
		while (din.available() > 0 && type != ']') {
			final JsonValue val = parse(din, type);
			if (prev != null) {
				prev.next = val;
				result.size++;
			} else {
				result.child = val;
				result.size = 1;
			}
			prev = val;
			type = din.readByte();
		}
		return result;
	}
	
	protected JsonValue parseObject(final DataInputStream din) throws IOException {
		JsonValue result = new JsonValue(JsonValue.ValueType.object);
		byte type = din.readByte();
		JsonValue prev = null;
		while (din.available() > 0 && type != '}') {
			if (type != 's' && type != 'S')
				throw new GdxRuntimeException("Only string key are currently supported");
			final String key = parseString(din, type);
			final JsonValue child = parse(din);
			child.setName(key);
			if (prev != null) {
				prev.next = child;
				result.size++;
			} else {
				result.child = child;
				result.size = 1;
			}
			prev = child;
			type = din.readByte();
		}
		return result;
	}
	
	protected JsonValue parseData(final DataInputStream din, final byte blockType) throws IOException {
		// FIXME: h/H is currently not following the specs because it lacks strong typed, fixed sized containers, 
		// see: https://github.com/thebuzzmedia/universal-binary-json/issues/27
		final byte dataType = din.readByte();
		final long size = blockType == 'A' ? readUInt(din) : (long)readUChar(din);
		final JsonValue result = new JsonValue(JsonValue.ValueType.array);
		JsonValue prev = null;
		for (long i = 0; i < size; i++) {
			final JsonValue val = parse(din, dataType);
			if (prev != null) {
				prev.next = val;
				result.size++;
			} else {
				result.child = val;
				result.size = 1;
			}
			prev = val;
		}
		return result;
	}
	
	protected String parseString(final DataInputStream din, final byte type) throws IOException {
		return readString(din, (type == 's') ? (long)readUChar(din) : readUInt(din));
	}
	
	protected short readUChar(final DataInputStream din) throws IOException {
		return (short)((short)din.readByte() & 0xFF);
	}
	
	protected long readUInt(final DataInputStream din) throws IOException {
		return ((long)din.readInt() & 0xFFFFFFFF);
	}
	
	protected String readString(final DataInputStream din, final long size) throws IOException {
		final byte data[] = new byte[(int)size];
		din.readFully(data);
		return new String(data, "UTF-8");
	}
}
