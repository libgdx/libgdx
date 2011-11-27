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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.Serializer;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.SerializationException;

/** A skin holds styles for widgets and the resources (texture regions, ninepatches, bitmap fonts, etc) for those styles. A skin
 * has a single texture that the resources may reference. This reduces the number of texture binds necessary for rendering many
 * different widgets.
 * <p>
 * The resources and styles for a skin are usually defined using JSON (or a format that is {@link OutputType#minimal JSON-like}),
 * which is formatted in this way:
 * 
 * <pre>
 * {
 * 	resources: {
 * 		className: {
 * 			name: value,
 * 			...
 * 		},
 * 		...
 * 	},
 * 	styles: {
 * 		className: {
 * 			name: value,
 * 			...
 * 		},
 * 		...
 * 	}
 * }
 * </pre>
 * 
 * There are two sections, one named "resources" and the other "styles". Each section has a class name, which has a number of
 * names and values. The name is the name of the resource or style for that class, and the value is the serialized resource or
 * style. Here is a real example:
 * 
 * <pre>
 * {
 * 	resources: {
 * 		com.badlogic.gdx.graphics.g2d.TextureRegion: {
 * 			check-on: { x: 13, y: 77, width: 14, height: 14 },
 * 			check-off: { x: 2, y: 97, width: 14, height: 14 }
 * 		},
 * 		com.badlogic.gdx.graphics.Color: {
 * 			white: { r: 1, g: 1, b: 1, a: 1 }
 * 		},
 * 		com.badlogic.gdx.graphics.g2d.BitmapFont: {
 * 			default-font: { file: default.fnt }
 * 		}
 * 	},
 * 	styles: {
 * 		com.badlogic.gdx.scenes.scene2d.ui.CheckBox$CheckBoxStyle: {
 * 			default: {
 * 				checkboxOn: check-on, checkboxOff: check-off,
 * 				font: default-font, fontColor: white
 * 			}
 * 		}
 * 	}
 * }
 * </pre>
 * 
 * Here some named resource are defined: texture regions, a color, and a bitmap font. Also, a {@link CheckBoxStyle} is defined
 * named "default" and it references the resources by name.
 * <p>
 * Styles and resources are retrieved from the skin using the type and name:
 * 
 * <pre>
 * Color highlight = skin.getResource(&quot;highlight&quot;, Color.class);
 * TextureRegion someRegion = skin.getResource(&quot;logo&quot;, TextureRegion.class);
 * CheckBoxStyle checkBoxStyle = skin.getStyle(&quot;bigCheckbox&quot;, CheckBoxStyle.class);
 * CheckBox checkBox = new CheckBox(&quot;Check me!&quot;, checkBoxStyle);
 * </pre>
 * 
 * For convenience, most widget constructors will accept a skin and look up the necessary style using the name "default".
 * <p>
 * The JSON required for a style is simply a JSON object with field names that match the Java field names. The JSON object's field
 * values can be an object to define a new Java object, or a string to reference a named resource of the expected type. Eg,
 * {@link LabelStyle} has two fields, font and fontColor, so the JSON could look like:
 * 
 * <pre>
 * someLabel: { font: small, fontColor: { r: 1, g: 0, b: 0, a: 1 } }
 * </pre>
 * 
 * When this is parsed, the "font" field is a BitmapFont and the string "small" is found, so a BitmapFont resource named "small"
 * is used. The "fontColor" field is a Color and a JSON object is found, so a new Color is created and the JSON object is used to
 * populate its fields.
 * <p>
 * The order resources are defined is important. Resources may reference previously defined resources. This is how a BitmapFont
 * can find a TextureRegion resource (see BitmapFont section below).
 * <p>
 * The following gives examples for the types of resources that are supported by default:
 * <p>
 * {@link Color}:
 * 
 * <pre>
 * { r: 1, g: 1, b: 1, a: 1 }
 * </pre>
 * 
 * {@link TextureRegion}:
 * 
 * <pre>
 * { x: 13, y: 77, width: 14, height: 14 }
 * </pre>
 * 
 * {@link NinePatch}:
 * 
 * <pre>
 * [
 * 	{ x: 2, y: 55, width: 5, height: 5 },
 * 	{ x: 7, y: 55, width: 2, height: 5 },
 * 	{ x: 9, y: 55, width: 5, height: 5 },
 * 	{ x: 2, y: 60, width: 5, height: 11 },
 * 	{ x: 7, y: 60, width: 2, height: 11 },
 * 	{ x: 9, y: 60, width: 5, height: 11 },
 * 	{ x: 2, y: 71, width: 5, height: 4 },
 * 	{ x: 7, y: 71, width: 2, height: 4 },
 * 	{ x: 9, y: 71, width: 5, height: 4 }
 * ]
 * </pre>
 * 
 * {@link NinePatch} can also be specified as a single region, which is set as the center of the ninepatch:
 * 
 * <pre>
 * [ { width: 20, height: 20, x: 6, y: 2 } ]
 * </pre>
 * 
 * This notation is useful to use a single region as a ninepatch. Eg, when creating a button made up of a single image for the
 * {@link ButtonStyle#up} field, which is a ninepatch.
 * <p>
 * {@link BitmapFont}:
 * 
 * <pre>
 * { file: default.fnt }
 * </pre>
 * 
 * First the skin tries to find the font file in the directory containing the skin file. If not found there, it uses the specified
 * path as an {@link FileType#Internal} path. The bitmap font will use a texture region with the same name as the font file
 * without the file extension. If no texture region with that name is defined in the skin (note the order resources are defined is
 * important), it will look in the same directory as the font file for a PNG with the same name as the font file but with a "png"
 * file extension.
 * <p>
 * TintedNinePatch provides a mechanism for tinting an existing NinePatch:
 * 
 * <pre>
 * { name: whiteButton, color: blue }
 * </pre>
 * 
 * This would create a new NinePatch identical to the NinePatch named "whiteButton" and tint it with the color named "blue".
 * <p>
 * The skin JSON is extensible. Styles and resources for your own widgets may be included in the skin, usually without writing any
 * code. Deserialization is handled by the {@link Json} class, which automatically serializes and deserializes most objects. While
 * nearly any style object can be automatically deserialized, often resource objects require custom deserialization. Eg,
 * TextureRegion, BitmapFont, and NinePatch need to reference the skin's single texture. If needed,
 * {@link #getJsonLoader(FileHandle)} may be overridden to register additional custom {@link Serializer serializers}. See the
 * source for {@link Skin#getJsonLoader(FileHandle)} for examples on how to write serializers.
 * <p>
 * Note that there is a SkinPacker class in the gdx-tools project that can take a directory of individual images, pack them into a
 * single texture, and write the proper texture region and ninepatch entries to a skin JSON file. The styles and other resources
 * sections still need to be written by hand, but SkinPacker makes the otherwise tedious entry of pixel coordinates unnecessary.
 * @author Nathan Sweet */
