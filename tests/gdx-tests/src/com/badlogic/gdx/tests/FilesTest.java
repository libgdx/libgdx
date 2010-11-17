
package com.badlogic.gdx.tests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.BitmapFont;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class FilesTest extends GdxTest {
	String message = "";
	boolean success;
	BitmapFont font;
	SpriteBatch batch;

	@Override public void create () {
		font = new BitmapFont();
		batch = new SpriteBatch();

		if (Gdx.files.isExternalStorageAvailable()) {
			message += "External storage available\n";
			message += "External storage path: " + Gdx.files.getExternalStoragePath() + "\n";

			try {
				InputStream in = Gdx.files.internal("data/cube.obj").read();
				try {
					in.close();
				} catch (IOException e) {
				}
				message += "Open internal success\n";
			} catch (Throwable e) {
				message += "Couldn't open internal data/cube.obj\n" + e.getMessage() + "\n";
			}

			BufferedWriter out = null;
			try {
				out = new BufferedWriter(new OutputStreamWriter(Gdx.files.external("test.txt").write(false)));
				out.write("test");
				message += "Write external success\n";
			} catch (GdxRuntimeException ex) {
				message += "Couldn't open externalstorage/test.txt\n";
			} catch (IOException e) {
				message += "Couldn't write externalstorage/test.txt\n";
			} finally {
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
					}
				}
			}

			try {
				InputStream in = Gdx.files.external("test.txt").read();
				try {
					in.close();
				} catch (IOException e) {
				}
				message += "Open external success\n";
			} catch (Throwable e) {
				message += "Couldn't open internal externalstorage/test.txt\n" + e.getMessage() + "\n";
			}

			BufferedReader in = null;
			try {
				in = new BufferedReader(new InputStreamReader(Gdx.files.external("test.txt").read()));
				if (!in.readLine().equals("test"))
					message += "Read result wrong\n";
				else
					message += "Read external success\n";
			} catch (GdxRuntimeException ex) {
				message += "Couldn't open externalstorage/test.txt\n";
			} catch (IOException e) {
				message += "Couldn't read externalstorage/test.txt\n";
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
					}
				}
			}

			if (!Gdx.files.external("test.txt").delete()) message += "Couldn't delete externalstorage/test.txt";
		} else {
			message += "External storage not available";
		}

		// Can only really test external with this, since internal you can't create files, and absolute on Android you don't have
		// permissions to create files.
		testWriteRead("meow", FileType.External);
	}

	private void testWriteRead (String path, FileType type) {
		FileHandle handle = Gdx.files.getFileHandle(path, type);
		if (handle.exists()) fail();
		if (handle.isDirectory()) fail();
		if (handle.delete()) fail();
		if (handle.list().length != 0) fail();
		if (handle.child("meow").exists()) fail();
		if (!handle.parent().exists()) fail();
		try {
			handle.read();
			fail();
		} catch (Exception ignored) {
		}
		handle.mkdirs();
		if (!handle.exists()) fail();
		if (!handle.isDirectory()) fail();
		if (handle.list().length != 0) fail();
		handle.child("meow").mkdirs();
		if (handle.list().length != 1) fail();
		FileHandle child = handle.list()[0];
		if (!child.name().equals("meow")) fail();
		if (!child.parent().exists()) fail();
		if (!handle.deleteDirectory()) fail();
		if (handle.exists()) fail();
	}

	private void fail () {
		throw new RuntimeException();
	}

	@Override public void render () {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		batch.begin();
		font.drawMultiLineText(batch, message, 20, Gdx.graphics.getHeight() - 20, Color.WHITE);
		batch.end();
	}

	@Override public boolean needsGL20 () {
		return false;
	}

}
