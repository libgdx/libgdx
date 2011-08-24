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

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ComboBox.ComboBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ImageToggleButton.ImageToggleButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Pane.PaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane.SplitPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ToggleButton.ToggleButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

/** <p>
 * A skin defines graphical resources like {@link NinePatch}, {@link TextureRegion}, {@link Color} and {@link BitmapFont}
 * instances as well as widget styles.
 * </p>
 * 
 * <p>
 * Every widget found in this UI API, like {@link Button} or {@link SplitPane} has an associated style, like {@link ButtonStyle}
 * or {@link SplitPaneStyle}. These styles are defined in an XML file and reference (nine-patch)regions in a single texture,
 * colors and fonts to be used for any widget instantiated with that style.
 * </p>
 * 
 * <p>
 * This class loads a widget style XML file and exposes all parts defined in it via getter methods. Additionally it allows to
 * instantiate the default widgets via convenience methods like {@link Skin#newButton(String, String)}.
 * </p>
 * 
 * <p>
 * Additionally you can instantiate an empty Skin via {@link Skin#Skin(Texture)} and add graphical resources and styles
 * programmatically.
 * </p>
 * 
 * <p>
 * The Skin has a couple of resources it manages, namely the {@link Texture} containing all (nine-patch)regions as well as all the
 * {@link BitmapFont} instances defined in the XML file. To dispose all resouces use the {@link Skin#dispose()} method.
 * </p>
 * 
 * <p>
 * A skin XML file has a root element called &ltskin&gt. Within this tag there are two elements excepted:
 * </p>
 * 
 * <pre>
 * {@code
 * <skin> 
 *    <library>
 *    ... define nine-patches, texture regions, colors and fonts here ...
 *    </library>
 *    <widgetstyles>
 *    ... define widget styles based on library elements here ...
 *    </widgetstyles>
 * </skin>
 * }
 * </pre>
 * 
 * <p>
 * The {@code <library>} element contains the definitions of nine-patches, regions, colors and fonts. Every one of these has to
 * have a unique name. When defining a widget style in the {@code <widgetstyles>} section you will reference these.
 * </p>
 * 
 * <p>
 * Regions, which are loaded as {@link TextureRegion} instances, define an area in the skin's texture. They are specified in
 * pixels, with the origin being in the top left corner of the texture image, with the y-axis pointing downwards. Coordinates and
 * sizes are given as integers relative to that coordinate system.
 * </p>
 * 
 * <pre>
 * {@code 
 * <region name="itsName" x="0" y="0" width="10" height="10"/>
 * }
 * </pre>
 * 
 * 
 * <p>
 * Nine-patches are composed of 9 regions and are defined like this:
 * </p>
 * 
 * <pre>
 * {@code
 * <ninepatch name="itsName">
 *    // top regions, left to right
 *    <region x="0" y="0" width="10" height="10"/>
 *    <region x="0" y="0" width="10" height="10"/>
 *    <region x="0" y="0" width="10" height="10"/>
 *    
 *    // center regions, left to right
 *    <region x="0" y="0" width="10" height="10"/>
 *    <region x="0" y="0" width="10" height="10"/>
 *    <region x="0" y="0" width="10" height="10"/>
 *    
 *    // bottom regions, left to right
 *    <region x="0" y="0" width="10" height="10"/>
 *    <region x="0" y="0" width="10" height="10"/>
 *    <region x="0" y="0" width="10" height="10"/>
 * </ninepatch>
 * }
 * </pre>
 * 
 * <p>
 * Note that the regions in each column have to have the same width, and the regions in each row have to have the same
 * width!</pre>
 * 
 * <p>
 * Colors are defined as RGBA values in the range [0,1] like this:
 * </p>
 * 
 * <pre>
 * {@code 
 * <color name="itsName" r="1" g="1" b="1" a="1"/>
 * }
 * </pre>
 * 
 * <p>
 * Fonts are defined like this:
 * </p>
 * 
 * <pre>
 * {@code 
 * <font name="itsNAme" file="path/to/the/font.fnt"/>
 * }
 * </pre>
 * 
 * <p>
 * The font's files have to be internal files {@link Files#internal(String)}, the path given is relative to the internal files
 * path (e.g. assets/ on Android, the apps root directory on the desktop).
 * </p>
 * 
 * <p>
 * These four types of graphical resources can then be referenced in widget style definitions via their name. A button style for
 * example needs a ninepatch for its background in the "down" (pressed) state, a ninepatch for its background in the "up"
 * (unpressed) state and a font and font color to display its label text. A button style could be defined like this:
 * </p>
 * 
 * <pre>
 * {@code
 * <buttonstyle name="default" down="button-down-patch" up="button-up-patch" font="default-font" fontColor="white"/>
 * }
 * </pre>
 * 
 * <p>
 * The {@code <buttonstyle>} would be a child of the {@code <widgetstyles>} element. Its attributes reference nine-patches, fonts
 * and colors from the {@code <librar>} element via their names. The above example requires a nine-patch named "button-down-patch"
 * to be defined for example.
 * </p>
 * 
 * <p>
 * Refer to the invidual widget classes like {@link Button}, {@link ComboBox}, {@link SplitPane} and so on to find out how to
 * define a style for them in the XML file.
 * </p>
 * @author mzechner */