public class Skin implements Disposable {
	ObjectMap<Class, ObjectMap<String, Object>> resources = new ObjectMap();
	ObjectMap<Class, ObjectMap<String, Object>> styles = new ObjectMap();
	Texture texture;

	public Skin () {
	}

	public Skin (FileHandle skinFile, FileHandle textureFile) {
		texture = new Texture(textureFile);
		load(skinFile);
	}

	public Skin (FileHandle skinFile, Texture texture) {
		this.texture = texture;
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		load(skinFile);
	}

	public void load (FileHandle skinFile) {
		try {
			getJsonLoader(skinFile).fromJson(Skin.class, skinFile);
		} catch (SerializationException ex) {
			throw new SerializationException("Error reading file: " + skinFile, ex);
		}
	}

	public void addResource (String name, Object resource) {
		if (name == null) throw new IllegalArgumentException("name cannot be null.");
		if (resource == null) throw new IllegalArgumentException("resource cannot be null.");
		ObjectMap<String, Object> typeResources = resources.get(resource.getClass());
		if (typeResources == null) {
			typeResources = new ObjectMap();
			resources.put(resource.getClass(), typeResources);
		}
		typeResources.put(name, resource);
	}

	public <T> T getResource (String name, Class<T> type) {
		if (name == null) throw new IllegalArgumentException("name cannot be null.");
		ObjectMap<String, Object> typeResources = resources.get(type);
		if (typeResources == null)
			throw new GdxRuntimeException("No " + type.getName() + " resource registered with name: " + name);
		Object resource = typeResources.get(name);
		if (resource == null) throw new GdxRuntimeException("No " + type.getName() + " resource registered with name: " + name);
		return (T)resource;
	}

