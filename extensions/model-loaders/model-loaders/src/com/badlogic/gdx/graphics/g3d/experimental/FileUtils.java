package com.badlogic.gdx.graphics.g3d.experimental;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Loader for text based shader files.
 *
 */
public class FileUtils {

	private final static int MAX_BUFFER_SIZE = 8192;
	
	private static String read(InputStream in) {
		StringBuilder stringBuilder = new StringBuilder(MAX_BUFFER_SIZE);

		try {
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(in, "UTF-8"), MAX_BUFFER_SIZE);
			char[] buffer = new char[MAX_BUFFER_SIZE];

			int read;
			while ((read = bufferedReader.read(buffer, 0, buffer.length)) > 0) {
				stringBuilder.append(buffer, 0, read);
			}
			return stringBuilder.toString();
		} catch (Exception e) {
			throw new RuntimeException(
					"failed to get string from input stream", e);
		} finally {
			try {
				in.close();
			} catch (Exception e) {

			}
		}
	}

	private static InputStream getClasspathInputStream(String path) {
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		InputStream in = classLoader.getResourceAsStream(path);
		if (in == null)
			throw new RuntimeException("couldnt find stream for " + path);
		return in;
	}


	/** Method for loading file named path
	 * @param path
	 * @return file content as string
	 */
	public static String getContent(String path) {
		return read(getClasspathInputStream(path));

	}
}