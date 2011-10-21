/*******************************************************************************
 * Copyright (c) 2011, Nathan Sweet <nathan.sweet@gmail.com>
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package com.badlogic.gdx.scenes.scene2d.ui.tablelayout;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Layout;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.esotericsoftware.tablelayout.Toolkit;

/** @author Nathan Sweet */
public class LibgdxToolkit extends Toolkit<Actor, Table, TableLayout> {
	static {
		addClassPrefix("com.badlogic.gdx.scenes.scene2d.");
		addClassPrefix("com.badlogic.gdx.scenes.scene2d.ui.");
	}

	static public LibgdxToolkit instance = new LibgdxToolkit();

	static boolean drawDebug;

	public Actor wrap (TableLayout layout, Object object) {
		if (object instanceof String) {
			if (layout.skin == null) throw new IllegalStateException("Label cannot be created, no skin has been set.");
			return new Label((String)object, layout.skin);
		}
		if (object == null) {
			Group group = new Group();
			group.transform = false;
			return group;
		}
		return super.wrap(layout, object);
	}

	public Actor newWidget (TableLayout layout, String className) {
		try {
			return super.newWidget(layout, className);
		} catch (RuntimeException ex) {
			Skin skin = layout.skin;
			if (skin != null) {
				if (skin.hasResource(className, TextureRegion.class))
					return new Image(skin.getResource(className, TextureRegion.class));
				if (skin.hasResource(className, NinePatch.class)) return new Image(skin.getResource(className, NinePatch.class));
			}
			if (layout.assetManager != null && layout.assetManager.isLoaded(className, Texture.class))
				return new Image(new TextureRegion(layout.assetManager.get(className, Texture.class)));
			throw ex;
		}
	}

	protected Actor newInstance (TableLayout layout, String className) throws Exception {
		try {
			return super.newInstance(layout, className);
		} catch (Exception ex) {
			// Try a Skin constructor.
			if (layout.skin != null) {
				try {
					return (Actor)Class.forName(className).getConstructor(Skin.class).newInstance(layout.skin);
				} catch (Exception ignored) {
				}
			}
			// Try a Stage constructor.
			if (layout.stage != null) {
				try {
					return (Actor)Class.forName(className).getConstructor(Stage.class).newInstance(layout.stage);
				} catch (Exception ignored) {
				}
			}
			throw ex;
		}
	}

	public void setProperty (TableLayout layout, Actor object, String name, List<String> values) {
		try {
			super.setProperty(layout, object, name, values);
		} catch (RuntimeException ex) {
			// style:stylename, set widget style from skin.
			if (layout.skin != null && values.size() == 1 && name.equalsIgnoreCase("style")) {
				Field field = getField(object.getClass(), "style");
				if (field != null) {
					String styleName = values.get(0);
					Class styleClass = field.getType();
					if (layout.skin.hasStyle(styleName, styleClass)) {
						try {
							Method setStyleMethod = object.getClass().getMethod("setStyle", styleClass);
							setStyleMethod.invoke(object, layout.skin.getStyle(styleName, styleClass));
							return;
						} catch (Exception ex2) {
							throw new GdxRuntimeException("Unable to set style: " + styleName, ex2);
						}
					}
				}
			}
			throw ex;
		}
	}

	protected Object convertType (TableLayout layout, Object parentObject, Class memberType, String memberName, String value) {
		// Find TextureRegion and NinePatch in skin.
		if (layout.skin != null) {
			if (memberType == NinePatch.class) {
				if (layout.skin.hasResource(value, NinePatch.class)) return layout.skin.getResource(value, NinePatch.class);
			} else if (memberType == TextureRegion.class) {
				if (layout.skin.hasResource(value, TextureRegion.class)) return layout.skin.getResource(value, TextureRegion.class);
			}
		}
		// Find Texture, TextureRegion and NinePatch in asset manager.
		if (layout.assetManager != null) {
			if (memberType == NinePatch.class) {
				if (layout.assetManager.isLoaded(value, Texture.class))
					return new NinePatch(new TextureRegion(layout.assetManager.get(value, Texture.class)));
			} else if (memberType == Texture.class) {
				if (layout.assetManager.isLoaded(value, Texture.class)) return layout.assetManager.get(value, Texture.class);
			} else if (memberType == TextureRegion.class) {
				if (layout.assetManager.isLoaded(value, Texture.class))
					return new TextureRegion(layout.assetManager.get(value, Texture.class));
			}
		}
		return super.convertType(layout, parentObject, memberType, memberName, value);
	}

	public Table newTable (Table parent) {
		Table table = super.newTable(parent);
		TableLayout layout = parent.getTableLayout();
		table.setSkin(layout.skin);
		table.setAssetManager(layout.assetManager);
		table.setStage(layout.stage);
		return table;
	}

	public TableLayout getLayout (Table table) {
		return table.getTableLayout();
	}

	public Actor newStack () {
		return new Stack();
	}

	public void addChild (Actor parent, Actor child, String layoutString) {
		if (child.parent != null) child.remove();
		((Group)parent).addActor(child);
	}

	public void removeChild (Actor parent, Actor child) {
		((Group)parent).removeActor(child);
	}

	public int getMinWidth (Actor actor) {
		if (actor instanceof Layout) return (int)((Layout)actor).getMinWidth();
		return (int)actor.width;
	}

	public int getMinHeight (Actor actor) {
		if (actor instanceof Layout) return (int)((Layout)actor).getMinHeight();
		return (int)actor.height;
	}

	public int getPrefWidth (Actor actor) {
		if (actor instanceof Layout) return (int)((Layout)actor).getPrefWidth();
		return (int)actor.width;
	}

	public int getPrefHeight (Actor actor) {
		if (actor instanceof Layout) return (int)((Layout)actor).getPrefHeight();
		return (int)actor.height;
	}

	public int getMaxWidth (Actor actor) {
		if (actor instanceof Layout) return (int)((Layout)actor).getMaxWidth();
		return 0;
	}

	public int getMaxHeight (Actor actor) {
		if (actor instanceof Layout) return (int)((Layout)actor).getMaxHeight();
		return 0;
	}

	public void clearDebugRectangles (TableLayout layout) {
		if (layout.debugRects != null) layout.debugRects.clear();
	}

	public void addDebugRectangle (TableLayout layout, int type, int x, int y, int w, int h) {
		drawDebug = true;
		if (layout.debugRects == null) layout.debugRects = new Array();
		layout.debugRects.add(new DebugRect(type, x, (int)(layout.getTable().height - y), w, h));
	}

	static class DebugRect extends Rectangle {
		final int type;

		public DebugRect (int type, int x, int y, int width, int height) {
			super(x, y, width, height);
			this.type = type;
		}
	}
}
