package com.badlogic.gdx.backends.gwt.utils;

public class MimeTypeIdentifier {

	public static String getMimeType (byte[] data) {
		if (isPng(data)) {
			return "image/png";
		} else if (isJpg(data)) {
			return "image/jpeg";
		}

		return "";
	}

	/** Based on table at: https://www.sparkhound.com/blog/detect-image-file-types-through-byte-arrays
	 * @param data
	 * @return */
	protected static boolean isPng(byte[] data) {
		if (data == null || data.length < 4) {
			return false;
		}
		return data[0] == (byte) 0x89 && data[1] == (byte) 0x50 && data[2] == (byte) 0x4e && data[3] == (byte) 0x47;
	}

	protected static boolean isJpg(byte[] data) {
		if (data == null || data.length < 3) {
			return false;
		}
		return data[0] == (byte) 0xff && data[1] == (byte) 0xd8 && data[2] == (byte) 0xff;
	}
}