public class Skin implements Disposable {
	/** registered widget styles **/
	Map<String, NinePatch> ninePatches = new HashMap<String, NinePatch>();
	Map<String, TextureRegion> regions = new HashMap<String, TextureRegion>();
	Map<String, Color> colors = new HashMap<String, Color>();
	Map<String, BitmapFont> fonts = new HashMap<String, BitmapFont>();
	Map<String, ButtonStyle> buttonStyles = new HashMap<String, ButtonStyle>();
	Map<String, CheckBoxStyle> checkBoxStyles = new HashMap<String, CheckBoxStyle>();
	Map<String, SliderStyle> sliderStyles = new HashMap<String, SliderStyle>();
	Map<String, LabelStyle> labelStyles = new HashMap<String, LabelStyle>();
	Map<String, ToggleButtonStyle> toggleButtonStyles = new HashMap<String, ToggleButtonStyle>();
	Map<String, ListStyle> listStyles = new HashMap<String, ListStyle>();
	Map<String, PaneStyle> paneStyles = new HashMap<String, PaneStyle>();
	Map<String, ScrollPaneStyle> scrollPaneStyles = new HashMap<String, ScrollPaneStyle>();
	Map<String, SplitPaneStyle> splitPaneStyles = new HashMap<String, SplitPaneStyle>();
	Map<String, TextFieldStyle> textFieldStyles = new HashMap<String, TextFieldStyle>();
	Map<String, ComboBoxStyle> comboBoxStyles = new HashMap<String, ComboBoxStyle>();
	Map<String, ImageButtonStyle> imageButtonStyles = new HashMap<String, ImageButtonStyle>();
	Map<String, ImageToggleButtonStyle> imageToggleButtonStyles = new HashMap<String, ImageToggleButtonStyle>();
	Map<String, WindowStyle> windowStyles = new HashMap<String, WindowStyle>();
	Texture texture;

	/** Creates a new Skin from the given XML skin file and {@link Texture} image.
	 * @param skinFile the XML skin file
	 * @param textureFile the texture image */
	public Skin (FileHandle skinFile, FileHandle textureFile) {
		texture = new Texture(textureFile);
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		parseSkin(skinFile);
	}

	/** Creates an empty skin, using the given {@link Texture}
	 * @param texture the Texture */
	public Skin (Texture texture) {
		this.texture = texture;
	}

	/** Parses the given XML skin file and adds all assets (regions, patches, colors, fonts) and widget styles to this skin. If an
	 * asset or widget style with the same name was already in the skin it will be overwritten. Widgets already created via this
	 * skin won't change their style!
	 * @param skinFile the XML skin file */
	public void parseSkin (FileHandle skinFile) {
		try {
			XmlReader xml = new XmlReader();
			Element skin = xml.parse(skinFile);
			parseLibrary(skin, skinFile);
			parseWidgetStyles(skin.getChildByName("widgetStyles"));
		} catch (Exception e) {
			throw new GdxRuntimeException("Couldn't parse skinFile", e);
		}
	}

	protected void parseWidgetStyles (Element styles) {
		parseButtonStyles(styles);
		parseCheckBoxStyles(styles);
		parseLabelStyles(styles);
		parseSliderStyles(styles);
		parseToggleButtonStyles(styles);
		parseListStyles(styles);
		parsePaneStyles(styles);
		parseScrollPaneStyles(styles);
		parseSplitPaneStyles(styles);
		parseTextFieldStyles(styles);
		parseComboBoxStyles(styles);
		parseWindowStyles(styles);
		parseImageButtonStyles(styles);
		parseImageToggleButtonStyles(styles);
	}

	private void error (String msg, Element element) {
		throw new GdxRuntimeException(msg + ", element: " + element.toString());
	}

	private void parseWindowStyles (Element styles) {
		for (Element style : styles.getChildrenByName("window")) {
			String name = style.getAttribute("name");
			NinePatch background = getNinePatch(style.getAttribute("background"));
			BitmapFont font = getFont(style.getAttribute("titleFont"));
			Color fontColor = getColor(style.getAttribute("titleFontColor"));

			if (name == null) error("No name given for window style", style);
			if (background == null) error("No 'background' nine-patch given for window style", style);
			if (font == null) error("No 'font' given for window style", style);
			if (fontColor == null) error("No 'fontColor' given for window style", style);

			windowStyles.put(name, new WindowStyle(font, fontColor, background));
		}
	}

	private void parseComboBoxStyles (Element styles) {
		for (Element style : styles.getChildrenByName("combobox")) {
			String name = style.getAttribute("name");
			NinePatch background = getNinePatch(style.getAttribute("background"));
			NinePatch listBackground = getNinePatch(style.getAttribute("listBackground"));
			NinePatch listSelection = getNinePatch(style.getAttribute("listSelection"));
			BitmapFont font = getFont(style.getAttribute("font"));
			Color fontColor = getColor(style.getAttribute("fontColor"));

			if (name == null) error("No name given for combobox style", style);
			if (background == null) error("No 'background' nine-patch given for combobox style", style);
			if (listBackground == null) error("No 'listBackground' nine-patch given for combobox style", style);
			if (listSelection == null) error("No 'listSelection' nine-patch given for combobox style", style);
			if (font == null) error("No 'font' given for combobox style", style);
			if (fontColor == null) error("No 'fontColor' given for combobox style", style);

			comboBoxStyles.put(name, new ComboBoxStyle(font, fontColor, background, listBackground, listSelection));
		}
	}

