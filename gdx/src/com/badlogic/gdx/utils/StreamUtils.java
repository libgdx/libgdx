
package com.badlogic.gdx.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Net.HttpRequest;

/** Provides utility methods to copy streams */
public class StreamUtils {

	/** Copy the data from an {@link InputStream} to an {@link OutputStream}.
	 * @throws IOException */
	public static void copyStream (InputStream input, OutputStream output) throws IOException {
		copyStream(input, output, 8192);
	}

	/** Copy the data from an {@link InputStream} to an {@link OutputStream}.
	 * @throws IOException */
	public static void copyStream (InputStream input, OutputStream output, int bufferSize) throws IOException {
		byte[] buffer = new byte[bufferSize];
		int bytesRead;
		while ((bytesRead = input.read(buffer)) != -1) {
			output.write(buffer, 0, bytesRead);
		}
	}
}
