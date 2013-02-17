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

package com.badlogic.gdx.maps.gleed;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.CircleMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * @author David Saltares
 * 
 * @brief asynchronously Loads GLEED formatted maps 
 *
 */
public class GleedMapLoader  extends AsynchronousAssetLoader<Map, GleedMapLoader.Parameters > {

	static public class Parameters extends AssetLoaderParameters<Map> {
	}
	
	private String atlasFile = "";
	private TextureAtlas atlas = null;
	private String pathRoot = "data";
	private Map map = null;
	private AssetManager assetManager = null;
	
	/**
	 * @param resolver
	 */
	public GleedMapLoader (FileHandleResolver resolver) {
		super(resolver);
	}
	
	/**
	 * Asynchronously loads a GleedMap
	 * 
	 * @params manager asset manager to load the map
	 * @params fileName gleed map file
	 * @params parameters additional parameters, not used for now
	 */
	@Override
	public void loadAsync (AssetManager manager, String fileName, Parameters parameter) {
		assetManager = manager;
		
		try {
			XmlReader reader = new XmlReader();
			Element root = reader.parse(Gdx.files.internal(fileName));
			
			if (map == null) {
				map = new Map();
				loadProperties(root, map.getProperties());
			}
			
			if (!atlasFile.isEmpty()) {
				atlas = manager.get(atlasFile, TextureAtlas.class);
			}
			
			Array<Element> layerElements = root.getChildByName("Layers").getChildrenByName("Layer");
			
			for (int i = 0; i < layerElements.size; ++i) {
				Element layerElement = layerElements.get(i);
				loadLayer(layerElement);
			}
			
		} catch (Exception e) {
			throw new GdxRuntimeException("Couldn't load Gleed map '" + fileName + "'", e);
		}
	}
	
	/**
	 * Synchronously loads a GleedMap
	 * 
	 * @params manager asset manager to load the map
	 * @params fileName gleed map file
	 * @params parameters additional parameters, not used for now
	 * 
	 * @return gleed map
	 */
	@Override
	public Map loadSync (AssetManager manager, String fileName, Parameters parameter) {
		return map;
	}
	