	private void parseTextFieldStyles (Element styles) {
		for (Element style : styles.getChildrenByName("textfield")) {
			String name = style.getAttribute("name");
			NinePatch background = getNinePatch(style.getAttribute("background"));
			NinePatch cursor = getNinePatch(style.getAttribute("cursor"));
			BitmapFont font = getFont(style.getAttribute("font"));
			Color fontColor = getColor(style.getAttribute("fontColor"));
			TextureRegion selection = getRegion(style.getAttribute("selection"));

			if (name == null) error("No name given for textfield style", style);
			if (background == null) error("No 'background' nine-patch given for textfield style", style);
			if (cursor == null) error("No 'cursor' nine-patch given for textfield style", style);
			if (font == null) error("No 'font' given for textfield style", style);
			if (fontColor == null) error("No 'fontColor' given for textfield stye", style);
			if (selection == null) error("No 'selection' region given for textfield style", style);

			textFieldStyles.put(name, new TextFieldStyle(font, fontColor, cursor, selection, background));
		}
	}

	private void parseSplitPaneStyles (Element styles) {
		for (Element style : styles.getChildrenByName("splitpane")) {
			String name = style.getAttribute("name");
			NinePatch handle = getNinePatch(style.getAttribute("handle"));

			if (name == null) error("No name given for splitpane style", style);
			if (handle == null) error("No 'handle' given for splitpane style", style);

			splitPaneStyles.put(name, new SplitPaneStyle(handle));
		}
	}

	private void parseScrollPaneStyles (Element styles) {
		for (Element style : styles.getChildrenByName("scrollpane")) {
			String name = style.getAttribute("name");
			NinePatch background = getNinePatch(style.getAttribute("background"));
			NinePatch hScroll = getNinePatch(style.getAttribute("hScroll"));
			NinePatch hScrollKnob = getNinePatch(style.getAttribute("hScrollKnob"));
			NinePatch vScroll = getNinePatch(style.getAttribute("vScroll"));
			NinePatch vScrollKnob = getNinePatch(style.getAttribute("vScrollKnob"));

			if (name == null) error("No name given for scrollpane style", style);
			if (background == null) error("No 'background' given for scrollpane style", style);
			if (hScroll == null) error("No 'hScroll' given for scrollpane style", style);
			if (hScrollKnob == null) error("No 'hScrollKnob' given for scrollpane style", style);
			if (vScroll == null) error("No 'vScroll' given for scrollpane style", style);
			if (vScrollKnob == null) error("No 'vScrollKnob' given for scrollpane style", style);

			scrollPaneStyles.put(name, new ScrollPaneStyle(background, hScroll, hScrollKnob, vScroll, vScrollKnob));
		}
	}

	private void parsePaneStyles (Element styles) {
		for (Element style : styles.getChildrenByName("pane")) {
			String name = style.getAttribute("name");
			NinePatch patch = getNinePatch(style.getAttribute("background"));

			if (name == null) error("No name given for pane style", style);
			if (patch == null) error("No 'background' given for pane style", style);

			paneStyles.put(name, new PaneStyle(patch));
		}
	}

	private void parseListStyles (Element styles) {
		for (Element style : styles.getChildrenByName("list")) {
			String name = style.getAttribute("name");
			BitmapFont font = getFont(style.getAttribute("font"));
			Color fontColorUnselected = getColor(style.getAttribute("fontColorUnselected"));
			Color fontColorSelected = getColor(style.getAttribute("fontColorSelected"));
			NinePatch selectedPatch = getNinePatch(style.getAttribute("selected"));

			if (name == null) error("No name given for list style", style);
			if (font == null) error("No font given for list style", style);
			if (fontColorUnselected == null) error("No 'fontColorUnselected' given for list style", style);
			if (fontColorSelected == null) error("No 'fontColorSelected' given for list style", style);
			if (selectedPatch == null) error("No 'selected' nine-patch given for list style", style);

			listStyles.put(name, new ListStyle(font, fontColorSelected, fontColorUnselected, selectedPatch));
		}
	}

	private void parseImageToggleButtonStyles (Element styles) {
		for (Element style : styles.getChildrenByName("imagetogglebutton")) {
			String name = style.getAttribute("name");
			NinePatch down = getNinePatch(style.getAttribute("down"));
			NinePatch up = getNinePatch(style.getAttribute("up"));

			if (name == null) error("No name given for togglebutton style", style);
			if (down == null) error("No 'down' nine-patch given for togglebutton style", style);
			if (up == null) error("No 'up' nine-patch given for togglebutton style", style);

			imageToggleButtonStyles.put(name, new ImageToggleButtonStyle(down, up));
		}
	}

	private void parseToggleButtonStyles (Element styles) {
		for (Element style : styles.getChildrenByName("togglebutton")) {
			String name = style.getAttribute("name");
			NinePatch down = getNinePatch(style.getAttribute("down"));
			NinePatch up = getNinePatch(style.getAttribute("up"));
			BitmapFont font = getFont(style.getAttribute("font"));
			Color fontColor = getColor(style.getAttribute("fontColor"));

			if (name == null) error("No name given for togglebutton style", style);
			if (down == null) error("No 'down' nine-patch given for togglebutton style", style);
			if (up == null) error("No 'up' nine-patch given for togglebutton style", style);
			if (font == null) error("No 'font' given for togglebutton style", style);
			if (fontColor == null) error("No 'fontColor' given for togglebutton style", style);

			toggleButtonStyles.put(name, new ToggleButtonStyle(font, fontColor, down, up));
		}
	}

