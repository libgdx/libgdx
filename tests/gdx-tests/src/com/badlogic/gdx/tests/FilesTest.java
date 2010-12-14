/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.tests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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

		try {
			testClasspath();
			testInternal();
			testExternal();
			testAbsolute();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void testClasspath () throws IOException {
		FileHandle handle = Gdx.files.classpath("com/badlogic/gdx/utils/arial-15.png");
		if (!handle.exists()) fail();
		if (handle.isDirectory()) fail();
		try {
			handle.delete();
			fail();
		} catch (Exception expected) {
		}
		try {
			handle.list();
			fail();
		} catch (Exception expected) {
		}
		try {
			handle.read().close();
			fail();
		} catch (Exception ignored) {
		}
		FileHandle dir = Gdx.files.classpath("com/badlogic/gdx/utils");
		if (dir.isDirectory()) fail();
		FileHandle child = dir.child("arial-15.fnt");
		if (!child.name().equals("arial-15.fnt")) fail();
		if (!child.nameWithoutExtension().equals("arial-15")) fail();
		if (!child.extension().equals("fnt")) fail();
		InputStream input = handle.read();
		byte[] bytes = new byte[70000];
		if (input.read(bytes) != handle.length()) fail();
		input.close();
	}

	private void testInternal () throws IOException {
		FileHandle handle = Gdx.files.internal("data/badlogic.jpg");
		if (!handle.exists()) fail();
		if (handle.isDirectory()) fail();
		try {
			handle.delete();
			fail();
		} catch (Exception expected) {
		}
		if (handle.list().length != 0) fail();
		if (!handle.parent().exists()) fail();
		try {
			handle.read().close();
			fail();
		} catch (Exception ignored) {
		}
		FileHandle dir = Gdx.files.internal("data");
		if (!dir.path().equals("data")) fail();
		if (!dir.exists()) fail();
		if (!dir.isDirectory()) fail();
		if (dir.list().length == 0) fail();
		FileHandle child = dir.child("badlogic.jpg");
		if (!child.name().equals("badlogic.jpg")) fail();
		if (!child.nameWithoutExtension().equals("badlogic")) fail();
		if (!child.extension().equals("jpg")) fail();
		if (!child.parent().exists()) fail();
		FileHandle copy = Gdx.files.external("badlogic.jpg-copy");
		copy.delete();
		if (copy.exists()) fail();
		handle.copyTo(copy);
		if (!copy.exists()) fail();
		if (copy.length() != 68465) fail();
		copy.delete();
		if (copy.exists()) fail();
		InputStream input = handle.read();
		byte[] bytes = new byte[70000];
		if (input.read(bytes) != 68465) fail();
		input.close();
	}

	private void testExternal () throws IOException {
		String path = "meow";
		FileHandle handle = Gdx.files.external(path);
		handle.delete();
		if (handle.exists()) fail();
		if (handle.isDirectory()) fail();
		if (handle.delete()) fail();
		if (handle.list().length != 0) fail();
		if (handle.child("meow").exists()) fail();
		if (!handle.parent().exists()) fail();
		try {
			handle.read().close();
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
		OutputStream output = handle.write(false);
		output.write("moo".getBytes());
		output.close();
		if (!handle.exists()) fail();
		if (handle.length() != 3) fail();
		FileHandle copy = Gdx.files.external(path + "-copy");
		copy.delete();
		if (copy.exists()) fail();
		handle.copyTo(copy);
		if (!copy.exists()) fail();
		if (copy.length() != 3) fail();
		FileHandle move = Gdx.files.external(path + "-move");
		move.delete();
		if (move.exists()) fail();
		copy.moveTo(move);
		if (!move.exists()) fail();
		if (move.length() != 3) fail();
		move.deleteDirectory();
		if (move.exists()) fail();
		InputStream input = handle.read();
		byte[] bytes = new byte[6];
		if (input.read(bytes) != 3) fail();
		input.close();
		if (!new String(bytes, 0, 3).equals("moo")) fail();
		output = handle.write(true);
		output.write("cow".getBytes());
		output.close();
		if (handle.length() != 6) fail();
		input = handle.read();
		if (input.read(bytes) != 6) fail();
		input.close();
		if (!new String(bytes, 0, 6).equals("moocow")) fail();
		if (handle.isDirectory()) fail();
		if (handle.list().length != 0) fail();
		if (!handle.name().equals("meow")) fail();
		if (!handle.nameWithoutExtension().equals("meow")) fail();
		if (!handle.extension().equals("")) fail();
		handle.deleteDirectory();
		if (handle.exists()) fail();
		if (handle.isDirectory()) fail();
		handle.delete();
		handle.deleteDirectory();
	}

	private void testAbsolute () throws IOException {
		String path = new File(Gdx.files.getExternalStoragePath(), "meow").getAbsolutePath();
		FileHandle handle = Gdx.files.absolute(path);
		handle.delete();
		if (handle.exists()) fail();
		if (handle.isDirectory()) fail();
		if (handle.delete()) fail();
		if (handle.list().length != 0) fail();
		if (handle.child("meow").exists()) fail();
		if (!handle.parent().exists()) fail();
		try {
			handle.read().close();
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
		OutputStream output = handle.write(false);
		output.write("moo".getBytes());
		output.close();
		if (!handle.exists()) fail();
		if (handle.length() != 3) fail();
		FileHandle copy = Gdx.files.absolute(path + "-copy");
		copy.delete();
		if (copy.exists()) fail();
		handle.copyTo(copy);
		if (!copy.exists()) fail();
		if (copy.length() != 3) fail();
		FileHandle move = Gdx.files.absolute(path + "-move");
		move.delete();
		if (move.exists()) fail();
		copy.moveTo(move);
		if (!move.exists()) fail();
		if (move.length() != 3) fail();
		move.deleteDirectory();
		if (move.exists()) fail();
		InputStream input = handle.read();
		byte[] bytes = new byte[6];
		if (input.read(bytes) != 3) fail();
		input.close();
		if (!new String(bytes, 0, 3).equals("moo")) fail();
		output = handle.write(true);
		output.write("cow".getBytes());
		output.close();
		if (handle.length() != 6) fail();
		input = handle.read();
		if (input.read(bytes) != 6) fail();
		input.close();
		if (!new String(bytes, 0, 6).equals("moocow")) fail();
		if (handle.isDirectory()) fail();
		if (handle.list().length != 0) fail();
		if (!handle.name().equals("meow")) fail();
		if (!handle.nameWithoutExtension().equals("meow")) fail();
		if (!handle.extension().equals("")) fail();
		handle.deleteDirectory();
		if (handle.exists()) fail();
		if (handle.isDirectory()) fail();
		handle.delete();
		handle.deleteDirectory();
	}

	private void fail () {
		throw new RuntimeException();
	}

	@Override public void render () {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		batch.begin();
		font.drawMultiLine(batch, message, 20, Gdx.graphics.getHeight() - 20);
		batch.end();
	}

	@Override public boolean needsGL20 () {
		return false;
	}

}
