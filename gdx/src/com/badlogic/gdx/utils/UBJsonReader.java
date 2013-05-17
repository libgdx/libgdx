package com.badlogic.gdx.utils;

import java.io.DataInputStream;
import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonWriter.OutputType;

public class UBJsonReader {
	public JsonValue parse(final FileHandle fileHandle) {
		JsonValue result = null;
		try {
			DataInputStream din = new DataInputStream(fileHandle.read());
			result = parse(din);
			din.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public JsonValue parse(final DataInputStream din) throws IOException {
		return parse(din, din.readByte());
	}
	
	private JsonValue parse(final DataInputStream din, final byte type) throws IOException {
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
		else if (type == 'I')
			return new JsonValue(din.readLong());
		else if (type == 'd')
			return new JsonValue(din.readFloat());
		else if (type == 'D')
			return new JsonValue(din.readDouble());
		else if (type == 's' || type == 'S')
			return new JsonValue(parseString(din, type));
		return null;
	}
	
	private JsonValue parseArray(final DataInputStream din) throws IOException {
		JsonValue result = new JsonValue(JsonValue.ValueType.array);
		byte type = din.readByte();
		while (din.available() > 0 && type != ']') {
			result.addChild(parse(din, type));
			type = din.readByte();
		}
		return result;
	}
	
	private JsonValue parseObject(final DataInputStream din) throws IOException {
		JsonValue result = new JsonValue(JsonValue.ValueType.object);
		byte type = din.readByte();
		while (din.available() > 0 && type != '}') {
			if (type != 's' && type != 'S')
				throw new GdxRuntimeException("Only string key are currently supported");
			final String key = parseString(din, type);
			final JsonValue child = parse(din);
			child.setName(key);
			result.addChild(child);
			type = din.readByte();
		}
		return result;
	}
	
	private String parseString(final DataInputStream din, final byte type) throws IOException {
		return readString(din, (type == 's') ? (long)readUChar(din) : readUInt(din));
	}
	
	private short readUChar(final DataInputStream din) throws IOException {
		return (short)((short)din.readByte() & 0xFF);
	}
	
	private long readUInt(final DataInputStream din) throws IOException {
		return ((long)din.readInt() & 0xFFFFFFFF);
	}
	
	private String readString(final DataInputStream din, final long size) throws IOException {
		final byte data[] = new byte[(int)size];
		din.readFully(data);
		return new String(data, "UTF-8");
	}
}