	private void parseSliderStyles (Element styles) {
		for (Element style : styles.getChildrenByName("slider")) {
			String name = style.getAttribute("name");
			NinePatch sliderPatch = getNinePatch(style.getAttribute("slider"));
			TextureRegion knobPatch = getRegion(style.getAttribute("knob"));

			if (name == null) error("No name given for slider style", style);
			if (sliderPatch == null) error("No 'slider' nine-patch given for slider style", style);
			if (knobPatch == null) error("No 'knob' region given for slider style", style);
			sliderStyles.put(name, new SliderStyle(sliderPatch, knobPatch));
		}
	}

	private void parseCheckBoxStyles (Element styles) {
		for (Element style : styles.getChildrenByName("checkbox")) {
			String name = style.getAttribute("name");
			BitmapFont font = getFont(style.getAttribute("font"));
			Color fontColor = getColor(style.getAttribute("fontColor"));
			TextureRegion checked = getRegion(style.getAttribute("checked"));
			TextureRegion unchecked = getRegion(style.getAttribute("unchecked"));

			if (name == null) error("No name given for checkbox style", style);
			if (font == null) error("No 'font' given for checkbox style", style);
			if (fontColor == null) error("No 'fontColor' given for checkbox style", style);
			if (checked == null) error("No 'checked' region given for checkbox style", style);
			if (unchecked == null) error("No 'unchecked' region given for checkbox style", style);

			checkBoxStyles.put(name, new CheckBoxStyle(font, fontColor, checked, unchecked));
		}
	}

	private void parseImageButtonStyles (Element styles) {
		for (Element style : styles.getChildrenByName("imagebutton")) {
			String name = style.getAttribute("name");
			NinePatch down = getNinePatch(style.getAttribute("down"));
			NinePatch up = getNinePatch(style.getAttribute("up"));

			if (name == null) error("No name given for button style", style);
			if (down == null) error("No 'down' nine-patch given for button style", style);
			if (up == null) error("No 'up' nine-patch given for button style", style);

			imageButtonStyles.put(name, new ImageButtonStyle(down, up));
		}
	}

	private void parseButtonStyles (Element styles) {
		for (Element style : styles.getChildrenByName("button")) {
			String name = style.getAttribute("name");
			NinePatch down = getNinePatch(style.getAttribute("down"));
			NinePatch up = getNinePatch(style.getAttribute("up"));
			BitmapFont font = getFont(style.getAttribute("font"));
			Color fontColor = getColor(style.getAttribute("fontColor"));

			if (name == null) error("No name given for button style", style);
			if (down == null) error("No 'down' nine-patch given for button style", style);
			if (up == null) error("No 'up' nine-patch given for button style", style);
			if (font == null) error("No 'font' nine-patch given for button style", style);
			if (fontColor == null) error("No 'fontColor' given for button style", style);

			buttonStyles.put(name, new ButtonStyle(font, fontColor, down, up));
		}
	}

	private void parseLabelStyles (Element styles) {
		for (Element style : styles.getChildrenByName("label")) {
			String name = style.getAttribute("name");
			BitmapFont font = getFont(style.getAttribute("font"));
			Color fontColor = getColor(style.getAttribute("fontColor"));

			if (name == null) error("No name given for label style", style);
			if (font == null) error("No 'font' given for label style", style);
			if (fontColor == null) error("No 'fontColor' given for label style", style);

			labelStyles.put(style.getAttribute("name"), new LabelStyle(font, fontColor));
		}
	}

	private void parseLibrary (Element skin, FileHandle skinFile) {
		Element library = skin.getChildByName("library");
		parseColors(library);
		parseNinePatches(library);
		parseRegions(library);
		parseFonts(library, skinFile);
	}

	private void parseColors (Element library) {
		for (Element color : library.getChildrenByName("color")) {
			Color col = new Color(color.getFloatAttribute("r"), color.getFloatAttribute("g"), color.getFloatAttribute("b"),
				color.getFloatAttribute("a"));
			colors.put(color.getAttribute("name"), col);
		}
	}

	private void parseFonts (Element library, FileHandle skinFile) {
		for (Element font : library.getChildrenByName("font")) {
			String path = font.getAttribute("file");
			FileHandle file = skinFile.parent().child(path);
			if (!file.exists()) file = Gdx.files.internal(path);
			BitmapFont bitmapFont = new BitmapFont(file, false);
			fonts.put(font.getAttribute("name"), bitmapFont);
		}
	}

	private void parseRegions (Element library) {
		for (Element region : library.getChildrenByName("region")) {
			regions.put(region.getAttribute("name"), parseRegion(region));
		}
	}

	private void parseNinePatches (Element library) {
		for (Element ninePatch : library.getChildrenByName("ninepatch")) {
			ninePatches.put(ninePatch.getAttribute("name"), parseNinePatch(ninePatch));
		}
	}

	private NinePatch parseNinePatch (Element ninePatch) {
		TextureRegion[] regions = new TextureRegion[9];
		Array<Element> patches = ninePatch.getChildrenByName("region");
		if (regions.length != 9) error("Nine-patch definition has to have 9 regions", ninePatch);
		for (int i = 0; i < regions.length; i++) {
			regions[i] = parseRegion(patches.get(i));
		}
		return new NinePatch(regions);
	}

