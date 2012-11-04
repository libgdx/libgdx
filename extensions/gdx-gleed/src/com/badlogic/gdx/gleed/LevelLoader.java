/*******************************************************************************
 * Copyright 2012 David Saltares
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

package com.badlogic.gdx.gleed;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * @author David Saltares
 * @date 03/11/2012
 * 
 * @brief GLEED2D level loader to use along the AssetManager system
 */
public class LevelLoader extends AsynchronousAssetLoader<Level, LevelLoader.GLEEDParameter > implements Disposable {

	static public class GLEEDParameter extends AssetLoaderParameters<Level> {
	}

	static private Logger s_logger = new Logger("LevelLoader");
	
	private Level m_level = null;
	private String m_pathRoot = "data";
	private TextureAtlas m_atlas = null;
	private String m_atlasFile = null;
	private AssetManager m_assetManager;
	
	/**
	 * @param loggingLevel amount of debug information the loader will output
	 */
	public static void setLoggingLevel(int loggingLevel) {
		s_logger.setLevel(loggingLevel);
	}
	
	/**
	 * @param resolver file handle resolver to find the proper file
	 * 
	 */
	public LevelLoader(FileHandleResolver resolver) {
		super(resolver);
	}

	/**
	 * Loads a level in an Asynchronous manner
	 * 
	 * @param manager asset manager to use
	 * @param fileName level file to load
	 * @param parameters custom params (not used)
	 */
	@Override
	public void loadAsync(AssetManager manager, String fileName, GLEEDParameter parameter) {
		m_assetManager = manager;
		
		s_logger.info("loading file " + fileName);
		
		try {
			// Parse xml document
			XmlReader reader = new XmlReader();
			Element root = reader.parse(Gdx.files.internal(fileName));
			
			// Create level and load properties
			s_logger.info("loading level properties");
			if (m_level == null) {
				m_level = new Level(fileName);
				m_level.properties.load(root);
			}
			
			// Load atlas if necessary
			if (!m_atlasFile.isEmpty()) {
				s_logger.info("fetching texture atlas " + m_atlasFile);
				m_atlas = manager.get(m_atlasFile, TextureAtlas.class);
			}
			
			// Load layers
			s_logger.info("loading layers");
			Array<Element> layerElements = root.getChildByName("Layers").getChildrenByName("Layer");
			
			for (int i = 0; i < layerElements.size; ++i) {
				Element layerElement = layerElements.get(i);
				loadLayer(layerElement);
			}
			
		} catch (Exception e) {
			s_logger.error("error loading file " + fileName + " " + e.getMessage());
		}
	}

	/**
	 * Loads a level in an Synchronous manner
	 * 
	 * @param manager asset manager to use
	 * @param fileName level file to load
	 * @param parameters custom params (not used)
	 */
	@Override
	public Level loadSync(AssetManager manager, String fileName, GLEEDParameter parameter) {
		return m_level;
	}

