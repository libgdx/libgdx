package com.badlogic.gdx.graphics.g3d.newparticles;

import java.io.BufferedReader;
import java.io.IOException;

public class Utils {
	public static String readString (BufferedReader reader, String name) throws IOException {
		String line = reader.readLine();
		if (line == null) throw new IOException("Missing value: " + name);
		return line.substring(line.indexOf(":") + 1).trim();
	}

	public static boolean readBoolean (BufferedReader reader, String name) throws IOException {
		return Boolean.parseBoolean(readString(reader, name));
	}

	public static int readInt (BufferedReader reader, String name) throws IOException {
		return Integer.parseInt(readString(reader, name));
	}

	public static float readFloat (BufferedReader reader, String name) throws IOException {
		return Float.parseFloat(readString(reader, name));
	}
}
