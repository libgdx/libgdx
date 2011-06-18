package com.badlogic.gdx.tests;

import java.io.BufferedInputStream;
import java.io.InputStream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.ObjectMap;

public class FastTextReadingTest extends GdxTest {

	@Override public boolean needsGL20 () {
		return false;
	}

	ObjectMap<String, String> words = new ObjectMap<String, String>(120000);
	
	public void create() {
		long start = 0;							
		
		words.clear();
		start = System.nanoTime();
		readWordStringBuilder();				
		Gdx.app.log("FastTexReadingTest", "StringBuilder loading took: " + (System.nanoTime() - start) / 1000000000.0f);
		Gdx.app.log("FastTexReadingTest", "words: " + words.size);
		
		words.clear();
		start = System.nanoTime();		
		readWordIntArrayISO();
		Gdx.app.log("FastTexReadingTest", "IntArrayISO loading took: " + (System.nanoTime() - start) / 1000000000.0f);
		Gdx.app.log("FastTexReadingTest", "words: " + words.size);
		
		words.clear();
		start = System.nanoTime();		
		readWordIntArrayISOMethod();
		Gdx.app.log("FastTexReadingTest", "IntArrayISOMethod loading took: " + (System.nanoTime() - start) / 1000000000.0f);
		Gdx.app.log("FastTexReadingTest", "words: " + words.size);
		
		words.clear();
		start = System.nanoTime();
		readWordStringBuilder();				
		Gdx.app.log("FastTexReadingTest", "StringBuilder loading took: " + (System.nanoTime() - start) / 1000000000.0f);
		Gdx.app.log("FastTexReadingTest", "words: " + words.size);
		
		Gdx.app.log("FastTexReadingTest", "contains boobies: " + words.containsKey("boobies"));
	}
	
	private void readWordIntArrayISO() {
		InputStream in = new BufferedInputStream(Gdx.files.internal("data/words.txt").read());
		try {			
			byte[] buffer = new byte[1024*20];
			int[] word = new int[100];
			int charIdx = 0;
			int readBytes = 0;
			
			while((readBytes =in.read(buffer)) != -1) {
				for(int i = 0; i < readBytes; i++) {
					byte c = (byte)buffer[i];
					if(c == '\n') {
						String wordStr = new String(word, 0, charIdx);
						words.put(wordStr, wordStr);
						charIdx = 0;
					} else {
						word[charIdx++] = c; 
					}
				}
			}
		} catch(Exception e) {			
		} finally {
			try { in.close(); } catch(Exception e) { };
		}
	}
	
	private void readWordStringBuilder() {
		InputStream in = new BufferedInputStream(Gdx.files.internal("data/words.txt").read());
		try {			
			byte[] buffer = new byte[1024*10];						
			int readBytes = 0;			
			StringBuilder builder = new StringBuilder();
			while((readBytes =in.read(buffer)) != -1) {
				for(int i = 0; i < readBytes; i++) {
					char c = (char)buffer[i];
					if(c == '\n') {
						String wordStr = builder.toString();
						words.put(wordStr, wordStr);	
						builder.setLength(0);
					} else {
						builder.append(c);
					}
				}
			}
		} catch(Exception e) {			
		} finally {
			try { in.close(); } catch(Exception e) { };
		}
	}
	
	private void readWordIntArrayISOMethod() {
		InputStream in = new BufferedInputStream(Gdx.files.internal("data/words.txt").read());
		try {			
			byte[] buffer = new byte[1024*20];
			int[] word = new int[100];
			int charIdx = 0;
			int readBytes = 0;
			
			while((readBytes =in.read(buffer)) != -1) {
				charIdx = readBuffer(buffer, readBytes, word, charIdx);
			}
		} catch(Exception e) {			
		} finally {
			try { in.close(); } catch(Exception e) { };
		}
	}
	
	private int readBuffer(final byte[] buffer, final int readBytes, final int[] word, int charIdx) {
		for(int i = 0; i < readBytes; i++) {
			byte c = (byte)buffer[i];
			if(c == '\n') {
				String wordStr = new String(word, 0, charIdx);
				words.put(wordStr, wordStr);
				charIdx = 0;
			} else {
				word[charIdx++] = c; 
			}
		}
		return charIdx;
	}
	
	public void render() {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
	}
}