	/**
	 * Gets TextureAtlas or Texture assets required by the level
	 * 
	 * @param fileName to get dependencies from
	 * @param parameters custom params (not used)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Array<AssetDescriptor> getDependencies(String fileName, GLEEDParameter parameter) {

		s_logger.info("getting asset dependencies for " + fileName);
		Array<AssetDescriptor> dependencies = new Array<AssetDescriptor>();
		
		try {
			// Parse xml document
			XmlReader reader = new XmlReader();
			Element root = reader.parse(Gdx.files.internal(fileName));

			if (m_level == null) {
				m_level = new Level(fileName);
				m_level.properties.load(root);
			}
			
			// Check texture atlass
			m_atlasFile = m_level.properties.getString("atlas", "");
			m_pathRoot = m_level.properties.getString("assetRoot", "data");
			
			if (!m_atlasFile.isEmpty()) {
				s_logger.info("texture atlas dependency " + m_atlasFile);
				dependencies.add(new AssetDescriptor(m_atlasFile, TextureAtlas.class));
			}
			else {
				s_logger.info("textures asset folder " + m_pathRoot);
				Array<Element> elements = root.getChildrenByNameRecursively("Item");
				
				for (int i = 0; i < elements.size; ++i) {
					Element element = elements.get(i);
					
					if (element.getAttribute("xsi:type", "").equals("TextureItem")) {
						String[] pathParts = element.getChildByName("texture_filename").getText().split("\\\\");
						s_logger.info("texture dependency " + m_pathRoot + "/" + pathParts[pathParts.length - 1]);
						dependencies.add(new AssetDescriptor(m_pathRoot + "/" + pathParts[pathParts.length - 1], Texture.class));
					}
				}
			}
			
		} catch (Exception e) {
			s_logger.error("error loading asset dependencies " + fileName + " " + e.getMessage());
		}
		
		return dependencies;
	}
	
	private void loadLayer(Element element) {
		Layer layer = new Layer();
		
		layer.properties.load(element);
		
		layer.name = element.getAttribute("Name", "");
		layer.visible = Boolean.parseBoolean(element.getAttribute("Visible", "true"));
		
		s_logger.info("loading layer " + layer.name);
		Array<Element> items = element.getChildByName("Items").getChildrenByName("Item");
		
		for (int i = 0; i < items.size; ++i) {
			Element item = items.get(i);
			String type = item.getAttribute("xsi:type");
			
			if (type.equals("TextureItem")) {
				TextureElement texture = new TextureElement();
				loadElement(texture, item);
				loadTextureElement(texture, item);
				layer.textures.add(texture);
			}
			else if (type.equals("PathItem")) {
				PathElement path = new PathElement();
				loadElement(path, item);
				loadPathElement(path, item);
				layer.objects.add(path);
			}
			else if (type.equals("RectangleItem")) {
				RectangleElement rectangle = new RectangleElement();
				loadElement(rectangle, item);
				loadRectangleElement(rectangle, item);
				layer.objects.add(rectangle);
				
			}
			else if (type.equals("CircleItem")) {
				CircleElement circle = new CircleElement();
				loadElement(circle, item);
				loadCircleElement(circle, item);
				layer.objects.add(circle);
			}
		}
		
		m_level.layers.add(layer);
	}
	
	private void loadElement(LevelObject levelObject, Element element) {
		levelObject.name = element.getAttribute("Name", "");
		levelObject.visible = Boolean.parseBoolean(element.getAttribute("Visible", "true"));
		levelObject.properties.load(element);
		
		s_logger.info("loading element " + levelObject.name);
	}
	
	private void loadTextureElement(TextureElement texture, Element item) {

		Element positionElement = item.getChildByName("Position");
		texture.position.x = Float.parseFloat(positionElement.getChildByName("X").getText());
		texture.position.y = -Float.parseFloat(positionElement.getChildByName("Y").getText());
		
		Element origin = item.getChildByName("Origin");
		texture.originX = Float.parseFloat(origin.getChildByName("X").getText());
		texture.originY = Float.parseFloat(origin.getChildByName("Y").getText());
		
		Element scale = item.getChildByName("Scale");
		texture.scaleX = Float.parseFloat(scale.getChildByName("X").getText());
		texture.scaleY = Float.parseFloat(scale.getChildByName("Y").getText());
		
		Element colorElement = item.getChildByName("TintColor");
		texture.color.r = Float.parseFloat(colorElement.getChildByName("R").getText()) / 255.0f;
		texture.color.g = Float.parseFloat(colorElement.getChildByName("G").getText()) / 255.0f;
		texture.color.b = Float.parseFloat(colorElement.getChildByName("B").getText()) / 255.0f;
		texture.color.a = Float.parseFloat(colorElement.getChildByName("A").getText()) / 255.0f;
		
		texture.rotation = Float.parseFloat(item.getChildByName("Rotation").getText());
		
		if (m_atlasFile.isEmpty()) {
			String[] pathParts = item.getChildByName("texture_filename").getText().split("\\\\");
			texture.path = m_pathRoot + "/" + pathParts[pathParts.length - 1];
			texture.region.setRegion(m_assetManager.get(texture.path, Texture.class));
		}
		else {
			String[] assetParts = item.getChildByName("asset_name").getText().split("\\\\");
			texture.path = assetParts[assetParts.length - 1];
			TextureRegion region = m_atlas.findRegion(texture.path);
			texture.region.setRegion(region);
		}
		
		texture.region.flip(Boolean.parseBoolean(item.getChildByName("FlipHorizontally").getText()),
							Boolean.parseBoolean(item.getChildByName("FlipVertically").getText()));
	}
	
	private void loadCircleElement(CircleElement circle, Element item) {
		Element position = item.getChildByName("Position");
		
		Element colorElement = item.getChildByName("FillColor");
		circle.color.r = Float.parseFloat(colorElement.getChildByName("R").getText()) / 255.0f;
		circle.color.g = Float.parseFloat(colorElement.getChildByName("G").getText()) / 255.0f;
		circle.color.b = Float.parseFloat(colorElement.getChildByName("B").getText()) / 255.0f;
		circle.color.a = Float.parseFloat(colorElement.getChildByName("A").getText()) / 255.0f;
		
		circle.circle = new Circle(new Vector2(Float.parseFloat(position.getChildByName("X").getText()),
											   -Float.parseFloat(position.getChildByName("Y").getText())),
											   Float.parseFloat(item.getChildByName("Radius").getText()));
	}
	
	private void loadRectangleElement(RectangleElement rectangle, Element item) {
		Element position = item.getChildByName("Position");
		
		Element colorElement = item.getChildByName("FillColor");
		rectangle.color.r = Float.parseFloat(colorElement.getChildByName("R").getText()) / 255.0f;
		rectangle.color.g = Float.parseFloat(colorElement.getChildByName("G").getText()) / 255.0f;
		rectangle.color.b = Float.parseFloat(colorElement.getChildByName("B").getText()) / 255.0f;
		rectangle.color.a = Float.parseFloat(colorElement.getChildByName("A").getText()) / 255.0f;
		
		rectangle.rectangle.x = Float.parseFloat(position.getChildByName("X").getText());
		rectangle.rectangle.y = -Float.parseFloat(position.getChildByName("Y").getText());
		rectangle.rectangle.width = Float.parseFloat(item.getChildByName("Width").getText());
		rectangle.rectangle.height = Float.parseFloat(item.getChildByName("Height").getText());
	}
	
	private void loadPathElement(PathElement path, Element item) {
		Array<Element> pointElements = item.getChildByName("WorldPoints").getChildrenByName("Vector2");
		float[] vertices = new float[pointElements.size * 2];
		
		for (int j = 0; j < pointElements.size; ++j) {
			Element pointElement = pointElements.get(j);
			vertices[j * 2] = Float.parseFloat(pointElement.getChildByName("X").getText());
			vertices[j * 2 + 1] = -Float.parseFloat(pointElement.getChildByName("Y").getText());
		}
		
		path.polygon = new Polygon(vertices);
		
		path.lineWidth = Integer.parseInt(item.getChildByName("LineWidth").getText());
		
		Element colorElement = item.getChildByName("LineColor");
		path.color.r = Float.parseFloat(colorElement.getChildByName("R").getText()) / 255.0f;
		path.color.g = Float.parseFloat(colorElement.getChildByName("G").getText()) / 255.0f;
		path.color.b = Float.parseFloat(colorElement.getChildByName("B").getText()) / 255.0f;
		path.color.a = Float.parseFloat(colorElement.getChildByName("A").getText()) / 255.0f;
	}
	
	@Override
	public void dispose() {
		s_logger.info("disposing level assets " + m_level.file);
		
		for (int i = 0; i < m_level.layers.size; ++i) {
			Array<TextureElement> textures = m_level.layers.get(i).textures;
			
			for (int j = 0; j < textures.size; ++j) {
				TextureElement texture = textures.get(j);
				
				if (m_assetManager.isLoaded(texture.path, Texture.class)) {
					m_assetManager.unload(texture.path);
				}
			}
		}
	}
}