	private TextureRegion parseRegion (Element region) {
		return new TextureRegion(texture, region.getIntAttribute("x"), region.getIntAttribute("y"),
			region.getIntAttribute("width"), region.getIntAttribute("height"));
	}

	/** Returns a {@link Color} with the given name.
	 * @param name the name
	 * @return the Color or null */
	public Color getColor (String name) {
		return colors.get(name);
	}

	/** Adds the {@link Color} with the given name. Overwrites and previously stored Color with that name. Will not have an effect
	 * on widget styles already added to this skin!
	 * @param name the name
	 * @param color the Color */
	public void addColor (String name, Color color) {
		colors.put(name, color);
	}

	/** Returns a {@link NinePatch} with the given name.
	 * @param name the name
	 * @return the NinePatch or null */
	public NinePatch getNinePatch (String name) {
		return ninePatches.get(name);
	}

	/** Adds the {@link NinePatch} with the given name. Overwrites and previously stored NinePatch with that name. Will not have an
	 * effect on widget styles already added to this skin!
	 * @param name the name
	 * @param ninePatch the Color */
	public void addNinePatch (String name, NinePatch ninePatch) {
		ninePatches.put(name, ninePatch);
	}

	/** Returns a {@link TextureRegion} with the given name.
	 * @param name the name
	 * @return the TextureRegion or null. */
	public TextureRegion getRegion (String name) {
		return regions.get(name);
	}

	/** Adds the {@link TextureRegion} with the given name. Overwrites any previously stored TextureRegion with that name. Will not
	 * have an effect on widget styles already added to this skin!
	 * @param name the name
	 * @param region the TextureRegion */
	public void addRegion (String name, TextureRegion region) {
		regions.put(name, region);
	}

	/** Returns a {@link BitmapFont} with the given name.
	 * @param name the name
	 * @return the BitmapFont or null. */
	public BitmapFont getFont (String name) {
		return fonts.get(name);
	}

	/** Adds the {@link BitmapFont} with the given name. Overwrites any previously stored BitmapFont with that name. Will not have
	 * an effect on widget styles already added to this skin!
	 * @param name the name
	 * @param font the BitmapFont */
	public void addFont (String name, BitmapFont font) {
		fonts.put(name, font);
	}

	/** Returns the {@link ButtonStyle} with the given name.
	 * @param name the name
	 * @return the ButtonStyle or null */
	public ButtonStyle getButtonStyle (String name) {
		return buttonStyles.get(name);
	}

	/** Adds the {@link ButtonStyle} with the given name. Overwrites any previously stored style with that name. Will not have an
	 * effect on widgets already created!
	 * @param name the name of the style
	 * @param style the ButtonStyle */
	public void addButtonStyle (String name, ButtonStyle style) {
		buttonStyles.put(name, style);
	}

	/** Returns the {@link ImageButtonStyle} with the given name.
	 * @param name the name
	 * @return the ImageButtonStyle or null */
	public ImageButtonStyle getImageButtonStyle (String name) {
		return imageButtonStyles.get(name);
	}

	/** Adds the {@link ImageButtonStyle} with the given name. Overwrites any previously stored style with that name. Will not have
	 * an effect on widgets already created!
	 * @param name the name of the style
	 * @param style the style */
	public void addImageButtonStyle (String name, ImageButtonStyle style) {
		imageButtonStyles.put(name, style);
	}

	/** Returns the {@link ImageToggleButtonStyle} with the given name.
	 * @param name the name
	 * @return the ImageToggleButtonStyle or null */
	public ImageToggleButtonStyle getImageToggleButtonStyle (String name) {
		return imageToggleButtonStyles.get(name);
	}

	/** Adds the {@link ImageToggleButtonStyle} with the given name. Overwrites any previously stored style with that name. Will not
	 * have an effect on widgets already created!
	 * @param name the name of the style
	 * @param style the style */
	public void addImageToggleButtonStyle (String name, ImageToggleButtonStyle style) {
		imageToggleButtonStyles.put(name, style);
	}

	/** Returns the {@link CheckBoxStyle} with the given name.
	 * @param name the name
	 * @return the CheckBoxStyle or null */
	public CheckBoxStyle getCheckBoxStyle (String name) {
		return checkBoxStyles.get(name);
	}

	/** Adds the {@link CheckBoxStyle} with the given name. Overwrites any previously stored style with that name. Will not have an
	 * effect on widgets already created!
	 * @param name the name of the style
	 * @param style the style */
	public void addCheckBoxStyle (String name, CheckBoxStyle style) {
		checkBoxStyles.put(name, style);
	}

	/** Returns the {@link ComboBoxStyle} with the given name.
	 * @param name the name
	 * @return the ComboBoxStyle or null */
	public ComboBoxStyle getComboBoxStyle (String name) {
		return comboBoxStyles.get(name);
	}

	/** Adds the {@link ComboBoxStyle} with the given name. Overwrites any previously stored style with that name. Will not have an
	 * effect on widgets already created!
	 * @param name the name of the style
	 * @param style the style */
	public void addComboBoxStyle (String name, ComboBoxStyle style) {
		comboBoxStyles.put(name, style);
	}

	/** Returns the {@link LabelStyle} with the given name.
	 * @param name the name
	 * @return the LabelStyle or null */
	public LabelStyle getLabelStyle (String name) {
		return labelStyles.get(name);
	}

