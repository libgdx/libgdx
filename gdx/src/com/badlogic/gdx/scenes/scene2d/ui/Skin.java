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

import java.lang.reflect.Method;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasSprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.ReadOnlySerializer;
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
	TextureAtlas atlas;

	public Skin (TextureAtlas atlas) {
		this.atlas = atlas;
		add(atlas);
	}

	public Skin (FileHandle skinFile, TextureAtlas atlas) {
		this.atlas = atlas;
		add(atlas);
		load(skinFile);
	}

	public Skin (FileHandle skinFile) {
		this.atlas = new TextureAtlas(skinFile.sibling(skinFile.nameWithoutExtension() + ".atlas"));
		add(atlas);
		load(skinFile);
	}

	public void load (FileHandle skinFile) {
		try {
			getJsonLoader(skinFile).fromJson(Skin.class, skinFile);
		} catch (SerializationException ex) {
			throw new SerializationException("Error reading file: " + skinFile, ex);
		}
	}

	private void add (TextureAtlas atlas) {
		Array<AtlasRegion> regions = atlas.getRegions();
		for (int i = 0, n = regions.size; i < n; i++) {
			AtlasRegion region = regions.get(i);
			add(region.name, region, TextureRegion.class);
		}
	}

	public void add (String name, Object resource) {
		add(name, resource, resource.getClass());
	}

	public void add (String name, Object resource, Class type) {
		if (name == null) throw new IllegalArgumentException("name cannot be null.");
		if (resource == null) throw new IllegalArgumentException("resource cannot be null.");
		ObjectMap<String, Object> typeResources = resources.get(type);
		if (typeResources == null) {
			typeResources = new ObjectMap();
			resources.put(type, typeResources);
		}
		typeResources.put(name, resource);
	}

	public <T> T get (Class<T> type) {
		return get("default", type);
	}

	public <T> T get (String name, Class<T> type) {
		if (name == null) throw new IllegalArgumentException("name cannot be null.");
		if (type == null) throw new IllegalArgumentException("type cannot be null.");

		if (type == Drawable.class) return (T)getDrawable(name);
		if (type == TextureRegion.class) return (T)getRegion(name);
		if (type == NinePatch.class) return (T)getPatch(name);
		if (type == Sprite.class) return (T)getSprite(name);

		ObjectMap<String, Object> typeResources = resources.get(type);
		if (typeResources == null) throw new GdxRuntimeException("No " + type.getName() + " registered with name: " + name);
		Object resource = typeResources.get(name);
		if (resource == null) throw new GdxRuntimeException("No " + type.getName() + " registered with name: " + name);
		return (T)resource;
	}

	public <T> T optional (String name, Class<T> type) {
		if (name == null) throw new IllegalArgumentException("name cannot be null.");
		if (type == null) throw new IllegalArgumentException("type cannot be null.");
		ObjectMap<String, Object> typeResources = resources.get(type);
		if (typeResources == null) return null;
		return (T)typeResources.get(name);
	}

	public boolean has (String name, Class type) {
		ObjectMap<String, Object> typeResources = resources.get(type);
		if (typeResources == null) return false;
		return typeResources.containsKey(name);
	}

	/** Returns the name to resource mapping for the specified type, or null if no resources of that type exist. */
	public <T> ObjectMap<String, T> getAll (Class<T> type) {
		return (ObjectMap<String, T>)resources.get(type);
	}

	public Color getColor (String name) {
		return get(name, Color.class);
	}

	public BitmapFont getFont (String name) {
		return get(name, BitmapFont.class);
	}

	public TextureRegion getRegion (String name) {
		TextureRegion region = optional(name, TextureRegion.class);
		if (region != null) return region;

		Texture texture = optional(name, Texture.class);
		if (texture == null) throw new GdxRuntimeException("No TextureRegion or Texture registered with name: " + name);
		region = new TextureRegion(texture);
		add(name, region, Texture.class);
		return region;
	}

	/** Returns a registered ninepatch. If no ninepatch is found but a region exists with the name, the region is returned as a
	 * ninepatch. If the region is an {@link AtlasRegion} then the {@link AtlasRegion#splits} are used. */
	public NinePatch getPatch (String name) {
		NinePatch patch = optional(name, NinePatch.class);
		if (patch != null) return patch;

		try {
			TextureRegion region = getRegion(name);
			if (region instanceof AtlasRegion) {
				int[] splits = ((AtlasRegion)region).splits;
				if (splits != null) 
					patch = new NinePatch(region, splits[0], splits[1], splits[2], splits[3]);
			}
			if (patch == null) patch = new NinePatch(region);
			add(name, patch, NinePatch.class);
			return patch;
		} catch (GdxRuntimeException ex) {
			throw new GdxRuntimeException("No NinePatch, TextureRegion, or Texture registered with name: " + name);
		}
	}

	public Sprite getSprite (String name) {
		Sprite sprite = optional(name, Sprite.class);
		if (sprite != null) return sprite;

		try {
			TextureRegion textureRegion = getRegion(name);
			if (textureRegion instanceof AtlasRegion) {
				AtlasRegion region = (AtlasRegion)textureRegion;
				if (region.rotate || region.packedWidth != region.originalWidth || region.packedHeight != region.originalHeight)
					sprite = new AtlasSprite(region);
			}
			if (sprite == null) sprite = new Sprite(textureRegion);
			add(name, sprite, NinePatch.class);
			return sprite;
		} catch (GdxRuntimeException ex) {
			throw new GdxRuntimeException("No NinePatch, TextureRegion, or Texture registered with name: " + name);
		}
	}

	public Drawable getDrawable (String name) {
		Drawable drawable = optional(name, Drawable.class);
		if (drawable != null) return drawable;

		// Use texture or texture region. If it has splits, use ninepatch. If it has rotation or whitespace stripping, use sprite.
		try {
			TextureRegion textureRegion = getRegion(name);
			if (textureRegion instanceof AtlasRegion) {
				AtlasRegion region = (AtlasRegion)textureRegion;
				if (region.splits != null)
					drawable = new NinePatchDrawable(getPatch(name));
				else if (region.rotate || region.packedWidth != region.originalWidth || region.packedHeight != region.originalHeight)
					drawable = new SpriteDrawable(getSprite(name));
			}
			if (drawable == null) drawable = new TextureRegionDrawable(textureRegion);
		} catch (GdxRuntimeException ignored) {
		}

		// Check for explicit registration of ninepatch or sprite.
		if (drawable == null) {
			NinePatch patch = optional(name, NinePatch.class);
			if (patch != null)
				drawable = new NinePatchDrawable(patch);
			else {
				Sprite sprite = optional(name, Sprite.class);
				if (sprite != null)
					drawable = new SpriteDrawable(sprite);
				else
					throw new GdxRuntimeException("No Drawable, NinePatch, TextureRegion, Texture, or Sprite registered with name: "
						+ name);
			}
		}

		add(name, drawable, Drawable.class);
		return drawable;
	}

	/** Returns the name of the specified style object, or null if it is not in the skin. This compares potentially every style
	 * object in the skin of the same type as the specified style, which may be a somewhat expensive operation. */
	public String find (Object resource) {
		if (resource == null) throw new IllegalArgumentException("style cannot be null.");
		ObjectMap<String, Object> typeResources = resources.get(resource.getClass());
		if (typeResources == null) return null;
		return typeResources.findKey(resource, true);
	}

	public Drawable newDrawable (String name) {
		Drawable drawable = getDrawable(name);
		if (drawable instanceof TextureRegionDrawable) return new TextureRegionDrawable((TextureRegionDrawable)drawable);
		if (drawable instanceof NinePatchDrawable) return new NinePatchDrawable((NinePatchDrawable)drawable);
		if (drawable instanceof SpriteDrawable) return new SpriteDrawable((SpriteDrawable)drawable);
		throw new GdxRuntimeException("Unable to copy, unknown drawable type: " + drawable.getClass());
	}

	public Drawable newDrawable (String name, Color tint) {
		Drawable drawable = getDrawable(name);
		if (drawable instanceof TextureRegionDrawable) {
			Sprite sprite = new Sprite(((TextureRegionDrawable)drawable).getRegion());
			sprite.setColor(tint);
			return new SpriteDrawable(sprite);
		}
		if (drawable instanceof NinePatchDrawable) {
			NinePatchDrawable patchDrawable = new NinePatchDrawable((NinePatchDrawable)drawable);
			patchDrawable.setPatch(new NinePatch(patchDrawable.getPatch(), tint));
			return patchDrawable;
		}
		if (drawable instanceof SpriteDrawable) {
			SpriteDrawable spriteDrawable = new SpriteDrawable((SpriteDrawable)drawable);
			Sprite sprite = new Sprite(spriteDrawable.getSprite());
			sprite.setColor(tint);
			spriteDrawable.setSprite(sprite);
			return spriteDrawable;
		}
		throw new GdxRuntimeException("Unable to copy, unknown drawable type: " + drawable.getClass());
	}

	/** Sets the style on the actor to disabled or enabled. This is done by appending "-disabled" to the style name when enabled is
	 * false, and removing "-disabled" from the style name when enabled is true. A method named "getStyle" is called the actor via
	 * reflection and the name of that style is found in the skin. If the actor doesn't have a "getStyle" method or the style was
	 * not found in the skin, no exception is thrown and the actor is left unchanged. */
	public void setEnabled (Actor actor, boolean enabled) {
		actor.setTouchable(enabled);
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
		String name = find(style);
		if (name == null) return;
		name = name.replace("-disabled", "") + (enabled ? "" : "-disabled");
		style = get(name, style.getClass());
		// Set new style.
		method = findMethod(actor.getClass(), "setStyle");
		if (method == null) return;
		try {
			method.invoke(actor, style);
		} catch (Exception ignored) {
		}
	}

	/** Returns the {@link TextureAtlas} that resources in this skin reference. */
	public TextureAtlas getAtlas () {
		return atlas;
	}

	/** Disposes the {@link Texture} and all {@link Disposable} resources of this Skin. */
	@Override
	public void dispose () {
		atlas.dispose(); // BOZO - Only if owned.
		for (Entry<Class, ObjectMap<String, Object>> entry : resources.entries()) {
			if (!Disposable.class.isAssignableFrom(entry.key)) continue;
			for (Object resource : entry.value.values())
				((Disposable)resource).dispose();
		}
	}

	protected Json getJsonLoader (final FileHandle skinFile) {
		final Skin skin = this;

		final Json json = new Json() {
			public <T> T readValue (Class<T> type, Class elementType, Object jsonData) {
				// If the JSON is a string but the type is not, look up the actual value by name.
				if (jsonData instanceof String && !CharSequence.class.isAssignableFrom(type)) return get((String)jsonData, type);
				return super.readValue(type, elementType, jsonData);
			}
		};
		json.setTypeName(null);
		json.setUsePrototypes(false);

		json.setSerializer(Skin.class, new ReadOnlySerializer<Skin>() {
			public Skin read (Json json, Object jsonData, Class ignored) {
				ObjectMap<String, ObjectMap> typeToValueMap = (ObjectMap)jsonData;
				for (Entry<String, ObjectMap> typeEntry : typeToValueMap.entries()) {
					String className = typeEntry.key;
					ObjectMap<String, ObjectMap> valueMap = (ObjectMap)typeEntry.value;
					try {
						readNamedObjects(json, Class.forName(className), valueMap);
					} catch (ClassNotFoundException ex) {
						throw new SerializationException(ex);
					}
				}
				return skin;
			}

			private void readNamedObjects (Json json, Class type, ObjectMap<String, ObjectMap> valueMap) {
				for (Entry<String, ObjectMap> valueEntry : valueMap.entries()) {
					String name = valueEntry.key;
					Object object = json.readValue(type, valueEntry.value);
					if (object == null) continue;
					try {
						add(name, object);
					} catch (Exception ex) {
						throw new SerializationException("Error reading " + type.getSimpleName() + ": " + valueEntry.key, ex);
					}
				}
			}
		});

		json.setSerializer(BitmapFont.class, new ReadOnlySerializer<BitmapFont>() {
			public BitmapFont read (Json json, Object jsonData, Class type) {
				String path = json.readValue("file", String.class, jsonData);

				FileHandle fontFile = skinFile.parent().child(path);
				if (!fontFile.exists()) fontFile = Gdx.files.internal(path);
				if (!fontFile.exists()) throw new SerializationException("Font file not found: " + fontFile);

				// Use a region with the same name as the font, else use a PNG file in the same directory as the FNT file.
				String regionName = fontFile.nameWithoutExtension();
				try {
					TextureRegion region = skin.optional(regionName, TextureRegion.class);
					if (region != null)
						return new BitmapFont(fontFile, region, false);
					else {
						FileHandle imageFile = fontFile.parent().child(regionName + ".png");
						if (imageFile.exists())
							return new BitmapFont(fontFile, imageFile, false);
						else
							return new BitmapFont(fontFile, false);
					}
				} catch (RuntimeException ex) {
					throw new SerializationException("Error loading bitmap font: " + fontFile, ex);
				}
			}
		});

		json.setSerializer(Color.class, new ReadOnlySerializer<Color>() {
			public Color read (Json json, Object jsonData, Class type) {
				if (jsonData instanceof String) return get((String)jsonData, Color.class);
				ObjectMap map = (ObjectMap)jsonData;
				float r = json.readValue("r", float.class, 0f, jsonData);
				float g = json.readValue("g", float.class, 0f, jsonData);
				float b = json.readValue("b", float.class, 0f, jsonData);
				float a = json.readValue("a", float.class, 1f, jsonData);
				return new Color(r, g, b, a);
			}
		});

		json.setSerializer(TintedDrawable.class, new ReadOnlySerializer() {
			public Object read (Json json, Object jsonData, Class type) {
				String name = json.readValue("name", String.class, jsonData);
				Color color = json.readValue("color", Color.class, jsonData);
				return newDrawable(name, color);
			}
		});

		return json;
	}

	static private Method findMethod (Class type, String name) {
		Method[] methods = type.getMethods();
		for (int i = 0, n = methods.length; i < n; i++) {
			Method method = methods[i];
			if (method.getName().equals(name)) return method;
		}
		return null;
	}

	/** @author Nathan Sweet */
	static public class TintedDrawable {
		public String name;
		public Color color;
	}
}
