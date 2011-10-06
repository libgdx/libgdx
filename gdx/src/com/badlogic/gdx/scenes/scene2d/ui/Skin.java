/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.scenes.scene2d.ui;

import java.io.IOException;
import java.io.Writer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.Serializer;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.SerializationException;

/** @author Nathan Sweet */
public class Skin implements Disposable {
	static public class SkinData {
		public ObjectMap<Class, ObjectMap<String, Object>> resources = new ObjectMap();
		public transient Texture texture;
	}

	ObjectMap<Class, ObjectMap<String, Object>> styles = new ObjectMap();
	final SkinData data;

	public Skin () {
		data = new SkinData();
	}

	public Skin (FileHandle skinFile, FileHandle textureFile) {
		data = new SkinData();
		data.texture = new Texture(textureFile);
		try {
			getJsonLoader(skinFile).fromJson(Skin.class, skinFile);
		} catch (SerializationException ex) {
			throw new SerializationException("Error reading file: " + skinFile, ex);
		}
	}

	public Skin (FileHandle skinFile, SkinData data) {
		this.data = data;
		data.texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		try {
			getJsonLoader(skinFile).fromJson(Skin.class, skinFile);
		} catch (SerializationException ex) {
			throw new SerializationException("Error reading file: " + skinFile, ex);
		}
	}

	public <T> void addResource (String name, T resource) {
		if (resource == null) throw new IllegalArgumentException("resource cannot be null.");
		ObjectMap<String, Object> typeResources = data.resources.get(resource.getClass());
		if (typeResources == null) {
			typeResources = new ObjectMap();
			data.resources.put(resource.getClass(), typeResources);
		}
		typeResources.put(name, resource);
	}

	public <T> T getResource (String name, Class<T> type) {
		ObjectMap<String, Object> typeResources = data.resources.get(type);
		if (typeResources == null) throw new GdxRuntimeException("No resources registered with type: " + type.getName());
		Object resource = typeResources.get(name);
		if (resource == null) throw new GdxRuntimeException("No " + type.getName() + " resource registered with name: " + name);
		return (T)resource;
	}

	public <T> void addStyle (String name, T style) {
		if (style == null) throw new IllegalArgumentException("style cannot be null.");
		ObjectMap<String, Object> typeStyles = styles.get(style.getClass());
		if (typeStyles == null) {
			typeStyles = new ObjectMap();
			styles.put(style.getClass(), typeStyles);
		}
		typeStyles.put(name, style);
	}

	public <T> T getStyle (Class<T> type) {
		return getStyle("default", type);
	}

	public <T> T getStyle (String name, Class<T> type) {
		ObjectMap<String, Object> typeStyles = styles.get(type);
		if (typeStyles == null) throw new GdxRuntimeException("No styles registered with type: " + type.getName());
		Object style = typeStyles.get(name);
		if (style == null) throw new GdxRuntimeException("No " + type.getName() + " style registered with name: " + name);
		return (T)style;
	}

	/** Disposes the {@link Texture} and all {@link Disposable} resources of this Skin. */
	@Override
	public void dispose () {
		data.texture.dispose();
		for (Object object : data.resources.values())
			if (object instanceof Disposable) ((Disposable)object).dispose();
	}

	public void setTexture (Texture texture) {
		data.texture = texture;
	}

	/** @return the {@link Texture} containing all {@link NinePatch} and {@link TextureRegion} pixels of this Skin. */
	public Texture getTexture () {
		return data.texture;
	}

	public void save (FileHandle skinFile) {
		String text = getJsonLoader(null).prettyPrint(this, true);
		Writer writer = skinFile.writer(false);
		try {
			writer.write(text);
			writer.close();
		} catch (IOException ex) {
			throw new GdxRuntimeException(ex);
		}
	}