	public boolean hasResource (String name, Class type) {
		ObjectMap<String, Object> typeResources = resources.get(type);
		if (typeResources == null) return false;
		Object resource = typeResources.get(name);
		if (resource == null) return false;
		return true;
	}

	public NinePatch getPatch (String name) {
		return getResource(name, NinePatch.class);
	}

	public Color getColor (String name) {
		return getResource(name, Color.class);
	}

	public BitmapFont getFont (String name) {
		return getResource(name, BitmapFont.class);
	}

	public TextureRegion getRegion (String name) {
		return getResource(name, TextureRegion.class);
	}

	public void addStyle (String name, Object style) {
		if (name == null) throw new IllegalArgumentException("name cannot be null.");
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
		if (name == null) throw new IllegalArgumentException("name cannot be null.");
		ObjectMap<String, Object> typeStyles = styles.get(type);
		if (typeStyles == null) throw new GdxRuntimeException("No styles registered with type: " + type.getName());
		Object style = typeStyles.get(name);
		if (style == null) throw new GdxRuntimeException("No " + type.getName() + " style registered with name: " + name);
		return (T)style;
	}

	public boolean hasStyle (String name, Class type) {
		ObjectMap<String, Object> typeStyles = styles.get(type);
		if (typeStyles == null) return false;
		Object style = typeStyles.get(name);
		if (style == null) return false;
		return true;
	}

	/** Returns the name of the specified style object, or null if it is not in the skin. This compares potentially every style
	 * object in the skin of the same type as the specified style, which may be a somewhat expensive operation. */
	public String findStyleName (Object style) {
		if (style == null) throw new IllegalArgumentException("style cannot be null.");
		ObjectMap<String, Object> typeStyles = styles.get(style.getClass());
		if (typeStyles == null) return null;
		return typeStyles.findKey(style, true);
	}

	public void setEnabled (Actor actor, boolean enabled) {
		actor.touchable = enabled;
		// Get current style.
		Method method = findMethod(actor.getClass(), "getStyle");
		if (method == null) return;
		Object style;
		try {
			style = method.invoke(actor);
		} catch (Exception ignored) {
			return;
		}
		// Determine new style.
		String name = findStyleName(style);
		if (name == null) return;
		name = name.replace("-disabled", "") + (enabled ? "" : "-disabled");
		style = getStyle(name, style.getClass());
		// Set new style.
		method = findMethod(actor.getClass(), "setStyle");
		if (method == null) return;
		try {
			method.invoke(actor, style);
		} catch (Exception ignored) {
		}
	}

	static private Method findMethod (Class type, String name) {
		Method[] methods = type.getMethods();
		for (int i = 0, n = methods.length; i < n; i++) {
			Method method = methods[i];
			if (method.getName().equals(name)) return method;
		}
		return null;
	}

	public void setTexture (Texture texture) {
		this.texture = texture;
	}

	/** Returns the single {@link Texture} that all resources in this skin reference. */
	public Texture getTexture () {
		return texture;
	}

