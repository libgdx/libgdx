package com.badlogic.gdx.graphics.g3d.loaders.collada;

import java.util.Arrays;

import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Xml.Element;

public class Source {
	String id;
	int components;
	int count;
	float[] data;	
	
	public Source (Element source) {
		this.id = source.getAttribute("id");
		parseComponents(source);
	}
	
	private void parseComponents(Element source) {
		Element floatArray = source.getChildByName("float_array");
		Element technique = source.getChildByName("technique_common");		
		if(floatArray == null) throw new GdxRuntimeException("no <float_array> element in source '" + id + "'");
		if(technique == null) throw new GdxRuntimeException("no <technique_common> element in source '" + id + "'");		
		Element accessor = technique.getChildByName("accessor");
		if(accessor == null) throw new GdxRuntimeException("no <accessor> element in source '" + id + "'");
		
		// read number of elements, number of components per element (e.g. 3 for x, y, z)
		data = new float[Integer.parseInt(floatArray.getAttribute("count"))];
		count = Integer.parseInt(accessor.getAttribute("count"));
		components = Integer.parseInt(accessor.getAttribute("stride"));
		
		// read elements into data[]
		String[] tokens = floatArray.getText().split("\\s+");
		for(int i = 0; i < tokens.length; i++) {
			data[i] = Float.parseFloat(tokens[i]);
		}						
	}
}