	/** Adds the {@link LabelStyle} with the given name. Overwrites any previously stored style with that name. Will not have an
	 * effect on widgets already created!
	 * @param name the name of the style
	 * @param style the style */
	public void addLabelStyle (String name, LabelStyle style) {
		labelStyles.put(name, style);
	}

	/** Returns the {@link ListStyle} with the given name.
	 * @param name the name
	 * @return the ListStyle or null */
	public ListStyle getListStyle (String name) {
		return listStyles.get(name);
	}

	/** Adds the {@link ListStyle} with the given name. Overwrites any previously stored style with that name. Will not have an
	 * effect on widgets already created!
	 * @param name the name of the style
	 * @param style the style */
	public void addListStyle (String name, ListStyle style) {
		listStyles.put(name, style);
	}

	/** Returns the {@link PaneStyle} with the given name.
	 * @param name the name
	 * @return the PaneStyle or null */
	public PaneStyle getPaneStyle (String name) {
		return paneStyles.get(name);
	}

	/** Adds the {@link PaneStyle} with the given name. Overwrites any previously stored style with that name. Will not have an
	 * effect on widgets already created!
	 * @param name the name of the style
	 * @param style the style */
	public void addPaneStyle (String name, PaneStyle style) {
		paneStyles.put(name, style);
	}

	/** Returns the {@link ScrollPaneStyle} with the given name.
	 * @param name the name
	 * @return the ScrollPaneStyle or null */
	public ScrollPaneStyle getScrollPaneStyle (String name) {
		return scrollPaneStyles.get(name);
	}

	/** Adds the {@link ScrollPaneStyle} with the given name. Overwrites any previously stored style with that name. Will not have
	 * an effect on widgets already created!
	 * @param name the name of the style
	 * @param style the style */
	public void addScrollPaneStyle (String name, ScrollPaneStyle style) {
		scrollPaneStyles.put(name, style);
	}

	/** Returns the {@link SliderStyle} with the given name.
	 * @param name the name
	 * @return the SliderStyle or null */
	public SliderStyle getSliderStyle (String name) {
		return sliderStyles.get(name);
	}

	/** Adds the {@link SliderStyle} with the given name. Overwrites any previously stored style with that name. Will not have an
	 * effect on widgets already created!
	 * @param name the name of the style
	 * @param style the style */
	public void addSliderStyle (String name, SliderStyle style) {
		sliderStyles.put(name, style);
	}

	/** Returns the {@link SplitPaneStyle} with the given name.
	 * @param name the name
	 * @return the SplitPaneStyle or null */
	public SplitPaneStyle getSplitPaneStyle (String name) {
		return splitPaneStyles.get(name);
	}

	/** Adds the {@link SplitPaneStyle} with the given name. Overwrites any previously stored style with that name. Will not have an
	 * effect on widgets already created!
	 * @param name the name of the style
	 * @param style the style */
	public void addSplitPaneStyle (String name, SplitPaneStyle style) {
		splitPaneStyles.put(name, style);
	}

	/** Returns the {@link TextFieldStyle} with the given name.
	 * @param name the name
	 * @return the TextFieldStyle or null */
	public TextFieldStyle getTextFieldStyle (String name) {
		return textFieldStyles.get(name);
	}

	/** Adds the {@link TextFieldStyle} with the given name. Overwrites any previously stored style with that name. Will not have an
	 * effect on widgets already created!
	 * @param name the name of the style
	 * @param style the style */
	public void addTextFieldStyle (String name, TextFieldStyle style) {
		textFieldStyles.put(name, style);
	}

	/** Returns the {@link ToggleButtonStyle} with the given name.
	 * @param name the name
	 * @return the ToggleButtonStyle or null */
	public ToggleButtonStyle getToggleButtonStyle (String name) {
		return toggleButtonStyles.get(name);
	}

	/** Adds the {@link ToggleButtonStyle} with the given name. Overwrites any previously stored style with that name. Will not have
	 * an effect on widgets already created!
	 * @param name the name of the style
	 * @param style the style */
	public void addToggleButtonStyle (String name, ToggleButtonStyle style) {
		toggleButtonStyles.put(name, style);
	}

	/** Returns the {@link WindowStyle} with the given name.
	 * @param name the name
	 * @return the WindowStyle or null */
	public WindowStyle getWindowStyle (String name) {
		return windowStyles.get(name);
	}

	/** Adds the {@link WindowStyle} with the given name. Overwrites any previously stored style with that name. Will not have an
	 * effect on widgets already created!
	 * @param name the name of the style
	 * @param style the style */
	public void addWindowStyle (String name, WindowStyle style) {
		windowStyles.put(name, style);
	}

	/** Creates a new {@link ImageButton}, using the style with 'default'.
	 * @param name the name of the button
	 * @param image the image to be displayed on the button
	 * @return the ImageButton */
	public ImageButton newImageButton (String name, TextureRegion image) {
		return newImageButton(name, image, "default");
	}

	/** Creates a new {@link ImageButton}, using the style with 'default'.
	 * @param name the name of the button
	 * @param image the image to be displayed on the button
	 * @param style the name of the {@link ImageButtonStyle}
	 * @return the ImageButton */
	public ImageButton newImageButton (String name, TextureRegion image, String style) {
		return new ImageButton(name, image, imageButtonStyles.get(style));
	}

