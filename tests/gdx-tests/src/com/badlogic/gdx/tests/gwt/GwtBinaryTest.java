package com.badlogic.gdx.tests.gwt;

import java.io.DataInputStream;
import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.tests.utils.GdxTest;

public class GwtBinaryTest extends GdxTest {

	@Override
	public void create () {
		DataInputStream in = new DataInputStream(Gdx.files.internal("data/arial.ttf").read());
		for(int i = 0; i < 10; i++) {
			try {
				System.out.println(in.read());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
