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
	
	@Override
	public void create() {
		font = new BitmapFont();
		batch = new SpriteBatch();
		
		if(Gdx.files.isExternalStorageAvailable()) {
			message += "external storage available\n";
			message += "external storage path: " + Gdx.files.getExternalStoragePath() + "\n";
			
			try {
				InputStream in = Gdx.files.readFile( "data/cube.obj", FileType.Internal);
				try { in.close(); } catch (IOException e) {	}
				message += "open internal success\n";
			} catch(Throwable e) {
				message += "Couldn't open internal data/cube.obj\n" + e.getMessage() + "\n";
			}
			
			BufferedWriter out = null;
			try {				
				out = new BufferedWriter(new OutputStreamWriter(Gdx.files.writeFile("test.txt", FileType.External)));
				out.write("test");		
				message += "write external success\n";
			} catch(GdxRuntimeException ex) {
				message +="Couldn't open externalstorage/test.txt\n";
			} catch (IOException e) {
				message +="Couldn't write externalstorage/test.txt\n";
			} finally {
				if(out != null) {
					try { out.close(); } catch (IOException e) {	}
				}
			}
			
			try {
				InputStream in = Gdx.files.readFile( "test.txt", FileType.External);
				try { in.close(); } catch (IOException e) {	}
				message +="Open external success\n";
			} catch(Throwable e) {
				message += "Couldn't open internal externalstorage/test.txt\n" + e.getMessage() + "\n";
			}
			
			BufferedReader in = null;			
			try {				
				in = new BufferedReader(new InputStreamReader(Gdx.files.readFile("test.txt", FileType.External)));
				if(!in.readLine().equals("test"))
					message += "Read result wrong\n";
				else
					message +="Read external success\n";
			} catch(GdxRuntimeException ex) {
				message +="Couldn't open externalstorage/test.txt\n";
			} catch (IOException e) {
				message +="Couldn't read externalstorage/test.txt\n";
			} finally {
				if(out != null) {
					try { out.close(); } catch (IOException e) {	}
				}
			}
			
			if(!new File(Gdx.files.getExternalStoragePath() + "test.txt").delete())
				message += "couldn't delete externalstorage/test.txt";
		} else {
			message += "External storage not available";
		}
	}
	
	@Override
	public void render() {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		batch.begin();
		font.drawMultiLineText(batch, message, 20, Gdx.graphics.getHeight() - 20, Color.WHITE );
		batch.end();
	}
	
	@Override
	public boolean needsGL20() {
		return false;
	}

}