	/** Disposes the {@link Texture} and all {@link Disposable} resources of this Skin. */
	@Override
	public void dispose () {
		texture.dispose();
		for (Entry<Class, ObjectMap<String, Object>> entry : resources.entries()) {
			if (!Disposable.class.isAssignableFrom(entry.key)) continue;
			for (Object resource : entry.value.values())
				((Disposable)resource).dispose();
		}
	}

	public void save (FileHandle skinFile) {
		String text = getJsonLoader(null).prettyPrint(this, 130);
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

		final Json json = new Json();
		json.setTypeName(null);
		json.setUsePrototypes(false);

		// Writes names of resources instead of objects.
		class AliasWriter implements Serializer {
			final ObjectMap<String, ?> map;

			public AliasWriter (Class type) {
				map = resources.get(type);
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
				throw new UnsupportedOperationException();
			}
		}

		json.setSerializer(Skin.class, new Serializer<Skin>() {
			public void write (Json json, Skin skin, Class valueType) {
				json.writeObjectStart();
				json.writeValue("resources", skin.resources);
				for (Entry<Class, ObjectMap<String, Object>> entry : resources.entries())
					json.setSerializer(entry.key, new AliasWriter(entry.key));
				json.writeField(skin, "styles");
				json.writeObjectEnd();
			}

			public Skin read (Json json, Object jsonData, Class ignored) {
				ObjectMap map = (ObjectMap)jsonData;
				readTypeMap(json, (ObjectMap)map.get("resources"), true);
				readTypeMap(json, (ObjectMap)map.get("styles"), false);
				return skin;
			}

			private void readTypeMap (Json json, ObjectMap<String, ObjectMap> typeToValueMap, boolean isResource) {
				if (typeToValueMap == null)
					throw new SerializationException("Skin file is missing a \"" + (isResource ? "resources" : "styles")
						+ "\" section.");
				for (Entry<String, ObjectMap> typeEntry : typeToValueMap.entries()) {
					String className = typeEntry.key;
					ObjectMap<String, ObjectMap> valueMap = (ObjectMap)typeEntry.value;
					try {
						readNamedObjects(json, Class.forName(className), valueMap, isResource);
					} catch (ClassNotFoundException ex) {
						throw new SerializationException(ex);
					}
				}
			}

			private void readNamedObjects (Json json, Class type, ObjectMap<String, ObjectMap> valueMap, boolean isResource) {
				for (Entry<String, ObjectMap> valueEntry : valueMap.entries()) {
					String name = valueEntry.key;
					Object object = json.readValue(type, valueEntry.value);
					if (object == null) continue;
					try {
						if (isResource)
							addResource(name, object);
						else
							addStyle(name, object);
					} catch (Exception ex) {
						throw new SerializationException("Error reading " + type.getSimpleName() + ": " + valueEntry.key, ex);
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
				if (jsonData instanceof String) return getResource((String)jsonData, TextureRegion.class);
				int x = json.readValue("x", int.class, jsonData);
				int y = json.readValue("y", int.class, jsonData);
				int width = json.readValue("width", int.class, jsonData);
				int height = json.readValue("height", int.class, jsonData);
				return new TextureRegion(skin.texture, x, y, width, height);
			}
		});

		json.setSerializer(BitmapFont.class, new Serializer<BitmapFont>() {
			public void write (Json json, BitmapFont font, Class valueType) {
				json.writeObjectStart();
				json.writeValue("file", font.getData().getFontFile().toString().replace('\\', '/'));
				json.writeObjectEnd();
			}

			public BitmapFont read (Json json, Object jsonData, Class type) {
				if (jsonData instanceof String) return getResource((String)jsonData, BitmapFont.class);
				String path = json.readValue("file", String.class, jsonData);

				FileHandle file = skinFile.parent().child(path);
				if (!file.exists()) file = Gdx.files.internal(path);
				if (!file.exists()) throw new SerializationException("Font file not found: " + file);

				// Use a region with the same name as the font, else use a PNG file in the same directory as the FNT file.
				TextureRegion region = null;
				if (skin.hasResource(file.nameWithoutExtension(), TextureRegion.class))
					region = skin.getResource(file.nameWithoutExtension(), TextureRegion.class);
				try {
					return new BitmapFont(file, region, false);
				} catch (RuntimeException ex) {
					throw new SerializationException("Error loading bitmap font: " + file, ex);
				}
			}
		});

		json.setSerializer(NinePatch.class, new Serializer<NinePatch>() {
			public void write (Json json, NinePatch ninePatch, Class valueType) {
				TextureRegion[] patches = ninePatch.getPatches();
				boolean singlePatch = patches[0] == null && patches[1] == null && patches[2] == null && patches[3] == null
					&& patches[4] != null && patches[5] == null && patches[6] == null && patches[7] == null && patches[8] == null;
				if (ninePatch.getColor() != null) {
					json.writeObjectStart();
					json.writeValue("color", ninePatch.getColor());
					if (singlePatch)
						json.writeValue("region", patches[4]);
					else
						json.writeValue("regions", patches);
					json.writeObjectEnd();
				} else {
					if (singlePatch)
						json.writeValue(patches[4]);
					else
						json.writeValue(patches);
				}
			}

			public NinePatch read (Json json, Object jsonData, Class type) {
				if (jsonData instanceof String) return getResource((String)jsonData, NinePatch.class);
				if (jsonData instanceof Array) {
					TextureRegion[] regions = json.readValue(TextureRegion[].class, jsonData);
					if (regions.length == 1) return new NinePatch(regions[0]);
					return new NinePatch(regions);
				} else {
					ObjectMap map = (ObjectMap)jsonData;
					NinePatch ninePatch;
					if (map.containsKey("regions"))
						ninePatch = new NinePatch(json.readValue("regions", TextureRegion[].class, jsonData));
					else if (map.containsKey("region"))
						ninePatch = new NinePatch(json.readValue("region", TextureRegion.class, jsonData));
					else
						ninePatch = new NinePatch(json.readValue(TextureRegion.class, jsonData));
					// throw new SerializationException("Missing ninepatch regions: " + map);
					if (map.containsKey("color")) ninePatch.setColor(json.readValue("color", Color.class, jsonData));
					return ninePatch;
				}
			}
		});

		json.setSerializer(Color.class, new Serializer<Color>() {
			public void write (Json json, Color color, Class valueType) {
				json.writeObjectStart();
				json.writeFields(color);
				json.writeObjectEnd();
			}

			public Color read (Json json, Object jsonData, Class type) {
				if (jsonData instanceof String) return getResource((String)jsonData, Color.class);
				ObjectMap map = (ObjectMap)jsonData;
				float r = json.readValue("r", float.class, 0f, jsonData);
				float g = json.readValue("g", float.class, 0f, jsonData);
				float b = json.readValue("b", float.class, 0f, jsonData);
				float a = json.readValue("a", float.class, 1f, jsonData);
				return new Color(r, g, b, a);
			}
		});

		json.setSerializer(TintedNinePatch.class, new Serializer() {
			public void write (Json json, Object tintedPatch, Class valueType) {
				json.writeObjectStart();
				json.writeField(tintedPatch, "name");
				json.writeField(tintedPatch, "color");
				json.writeObjectEnd();
			}

			public Object read (Json json, Object jsonData, Class type) {
				String name = json.readValue("name", String.class, jsonData);
				Color color = json.readValue("color", Color.class, jsonData);
				return new NinePatch(getResource(name, NinePatch.class), color);
			}
		});

		return json;
	}

	static public class TintedNinePatch extends NinePatch {
		public String name;
		public Color color;

		public TintedNinePatch (NinePatch ninePatch, Color color) {
			super(ninePatch, color);
		}
	}
}
