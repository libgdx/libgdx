package com.badlogic.gdx.graphics.g3d.loaders.collada;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.badlogic.gdx.utils.Xml;
import com.badlogic.gdx.utils.Xml.Element;

public class ColladaLoader {
	public static void main(String[] argv) throws FileNotFoundException, IOException {
		Xml xml = new Xml();
		Element element = xml.parse(new FileInputStream("data/goblin.dae"));
		System.out.println(element);
	}
}
