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
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.ReadOnlySerializer;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.SerializationException;
import com.badlogic.gwtref.client.ReflectionCache;

/** A skin has a {@link TextureAtlas} and stores resources for UI widgets to use (texture regions, ninepatches, fonts, colors,
 * etc). Resources are named and can be looked up by name and type. Skin provides useful conversions, such as allowing access to
 * regions in the atlas as ninepatches, sprites, drawables, etc.
 * <p>
 * Resources can be added to a skin using code, or defined in JSON. Names can be used in JSON to reference already defined
 * resources or regions in the atlas. The JSON format is:
 * 
 * <pre>
 * {
 * 	className: {
 * 		name: value,
 * 		...
 * 	},
 * 	className: {
 * 		name: value,
 * 		...
 * 	},
 * 	...
 * }
 * </pre>
 * 
 * The class name is the fully qualified Java class name for the type of resource. The name is the name of the resource for that
 * class, and the value is the serialized resource or style.
 * @author Nathan Sweet */
public class Skin implements Disposable {
	ObjectMap<Class, ObjectMap<String, Object>> resources = new ObjectMap();
	TextureAtlas atlas;

	/** Creates an empty skin. */
    public Skin () {
    }

    /** Creates a skin containing the resources in the specified skin JSON file. If a file in the same directory with a ".atlas"
     * extension exists, it is loaded as a {@link TextureAtlas} and the texture regions added to the skin. The atlas is
     * automatically disposed when the skin is disposed. */
    public Skin (FileHandle skinFile) {
            FileHandle atlasFile = skinFile.sibling(skinFile.nameWithoutExtension() + ".atlas");
            if (atlasFile.exists()) {
                    atlas = new TextureAtlas(atlasFile);
                    addRegions(atlas);
            }

            load(skinFile);
    }

    /** Creates a skin containing the resources in the specified skin JSON file and the texture regions from the specified atlas.
     * The atlas is automatically disposed when the skin is disposed. */
    public Skin (FileHandle skinFile, TextureAtlas atlas) {
            this.atlas = atlas;
            addRegions(atlas);
            load(skinFile);
    }

    /** Adds all resources in the specified skin JSON file. */
    public void load (FileHandle skinFile) {
            try {
                    getJsonLoader(skinFile).fromJson(Skin.class, skinFile);
            } catch (SerializationException ex) {
                    throw new SerializationException("Error reading file: " + skinFile, ex);
            }
    }

    /** Adds all named txeture regions from the atlas. The atlas will not be automatically disposed when the skin is disposed. */
    public void addRegions (TextureAtlas atlas) {
            Array<AtlasRegion> regions = atlas.getRegions();
            for (int i = 0, n = regions.size; i < n; i++) {
                    AtlasRegion region = regions.get(i);
                    add(region.name, region, TextureRegion.class);
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
				if (splits != null) patch = new NinePatch(region, splits[0], splits[1], splits[2], splits[3]);
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
		// Get current style.
		com.badlogic.gwtref.client.Method method = findMethod(actor.getClass(), "getStyle");
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

	/** Disposes the {@link TextureAtlas} and all {@link Disposable} resources in the skin. */
	public void dispose () {
		atlas.dispose();
		for (ObjectMap<String, Object> entry : resources.values()) {
			for (Object resource : entry.values())
				if (resource instanceof Disposable) ((Disposable)resource).dispose();
		}
	}

	protected Json getJsonLoader (final FileHandle skinFile) {
		final Skin skin = this;

		final Json json = new Json() {
			@Override
			public <T> T readValue (Class<T> type, Class elementType, Object jsonData) {
				// If the JSON is a string but the type is not, look up the actual value by name.				
				if (jsonData instanceof String && !ReflectionCache.getType(type).isAssignableFrom(ReflectionCache.getType(CharSequence.class))) return get((String)jsonData, type);
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
						readNamedObjects(json, ReflectionCache.forName(className).getClassOfType(), valueMap);
					} catch (ClassNotFoundException ex) {
						throw new SerializationException(ex);
					}
				}
				return skin;
			}

			private void readNamedObjects (Json json, Class type, ObjectMap<String, ObjectMap> valueMap) {
				Class addType = type == TintedDrawable.class ? Drawable.class : type;
				for (Entry<String, ObjectMap> valueEntry : valueMap.entries()) {
					String name = valueEntry.key;
					Object object = json.readValue(type, valueEntry.value);
					if (object == null) continue;
					try {
						add(name, object, addType);
					} catch (Exception ex) {
						throw new SerializationException("Error reading " + type.getName() + ": " + valueEntry.key, ex);
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

	static private com.badlogic.gwtref.client.Method findMethod (Class type, String name) {
		com.badlogic.gwtref.client.Method[] methods = ReflectionCache.getType(type).getMethods();
		for (int i = 0, n = methods.length; i < n; i++) {
			com.badlogic.gwtref.client.Method method = methods[i];
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