	/**
	 * @params fileName gleed map file
	 * @params parameters additional parameters, not used for now
	 * 
	 * @return map dependencies (either texture atlas or several textures)
	 */
	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, Parameters parameter) {
		Array<AssetDescriptor> dependencies = new Array<AssetDescriptor>();
		
		try {
			XmlReader reader = new XmlReader();
			Element root = reader.parse(Gdx.files.internal(fileName));
			
			if (map == null) {
				map = new Map();
				
				loadProperties(root, map.getProperties());
			}
			
			MapProperties properties = map.getProperties();
			
			atlasFile = properties.getAsString("atlas", "");
			pathRoot = properties.getAsString("assetRoot", "data");
			
			if (!atlasFile.isEmpty()) {
				dependencies.add(new AssetDescriptor(atlasFile, TextureAtlas.class));
			}
			else {
				Array<Element> elements = root.getChildrenByNameRecursively("Item");
				
				for (int i = 0; i < elements.size; ++i) {
					Element element = elements.get(i);
					
					if (element.getAttribute("xsi:type", "").equals("TextureItem")) {
						String[] pathParts = element.getChildByName("texture_filename").getText().split("\\\\");
						dependencies.add(new AssetDescriptor(pathRoot + "/" + pathParts[pathParts.length - 1], Texture.class));
					}
				}
			}
			
		} catch (Exception e) {
			throw new GdxRuntimeException("Couldn't load dependencies of Gleed map '" + fileName + "'", e);
		}
		
		return dependencies;
	}
	
	private Color loadColor(Element colorElement) {
		return new Color(Float.parseFloat(colorElement.getChildByName("R").getText()) / 255.0f,
							  Float.parseFloat(colorElement.getChildByName("G").getText()) / 255.0f,
							  Float.parseFloat(colorElement.getChildByName("B").getText()) / 255.0f,
							  Float.parseFloat(colorElement.getChildByName("A").getText()) / 255.0f);
	}
	
	private void loadProperties(Element element, MapProperties properties) {
		Element customProperty = element.getChildByName("CustomProperties");
		 
		if (customProperty != null) {
			Array<Element> propertiesElements = customProperty.getChildrenByName("Property");
			
			for (int i = 0; i < propertiesElements.size; ++i) {
				Element property = propertiesElements.get(i);
				String type = property.getAttribute("Type");
				
				if (type == null) {
					continue;
				}
				
				if (type.equals("string")) {
					properties.put(property.getAttribute("Name"), property.getChildByName("string").getText());
				}
				else if (type.equals("bool")) {
					properties.put(property.getAttribute("Name"), Boolean.parseBoolean(property.getChildByName("boolean").getText()));
				}
				else if (type.equals("Vector2")) {
					Element vectorElement = property.getChildByName("Vector2");
					Vector2 v = new Vector2(Float.parseFloat(vectorElement.getChildByName("X").getText()),
													Float.parseFloat(vectorElement.getChildByName("Y").getText()));
					
					properties.put(property.getAttribute("Name"), v);
				}
				else if (type.equals("Color")) {
					Element colorElement = property.getChildByName("Color");
					Color c = new Color(Float.parseFloat(colorElement.getChildByName("R").getText()) / 255.0f,
										Float.parseFloat(colorElement.getChildByName("G").getText()) / 255.0f,
										Float.parseFloat(colorElement.getChildByName("B").getText()) / 255.0f,
										Float.parseFloat(colorElement.getChildByName("A").getText()) / 255.0f);
					
					properties.put(property.getAttribute("Name"), c);
				}
			}
		}
	}
	
	private void loadLayer(Element element) {
		MapLayer layer = new MapLayer();
		
		loadProperties(element, layer.getProperties());
		
		layer.setName(element.getAttribute("Name", ""));
		layer.setVisible(Boolean.parseBoolean(element.getAttribute("Visible", "true")));
		
		Array<Element> items = element.getChildByName("Items").getChildrenByName("Item");
		
		for (int i = 0; i < items.size; ++i) {
			Element item = items.get(i);
			String type = item.getAttribute("xsi:type");
			MapObject mapObject;;
			
			if (type.equals("TextureItem")) {
				mapObject = loadTexture(item);
			}
			else if (type.equals("PathItem")) {
				if (isPolygon(item)) {
					mapObject = loadPolygon(item);
				}
				else {
					mapObject = loadPolyline(item);
				}
			}
			else if (type.equals("RectangleItem")) {
				mapObject = loadRectangle(item);
				
			}
			else if (type.equals("CircleItem")) {
				mapObject = loadCircle(item);
			}
			else {
				continue;
			}
			
			layer.getObjects().addObject(mapObject);
		}
		
		map.getLayers().addLayer(layer);
	}
	
	private void loadObject(Element element, MapObject mapObject) {
		mapObject.setName(element.getAttribute("Name", ""));
		mapObject.setVisible(Boolean.parseBoolean(element.getAttribute("Visible", "true")));
		loadProperties(element, mapObject.getProperties());
	}
	
	private TextureMapObject loadTexture(Element item) {
		
		TextureMapObject texture = new TextureMapObject();
		
		loadObject(item, texture);
		
		texture.setColor(loadColor(item.getChildByName("TintColor")));
		
		TextureRegion region;
		
		if (atlasFile.isEmpty()) {
			String[] pathParts = item.getChildByName("texture_filename").getText().split("\\\\");
			region = new TextureRegion(assetManager.get(pathRoot + "/" + pathParts[pathParts.length - 1], Texture.class));
		}
		else {
			String[] assetParts = item.getChildByName("asset_name").getText().split("\\\\");
			region = new TextureRegion(atlas.findRegion(assetParts[assetParts.length - 1]));
		}
		
		region.flip(Boolean.parseBoolean(item.getChildByName("FlipHorizontally").getText()),
						Boolean.parseBoolean(item.getChildByName("FlipVertically").getText()));
		
		texture.setTextureRegion(region);
		
		Element positionElement = item.getChildByName("Position");
		texture.setX(Float.parseFloat(positionElement.getChildByName("X").getText()));
		texture.setY(-Float.parseFloat(positionElement.getChildByName("Y").getText()));
		
		Element origin = item.getChildByName("Origin");
		texture.setOriginX(Float.parseFloat(origin.getChildByName("X").getText()));
		texture.setOriginY(Float.parseFloat(origin.getChildByName("Y").getText()));
		
		Element scale = item.getChildByName("Scale");
		texture.setScaleX(Float.parseFloat(scale.getChildByName("X").getText()));
		texture.setScaleY(Float.parseFloat(scale.getChildByName("Y").getText()));
		
		texture.setRotation(Float.parseFloat(item.getChildByName("Rotation").getText()));
		
		return texture;
	}
	
	private CircleMapObject loadCircle(Element item) {
		CircleMapObject circle = new CircleMapObject();
		
		loadObject(item, circle);
		
		circle.setColor(loadColor(item.getChildByName("FillColor")));
		
		Element position = item.getChildByName("Position");
		circle.getCircle().set(Float.parseFloat(position.getChildByName("X").getText()),
									  -Float.parseFloat(position.getChildByName("Y").getText()),
									  Float.parseFloat(item.getChildByName("Radius").getText()));
		
		return circle;
	}
	
	private RectangleMapObject loadRectangle(Element item) {
		RectangleMapObject rectangle = new RectangleMapObject();
		
		loadObject(item, rectangle);
		
		rectangle.setColor(loadColor(item.getChildByName("FillColor")));
		
		Element position = item.getChildByName("Position");
		rectangle.getRectangle().set(Float.parseFloat(position.getChildByName("X").getText()),
											  -Float.parseFloat(position.getChildByName("Y").getText()),
											  Float.parseFloat(item.getChildByName("Width").getText()),
											  Float.parseFloat(item.getChildByName("Height").getText()));
		
		return rectangle;
	}
	
	private PolygonMapObject loadPolygon(Element item) {
		PolygonMapObject polygon = new PolygonMapObject();
		
		loadObject(item, polygon);
		
		Array<Element> pointElements = item.getChildByName("WorldPoints").getChildrenByName("Vector2");
		float[] vertices = new float[pointElements.size * 2];
		
		for (int j = 0; j < pointElements.size; ++j) {
			Element pointElement = pointElements.get(j);
			vertices[j * 2] = Float.parseFloat(pointElement.getChildByName("X").getText());
			vertices[j * 2 + 1] = -Float.parseFloat(pointElement.getChildByName("Y").getText());
		}
		
		polygon.setPolygon(new Polygon(vertices));
		polygon.setColor(loadColor(item.getChildByName("LineColor")));
		
		return polygon;
	}
	
	private PolylineMapObject loadPolyline(Element item) {
		PolylineMapObject polyline = new PolylineMapObject();
		
		loadObject(item, polyline);
		
		Array<Element> pointElements = item.getChildByName("WorldPoints").getChildrenByName("Vector2");
		float[] vertices = new float[pointElements.size * 2];
		
		for (int j = 0; j < pointElements.size; ++j) {
			Element pointElement = pointElements.get(j);
			vertices[j * 2] = Float.parseFloat(pointElement.getChildByName("X").getText());
			vertices[j * 2 + 1] = -Float.parseFloat(pointElement.getChildByName("Y").getText());
		}
		
		polyline.setPolygon(new Polygon(vertices));
		polyline.setColor(loadColor(item.getChildByName("LineColor")));
		
		return polyline;
	}
	
	private boolean isPolygon(Element item) {
		return Boolean.parseBoolean(item.getChildByName("IsPolygon").getText());
	}
}