	/** Creates a new {@link ImageToggleButton}, using the style with 'default'.
	 * @param name the name of the button
	 * @param image the image to be displayed on the button
	 * @return the ImageToggleButton */
	public ImageToggleButton newImageToggleButton (String name, TextureRegion image) {
		return newImageToggleButton(name, image, "default");
	}

	/** Creates a new {@link ImageToggleButton}, using the style with 'default'.
	 * @param name the name of the button
	 * @param image the image to be displayed on the button
	 * @param style the name of the {@link ImageToggleButtonStyle}
	 * @return the ImageButton */
	public ImageToggleButton newImageToggleButton (String name, TextureRegion image, String style) {
		return new ImageToggleButton(name, image, imageToggleButtonStyles.get(style));
	}

	/** Creates a new {@link Button}, using the style with 'default'.
	 * @param name the name of the button
	 * @param label the label to be displayed on the button, can be multiline
	 * @return the Button */
	public Button newButton (String name, String label) {
		return newButton(name, label, "default");
	}

	/** Creates a new {@link Button}, using the given style.
	 * @param name the name of the Button
	 * @param label the label to be displayed on the button, can be multiline
	 * @param style the name of the {@link ButtonStyle}
	 * @return the Button */
	public Button newButton (String name, String label, String style) {
		return new Button(name, label, buttonStyles.get(style));
	}

	/** Creates a new {@link ToggleButton}, using the style named 'default'.
	 * @param name the name of the toggle button
	 * @param label the label to be displayed on the button, can be multline
	 * @return the ToggleButton */
	public ToggleButton newToggleButton (String name, String label) {
		return newToggleButton(name, label, "default");
	}

	/** Creates a new {@link ToggleButton}, using the given style.
	 * @param name the name of the toggle button
	 * @param label the label to be displayed on the button, can be multiline
	 * @param style the name of the style
	 * @return the ToggleButton */
	public ToggleButton newToggleButton (String name, String label, String style) {
		return new ToggleButton(name, label, toggleButtonStyles.get(style));
	}

	/** Creates a new {@link CheckBox}, using the style named 'default'.
	 * @param name the name of the check box
	 * @param label the label to be displayed beside the checkbox, singleline
	 * @return the CheckBox */
	public CheckBox newCheckBox (String name, String label) {
		return new CheckBox(name, label, checkBoxStyles.get("default"));
	}

	/** Creates a new {@link CheckBox}, using the given style.
	 * @param name the name of the check box
	 * @param label the label to be displayed beside the checkbox, singleline
	 * @param style the name of the style
	 * @return the CheckBox */
	public CheckBox newCheckBox (String name, String label, String style) {
		return new CheckBox(name, label, checkBoxStyles.get(style));
	}

	/** Creates a new {@link Label}, using the style named 'default'
	 * @param name the name of the label
	 * @param label the text to be displayed, can be multiline
	 * @return the Label */
	public Label newLabel (String name, String label) {
		return new Label(name, label, labelStyles.get("default"));
	}

	/** Creates a new {@link Label}, using the given style.
	 * @param name the name of the label
	 * @param label the text to be displayed, can be multiline
	 * @param style the name of the style
	 * @return the Label */
	public Label newLabel (String name, String label, String style) {
		return new Label(name, label, labelStyles.get(style));
	}

	/** Creates a new {@link Slider}, using the style named 'default'
	 * @param name the name of the slider
	 * @param prefWidth the preferred width of the slider
	 * @param min the mininum value the slider can take on
	 * @param max the maximum value the slider can take on
	 * @param step the step size between individual slider values, e.g. 1 or 0.1 etc.
	 * @return the Slider */
	public Slider newSlider (String name, float prefWidth, float min, float max, float step) {
		return newSlider(name, prefWidth, min, max, step, "default");
	}

	/** Creates a new {@link Slider}, using the given style.
	 * @param name the name of the slider
	 * @param prefWidth the preferred width of the slider
	 * @param min the mininum value the slider can take on
	 * @param max the maximum value the slider can take on
	 * @param step the step size between individual slider values, e.g. 1 or 0.1 etc.
	 * @param style the name of the style.
	 * @return the Slider */
	public Slider newSlider (String name, float prefWidth, float min, float max, float step, String style) {
		return new Slider(name, prefWidth, min, max, step, sliderStyles.get(style));
	}

	/** Creates a new {@link List}, using the style named 'default'
	 * @param name the name of the list
	 * @param entries the String entries to be displayed in the list, singleline
	 * @return the List */
	public List newList (String name, String[] entries) {
		return newList(name, entries, "default");
	}

	/** Creates a new {@link List}, using the given style.
	 * @param name the name of the list
	 * @param entries the String entries to be displayed in the list, singleline
	 * @param style the name of the style
	 * @return the List */
	public List newList (String name, String[] entries, String style) {
		return new List(name, entries, listStyles.get(style));
	}

	/** Creates a new {@link Pane}, using the style named 'default'
	 * @param name the name of the pane
	 * @param stage the {@link Stage} this pane will be added to, needed for clipping
	 * @param prefWidth the preferred width
	 * @param prefHeight the preferred height
	 * @return the Pane */
	public Pane newPane (String name, Stage stage, int prefWidth, int prefHeight) {
		return newPane(name, stage, prefWidth, prefHeight, "default");
	}

