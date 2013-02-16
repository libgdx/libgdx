package com.badlogic.gdx.maps;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;

/**
 * @brief Generic map
 * 
 * A Map instance contains the following data
 * 
 * <ul>
 * <li> MapLayers<ul>
 * 	<li>MapLayer<ul>
 * 		<li>MapObjects<ul>
 * 			<li>MapObject<ul>
 * 				<li>Can be: TextureMapObject, CircleMapObject, RectangleMapObject, PolygonMapObject or PolylineMapObject</li>
 * 				<li>MapProperties</li>
 * 			</ul></li>
 * 		</ul></li>
 * 		<li>MapProperties</li>	
 * 	</ul></li>
 * </ul></li>
 * <li> MapProperties
 * </ul>
 */
public class Map implements Disposable {
	private MapLayers layers = new MapLayers();
	private MapProperties properties = new MapProperties();
	
	/**
	 * @return map's layers
	 */
	public MapLayers getLayers() {
		return layers;
	}

	/**
	 * @return map's properties set
	 */
	public MapProperties getProperties() {
		return properties;
	}
	
	/**
	 * Creates empty map
	 */
	public Map() {
		
	}

	/**
	 * Disposes all resources like {@link Texture} instances that
	 * the map owns. Not necessary if the Map was loaded via
	 * an {@link AssetManager}
	 */
	@Override
	public void dispose () {
	}
}
