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

package com.badlogic.gdx.graphics;

import com.badlogic.gdx.utils.ObjectMap;

/** A general purpose class containing named colors that can be changed at will. For example, the markup language defined by the
 * {@code BitmapFontCache} class uses this class to retrieve colors and the user can define his own colors.
 * 
 * @author davebaol */
public final class Colors {

	private static final ObjectMap<String, Color> map = new ObjectMap<String, Color>();

	static {
		reset();
	}

	/** Returns the color map. */
	public static ObjectMap<String, Color> getColors () {
		return map;
	}

	/** Convenience method to lookup a color by {@code name}. The invocation of this method is equivalent to the expression
	 * {@code Colors.getColors().get(name)}
	 * 
	 * If a hex string with 6 (or 8 with alpha channel) characters is given as parameter @code name then
	 * a new Color is returned with specified RGB (RGBA) channel values.
	 *
	 * @param name the name of the color
	 * @return the color to which the specified {@code name} is mapped, or {@code null} if there was no mapping for {@code name}
	 *         . */
	public static Color get(String name) {
		if (name.length() == 6 || name.length() == 8)
		{
		    boolean isHex = true;
		    for (int i=0; i<name.length(); i++)
		    {
			if (Character.digit(name.charAt(i), 16) == -1)
			{
			    isHex = false;
			    break;
			}
		    }

		    if (isHex)
		    {
			int r, g, b, a;
			r = Integer.parseInt(String.valueOf(name.subSequence(0, 2)), 16);
			g = Integer.parseInt(String.valueOf(name.subSequence(2, 4)), 16);
			b = Integer.parseInt(String.valueOf(name.subSequence(4, 6)), 16);
			if (name.length() == 8)
			    a = Integer.parseInt(String.valueOf(name.subSequence(6, 8)), 16);
			else
			    a = 255;

			return new Color(r / 255f, g / 255f, b / 255f, a / 255f);
		    }

		}

		return (Color)map.get(name);
	    }

	/** Convenience method to add a {@code color} with its {@code name}. The invocation of this method is equivalent to the
	 * expression {@code Colors.getColors().put(name, color)}
	 * 
	 * @param name the name of the color
	 * @param color the color
	 * @return the previous {@code color} associated with {@code name}, or {@code null} if there was no mapping for {@code name}
	 *         . */
	public static Color put (String name, Color color) {
		return map.put(name, color);
	}

	/** Resets the color map to the predefined colors. */
	public static void reset () {
		map.clear();
		map.put("CLEAR", Color.CLEAR);
		map.put("BLACK", Color.BLACK);

		map.put("WHITE", Color.WHITE);
		map.put("LIGHT_GRAY", Color.LIGHT_GRAY);
		map.put("GRAY", Color.GRAY);
		map.put("DARK_GRAY", Color.DARK_GRAY);

		map.put("BLUE", Color.BLUE);
		map.put("NAVY", Color.NAVY);
		map.put("ROYAL", Color.ROYAL);
		map.put("SLATE", Color.SLATE);
		map.put("SKY", Color.SKY);
		map.put("CYAN", Color.CYAN);
		map.put("TEAL", Color.TEAL);

		map.put("GREEN", Color.GREEN);
		map.put("CHARTREUSE", Color.CHARTREUSE);
		map.put("LIME", Color.LIME);
		map.put("FOREST", Color.FOREST);
		map.put("OLIVE", Color.OLIVE);

		map.put("YELLOW", Color.YELLOW);
		map.put("GOLD", Color.GOLD);
		map.put("GOLDENROD", Color.GOLDENROD);
		map.put("ORANGE", Color.ORANGE);

		map.put("BROWN", Color.BROWN);
		map.put("TAN", Color.TAN);
		map.put("FIREBRICK", Color.FIREBRICK);

		map.put("RED", Color.RED);
		map.put("SCARLET", Color.SCARLET);
		map.put("CORAL", Color.CORAL);
		map.put("SALMON", Color.SALMON);
		map.put("PINK", Color.PINK);
		map.put("MAGENTA", Color.MAGENTA);

		map.put("PURPLE", Color.PURPLE);
		map.put("VIOLET", Color.VIOLET);
		map.put("MAROON", Color.MAROON);
	}

	private Colors () {
	}

}
