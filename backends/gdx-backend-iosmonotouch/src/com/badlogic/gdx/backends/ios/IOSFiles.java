package com.badlogic.gdx.backends.ios;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.files.FileHandle;

public class IOSFiles implements Files {
	@Override
	public FileHandle getFileHandle(String path, FileType type) {
		return null;
	}

	@Override
	public FileHandle classpath(String path) {
		return null;
	}

	@Override
	public FileHandle internal(String path) {
		return null;
	}

	@Override
	public FileHandle external(String path) {
		return null;
	}

	@Override
	public FileHandle absolute(String path) {
		return null;
	}

	@Override
	public FileHandle local(String path) {
		return null;
	}

	@Override
	public String getExternalStoragePath() {
		return null;
	}

	@Override
	public boolean isExternalStorageAvailable() {
		return false;
	}

	@Override
	public String getLocalStoragePath() {
		return null;
	}

	@Override
	public boolean isLocalStorageAvailable() {
		return false;
	}
}
