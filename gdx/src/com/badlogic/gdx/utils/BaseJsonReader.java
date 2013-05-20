package com.badlogic.gdx.utils;

import java.io.InputStream;

import com.badlogic.gdx.files.FileHandle;

public interface BaseJsonReader {
	JsonValue parse (InputStream input);
	JsonValue parse (FileHandle file);
}