	protected Json getJsonLoader (final FileHandle skinFile) {
		final Skin skin = this;

		Json json = new Json();
		json.setTypeName(null);
		json.setUsePrototypes(false);

		class AliasSerializer implements Serializer {
			final ObjectMap<String, ?> map;

			public AliasSerializer (ObjectMap<String, ?> map) {
				this.map = map;
			}

			public void write (Json json, Object object, Class valueType) {
				for (Entry<String, ?> entry : map.entries()) {
					if (entry.value.equals(object)) {
						json.writeValue(entry.key);
						return;
					}
				}
				throw new SerializationException(object.getClass().getSimpleName() + " not found: " + object);
			}

			public Object read (Json json, Object jsonData, Class type) {
				String name = (String)jsonData;
				Object object = map.get(name);
				if (object == null) {
					ObjectMap<String, Object> regions = data.resources.get(TextureRegion.class);
					if (regions != null) {
						object = regions.get(name);
						if (object != null) object = new NinePatch((TextureRegion)object);	
					}
					if (object == null)
						throw new SerializationException("Skin has a " + type.getSimpleName()
							+ " that could not be found in the resources: " + jsonData);
				}
				return object;
			}
		}

		json.setSerializer(Skin.class, new Serializer<Skin>() {
			public void write (Json json, Skin skin, Class valueType) {
				json.writeObjectStart();
				json.writeValue("resources", skin.data.resources);
				for (Entry<Class, ObjectMap<String, Object>> entry : data.resources.entries())
					json.setSerializer(entry.key, new AliasSerializer(entry.value));
				json.writeField(skin, "styles");
				json.writeObjectEnd();
			}

			public Skin read (Json json, Object jsonData, Class ignored) {
				ObjectMap map = (ObjectMap)jsonData;
				readTypeMap(json, (ObjectMap)map.get("resources"), true);
				for (Entry<Class, ObjectMap<String, Object>> entry : data.resources.entries())
					json.setSerializer(entry.key, new AliasSerializer(entry.value));
				readTypeMap(json, (ObjectMap)map.get("styles"), false);
				return skin;
			}

			private void readTypeMap (Json json, ObjectMap<String, ObjectMap> typeToValueMap, boolean isResource) {
				if (typeToValueMap == null)
					throw new SerializationException("Skin file is missing a \"" + (isResource ? "resources" : "styles")
						+ "\" section.");
				for (Entry<String, ObjectMap> typeEntry : typeToValueMap.entries()) {
					Class type;
					try {
						type = Class.forName(typeEntry.key);
					} catch (ClassNotFoundException ex) {
						throw new SerializationException(ex);
					}
					ObjectMap<String, ObjectMap> valueMap = (ObjectMap)typeEntry.value;
					for (Entry<String, ObjectMap> valueEntry : valueMap.entries()) {
						try {
							if (isResource)
								addResource(valueEntry.key, json.readValue(type, valueEntry.value));
							else
								addStyle(valueEntry.key, json.readValue(type, valueEntry.value));
						} catch (Exception ex) {
							throw new SerializationException("Error reading " + type.getSimpleName() + ": " + valueEntry.key, ex);
						}
					}
				}
			}
		});

		json.setSerializer(TextureRegion.class, new Serializer<TextureRegion>() {
			public void write (Json json, TextureRegion region, Class valueType) {
				json.writeObjectStart();
				json.writeValue("x", region.getRegionX());
				json.writeValue("y", region.getRegionY());
				json.writeValue("width", region.getRegionWidth());
				json.writeValue("height", region.getRegionHeight());
				json.writeObjectEnd();
			}

			public TextureRegion read (Json json, Object jsonData, Class type) {
				int x = json.readValue("x", int.class, jsonData);
				int y = json.readValue("y", int.class, jsonData);
				int width = json.readValue("width", int.class, jsonData);
				int height = json.readValue("height", int.class, jsonData);
				return new TextureRegion(skin.data.texture, x, y, width, height);
			}
		});

		json.setSerializer(BitmapFont.class, new Serializer<BitmapFont>() {
			public void write (Json json, BitmapFont font, Class valueType) {
				json.writeValue(font.getData().getFontFile().toString().replace('\\', '/'));
			}

			public BitmapFont read (Json json, Object jsonData, Class type) {
				String path = json.readValue(String.class, jsonData);
				FileHandle file = skinFile.parent().child(path);
				if (!file.exists()) file = Gdx.files.internal(path);
				return new BitmapFont(file, false);
			}
		});

		json.setSerializer(NinePatch.class, new Serializer<NinePatch>() {
			public void write (Json json, NinePatch ninePatch, Class valueType) {
				json.writeValue(ninePatch.getPatches());
			}

			public NinePatch read (Json json, Object jsonData, Class type) {
				return new NinePatch(json.readValue(TextureRegion[].class, jsonData));
			}
		});

		return json;
	}
}