	/** Creates a new {@link Pane}, using the given style.
	 * @param name the name of the pane
	 * @param stage the {@link Stage} this pane will be added to, needed for clipping
	 * @param prefWidth the preferred width
	 * @param prefHeight the preferred height
	 * @param style the name of the style
	 * @return the Pane */
	public Pane newPane (String name, Stage stage, int prefWidth, int prefHeight, String style) {
		return new Pane(name, stage, prefWidth, prefHeight, paneStyles.get(style));
	}

	/** Creates a new {@link ScrollPane}, using the style named 'default'
	 * @param name the name of the scroll pane
	 * @param stage the {@link Stage} this scroll pane will be added to, needed for clipping
	 * @param widget the {@link Actor} this scroll pane should contain
	 * @param prefWidth the preferred width
	 * @param prefHeight the preferred height
	 * @return the ScrollPane */
	public ScrollPane newScrollPane (String name, Stage stage, Actor widget, int prefWidth, int prefHeight) {
		return newScrollPane(name, stage, widget, prefWidth, prefHeight, "default");
	}

	/** Creates a new {@link ScrollPane}, using the given style named.
	 * @param name the name of the scroll pane
	 * @param stage the {@link Stage} this scroll pane will be added to, needed for clipping
	 * @param widget the {@link Actor} this scroll pane should contain
	 * @param prefWidth the preferred width
	 * @param prefHeight the preferred height
	 * @param style the name of the style
	 * @return the ScrollPane */
	public ScrollPane newScrollPane (String name, Stage stage, Actor widget, int prefWidth, int prefHeight, String style) {
		return new ScrollPane(name, stage, widget, prefWidth, prefHeight, scrollPaneStyles.get(style));
	}

	/** Creates a new {@link SplitPane}, using the given style. There is no 'default' version of this method as a vertical split
	 * pane is likely to use a different style than a horizontal split pane.
	 * 
	 * @param name the name of the split pane
	 * @param stage the {@link Stage} this split pane will be added to, needed for clipping
	 * @param firstWidget the first widget (left or top)
	 * @param secondWidget the second widget (right or bottom)
	 * @param vertical whether this split pane is vertically aligned or not (horizontal)
	 * @param prefWidth the preferred width
	 * @param prefHeight the preferred height
	 * @param style the name of the style
	 * @return the SplitPane */
	public SplitPane newSplitPane (String name, Stage stage, Actor firstWidget, Actor secondWidget, boolean vertical,
		int prefWidth, int prefHeight, String style) {
		return new SplitPane(name, stage, firstWidget, secondWidget, vertical, prefWidth, prefHeight, splitPaneStyles.get(style));
	}

	/** Creates a new {@link TextField}, using the style named 'default'.
	 * @param name the name of the text field
	 * @param prefWidth the preferred width
	 * @return the TextField */
	public TextField newTextField (String name, float prefWidth) {
		return newTextField(name, prefWidth, "default");
	}

	/** Creates a new {@link TextField}, using the given style.
	 * @param name the name of the text field
	 * @param prefWidth the preferred width
	 * @param style the name of the style
	 * @return the TextField */
	public TextField newTextField (String name, float prefWidth, String style) {
		return new TextField(name, prefWidth, textFieldStyles.get(style));
	}

	/** Creates a new {@link ComboBox}, using the style named 'default'
	 * @param name the name of the combo box
	 * @param entries the String entries to be displayed in the combobox
	 * @param stage the {@link Stage} this combobox will be added to, needed for clipping and displaying the selection list
	 * @return the ComboBox */
	public ComboBox newComboBox (String name, String[] entries, Stage stage) {
		return newComboBox(name, entries, stage, "default");
	}

	/** Creates a new {@link ComboBox}, using the given style
	 * @param name the name of the combo box
	 * @param entries the String entries to be displayed in the combobox
	 * @param stage the {@link Stage} this combobox will be added to, needed for clipping and displaying the selection list
	 * @param style the name of the style
	 * @return the ComboBox */
	public ComboBox newComboBox (String name, String[] entries, Stage stage, String style) {
		return new ComboBox(name, entries, stage, comboBoxStyles.get(style));
	}

	/** Creates a new {@link Window}, using the style named 'default'
	 * @param name the name of the window
	 * @param stage the {@link Stage} this window will be added to, needed for clipping
	 * @param title the title of this window
	 * @param width the width of this window
	 * @param height the height of this window
	 * @return the Window */
	public Window newWindow (String name, Stage stage, String title, int width, int height) {
		return newWindow(name, stage, title, width, height, "default");
	}

	/** Creates a new {@link Window}, using the given style named.
	 * @param name the name of the window
	 * @param stage the {@link Stage} this window will be added to, needed for clipping
	 * @param title the title of this window
	 * @param width the width of this window
	 * @param height the height of this window
	 * @param style the name of the style
	 * @return the Window */
	public Window newWindow (String name, Stage stage, String title, int width, int height, String style) {
		return new Window(name, stage, title, width, height, windowStyles.get(style));
	}

	/** Disposes the {@link Texture} and all {@link BitmapFont} instances of this Skin. */
	@Override
	public void dispose () {
		texture.dispose();
		for (BitmapFont font : fonts.values()) {
			font.dispose();
		}
	}

	/** @return the {@link Texture} containing all {@link NinePatch} and {@link TextureRegion} pixels of this Skin. */
	public Texture getTexture () {
		return texture;
	}
}
