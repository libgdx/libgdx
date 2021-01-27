
package com.badlogic.gdx.utils;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.utils.Json.Serializable;

/** @author Desu */
public abstract class ExtendableEnum {
	private String name;

	public ExtendableEnum (Class<? extends ExtendableEnum> clazz, String name) {
		this.name = name;
		addValue(clazz, this);
	}

	public String getName () {
		return name;
	}

	private static Map<Class<? extends ExtendableEnum>, Map<String, ExtendableEnum>> values = new HashMap<>();

	protected static void addValue (Class<? extends ExtendableEnum> clazz, ExtendableEnum value) {
		Map<String, ExtendableEnum> classMap = values.get(clazz);
		if (classMap == null) {
			classMap = new HashMap<>();
			values.put(clazz, classMap);
		}

		classMap.put(value.name, value);
	}

	public static ExtendableEnum getValue (Class<? extends ExtendableEnum> clazz, String name) {
		if (name == null) throw new NullPointerException("Name is null");

		Map<String, ExtendableEnum> classMap = values.get(clazz);
		if (classMap != null) {
			ExtendableEnum value = classMap.get(name);
			if (value != null) return value;
		}

		throw new IllegalArgumentException("No enum constant " + clazz.getCanonicalName() + "." + name);
	}
}
